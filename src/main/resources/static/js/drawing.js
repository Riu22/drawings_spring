document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('drawingCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    // --- Selectores de UI (Se mantienen solo para leer el estado) ---
    const color_picker = document.getElementById('colorPicker');
    const size_picker = document.getElementById('sizePicker');
    
    // Botones de herramientas
    const circle_btn = document.getElementById('circleBtn');
    const square_btn = document.getElementById('squareBtn');
    const triangle_btn = document.getElementById('triangleBtn');
    const star_btn = document.getElementById('starBtn');
    const free_draw_btn = document.getElementById('freeDrawBtn');
    const tool_buttons = [circle_btn, square_btn, triangle_btn, star_btn, free_draw_btn].filter(Boolean);

    // Botones de control
    const clear_btn = document.getElementById('clearBtn'); 

    // Lista de objetos (REINTEGRADO)
    const object_list_container = document.getElementById('objectList');

    // --- Variables de Estado Centralizadas ---
    let objects = []; // Array para guardar el contenido del dibujo (el JSON que se guardará)
    let object_id_counter = 0; // Se usa para dar IDs a los objetos
    
    let is_drawing = false;
    let current_mode = 'freeDraw';
    let current_path = null; // Para almacenar el trazo libre actual
    
    // Variables para SELECCIÓN y ARRASTRE (REINTEGRADO)
    let selected_object = null;
    let dragging_object = false;
    let drag_offset = { x: 0, y: 0 }; 

    // --- Funciones de Utilidad ---

    function update_color_and_size() {
        // Lee el color y tamaño actual de los selectores
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
        // Al cambiar de herramienta, deseleccionar objeto (a menos que el modo sea 'freeDraw' y haya un objeto seleccionado)
        if (mode !== selected_object?.type) {
             selected_object = null;
        }
        redraw_canvas();
        update_object_list();
    }

    // Limpiar canvas
    function clear_canvas() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        objects = []; // Limpiar la data
        selected_object = null;
        update_object_list();
    }

    // Redimensionar canvas
    function resize_canvas() {
        const parent = canvas.parentElement;
        if (!parent) return;

        canvas.width = parent.clientWidth || canvas.width;
        canvas.height = parent.clientHeight || canvas.height;

        redraw_canvas();
    }

    // Obtener posición del ratón
    function get_mouse_pos(canvas, evt) {
        const rect = canvas.getBoundingClientRect();
        const scale_x = canvas.width / rect.width;
        const scale_y = canvas.height / rect.height;

        return {
            x: (evt.clientX - rect.left) * scale_x,
            y: (evt.clientY - rect.top) * scale_y
        };
    }

    // --- Funciones de Objeto (Reintegradas) ---
    
    // Crear y añadir una forma (solo para clics)
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
        select_object(new_object); // Seleccionar inmediatamente el objeto creado
    }

    // Seleccionar un objeto
    function select_object(obj) {
        selected_object = obj;

        if (obj) {
            // Sincronizar UI con propiedades del objeto seleccionado
            if (color_picker) {
                color_picker.value = obj.color;
                // Disparar evento para actualizar el color actual en ctx, etc.
                color_picker.dispatchEvent(new Event('input', { bubbles: true })); 
            }

            if (size_picker) {
                size_picker.value = obj.size;
                // Disparar evento para actualizar el tamaño actual
                size_picker.dispatchEvent(new Event('input', { bubbles: true }));
            }
        }

        redraw_canvas();
        update_object_list();
    }

    // Eliminar un objeto
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

    // Actualizar lista de objetos en el DOM (REINTEGRADO)
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

    // Detectar si un clic está sobre un objeto (REINTEGRADO)
    function get_object_at_position(pos) {
        // Recorrer en orden inverso para seleccionar el objeto más reciente
        for (let i = objects.length - 1; i >= 0; i--) {
            const obj = objects[i];
            const size_half = (obj.size || 0) / 2; // Para formas

            if (obj.type === 'freeDraw') {
                // Para trazos libres, verificar proximidad a cualquier punto
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
                    // CÁLCULO DE TRIÁNGULO (simplificado del original)
                    const altura_total = Math.sqrt(Math.pow(obj.size, 2) - Math.pow(obj.size / 2, 2));
                    const altura_base = altura_total / 3;
                    const altura_punta = altura_total - altura_base;

                    const x1 = obj.x, y1 = obj.y - altura_punta;
                    const x2 = obj.x - size_half, y2 = obj.y + altura_base;
                    const x3 = obj.x + size_half, y3 = obj.y + altura_base;

                    const denominator = ((y2 - y3) * (x1 - x3) + (x3 - x2) * (y1 - y3));
                    const a = ((y2 - y3) * (pos.x - x3) + (x3 - x2) * (pos.y - y3)) / denominator;
                    const b = ((y3 - y1) * (pos.x - x3) + (x1 - x3) * (pos.y - y3)) / denominator;
                    const c = 1 - a - b;

                    if (a >= 0 && b >= 0 && c >= 0) {
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    // --- Funciones de Dibujo ---

    // Dibujar un objeto individual
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
            
            if (obj.type !== 'freeDraw') {
                 ctx.strokeRect(obj.x - size_half, obj.y - size_half, obj.size, obj.size);
            }
        }
    }
    



    // Redibujar todo el canvas
    function redraw_canvas() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);

        objects.forEach(obj => {
            const is_selected = selected_object && obj.id === selected_object.id;
            draw_object(obj, is_selected);
        });
    }

    // --- Manejo de Eventos de Dibujo y Edición (Reintegrado) ---

    // Iniciar dibujo o selección
    function start_drawing(e) {
        const pos = get_mouse_pos(canvas, e);

        // 1. Intentar seleccionar un objeto (REINTEGRADO)
        const clicked_object = get_object_at_position(pos);

        if (clicked_object) {
            select_object(clicked_object);
            dragging_object = true;

            // Calcular offset para arrastre
            if (clicked_object.type === 'freeDraw' && clicked_object.points.length > 0) {
                drag_offset.x = pos.x - clicked_object.points[0].x;
                drag_offset.y = pos.y - clicked_object.points[0].y;
            } else {
                drag_offset.x = pos.x - clicked_object.x;
                drag_offset.y = pos.y - clicked_object.y;
            }
        } else {
            // 2. Si no se seleccionó un objeto, iniciar dibujo (o crear forma)
            selected_object = null;
            redraw_canvas(); // Deseleccionar visualmente

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
                add_shape(pos); // Crea y selecciona la forma inmediatamente
            }
        }
    }

    // Mover objeto o dibujar
    function handle_mouse_move(e) {
        const pos = get_mouse_pos(canvas, e);

        if (dragging_object && selected_object) {
            // Mover objeto seleccionado (REINTEGRADO)
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

    // Detener dibujo o arrastre
    function stop_drawing() {
        if (current_mode === 'freeDraw' && is_drawing && current_path) {
            is_drawing = false;

            if (current_path.points.length > 1) {
                objects.push(current_path);
                select_object(current_path); // Seleccionar trazo recién creado
            }
            current_path = null;
        }

        if (dragging_object) {
            dragging_object = false;
        }
    }

    // --- Interfaz Global para Guardar/Cargar ---
    
    window.getCanvasData = function() {
        // Devuelve el estado actual de los objetos (sin historial)
        return JSON.stringify(objects);
    };
    
    window.loadDrawing = function(jsonString) {
        try {
            const loadedObjects = JSON.parse(jsonString);
            objects.length = 0; 
            objects.push(...loadedObjects);
            object_id_counter = objects.length > 0 ? Math.max(...objects.map(o => o.id)) + 1 : 0; 
            selected_object = null;
            redraw_canvas();
            update_object_list();
        } catch (e) {
            console.error("Error al cargar el dibujo JSON:", e);
        }
    };
    
    // --- Inicialización y Asignación de Eventos ---

    // Asignar eventos a botones de herramientas
    if (circle_btn) circle_btn.addEventListener('click', () => select_tool(circle_btn, 'circle'));
    if (square_btn) square_btn.addEventListener('click', () => select_tool(square_btn, 'square'));
    if (triangle_btn) triangle_btn.addEventListener('click', () => select_tool(triangle_btn, 'triangle'));
    if (star_btn) star_btn.addEventListener('click', () => select_tool(star_btn, 'star'));
    if (free_draw_btn) free_draw_btn.addEventListener('click', () => select_tool(free_draw_btn, 'freeDraw'));

    // Asignar eventos de control
    if (clear_btn) clear_btn.addEventListener('click', clear_canvas);

    // Listener para cambio de color (mantiene la edición del objeto seleccionado)
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

    // Listener para cambio de tamaño (mantiene la edición del objeto seleccionado)
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

    // Configuración inicial
    window.addEventListener('resize', resize_canvas);

    const parent = canvas.parentElement;
    canvas.width = parent.clientWidth;
    canvas.height = parent.clientHeight;
    
    update_color_and_size();
    select_tool(free_draw_btn, 'freeDraw'); // Seleccionar FreeDraw por defecto
    update_object_list(); // Inicializar lista vacía
});