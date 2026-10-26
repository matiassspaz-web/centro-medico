/* Gestión de turnos: listado con filtros, alta, edición y cambio de estado. */

let cuerpoTabla;
let formulario;
let modal;
let idEnEdicion = null;
let profesionales = [];

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    cuerpoTabla = document.querySelector("main table tbody");
    formulario = document.getElementById("formTurno");
    modal = new bootstrap.Modal(document.getElementById("modalTurno"));

    document.getElementById("buscarTurnoPaciente").closest(".card")
        .querySelector("button").addEventListener("click", () => cargarTurnos());

    document.getElementById("buscarTurnoPaciente").addEventListener("keydown", evento => {
        if (evento.key === "Enter") {
            evento.preventDefault();
            cargarTurnos();
        }
    });

    ["filtroTurnoProfesional", "filtroTurnoFecha", "filtroTurnoEstado"].forEach(id =>
        document.getElementById(id).addEventListener("change", () => cargarTurnos()));

    // El botón + Nuevo turno del encabezado abre el modal vacío.
    document.querySelector('main [data-bs-target="#modalTurno"]')
        .addEventListener("click", () => prepararAlta());

    // Al cambiar la especialidad en el modal, se acota la lista de profesionales.
    formulario.especialidadId.addEventListener("change", () => filtrarProfesionalesDelModal());

    formulario.addEventListener("submit", evento => {
        evento.preventDefault();
        guardar();
    });

    await Promise.all([cargarCombos(), cargarEspecialidadesDelModal()]);
    cargarTurnos();
});

/* ---------- Combos ---------- */

async function cargarCombos() {
    try {
        profesionales = await api.get("/profesionales");

        const filtro = document.getElementById("filtroTurnoProfesional");
        filtro.innerHTML = `<option value="">Todos los profesionales</option>` +
            profesionales.map(p => `<option value="${p.id}">${esc(p.nombreCompleto)}</option>`).join("");

        const estado = document.getElementById("filtroTurnoEstado");
        estado.innerHTML = `
            <option value="">Todos</option>
            <option value="PENDIENTE">Pendiente</option>
            <option value="ATENDIDO">Atendido</option>
            <option value="CANCELADO">Cancelado</option>`;

        filtrarProfesionalesDelModal();
    } catch (error) {
        manejarError(error);
    }
}

async function cargarEspecialidadesDelModal() {
    try {
        const especialidades = await api.get("/especialidades");
        formulario.especialidadId.innerHTML = `<option value="">Todas</option>` +
            especialidades.map(e => `<option value="${e.id}">${esc(e.nombre)}</option>`).join("");
    } catch (error) {
        manejarError(error);
    }
}

function filtrarProfesionalesDelModal() {
    const especialidadId = formulario.especialidadId.value;
    const seleccionActual = formulario.profesionalId.value;

    const disponibles = profesionales.filter(p =>
        p.activo && (!especialidadId || String(p.especialidadId) === especialidadId));

    formulario.profesionalId.innerHTML = `<option value="">Seleccionar</option>` +
        disponibles.map(p =>
            `<option value="${p.id}">${esc(p.nombreCompleto)} - ${esc(p.especialidadNombre)}</option>`
        ).join("");

    if (disponibles.some(p => String(p.id) === seleccionActual)) {
        formulario.profesionalId.value = seleccionActual;
    }
}

/* ---------- Listado ---------- */

async function cargarTurnos() {
    const parametros = new URLSearchParams();

    const texto = document.getElementById("buscarTurnoPaciente").value.trim();
    const profesionalId = document.getElementById("filtroTurnoProfesional").value;
    const fecha = document.getElementById("filtroTurnoFecha").value;
    const estado = document.getElementById("filtroTurnoEstado").value;

    if (texto) parametros.set("buscar", texto);
    if (profesionalId) parametros.set("profesionalId", profesionalId);
    if (fecha) parametros.set("fecha", fecha);
    if (estado) parametros.set("estado", estado);

    const consulta = parametros.toString();
    cuerpoTabla.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">Cargando...</td></tr>`;

    try {
        dibujar(await api.get("/turnos" + (consulta ? "?" + consulta : "")));
    } catch (error) {
        cuerpoTabla.innerHTML = `<tr><td colspan="7" class="text-center text-danger py-4">No se pudieron cargar los turnos</td></tr>`;
        manejarError(error);
    }
}

