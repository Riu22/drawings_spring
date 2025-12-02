document.getElementById('saveButton').addEventListener('click', async () => {

    const canvasData = window.getCanvasData();

    // 2. Pedir información adicional al usuario
    const title = prompt("Por favor, introduce un título para tu dibujo:");
    if (!title) {
        alert("El dibujo no se guardó. Se requiere un título.");
        return;
    }

    const ispublic = true;

    // 3. Crear el cuerpo de la solicitud JSON
    const dataToSend = {
        title: title,
        ispublic: ispublic,
        draw_content: canvasData,
    };

    console.log("Enviando datos de dibujo:", dataToSend);

    // 4. Realizar la solicitud FETCH al servidor
    try {
        const response = await fetch('/save', {
            method: 'POST',
            headers: {
                    'Content-Type': 'application/json'
                    },
            body: JSON.stringify(dataToSend)
        });

        if (response.status != 200 ) {
            throw new Error("Error del servidor: " + response.status);
        }
        }catch (error) {
            console.error("Error al guardar el dibujo:", error);
            alert("Hubo un error al guardar el dibujo. Por favor, inténtalo de nuevo.");
            return;
        }
    alert("¡Dibujo guardado con éxito!");
});