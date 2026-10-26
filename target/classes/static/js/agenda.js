/*
 * Agenda semanal por profesional.
 * La grilla se arma con los turnos que devuelve /api/turnos/agenda.
 */

const HORARIOS = ["08:00", "09:00", "10:00", "11:00", "16:00", "17:00"];

let comboProfesional;
let campoSemana;
let cuerpoAgenda;

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    comboProfesional = document.getElementById("agendaProfesional");
    campoSemana = document.getElementById("agendaSemana");
    cuerpoAgenda = document.getElementById("agendaBody");

    campoSemana.value = fechaHoyISO();

    document.getElementById("btnVerAgenda").addEventListener("click", dibujarAgenda);
    comboProfesional.addEventListener("change", dibujarAgenda);
    campoSemana.addEventListener("change", dibujarAgenda);
    document.getElementById("btnHoy").addEventListener("click", () => {
        campoSemana.value = fechaHoyISO();
        dibujarAgenda();
    });

    await cargarProfesionales();
    dibujarAgenda();
});

async function cargarProfesionales() {
    try {
        const profesionales = await api.get("/profesionales?soloActivos=true");

        if (profesionales.length === 0) {
            comboProfesional.innerHTML = `<option value="">No hay profesionales activos</option>`;
            return;
        }

        comboProfesional.innerHTML = profesionales.map(p =>
            `<option value="${p.id}" data-especialidad="${esc(p.especialidadNombre)}">
                ${esc(p.nombreCompleto)} · ${esc(p.especialidadNombre)}
             </option>`).join("");

    } catch (error) {
        manejarError(error);
    }
}

/* ---------- Fechas ---------- */

function lunesDe(fecha) {
    const dia = new Date(fecha);
    const numeroDia = dia.getDay();
    dia.setDate(dia.getDate() + (numeroDia === 0 ? -6 : 1 - numeroDia));
    dia.setHours(0, 0, 0, 0);
    return dia;
}

function aISO(fecha) {
    const mes = String(fecha.getMonth() + 1).padStart(2, "0");
    const dia = String(fecha.getDate()).padStart(2, "0");
    return `${fecha.getFullYear()}-${mes}-${dia}`;
}

function formatoCorto(fecha) {
    return fecha.toLocaleDateString("es-AR", { day: "2-digit", month: "2-digit" });
}

function nombreDia(fecha) {
    const nombre = fecha.toLocaleDateString("es-AR", { weekday: "long" });
    return nombre.charAt(0).toUpperCase() + nombre.slice(1);
}

/* ---------- Grilla ---------- */

async function dibujarAgenda() {
    const profesionalId = comboProfesional.value;
    if (!profesionalId) return;

    const lunes = lunesDe(new Date(`${campoSemana.value}T12:00:00`));
    const viernes = new Date(lunes);
    viernes.setDate(lunes.getDate() + 4);

    const opcion = comboProfesional.selectedOptions[0];
    document.getElementById("doctorTitle").textContent = opcion.textContent.split("·")[0].trim();
    document.getElementById("doctorSpecialty").textContent = opcion.dataset.especialidad || "";

    const dias = [];
    for (let i = 0; i < 5; i++) {
        const dia = new Date(lunes);
        dia.setDate(lunes.getDate() + i);
        dias.push(dia);

        const encabezado = document.getElementById(`day-${i}`);
        const esHoy = aISO(dia) === fechaHoyISO();
        encabezado.innerHTML = `<span class="day-name">${nombreDia(dia)}</span>
                                <span class="day-date">${formatoCorto(dia)}</span>
                                ${esHoy ? '<span class="today-badge">HOY</span>' : ''}`;
        encabezado.classList.toggle("today", esHoy);
    }

    try {
        const turnos = await api.get(
            `/turnos/agenda?profesionalId=${profesionalId}&desde=${aISO(lunes)}&hasta=${aISO(viernes)}`);

        // Se indexan los turnos por "fecha|hora" para ubicarlos en la grilla.
        const porCelda = {};
        turnos.forEach(t => { porCelda[`${t.fecha}|${t.hora}`] = t; });

        pintarTabla(dias, porCelda);
        pintarVistaMovil(dias, porCelda);
        actualizarResumen(dias, turnos);

    } catch (error) {
        manejarError(error);
    }
}

