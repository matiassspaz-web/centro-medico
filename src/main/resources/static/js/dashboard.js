/* Panel principal: muestra un resumen con datos reales de la API. */

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    const usuario = obtenerUsuario();
    const titulo = document.querySelector("main h1");
    if (titulo && usuario) {
        titulo.textContent = `Bienvenido, ${usuario.nombreCompleto}`;
    }

    await cargarResumen();
});

async function cargarResumen() {
    const tarjetas = document.querySelector("main .row.g-4");
    if (!tarjetas) return;

    try {
        const resumen = await api.get("/dashboard");

        const indicadores = [
            { etiqueta: "Pacientes registrados", valor: resumen.totalPacientes },
            { etiqueta: "Profesionales", valor: resumen.totalProfesionales },
            { etiqueta: "Turnos para hoy", valor: resumen.turnosHoy },
            { etiqueta: "Turnos pendientes", valor: resumen.turnosPendientes }
        ];

        const bloque = document.createElement("div");
        bloque.className = "row g-3 mb-4";
        bloque.innerHTML = indicadores.map(i => `
            <div class="col-6 col-lg-3">
                <div class="card border-0 shadow-sm h-100">
                    <div class="card-body text-center">
                        <div class="h2 fw-bold mb-0">${i.valor}</div>
                        <div class="text-muted small">${i.etiqueta}</div>
                    </div>
                </div>
            </div>
        `).join("");

        tarjetas.parentNode.insertBefore(bloque, tarjetas);
    } catch (error) {
        manejarError(error);
    }
}
