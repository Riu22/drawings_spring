document.getElementById('saveButton').addEventListener('click', async () => {

    const canvasData = windows.getCanvasData();

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
        drawContent: canvasData,
    };

    console.log("Enviando datos de dibujo:", dataToSend);

    // 4. Realizar la solicitud FETCH al servidor
    try {
        const response = await fetch('/save', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',

            },
            body: JSON.stringify(dataToSend)
        });

        // 5. Manejar la respuesta del servidor
        if (response.ok) {
            const result = await response.json();
            alert(`Dibujo guardado con éxito! ID: ${result.id}`);
            window.location.href = '/gallery';
        } else {
            const error = await response.json();
            console.error('Error al guardar el dibujo:', error);
            alert(`Error al guardar: ${error.message || response.statusText}`);
        }
    } catch (error) {
        console.error('Fallo en la conexión:', error);
        alert('Fallo al conectar con el servidor.');
    }
});