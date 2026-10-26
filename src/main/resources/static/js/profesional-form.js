/* Alta y edición de profesionales. */

let idProfesional = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    const formulario = document.getElementById("formProfesional");
    idProfesional = new URLSearchParams(window.location.search).get("id");

    await cargarEspecialidades();

    if (idProfesional) {
        document.getElementById("tituloFormulario").textContent = "Editar Profesional";
        await cargarProfesional(formulario);
    }

    formulario.addEventListener("submit", evento => {
        evento.preventDefault();
        guardar(formulario);
    });
});

async function cargarEspecialidades() {
    const combo = document.getElementById("especialidadId");

    try {
        const especialidades = await api.get("/especialidades");

        combo.innerHTML = `<option value="">Seleccionar</option>` +
            especialidades.map(e =>
                `<option value="${e.id}">${esc(e.nombre)}${e.activa ? "" : " (inactiva)"}</option>`
            ).join("");

    } catch (error) {
        manejarError(error);
    }
}

async function cargarProfesional(formulario) {
    try {
        const profesional = await api.get(`/profesionales/${idProfesional}`);

        formulario.matricula.value = profesional.matricula;
        formulario.nombre.value = profesional.nombre;
        formulario.apellido.value = profesional.apellido;
        formulario.especialidadId.value = profesional.especialidadId;
        formulario.telefono.value = profesional.telefono || "";
        formulario.email.value = profesional.email || "";
        formulario.activo.value = String(profesional.activo);

    } catch (error) {
        manejarError(error);
    }
}

async function guardar(formulario) {
    limpiarErroresDeCampos(formulario);

    const datos = {
        matricula: formulario.matricula.value.trim(),
        nombre: formulario.nombre.value.trim(),
        apellido: formulario.apellido.value.trim(),
        especialidadId: formulario.especialidadId.value ? Number(formulario.especialidadId.value) : null,
        telefono: formulario.telefono.value.trim(),
        email: formulario.email.value.trim(),
        activo: formulario.activo.value === "true"
    };

    const boton = formulario.querySelector('button[type="submit"]');
    boton.disabled = true;

    try {
        if (idProfesional) {
            await api.put(`/profesionales/${idProfesional}`, datos);
            avisar("Profesional actualizado correctamente", "success");
        } else {
            await api.post("/profesionales", datos);
            avisar("Profesional registrado correctamente", "success");
        }

        setTimeout(() => window.location.href = "profesionales.html", 900);

    } catch (error) {
        manejarError(error, formulario);
        boton.disabled = false;
    }
}
