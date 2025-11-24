document.addEventListener('DOMContentLoaded', () => {
    // Cargar el color guardado ANTES de inicializar Coloris
    const saved_state_json = localStorage.getItem('drawingEditorConfig');
    let initial_color = '#000000';

    if (saved_state_json) {
        const saved_state = JSON.parse(saved_state_json);
        initial_color = saved_state.color || '#000000';
    }

    // Establecer el color en el input ANTES de inicializar Coloris
    const color_picker = document.getElementById('colorPicker');
    if (color_picker) {
        color_picker.value = initial_color;
    }

    // Ahora inicializar Coloris con el color ya establecido
    Coloris({
        el: '#colorPicker',
        themeMode: 'dark',
        swatches: [
            '#d62828', '#f77f00', '#fcbf49', '#2a9d8f', '#0077b6',
            '#8338ec', '#ff006e', '#ffffff', '#8d99ae', '#000000'
        ]
    });

    // Listener para cambios de color
    color_picker.addEventListener('input', (event) => {
        console.log('Nuevo color seleccionado:', event.target.value);

        const color_change_event = new CustomEvent('drawingColorChange', {
            detail: { color: event.target.value }
        });
        document.dispatchEvent(color_change_event);
    });
});