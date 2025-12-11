document.addEventListener('DOMContentLoaded', () => {

    const saveButton = document.getElementById('saveButton');
    const autoSaveCheckbox = document.getElementById('autoSaveCheckbox');
    const titleInput = document.getElementById('drawTitleInput');

    if (!saveButton || !autoSaveCheckbox) {
        console.error("No se encontraron elementos críticos (saveButton o autoSaveCheckbox).");
        return;
    }

    window.current_draw_id = window.serverData?.drawId || null;
    console.log('ID del dibujo actual:', window.current_draw_id);

    let last_successful_save_content = window.serverData?.initialDrawContent || '[]';
    let autoSaveIntervalId = null;

    window.getCurrentDrawId = function() {
        return window.current_draw_id;
    };


    function startAutoSave() {
        if (autoSaveIntervalId !== null) clearInterval(autoSaveIntervalId);
        autoSaveIntervalId = setInterval(() => save_drawing_to_server(false), 30000);
        console.log("Guardado automático iniciado cada 30s.");
    }

    function stopAutoSave() {
        if (autoSaveIntervalId !== null) {
            clearInterval(autoSaveIntervalId);
            autoSaveIntervalId = null;
            console.log("Guardado automático detenido.");
        }
    }

    async function save_drawing_to_server(isManual = false) {

        const canvasData = window.getCanvasData();
        const currentDrawId = window.getCurrentDrawId();

        if (!isManual && canvasData === last_successful_save_content) {
            console.log("No hay cambios, omitiendo auto-guardado.");
            return;
        }

        let title = titleInput?.value.trim() || "Sin título";

        if (!title || title === "") {
            title = "Sin título";
        }

        if (isManual) {
            saveButton.disabled = true;
            saveButton.textContent = "Guardando...";
        }

        const ispublic = document.getElementById('isPublicCheckbox')?.checked || false;

        const dataToSend = {
            draw_id: currentDrawId,
            title: title,
            ispublic: ispublic,
            draw_content: canvasData,
        };

        console.log('Enviando datos al servidor:', dataToSend);

        try {
            const response = await fetch('/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dataToSend),
            });

            const responseText = await response.text();

            if (responseText.startsWith("ERROR_")) {
                throw new Error(`Error del servidor: ${responseText}`);
            }

            const receivedId = parseInt(responseText);

            if (isNaN(receivedId) || receivedId <= 0) {
                throw new Error(`Respuesta inválida del servidor: ${responseText}`);
            }

            if (!currentDrawId) {
                window.current_draw_id = receivedId;
                console.log(`Primer guardado. Nuevo ID asignado: ${window.current_draw_id}`);
            } else {
                console.log(`Dibujo actualizado. ID existente: ${currentDrawId}`);
            }

            last_successful_save_content = canvasData;
            console.log(`Guardado ${isManual ? 'manual' : 'automático'} exitoso.`);

            if (isManual) {
                alert("¡Dibujo guardado con éxito!");
            }

        } catch (error) {
            console.error("Fallo al guardar:", error);
            if (isManual) {
                alert(`Hubo un error al guardar el dibujo: ${error.message}. Inténtalo de nuevo.`);
            }
        } finally {
            if (isManual) {
                saveButton.disabled = false;
                saveButton.textContent = "Guardar";
            }
        }
    }

    saveButton.addEventListener('click', () => {
        save_drawing_to_server(true);
    });
    
    autoSaveCheckbox.addEventListener('change', () => {
        if (autoSaveCheckbox.checked) {
            startAutoSave();
        } else {
            stopAutoSave();
        }
    });

    if (autoSaveCheckbox.checked) {
        startAutoSave();
    }
});