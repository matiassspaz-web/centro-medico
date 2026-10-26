/* Listado de profesionales con filtro por texto y por especialidad. */

let cuerpoTabla;
let campoBusqueda;
let filtroEspecialidad;

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    cuerpoTabla = document.querySelector("main table tbody");
    campoBusqueda = document.getElementById("buscarProfesional");
    filtroEspecialidad = document.getElementById("filtroEspecialidad");

    campoBusqueda.closest(".card").querySelector("button")
        .addEventListener("click", () => cargarProfesionales());

    campoBusqueda.addEventListener("keydown", evento => {
        if (evento.key === "Enter") {
            evento.preventDefault();
            cargarProfesionales();
        }
    });

    filtroEspecialidad.addEventListener("change", () => cargarProfesionales());

    await cargarEspecialidades();
    cargarProfesionales();
});

/* El combo de especialidades se arma con los datos reales de la base. */
async function cargarEspecialidades() {
    try {
        const especialidades = await api.get("/especialidades");

        filtroEspecialidad.innerHTML =
            `<option value="">Todas las especialidades</option>` +
            especialidades.map(e => `<option value="${e.id}">${esc(e.nombre)}</option>`).join("");

    } catch (error) {
        manejarError(error);
    }
}

async function cargarProfesionales() {
    const texto = campoBusqueda.value.trim();
    const especialidadId = filtroEspecialidad.value;

    const parametros = new URLSearchParams();
    if (texto) parametros.set("buscar", texto);
    if (especialidadId) parametros.set("especialidadId", especialidadId);

    const consulta = parametros.toString();
    cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">Cargando...</td></tr>`;

    try {
        dibujar(await api.get("/profesionales" + (consulta ? "?" + consulta : "")));
    } catch (error) {
        cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-danger py-4">No se pudieron cargar los profesionales</td></tr>`;
        manejarError(error);
    }
}

function dibujar(profesionales) {
    if (profesionales.length === 0) {
        cuerpoTabla.innerHTML = `<tr><td colspan="6" class="text-center text-muted py-4">No se encontraron profesionales</td></tr>`;
        return;
    }

    cuerpoTabla.innerHTML = profesionales.map(p => `
        <tr>
            <td>${esc(p.matricula)}</td>
            <td>${esc(p.nombreCompleto)}</td>
            <td>${esc(p.especialidadNombre)}</td>
            <td>${esc(p.telefono)}</td>
            <td>
                <span class="badge text-bg-${p.activo ? "success" : "secondary"}">
                    ${p.activo ? "Activo" : "Inactivo"}
                </span>
            </td>
            <td class="text-center">
                <a href="profesional-form.html?id=${p.id}" class="btn btn-sm btn-outline-primary me-1">Editar</a>
                <button type="button" class="btn btn-sm btn-outline-danger" data-eliminar="${p.id}">Eliminar</button>
            </td>
        </tr>
    `).join("");

    cuerpoTabla.querySelectorAll("[data-eliminar]").forEach(boton =>
        boton.addEventListener("click", () => eliminar(boton.dataset.eliminar)));
}

async function eliminar(id) {
    if (!confirm("¿Confirma eliminar este profesional?")) return;

    try {
        await api.delete(`/profesionales/${id}`);
        avisar("Profesional eliminado", "success");
        cargarProfesionales();
    } catch (error) {
        manejarError(error);
    }
}
