/* Pantalla de inicio de sesión. */

document.addEventListener("DOMContentLoaded", () => {

    // Si ya hay una sesión abierta, no tiene sentido mostrar el login.
    if (obtenerToken()) {
        window.location.href = "dashboard.html";
        return;
    }

    const formulario = document.getElementById("formLogin");
    const boton = formulario.querySelector('button[type="submit"]');

    formulario.addEventListener("submit", async evento => {
        evento.preventDefault();
        limpiarErroresDeCampos(formulario);

        const username = formulario.username.value.trim();
        const password = formulario.password.value;

        if (!username || !password) {
            avisar("Complete usuario y contraseña", "warning");
            return;
        }

        boton.disabled = true;
        boton.textContent = "Ingresando...";

        try {
            const sesion = await api.post("/auth/login", { username, password });
            guardarSesion(sesion);
            window.location.href = "dashboard.html";
        } catch (error) {
            manejarError(error, formulario);
            boton.disabled = false;
            boton.textContent = "Iniciar sesión";
        }
    });
});
