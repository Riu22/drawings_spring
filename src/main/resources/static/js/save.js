// save.js

document.addEventListener('DOMContentLoaded', () => {

    const saveButton = document.getElementById('saveButton');
    const autoSaveCheckbox = document.getElementById('autoSaveCheckbox');
    const titleInput = document.getElementById('drawTitleInput');

    if (!saveButton || !autoSaveCheckbox) {
        console.error("No se encontraron elementos críticos (saveButton o autoSaveCheckbox).");
        return;
    }

    // ⭐ INICIALIZAR EL ID DEL DIBUJO DESDE EL SERVIDOR
    window.current_draw_id = window.serverData?.drawId || null;
    console.log('ID del dibujo actual:', window.current_draw_id);

    let last_successful_save_content = window.serverData?.initialDrawContent || '[]';
    let autoSaveIntervalId = null;

    // Helper para obtener el ID actual
    window.getCurrentDrawId = function() {
        return window.current_draw_id;
    };

    // --- Funciones de Control de Auto-Guardado ---

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

    // --- Función Principal de Guardado ---

    /**
     * @param {boolean} isManual Indica si el guardado fue forzado por el usuario.
     */
    async function save_drawing_to_server(isManual = false) {

        const canvasData = window.getCanvasData();
        const currentDrawId = window.getCurrentDrawId();

        // 1. Control de Cambios: Omitir auto-guardado si no hay cambios
        if (!isManual && canvasData === last_successful_save_content) {
            console.log("No hay cambios, omitiendo auto-guardado.");
            return;
        }

        // 2. Obtener el título del input
        let title = titleInput?.value.trim() || "Sin título";

        if (!title || title === "") {
            title = "Sin título";
        }

        // 3. Deshabilitar botón para evitar doble clic
        if (isManual) {
            saveButton.disabled = true;
            saveButton.textContent = "Guardando...";
        }

        // 4. OBTENER VALORES DEL USUARIO
        const ispublic = document.getElementById('isPublicCheckbox')?.checked || false;

        const dataToSend = {
            draw_id: currentDrawId, // null para nuevo, número para edición
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

            // ⭐ LECTURA DE RESPUESTA DE TEXTO (@ResponseBody String) ⭐
            const responseText = await response.text();

            if (responseText.startsWith("ERROR_")) {
                throw new Error(`Error del servidor: ${responseText}`);
            }

            const receivedId = parseInt(responseText);

            if (isNaN(receivedId) || receivedId <= 0) {
                throw new Error(`Respuesta inválida del servidor: ${responseText}`);
            }

            // 5. ⭐ LÓGICA CRÍTICA DE ACTUALIZACIÓN DE ID ⭐
            if (!currentDrawId) {
                // Es un dibujo nuevo, actualizar el ID global
                window.current_draw_id = receivedId;
                console.log(`Primer guardado. Nuevo ID asignado: ${window.current_draw_id}`);
            } else {
                console.log(`Dibujo actualizado. ID existente: ${currentDrawId}`);
            }

            // Éxito
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
            // 6. Rehabilitar botón después de finalizar (éxito o error)
            if (isManual) {
                saveButton.disabled = false;
                saveButton.textContent = "Guardar";
            }
        }
    }


    // -----------------------------------------------------------
    // LÓGICA DE INICIO Y CONTROL DE EVENTOS
    // -----------------------------------------------------------

    // 1. Conectar el botón de guardado manual
    saveButton.addEventListener('click', () => {
        save_drawing_to_server(true);
    });
    
    // 2. Controlar el estado del Auto-Guardado mediante el checkbox
    autoSaveCheckbox.addEventListener('change', () => {
        if (autoSaveCheckbox.checked) {
            startAutoSave();
        } else {
            stopAutoSave();
        }
    });

    // 3. Inicializar el Auto-Guardado al cargar la página
    if (autoSaveCheckbox.checked) {
        startAutoSave();
    }
});