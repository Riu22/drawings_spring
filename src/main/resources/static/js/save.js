// save.js

document.addEventListener('DOMContentLoaded', () => {

    const saveButton = document.getElementById('saveButton');
    const autoSaveCheckbox = document.getElementById('autoSaveCheckbox');
    
    if (!saveButton || !autoSaveCheckbox) {
        console.error("No se encontraron elementos críticos (saveButton o autoSaveCheckbox).");
        return;
    }

    let last_successful_save_content = window.initial_drawing_content || '{}';
    let autoSaveIntervalId = null;

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
     * @param {string | null} manualTitle Título capturado si es el primer guardado manual.
     */
    async function save_drawing_to_server(isManual = false, manualTitle = null) {
        
        const canvasData = window.getCanvasData();
        const oldCurrentDrawId = window.getCurrentDrawId(); // ID antes del guardado
        
        // 1. Control de Cambios: Omitir auto-guardado si no hay cambios
        if (!isManual && canvasData === last_successful_save_content) {
            return;
        }

        // 2. Deshabilitar botón para evitar doble clic y duplicación
        if (isManual) {
            saveButton.disabled = true;
            saveButton.textContent = "Guardando...";
        }

        // 3. OBTENER VALORES DEL USUARIO
        const ispublic = document.getElementById('isPublicCheckbox')?.checked || false;
        let title = "Borrador sin Título"; 
        
        if (manualTitle !== null) {
            title = manualTitle; // Título pasado desde el prompt del click event (V1)
        } else if (oldCurrentDrawId) {
            title = document.getElementById('drawTitleInput')?.value || "Dibujo Editado";
        }
        
        const dataToSend = {
            draw_id: oldCurrentDrawId, // null si es nuevo
            title: title,
            ispublic: ispublic,
            draw_content: canvasData,
        };

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

            // 4. ⭐ LÓGICA CRÍTICA DE ACTUALIZACIÓN DE ID ⭐
            if (!oldCurrentDrawId) {
                // El servidor acaba de crear el dibujo. Actualizamos el ID global.
                window.current_draw_id = receivedId;
                console.log(`Primer guardado. Nuevo ID asignado: ${window.current_draw_id}`);
            }

            // Éxito
            last_successful_save_content = canvasData; 
            console.log(`Guardado ${isManual ? 'manual' : 'automático'} exitoso.`); 

            if (isManual) {
                if (!oldCurrentDrawId) { 
                     alert("¡Dibujo guardado con éxito!");
                     return;
                }
                alert("¡Dibujo guardado con éxito!");
            }
                
        } catch (error) {
            console.error("Fallo al guardar:", error);
            if (isManual) {
                alert(`Hubo un error al guardar el dibujo: ${error.message}. Inténtalo de nuevo.`);
            }
        } finally {
            // 5. Rehabilitar botón después de finalizar (éxito o error)
            if (isManual) {
                saveButton.disabled = false;
                saveButton.textContent = "Guardar";
            }
        }
    }


    // -----------------------------------------------------------
    // LÓGICA DE INICIO Y CONTROL DE EVENTOS
    // -----------------------------------------------------------

    // 1. Conectar el botón de guardado manual (Manejando el PROMPT aquí)
    saveButton.addEventListener('click', () => {
        
        const oldCurrentDrawId = window.getCurrentDrawId();

        if (!oldCurrentDrawId) {
            // Es un dibujo nuevo (V1), pedimos el título ANTES de llamar a la función
            const title = prompt("Por favor, introduce un título para tu dibujo:");
            
            if (!title || title.trim() === "") {
                alert("El dibujo no se guardó. Se requiere un título válido.");
                return;
            }
            
            save_drawing_to_server(true, title); 
        } else {
            // Es un dibujo existente (V2+), el título se lee del input
            save_drawing_to_server(true); 
        }
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