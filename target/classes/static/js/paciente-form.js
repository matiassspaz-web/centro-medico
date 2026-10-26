/*
 * Alta y edición de pacientes.
 * Si la URL trae ?id=, el formulario carga ese paciente y lo actualiza (PUT).
 * Si no, crea uno nuevo (POST).
 */

let idPaciente = null;

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirSesion()) return;
    prepararNavegacion();

    const formulario = document.getElementById("formPaciente");
    idPaciente = new URLSearchParams(window.location.search).get("id");

    if (idPaciente) {
        document.getElementById("tituloFormulario").textContent = "Editar Paciente";
        await cargarPaciente(formulario);
    }

    formulario.addEventListener("submit", evento => {
        evento.preventDefault();
        guardar(formulario);
    });
});

async function cargarPaciente(formulario) {
    try {
        const paciente = await api.get(`/pacientes/${idPaciente}`);

        ["dni", "nombre", "apellido", "fechaNacimiento", "telefono", "email",
         "direccion", "obraSocial", "nroAfiliado", "grupoSanguineo", "alergias"]
            .forEach(campo => {
                if (paciente[campo] !== null && paciente[campo] !== undefined) {
                    formulario[campo].value = paciente[campo];
                }
            });

    } catch (error) {
        manejarError(error);
    }
}

async function guardar(formulario) {
    limpiarErroresDeCampos(formulario);

    const datos = {
        dni: formulario.dni.value.trim(),
        nombre: formulario.nombre.value.trim(),
        apellido: formulario.apellido.value.trim(),
        fechaNacimiento: formulario.fechaNacimiento.value || null,
        telefono: formulario.telefono.value.trim(),
        email: formulario.email.value.trim(),
        direccion: formulario.direccion.value.trim(),
        obraSocial: formulario.obraSocial.value,
        nroAfiliado: formulario.nroAfiliado.value.trim(),
        grupoSanguineo: formulario.grupoSanguineo.value,
        alergias: formulario.alergias.value.trim()
    };

    const boton = formulario.querySelector('button[type="submit"]');
    boton.disabled = true;

    try {
        if (idPaciente) {
            await api.put(`/pacientes/${idPaciente}`, datos);
            avisar("Paciente actualizado correctamente", "success");
        } else {
            await api.post("/pacientes", datos);
            avisar("Paciente registrado correctamente", "success");
        }

        setTimeout(() => window.location.href = "pacientes.html", 900);

    } catch (error) {
        manejarError(error, formulario);
        boton.disabled = false;
    }
}