function pintarTabla(dias, porCelda) {
    cuerpoAgenda.innerHTML = "";

    HORARIOS.forEach(hora => {
        const fila = document.createElement("tr");
        fila.innerHTML = `<th>${hora}</th>`;

        dias.forEach(dia => {
            const celda = document.createElement("td");
            const turno = porCelda[`${aISO(dia)}|${hora}`];

            if (turno) {
                celda.innerHTML = `<button class="slot" type="button">
                        <strong>${esc(turno.pacienteNombre)}</strong>
                        <small><i class="bi bi-clock me-1"></i>${hora} · ${turno.estado.toLowerCase()}</small>
                    </button>`;
                celda.querySelector("button").addEventListener("click", () => mostrarTurno(turno, dia));
            } else {
                celda.innerHTML = `<button class="free-slot" type="button">
                        <i class="bi bi-plus-lg"></i>
                        <span class="visually-hidden">Agregar turno</span>
                    </button>`;
                celda.querySelector("button").addEventListener("click", () => mostrarLibre(dia, hora));
            }

            fila.appendChild(celda);
        });

        cuerpoAgenda.appendChild(fila);
    });
}

function pintarVistaMovil(dias, porCelda) {
    const contenedor = document.getElementById("mobileAgenda");
    contenedor.innerHTML = "";

    dias.forEach(dia => {
        const tarjeta = document.createElement("div");
        tarjeta.className = "mobile-day";
        const esHoy = aISO(dia) === fechaHoyISO();

        tarjeta.innerHTML = `
            <div class="mobile-day-header">
                <div>
                    <div class="mobile-day-name">${nombreDia(dia)} ${esHoy ? '<span class="today-badge ms-1">HOY</span>' : ''}</div>
                    <div class="mobile-day-date">${formatoCorto(dia)}</div>
                </div>
                <i class="bi bi-calendar3 text-success"></i>
            </div>`;

        HORARIOS.forEach(hora => {
            const fila = document.createElement("div");
            fila.className = "mobile-slot";
            fila.innerHTML = `<div class="mobile-time">${hora}</div>`;

            const turno = porCelda[`${aISO(dia)}|${hora}`];

            if (turno) {
                fila.innerHTML += `<button class="slot" type="button">
                        <strong>${esc(turno.pacienteNombre)}</strong>
                        <small><i class="bi bi-check-circle me-1"></i>${turno.estado.toLowerCase()}</small>
                    </button>`;
                fila.querySelector("button").addEventListener("click", () => mostrarTurno(turno, dia));
            } else {
                fila.innerHTML += `<button class="free-slot" type="button">
                        <i class="bi bi-plus-lg me-1"></i>Disponible
                    </button>`;
                fila.querySelector("button").addEventListener("click", () => mostrarLibre(dia, hora));
            }

            tarjeta.appendChild(fila);
        });

        contenedor.appendChild(tarjeta);
    });
}

function actualizarResumen(dias, turnos) {
    const total = turnos.length;
    const hoy = fechaHoyISO();

    document.getElementById("totalTurnos").textContent = total;
    document.getElementById("turnosHoy").textContent = turnos.filter(t => t.fecha === hoy).length;
    document.getElementById("horariosLibres").textContent = (HORARIOS.length * 5) - total;
}

/* ---------- Modal ---------- */

function mostrarTurno(turno, dia) {
    document.getElementById("modalContent").innerHTML = `
        <div class="p-3 rounded-3" style="background:#f2fbf5">
            <div class="small text-muted mb-1">Paciente</div>
            <div class="fw-bold fs-5 mb-3">${esc(turno.pacienteNombre)}</div>
            <div class="row g-3">
                <div class="col-6"><div class="small text-muted">Fecha</div><strong>${formatoCorto(dia)}</strong></div>
                <div class="col-6"><div class="small text-muted">Horario</div><strong>${esc(turno.hora)}</strong></div>
                <div class="col-12">
                    <div class="small text-muted">Profesional</div>
                    <strong>${esc(turno.profesionalNombre)}</strong>
                    <div class="small text-muted">${esc(turno.especialidadNombre)}</div>
                </div>
                ${turno.motivo ? `<div class="col-12"><div class="small text-muted">Motivo</div>${esc(turno.motivo)}</div>` : ""}
                <div class="col-12"><div class="small text-muted">Estado</div>${etiquetaEstado(turno.estado)}</div>
            </div>
        </div>`;

    bootstrap.Modal.getOrCreateInstance(document.getElementById("turnoModal")).show();
}

function mostrarLibre(dia, hora) {
    document.getElementById("modalContent").innerHTML = `
        <div class="text-center py-3">
            <div class="summary-icon mx-auto mb-3"><i class="bi bi-calendar2-plus"></i></div>
            <h5 class="fw-bold">Horario disponible</h5>
            <p class="text-muted mb-0">
                Podés crear un turno para el <strong>${formatoCorto(dia)}</strong>
                a las <strong>${hora}</strong>.
            </p>
        </div>`;

    bootstrap.Modal.getOrCreateInstance(document.getElementById("turnoModal")).show();
}