function dibujar(turnos) {
    if (turnos.length === 0) {
        cuerpoTabla.innerHTML = `<tr><td colspan="7" class="text-center text-muted py-4">No se encontraron turnos</td></tr>`;
        return;
    }

    cuerpoTabla.innerHTML = turnos.map(t => `
        <tr>
            <td>${formatearFechaISO(t.fecha)}</td>
            <td>${esc(t.hora)}</td>
            <td>${esc(t.pacienteNombre)}</td>
            <td>${esc(t.profesionalNombre)}</td>
            <td>${esc(t.especialidadNombre)}</td>
            <td>${etiquetaEstado(t.estado)}</td>
            <td class="text-center">
                <button type="button" class="btn btn-sm btn-outline-primary me-1"
                        data-editar="${t.id}" ${t.estado === "CANCELADO" ? "disabled" : ""}>Editar</button>
                ${t.estado === "PENDIENTE" ? `
                    <button type="button" class="btn btn-sm btn-outline-success me-1"
                            data-atender="${t.id}">Atendido</button>
                    <button type="button" class="btn btn-sm btn-outline-danger"
                            data-cancelar="${t.id}">Cancelar</button>` : ""}
            </td>
        </tr>
    `).join("");

    cuerpoTabla.querySelectorAll("[data-editar]").forEach(boton =>
        boton.addEventListener("click", () => prepararEdicion(boton.dataset.editar)));

    cuerpoTabla.querySelectorAll("[data-atender]").forEach(boton =>
        boton.addEventListener("click", () => cambiarEstado(boton.dataset.atender, "ATENDIDO")));

    cuerpoTabla.querySelectorAll("[data-cancelar]").forEach(boton =>
        boton.addEventListener("click", () => {
            if (confirm("¿Confirma cancelar este turno?")) {
                cambiarEstado(boton.dataset.cancelar, "CANCELADO");
            }
        }));
}

/* ---------- Alta y edición ---------- */

function prepararAlta() {
    idEnEdicion = null;
    limpiarErroresDeCampos(formulario);
    formulario.reset();
    formulario.fecha.value = fechaHoyISO();
    filtrarProfesionalesDelModal();
    document.getElementById("tituloModalTurno").textContent = "Nuevo turno";
}

async function prepararEdicion(id) {
    try {
        const turno = await api.get(`/turnos/${id}`);

        idEnEdicion = id;
        limpiarErroresDeCampos(formulario);

        formulario.pacienteDni.value = turno.pacienteDni;
        formulario.especialidadId.value = turno.especialidadId;
        filtrarProfesionalesDelModal();
        formulario.profesionalId.value = turno.profesionalId;
        formulario.fecha.value = turno.fecha;
        formulario.hora.value = turno.hora;
        formulario.motivo.value = turno.motivo || "";

        document.getElementById("tituloModalTurno").textContent = "Editar turno";
        modal.show();
    } catch (error) {
        manejarError(error);
    }
}

async function guardar() {
    limpiarErroresDeCampos(formulario);

    const datos = {
        pacienteDni: formulario.pacienteDni.value.trim(),
        profesionalId: formulario.profesionalId.value ? Number(formulario.profesionalId.value) : null,
        fecha: formulario.fecha.value || null,
        hora: formulario.hora.value || null,
        motivo: formulario.motivo.value.trim()
    };

    try {
        if (idEnEdicion) {
            await api.put(`/turnos/${idEnEdicion}`, datos);
            avisar("Turno actualizado", "success");
        } else {
            await api.post("/turnos", datos);
            avisar("Turno registrado", "success");
        }

        modal.hide();
        cargarTurnos();
    } catch (error) {
        manejarError(error, formulario);
    }
}

async function cambiarEstado(id, estado) {
    try {
        await api.patch(`/turnos/${id}/estado`, { estado });
        avisar(`Turno marcado como ${estado.toLowerCase()}`, "success");
        cargarTurnos();
    } catch (error) {
        manejarError(error);
    }
}
