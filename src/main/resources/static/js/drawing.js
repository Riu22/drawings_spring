// drawing.js

document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('drawingCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    // --- Variables de Estado Centralizadas ---
    let objects = []; // Array para guardar el contenido del dibujo
    let object_id_counter = 0; // Contador de IDs
    
    let is_drawing = false;
    let current_mode = 'freeDraw';
    let current_path = null; // Para almacenar el trazo libre actual
    
    // Variables para SELECCIÓN y ARRASTRE
    let selected_object = null;
    let dragging_object = false;
    let drag_offset = { x: 0, y: 0 }; 

    // --- Selectores de UI ---
    const color_picker = document.getElementById('colorPicker');
    const size_picker = document.getElementById('sizePicker');
    const object_list_container = document.getElementById('objectList');
    
    // Botones de herramientas
    const circle_btn = document.getElementById('circleBtn');
    const square_btn = document.getElementById('squareBtn');
    const triangle_btn = document.getElementById('triangleBtn');
    const star_btn = document.getElementById('starBtn');
    const free_draw_btn = document.getElementById('freeDrawBtn');
    const clear_btn = document.getElementById('clearBtn'); 
    const tool_buttons = [circle_btn, square_btn, triangle_btn, star_btn, free_draw_btn].filter(Boolean);

    // --- Funciones de Utilidad ---

    function update_color_and_size() {
        const color = color_picker?.value || '#000000';
        const size = size_picker ? parseInt(size_picker.value) : 5;
        ctx.strokeStyle = color;
        ctx.fillStyle = color;
        return { color, size };
    }
    
    function select_tool(button, mode) {
        current_mode = mode;
        tool_buttons.forEach(btn => { if (btn) btn.classList.remove('selected'); });
        if (button) {
            button.classList.add('selected');
        } else if (free_draw_btn) {
            free_draw_btn.classList.add('selected');
        }
        is_drawing = false;
        // Al cambiar de herramienta, deseleccionar objeto
        if (selected_object) {
             selected_object = null;
        }
        redraw_canvas();
        update_object_list();
    }

    function clear_canvas() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        objects = []; // Limpiar la data
        selected_object = null;
        update_object_list();
    }

    function resize_canvas() {
        const parent = canvas.parentElement;
        if (!parent) return;

        // Guarda el estado actual del canvas (incluyendo zoom/transformaciones)
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);

        canvas.width = parent.clientWidth || canvas.width;
        canvas.height = parent.clientHeight || canvas.height;

        // Restaura la imagen para mantener el dibujo visible (aunque la posición no sea perfecta)
        ctx.putImageData(imageData, 0, 0);

        redraw_canvas();
    }

    function get_mouse_pos(canvas, evt) {
        const rect = canvas.getBoundingClientRect();
        // Escala el punto para obtener coordenadas reales del canvas (útil si hay zoom CSS)
        const scale_x = canvas.width / rect.width;
        const scale_y = canvas.height / rect.height;

        return {
            x: (evt.clientX - rect.left) * scale_x,
            y: (evt.clientY - rect.top) * scale_y
        };
    }

    // --- Funciones de Objeto ---
    
    function add_shape(pos) {
        const { x, y } = pos;
        const { color, size } = update_color_and_size(); 

        const new_object = {
            id: object_id_counter++,
            type: current_mode,
            x: x,
            y: y,
            size: size,
            color: color
        };

        objects.push(new_object);
        select_object(new_object); 
    }

    function select_object(obj) {
        selected_object = obj;

        if (obj) {
            // Sincronizar UI con propiedades del objeto seleccionado
            if (color_picker) color_picker.value = obj.color;
            if (size_picker) size_picker.value = obj.size;
        }

        redraw_canvas();
        update_object_list();
    }

    function delete_object(obj) {
        const index = objects.findIndex(o => o.id === obj.id);
        if (index !== -1) {
            objects.splice(index, 1);

            if (selected_object && selected_object.id === obj.id) {
                selected_object = null;
            }

            redraw_canvas();
            update_object_list();
        }
    }

    function update_object_list() {
        if (!object_list_container) return;

        object_list_container.innerHTML = '<h3>Objectes</h3>';

        if (objects.length === 0) {
            const empty_msg = document.createElement('p');
            empty_msg.style.color = '#999';
            empty_msg.style.fontSize = '0.9em';
            empty_msg.textContent = 'No hi ha objectes';
            object_list_container.appendChild(empty_msg);
            return;
        }

        objects.forEach((obj) => {
            const item = document.createElement('div');
            item.className = 'object-item';
            if (selected_object && obj.id === selected_object.id) {
                item.classList.add('selected');
            }

            const type_name = obj.type === 'circle' ? 'Cercle' :
                               obj.type === 'square' ? 'Quadrat' :
                               obj.type === 'triangle' ? 'Triangle' :
                               obj.type === 'star' ? 'Estrella' : 'Traç';

            item.innerHTML = `
                <div class="object-info">
                    <span>${type_name} #${obj.id}</span>
                    <span style="color: ${obj.color}; font-size: 1.2em;">●</span>
                    <small style="color: #666;">Mida: ${obj.size}</small>
                </div>
                <button class="delete-btn" title="Eliminar objecte">✕</button>
            `;

            const info_div = item.querySelector('.object-info');
            info_div.addEventListener('click', (e) => {
                e.stopPropagation();
                select_object(obj);
            });

            const delete_btn = item.querySelector('.delete-btn');
            delete_btn.addEventListener('click', (e) => {
                e.stopPropagation();
                delete_object(obj);
            });

            object_list_container.appendChild(item);
        });
    }

    function get_object_at_position(pos) {
        // Itera al revés para seleccionar el objeto más arriba
        for (let i = objects.length - 1; i >= 0; i--) {
            const obj = objects[i];
            const size_half = (obj.size || 0) / 2;

            // Detección de colisión para trazo libre (más complejo, basado en puntos)
            if (obj.type === 'freeDraw') {
                if (obj.points && obj.points.length > 0) {
                    const threshold = obj.size + 5;
                    for (let point of obj.points) {
                        const dx = pos.x - point.x;
                        const dy = pos.y - point.y;
                        const distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance <= threshold) {
                            return obj;
                        }
                    }
                }
            } else {
                // Detección de colisión para formas geométricas (basado en centro y tamaño)
                const dx = pos.x - obj.x;
                const dy = pos.y - obj.y;

                if (obj.type === 'circle' || obj.type === 'star') {
                    const distance = Math.sqrt(dx * dx + dy * dy);
                    if (distance <= size_half) {
                        return obj;
                    }
                } else if (obj.type === 'square') {
                    if (Math.abs(dx) <= size_half && Math.abs(dy) <= size_half) {
                        return obj;
                    }
                } else if (obj.type === 'triangle') {
                    // Implementación simplificada de detección de clic en un triángulo
                    // Puedes usar un cálculo más preciso si es necesario
                    if (Math.abs(dx) <= size_half && dy <= size_half) { 
                         return obj;
                    }
                }
            }
        }
        return null;
    }

    // --- Funciones de Dibujo ---

    function draw_object(obj, highlight = false) {
        ctx.fillStyle = obj.color;
        ctx.strokeStyle = obj.color;

        const size_half = (obj.size || 0) / 2;
        
        if (obj.type === 'circle') {
            ctx.beginPath();
            ctx.arc(obj.x, obj.y, size_half, 0, Math.PI * 2);
            ctx.fill();
        } else if (obj.type === 'square') {
            ctx.fillRect(obj.x - size_half, obj.y - size_half, obj.size, obj.size);
        } else if (obj.type === 'triangle') {
             // Dibujar un triángulo isósceles con el centro en (obj.x, obj.y)
            const h = Math.sqrt(Math.pow(obj.size, 2) - Math.pow(size_half, 2));
            const altura_base = h / 3;
            const altura_punta = h - altura_base;

            ctx.beginPath();
            ctx.moveTo(obj.x, obj.y - altura_punta);
            ctx.lineTo(obj.x - size_half, obj.y + altura_base);
            ctx.lineTo(obj.x + size_half, obj.y + altura_base);
            ctx.closePath();
            ctx.fill();
        } else if (obj.type === 'star') {
            // Estrella de 7 puntas (por ejemplo)
            const num_points = 7;
            const outer_radius = size_half;
            const inner_radius = outer_radius * 0.4;

            ctx.beginPath();
            for (let i = 0; i < num_points * 2; i++) {
                const angle = (Math.PI / num_points) * i - Math.PI / 2;
                const radius = i % 2 === 0 ? outer_radius : inner_radius;
                const x = obj.x + Math.cos(angle) * radius;
                const y = obj.y + Math.sin(angle) * radius;
                if (i === 0) {
                    ctx.moveTo(x, y);
                } else {
                    ctx.lineTo(x, y);
                }
            }
            ctx.closePath();
            ctx.fill();
        } else if (obj.type === 'freeDraw') {
            // Dibujar el trazo
            if (obj.points && obj.points.length > 0) {
                ctx.strokeStyle = obj.color;
                ctx.lineWidth = obj.size;
                ctx.lineCap = 'round';
                ctx.lineJoin = 'round';

                ctx.beginPath();
                ctx.moveTo(obj.points[0].x, obj.points[0].y);

                for (let i = 1; i < obj.points.length; i++) {
                    ctx.lineTo(obj.points[i].x, obj.points[i].y);
                }
                ctx.stroke();
            }
        }
        
        // Dibujar el recuadro de selección (highlight)
        if (highlight) {
            ctx.strokeStyle = '#00ff00';
            ctx.lineWidth = 2;
            
            // Dibujar un cuadrado alrededor del centro/límites
            if (obj.type !== 'freeDraw') {
                 ctx.strokeRect(obj.x - size_half - 5, obj.y - size_half - 5, obj.size + 10, obj.size + 10);
            } else if (obj.points && obj.points.length > 0) {
                 // Para trazo libre, puedes calcular su caja delimitadora (bounding box)
                 const minX = Math.min(...obj.points.map(p => p.x));
                 const minY = Math.min(...obj.points.map(p => p.y));
                 const maxX = Math.max(...obj.points.map(p => p.x));
                 const maxY = Math.max(...obj.points.map(p => p.y));
                 ctx.strokeRect(minX - 5, minY - 5, (maxX - minX) + 10, (maxY - minY) + 10);
            }
        }
    }
    
    function redraw_canvas() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        objects.forEach(obj => {
            const is_selected = selected_object && obj.id === selected_object.id;
            draw_object(obj, is_selected);
        });
    }

    // --- Manejo de Eventos de Dibujo y Edición ---

    function start_drawing(e) {
        const pos = get_mouse_pos(canvas, e);

        // 1. Intentar seleccionar/arrastrar un objeto
        const clicked_object = get_object_at_position(pos);

        if (clicked_object) {
            select_object(clicked_object);
            dragging_object = true;

            // Calcular offset para arrastre
            if (clicked_object.type === 'freeDraw' && clicked_object.points.length > 0) {
                // Usamos el centro de la bounding box o un punto fijo para el offset de trazos
                drag_offset.x = pos.x - clicked_object.points[0].x; 
                drag_offset.y = pos.y - clicked_object.points[0].y;
            } else {
                drag_offset.x = pos.x - clicked_object.x;
                drag_offset.y = pos.y - clicked_object.y;
            }
        } else {
            // 2. Si no se seleccionó un objeto, iniciar dibujo (o crear forma)
            selected_object = null;
            redraw_canvas(); 

            const { color, size } = update_color_and_size(); 

            if (current_mode === 'freeDraw') {
                is_drawing = true;
                current_path = {
                    id: object_id_counter++,
                    type: 'freeDraw',
                    color: color,
                    size: size,
                    points: [{ x: pos.x, y: pos.y }]
                };
            } else {
                add_shape(pos);
            }
        }
    }

    function handle_mouse_move(e) {
        const pos = get_mouse_pos(canvas, e);

        if (dragging_object && selected_object) {
            // Mover objeto seleccionado
            if (selected_object.type === 'freeDraw') {
                const delta_x = pos.x - drag_offset.x - selected_object.points[0].x;
                const delta_y = pos.y - drag_offset.y - selected_object.points[0].y;

                selected_object.points.forEach(point => {
                    point.x += delta_x;
                    point.y += delta_y;
                });
            } else {
                selected_object.x = pos.x - drag_offset.x;
                selected_object.y = pos.y - drag_offset.y;
            }
            redraw_canvas();
        } else if (current_mode === 'freeDraw' && is_drawing && current_path) {
            // Dibujar trazo libre
            current_path.points.push({ x: pos.x, y: pos.y });

            redraw_canvas();
            draw_object(current_path, false);
        }
    }

    function stop_drawing() {
        if (current_mode === 'freeDraw' && is_drawing && current_path) {
            is_drawing = false;

            if (current_path.points.length > 1) {
                objects.push(current_path);
                select_object(current_path); 
            }
            current_path = null;
        }

        if (dragging_object) {
            dragging_object = false;
        }
    }

    // ------------------------------------------------------------------------
    // --- INTERFAZ GLOBAL PARA save.js (Guardado/Carga de Estado) ---
    // ------------------------------------------------------------------------
    
    /**
     * Devuelve el estado actual de los objetos (JSON) para guardar.
     */
    window.getCanvasData = function() {
        return JSON.stringify(objects);
    };
    
    /**
     * Carga el estado del dibujo desde una cadena JSON.
     */
    window.loadDrawing = function(jsonString) {
        try {
            const loadedObjects = JSON.parse(jsonString);
            objects.length = 0; 
            objects.push(...loadedObjects);
            
            // ⭐️ AJUSTE CRÍTICO DEL CONTADOR DE IDs ⭐️
            const maxId = objects.length > 0 ? Math.max(...objects.map(o => o.id)) : -1;
            object_id_counter = maxId + 1; 
            
            selected_object = null;
            redraw_canvas();
            update_object_list();
        } catch (e) {
            console.error("Error al cargar el dibujo JSON:", e);
        }
    };
    
    /**
     * Función auxiliar para obtener el ID existente.
     */
    window.getCurrentDrawId = function() {
        // Retorna el ID (número) o null si es un dibujo nuevo.
        return window.current_draw_id > 0 ? window.current_draw_id : null; 
    };
    
    // ------------------------------------------------------------------------
    // --- Inicialización y Asignación de Eventos ---
    // ------------------------------------------------------------------------

    // Asignar eventos a botones de herramientas
    if (circle_btn) circle_btn.addEventListener('click', () => select_tool(circle_btn, 'circle'));
    if (square_btn) square_btn.addEventListener('click', () => select_tool(square_btn, 'square'));
    if (triangle_btn) triangle_btn.addEventListener('click', () => select_tool(triangle_btn, 'triangle'));
    if (star_btn) star_btn.addEventListener('click', () => select_tool(star_btn, 'star'));
    if (free_draw_btn) free_draw_btn.addEventListener('click', () => select_tool(free_draw_btn, 'freeDraw'));

    // Asignar eventos de control
    if (clear_btn) clear_btn.addEventListener('click', clear_canvas);

    // Listener para edición de objeto seleccionado (color)
    if (color_picker) {
        color_picker.addEventListener('input', () => {
            const { color } = update_color_and_size();
             if (selected_object) {
                 selected_object.color = color;
                 redraw_canvas();
                 update_object_list();
             }
        });
    }

    // Listener para edición de objeto seleccionado (tamaño)
    if (size_picker) {
        size_picker.addEventListener('input', () => {
            const { size } = update_color_and_size();
            if (selected_object) {
                selected_object.size = size;
                redraw_canvas();
                update_object_list();
            }
        });
    }

    // Eventos del ratón para dibujo y arrastre
    canvas.addEventListener('mousedown', start_drawing);
    canvas.addEventListener('mouseup', stop_drawing);
    canvas.addEventListener('mousemove', handle_mouse_move);
    canvas.addEventListener('mouseleave', stop_drawing);

    // Configuración inicial y carga
    window.addEventListener('resize', resize_canvas);

    // Llamadas iniciales
    resize_canvas(); // Inicializa el tamaño del canvas
    update_color_and_size();
    select_tool(free_draw_btn, 'freeDraw'); 
    
    // ⭐️ LÓGICA DE CARGA INICIAL ⭐️
    if (window.initial_drawing_content && window.initial_drawing_content !== '{}') {
        console.log("Cargando dibujo existente.");
        window.loadDrawing(window.initial_drawing_content);
    } else {
         console.log("Creando nuevo dibujo.");
    }
});