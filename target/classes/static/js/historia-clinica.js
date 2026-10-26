/*
 * Historia clínica: lista de pacientes, ficha, registro de atenciones
 * e historial cronológico, todo contra la API.
 */

let pacientes = [];
let pacienteActual = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;

    const usuario = obtenerUsuario();
    if (usuario) {
        const nombre = document.querySelector("header .text-right p.text-xs");
        if (nombre) nombre.textContent = usuario.nombreCompleto;
    }

    document.getElementById("inputFecha").value = fechaHoyISO();

    document.getElementById("buscarPacienteHc").addEventListener("input", evento =>
        dibujarListaPacientes(evento.target.value.trim().toLowerCase()));

    await Promise.all([cargarPacientes(), cargarProfesionales()]);
});

/* ---------- Pacientes ---------- */

async function cargarPacientes() {
    try {
        pacientes = await api.get("/pacientes");
        dibujarListaPacientes("");

        if (pacientes.length > 0) {
            seleccionarPaciente(pacientes[0].id);
        }
    } catch (error) {
        document.getElementById("patientList").innerHTML =
            `<p class="text-xs text-red-500">No se pudieron cargar los pacientes</p>`;
        manejarError(error);
    }
}

function dibujarListaPacientes(filtro) {
    const lista = document.getElementById("patientList");

    const visibles = pacientes.filter(p =>
        !filtro ||
        p.nombreCompleto.toLowerCase().includes(filtro) ||
        p.dni.includes(filtro));

    if (visibles.length === 0) {
        lista.innerHTML = `<p class="text-xs text-slate-400 italic">Sin resultados</p>`;
        return;
    }

    lista.innerHTML = visibles.map(p => {
        const activo = pacienteActual && pacienteActual.id === p.id;
        const iniciales = (p.apellido[0] + p.nombre[0]).toUpperCase();

        return `
        <button type="button" data-paciente="${p.id}"
                class="patient-btn w-full text-left p-3 rounded-lg border transition-all flex items-center justify-between group
                       ${activo ? "border-teal-500 bg-teal-50/50" : "border-slate-200 hover:border-teal-300 hover:bg-slate-50"}">
            <div class="flex items-center space-x-3">
                <div class="w-10 h-10 rounded-full flex items-center justify-center font-semibold text-sm
                            ${activo ? "bg-teal-100 text-teal-700" : "bg-slate-100 text-slate-600"}">
                    ${esc(iniciales)}
                </div>
                <div>
                    <p class="font-semibold text-slate-800 text-sm">${esc(p.nombreCompleto)}</p>
                    <p class="text-xs text-slate-500">${p.edad} años • DNI ${esc(p.dni)}</p>
                </div>
            </div>
            <i class="fa-solid fa-chevron-right text-xs ${activo ? "text-teal-600" : "text-slate-400"}"></i>
        </button>`;
    }).join("");

    lista.querySelectorAll("[data-paciente]").forEach(boton =>
        boton.addEventListener("click", () => seleccionarPaciente(Number(boton.dataset.paciente))));
}

async function seleccionarPaciente(id) {
    pacienteActual = pacientes.find(p => p.id === id);
    if (!pacienteActual) return;

    dibujarListaPacientes(document.getElementById("buscarPacienteHc").value.trim().toLowerCase());

    document.getElementById("summaryName").textContent = pacienteActual.nombreCompleto;
    document.getElementById("summaryAge").textContent = `${pacienteActual.edad} años`;
    document.getElementById("summaryHc").textContent = "HC-" + String(pacienteActual.id).padStart(5, "0");
    document.getElementById("summaryBlood").textContent = pacienteActual.grupoSanguineo || "Sin dato";
    document.getElementById("summaryAllergies").textContent = pacienteActual.alergias || "Sin dato";
    document.getElementById("historyPatientTitle").textContent = pacienteActual.nombreCompleto;

    await cargarHistorial();
}

async function cargarProfesionales() {
    const combo = document.getElementById("inputProfesional");

    try {
        const profesionales = await api.get("/profesionales?soloActivos=true");

        combo.innerHTML = `<option value="">Seleccionar profesional</option>` +
            profesionales.map(p => `<option value="${p.id}">${esc(p.nombreCompleto)}</option>`).join("");

    } catch (error) {
        manejarError(error);
    }
}

/* ---------- Historial ---------- */

async function cargarHistorial() {
    const lista = document.getElementById("chronologicalList");
    lista.innerHTML = `<div class="pl-6 text-slate-400 italic text-sm">Cargando historial...</div>`;

    try {
        const atenciones = await api.get(`/atenciones?pacienteId=${pacienteActual.id}`);
        document.getElementById("historyCount").textContent = atenciones.length;
        dibujarHistorial(atenciones);
    } catch (error) {
        lista.innerHTML = `<div class="pl-6 text-red-500 italic text-sm">No se pudo cargar el historial</div>`;
        manejarError(error);
    }
}

