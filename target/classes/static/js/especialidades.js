/* ABM de especialidades médicas. El alta y la edición usan el mismo modal. */

let cuerpoTabla;
let campoBusqueda;
let formulario;
let modal;
let idEnEdicion = null;

document.addEventListener("DOMContentLoaded", () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    cuerpoTabla = document.querySelector("main table tbody");
    campoBusqueda = document.getElementById("buscarEspecialidad");
    formulario = document.getElementById("formEspecialidad");
    modal = new bootstrap.Modal(document.getElementById("modalEspecialidad"));

    campoBusqueda.closest(".card").querySelector("button")
        .addEventListener("click", () => cargarEspecialidades());

    campoBusqueda.addEventListener("keydown", evento => {
        if (evento.key === "Enter") {
            evento.preventDefault();
            cargarEspecialidades();
        }
    });

    // El botón "+ Nueva especialidad" abre el modal vacío.
    document.querySelector('[data-bs-target="#modalEspecialidad"]')
        .addEventListener("click", () => prepararAlta());

    formulario.addEventListener("submit", evento => {
        evento.preventDefault();
        guardar();
    });

    cargarEspecialidades();
});

async function cargarEspecialidades() {
    const texto = campoBusqueda.value.trim();
    const ruta = texto ? `/especialidades?buscar=${encodeURIComponent(texto)}` : "/especialidades";

    cuerpoTabla.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">Cargando...</td></tr>`;

    try {
        dibujar(await api.get(ruta));
    } catch (error) {
        cuerpoTabla.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">No se pudieron cargar las especialidades</td></tr>`;
        manejarError(error);
    }
}

function dibujar(especialidades) {
    if (especialidades.length === 0) {
        cuerpoTabla.innerHTML = `<tr><td colspan="5" class="text-center text-muted py-4">No se encontraron especialidades</td></tr>`;
        return;
    }

    cuerpoTabla.innerHTML = especialidades.map(e => `
        <tr>
            <td>${esc(e.nombre)}</td>
            <td>${esc(e.descripcion)}</td>
            <td class="text-center">${e.cantidadProfesionales}</td>
            <td>
                <span class="badge text-bg-${e.activa ? "success" : "secondary"}">
                    ${e.activa ? "Activa" : "Inactiva"}
                </span>
            </td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-outline-primary me-1" data-editar="${e.id}">Editar</button>
                <button type="button" class="btn btn-sm btn-outline-danger" data-eliminar="${e.id}">Eliminar</button>
            </td>
        </tr>
    `).join("");

    cuerpoTabla.querySelectorAll("[data-editar]").forEach(boton =>
        boton.addEventListener("click", () => prepararEdicion(boton.dataset.editar)));

    cuerpoTabla.querySelectorAll("[data-eliminar]").forEach(boton =>
        boton.addEventListener("click", () => eliminar(boton.dataset.eliminar)));
}

function prepararAlta() {
    idEnEdicion = null;
    limpiarErroresDeCampos(formulario);
    formulario.reset();
    document.getElementById("tituloModalEspecialidad").textContent = "Nueva especialidad";
}

async function prepararEdicion(id) {
    try {
        const especialidad = await api.get(`/especialidades/${id}`);

        idEnEdicion = id;
        limpiarErroresDeCampos(formulario);
        formulario.nombre.value = especialidad.nombre;
        formulario.descripcion.value = especialidad.descripcion || "";
        formulario.activa.value = String(especialidad.activa);

        document.getElementById("tituloModalEspecialidad").textContent = "Editar especialidad";
        modal.show();
    } catch (error) {
        manejarError(error);
    }
}

async function guardar() {
    limpiarErroresDeCampos(formulario);

    const datos = {
        nombre: formulario.nombre.value.trim(),
        descripcion: formulario.descripcion.value.trim(),
        activa: formulario.activa.value === "true"
    };

    try {
        if (idEnEdicion) {
            await api.put(`/especialidades/${idEnEdicion}`, datos);
            avisar("Especialidad actualizada", "success");
        } else {
            await api.post("/especialidades", datos);
            avisar("Especialidad creada", "success");
        }

        modal.hide();
        cargarEspecialidades();
    } catch (error) {
        manejarError(error, formulario);
    }
}

async function eliminar(id) {
    if (!confirm("¿Confirma eliminar esta especialidad?")) return;

    try {
        await api.delete(`/especialidades/${id}`);
        avisar("Especialidad eliminada", "success");
        cargarEspecialidades();
    } catch (error) {
        manejarError(error);
    }
}
