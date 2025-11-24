//código Konami: ↑ ↑ ↓ ↓ ← → ← → B A
const konami_sequence = [
    'ArrowUp',
    'ArrowUp',
    'ArrowDown',
    'ArrowDown',
    'ArrowLeft',
    'ArrowRight',
    'ArrowLeft',
    'ArrowRight',
    'b',
    'a'
];

let current_sequence = [];
let last_key_time = Date.now();
const RESET_TIME = 3000;

document.addEventListener('keydown', (e) => {
    const current_time = Date.now();

    // Resetear si ha pasado mucho tiempo
    if (current_time - last_key_time > RESET_TIME) {
        current_sequence = [];
    }

    last_key_time = current_time;

    // Añadir tecla a la secuencia
    current_sequence.push(e.key);

    // Mantener solo las últimas teclas necesarias
    if (current_sequence.length > konami_sequence.length) {
        current_sequence.shift();
    }

    // Verificar si la secuencia coincide
    if (JSON.stringify(current_sequence) === JSON.stringify(konami_sequence)) {
        console.log('¡CÓDIGO KONAMI ACTIVADO!');
        draw_konami();
        current_sequence = []; // Resetear
    }
});

function draw_konami() {
    const canvas = document.getElementById('drawingCanvas');
    if (!canvas) return;

    const ctx = canvas.getContext('2d');

    // Guardar el contenido actual del canvas
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);

    // Guardar el estado actual del canvas
    ctx.save();

    // Configurar el estilo del texto
    ctx.font = 'bold 80px Arial';
    ctx.fillStyle = '#FF0000';
    ctx.strokeStyle = '#000000';
    ctx.lineWidth = 3;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';

    // Calcular posición central
    const center_x = canvas.width / 2;
    const center_y = canvas.height / 2;

    // Efecto de destello
    ctx.shadowColor = '#FF0000';
    ctx.shadowBlur = 20;

    // Dibujar el texto
    ctx.strokeText('KONAMI', center_x, center_y);
    ctx.fillText('KONAMI', center_x, center_y);

    // Restaurar el estado
    ctx.restore();

    // Eliminar el texto después de 3 segundos
    setTimeout(() => {
        ctx.putImageData(imageData, 0, 0);
        console.log('Efecto Konami completado');
    }, 3000);
}