function dibujarHistorial(atenciones) {
    const lista = document.getElementById("chronologicalList");

    if (atenciones.length === 0) {
        lista.innerHTML = `<div class="pl-6 text-slate-400 italic text-sm">
            No hay atenciones registradas para este paciente.
        </div>`;
        return;
    }

    lista.innerHTML = atenciones.map((a, indice) => `
        <div class="relative pl-6 pb-2">
            <div class="absolute -left-[17px] top-0 w-8 h-8 rounded-full bg-teal-600 text-white flex items-center justify-center text-xs shadow">
                <i class="fa-solid fa-stethoscope"></i>
            </div>

            <div class="bg-slate-50 border border-slate-200 rounded-xl p-4 space-y-3 hover:border-teal-300 transition-colors">
                <div class="flex flex-wrap justify-between items-center border-b border-slate-200 pb-2 gap-2">
                    <div>
                        <span class="text-xs font-bold text-teal-700 bg-teal-100 px-2 py-0.5 rounded">
                            <i class="fa-regular fa-calendar-alt mr-1"></i>${formatearFechaISO(a.fecha)}
                        </span>
                        <span class="text-xs font-medium text-slate-500 ml-2">
                            <i class="fa-solid fa-user-doctor mr-1"></i>${esc(a.profesionalNombre)}
                        </span>
                    </div>
                    <span class="text-[11px] text-slate-400">Atención #${atenciones.length - indice}</span>
                </div>

                <h4 class="text-sm font-bold text-slate-800">${esc(a.motivo)}</h4>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                    ${bloque("fa-head-side-cough", "Síntomas", a.sintomas)}
                    ${bloque("fa-microscope", "Diagnóstico", a.diagnostico)}
                </div>

                <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
                    ${bloque("fa-pills", "Tratamiento", a.tratamiento)}
                    ${bloque("fa-clipboard-list", "Indicaciones", a.indicaciones)}
                </div>

                ${a.observaciones ? `
                <div class="bg-amber-50/60 p-2.5 rounded-lg border border-amber-200/60 text-xs">
                    <span class="font-semibold text-amber-800 block mb-0.5">
                        <i class="fa-solid fa-circle-info mr-1"></i>Observaciones:
                    </span>
                    <p class="text-amber-900">${esc(a.observaciones)}</p>
                </div>` : ""}
            </div>
        </div>
    `).join("");
}

function bloque(icono, titulo, contenido) {
    return `
        <div class="bg-white p-2.5 rounded-lg border border-slate-100">
            <span class="font-semibold text-slate-600 block mb-1">
                <i class="fa-solid ${icono} text-teal-600 mr-1"></i>${titulo}:
            </span>
            <p class="text-slate-700">${esc(contenido)}</p>
        </div>`;
}

/* ---------- Registrar atención ---------- */

/* Llamada desde el onsubmit del formulario en el HTML. */
async function saveConsultation(evento) {
    evento.preventDefault();

    if (!pacienteActual) {
        avisar("Seleccione un paciente antes de registrar la atención", "warning");
        return;
    }

    const profesionalId = document.getElementById("inputProfesional").value;
    if (!profesionalId) {
        avisar("Seleccione el profesional que realiza la atención", "warning");
        return;
    }

    const datos = {
        pacienteId: pacienteActual.id,
        profesionalId: Number(profesionalId),
        fecha: document.getElementById("inputFecha").value || null,
        motivo: document.getElementById("inputMotivo").value.trim(),
        sintomas: document.getElementById("inputSintomas").value.trim(),
        diagnostico: document.getElementById("inputDiagnostico").value.trim(),
        tratamiento: document.getElementById("inputTratamiento").value.trim(),
        indicaciones: document.getElementById("inputIndicaciones").value.trim(),
        observaciones: document.getElementById("inputObservaciones").value.trim()
    };

    try {
        await api.post("/atenciones", datos);
        avisar("Atención registrada correctamente", "success");

        ["inputMotivo", "inputSintomas", "inputDiagnostico",
         "inputTratamiento", "inputIndicaciones", "inputObservaciones"]
            .forEach(id => document.getElementById(id).value = "");

        await cargarHistorial();
        switchTab("history");

    } catch (error) {
        // El backend devuelve los errores por campo; acá se muestran como aviso.
        const detalle = Object.values(error.errores || {}).join(" · ");
        avisar(detalle || error.message, "danger");
    }
}

/* Llamada desde los onclick de las pestañas en el HTML. */
function switchTab(pestania) {
    const panelFormulario = document.getElementById("tab-form");
    const panelHistorial = document.getElementById("tab-history");
    const botonFormulario = document.getElementById("tab-form-btn");
    const botonHistorial = document.getElementById("tab-history-btn");

    const activo = "pb-3 px-4 font-medium text-sm border-b-2 border-teal-600 text-teal-700 flex items-center space-x-2";
    const inactivo = "pb-3 px-4 font-medium text-sm border-b-2 border-transparent text-slate-500 hover:text-slate-700 flex items-center space-x-2";

    const esFormulario = pestania === "form";

    panelFormulario.classList.toggle("hidden", !esFormulario);
    panelHistorial.classList.toggle("hidden", esFormulario);
    botonFormulario.className = esFormulario ? activo : inactivo;
    botonHistorial.className = esFormulario ? inactivo : activo;
}
