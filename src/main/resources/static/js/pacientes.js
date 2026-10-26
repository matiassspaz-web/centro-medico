/* Listado de pacientes: carga, búsqueda y eliminación contra la API. */

let cuerpoTabla;
let campoBusqueda;

document.addEventListener("DOMContentLoaded", () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    cuerpoTabla = document.querySelector("main table tbody");
    campoBusqueda = document.getElementById("buscarPaciente");

    const botonBuscar = campoBusqueda.closest(".card").querySelector("button");
    botonBuscar.addEventListener("click", () => cargarPacientes());

    // Buscar también al presionar Enter.
    campoBusqueda.addEventListener("keydown", evento => {
        if (evento.key === "Enter") {
            evento.preventDefault();
            cargarPacientes();
        }
    });

    cargarPacientes();
});

async function cargarPacientes() {
    const texto = campoBusqueda.value.trim();
    const ruta = texto ? `/pacientes?buscar=${encodeURIComponent(texto)}` : "/pacientes";

    cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Cargando...</td></tr>`;

    try {
        const pacientes = await api.get(ruta);
        dibujarPacientes(pacientes);
    } catch (error) {
        cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">No se pudieron cargar los pacientes</td></tr>`;
        manejarError(error);
    }
}

function dibujarPacientes(pacientes) {
    if (pacientes.length === 0) {
        cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No se encontraron pacientes</td></tr>`;
        return;
    }

    cuerpoTabla.innerHTML = pacientes.map(p => `
        <tr>
            <td>${esc(p.dni)}</td>
            <td>${esc(p.apellido)}</td>
            <td>${esc(p.nombre)}</td>
            <td>${esc(p.telefono)}</td>
            <td>${esc(p.email)}</td>
            <td class="text-center">
                <a href="paciente-form.html?id=${p.id}" class="btn btn-sm btn-outline-primary me-1">Editar</a>
                <button type="button" class="btn btn-sm btn-outline-danger" data-eliminar="${p.id}">Eliminar</button>
            </td>
        </tr>
    `).join("");

    cuerpoTabla.querySelectorAll("[data-eliminar]").forEach(boton => {
        boton.addEventListener("click", () => {
            const fila = boton.closest("tr");
            const nombre = fila.children[1].textContent + ", " + fila.children[2].textContent;
            eliminarPaciente(boton.dataset.eliminar, nombre);
        });
    });
}

async function eliminarPaciente(id, nombre) {
    if (!confirm(`¿Confirma eliminar al paciente ${nombre}?`)) return;

    try {
        await api.delete(`/pacientes/${id}`);
        avisar("Paciente eliminado correctamente", "success");
        cargarPacientes();
    } catch (error) {
        manejarError(error);
    }
}
