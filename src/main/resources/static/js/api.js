/*
 * Capa de comunicación con la API REST del backend.
 * Todas las páginas usan estas funciones en lugar de llamar a fetch directamente.
 */

/*
 * Publicado en Vercel, el frontend queda en un dominio y el backend en otro
 * (Render), así que las llamadas tienen que llevar la URL completa. Cuando lo
 * sirve el propio Spring Boot, alcanza con la ruta relativa.
 */
const BACKEND = "https://TU-SERVICIO.onrender.com";
const API = location.hostname.endsWith("vercel.app") ? BACKEND + "/api" : "/api";
const CLAVE_TOKEN = "cm_token";
const CLAVE_USUARIO = "cm_usuario";

/* ---------- Sesión ---------- */

function guardarSesion(datos) {
    localStorage.setItem(CLAVE_TOKEN, datos.token);
    localStorage.setItem(CLAVE_USUARIO, JSON.stringify({
        username: datos.username,
        nombreCompleto: datos.nombreCompleto,
        rol: datos.rol
    }));
}

function obtenerToken() {
    return localStorage.getItem(CLAVE_TOKEN);
}

function obtenerUsuario() {
    const crudo = localStorage.getItem(CLAVE_USUARIO);
    return crudo ? JSON.parse(crudo) : null;
}

function cerrarSesion() {
    localStorage.removeItem(CLAVE_TOKEN);
    localStorage.removeItem(CLAVE_USUARIO);
    window.location.href = "index.html";
}

/**
 * Se llama al inicio de cada página privada: si no hay token, vuelve al login.
 */
function exigirSesion() {
    if (!obtenerToken()) {
        window.location.href = "index.html";
        return false;
    }
    return true;
}

/* ---------- Peticiones ---------- */

/**
 * Error con los datos que devolvió el backend, para poder mostrar
 * los mensajes de validación campo por campo.
 */
class ErrorApi extends Error {
    constructor(mensaje, estado, errores) {
        super(mensaje);
        this.estado = estado;
        this.errores = errores || {};
    }
}

async function peticion(metodo, ruta, cuerpo) {
    const opciones = {
        method: metodo,
        headers: { "Content-Type": "application/json" }
    };

    const token = obtenerToken();
    if (token) {
        opciones.headers["Authorization"] = "Bearer " + token;
    }

    if (cuerpo !== undefined) {
        opciones.body = JSON.stringify(cuerpo);
    }

    const respuesta = await fetch(API + ruta, opciones);

    if (respuesta.status === 401) {
        cerrarSesion();
        throw new ErrorApi("Su sesión expiró, vuelva a iniciar sesión", 401);
    }

    if (respuesta.status === 204) {
        return null;
    }

    const texto = await respuesta.text();
    const datos = texto ? JSON.parse(texto) : null;

    if (!respuesta.ok) {
        const mensaje = (datos && datos.mensaje) || "No se pudo completar la operación";
        throw new ErrorApi(mensaje, respuesta.status, datos && datos.errores);
    }

    return datos;
}

const api = {
    get: (ruta) => peticion("GET", ruta),
    post: (ruta, cuerpo) => peticion("POST", ruta, cuerpo),
    put: (ruta, cuerpo) => peticion("PUT", ruta, cuerpo),
    patch: (ruta, cuerpo) => peticion("PATCH", ruta, cuerpo),
    delete: (ruta) => peticion("DELETE", ruta)
};

/* ---------- Utilidades de interfaz ---------- */

/**
 * Muestra un cartel arriba de la página. Tipo: success, danger, warning o info.
 */
function avisar(mensaje, tipo = "info") {
    let contenedor = document.getElementById("avisos");

    if (!contenedor) {
        contenedor = document.createElement("div");
        contenedor.id = "avisos";
        contenedor.style.position = "fixed";
        contenedor.style.top = "1rem";
        contenedor.style.right = "1rem";
        contenedor.style.zIndex = "2000";
        contenedor.style.maxWidth = "420px";
        document.body.appendChild(contenedor);
    }

    const aviso = document.createElement("div");
    aviso.className = `alert alert-${tipo} shadow-sm`;
    aviso.textContent = mensaje;
    contenedor.appendChild(aviso);

    setTimeout(() => aviso.remove(), 4500);
}

/**
 * Pinta los mensajes de validación que devolvió el backend debajo de cada campo.
 * El objeto "errores" tiene la forma { nombreCampo: "mensaje" }.
 */
function mostrarErroresDeCampos(formulario, errores) {
    limpiarErroresDeCampos(formulario);

    Object.entries(errores).forEach(([campo, mensaje]) => {
        const input = formulario.querySelector(`[name="${campo}"]`);
        if (!input) return;

        input.classList.add("is-invalid");

        const detalle = document.createElement("div");
        detalle.className = "invalid-feedback";
        detalle.textContent = mensaje;
        input.insertAdjacentElement("afterend", detalle);
    });
}

function limpiarErroresDeCampos(formulario) {
    formulario.querySelectorAll(".is-invalid").forEach(i => i.classList.remove("is-invalid"));
    formulario.querySelectorAll(".invalid-feedback").forEach(e => e.remove());
}

/**
 * Maneja un error de la API: errores de validación al formulario, el resto como aviso.
 */
function manejarError(error, formulario) {
    if (formulario && error.errores && Object.keys(error.errores).length > 0) {
        mostrarErroresDeCampos(formulario, error.errores);
        avisar(error.message, "danger");
    } else {
        avisar(error.message, "danger");
    }
}

/* ---------- Formatos ---------- */

function formatearFechaISO(fechaIso) {
    if (!fechaIso) return "";
    const [anio, mes, dia] = fechaIso.split("-");
    return `${dia}/${mes}/${anio}`;
}

function fechaHoyISO() {
    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, "0");
    const dia = String(hoy.getDate()).padStart(2, "0");
    return `${hoy.getFullYear()}-${mes}-${dia}`;
}

const COLOR_ESTADO = {
    PENDIENTE: "warning",
    ATENDIDO: "success",
    CANCELADO: "secondary"
};

function etiquetaEstado(estado) {
    const texto = estado.charAt(0) + estado.slice(1).toLowerCase();
    return `<span class="badge text-bg-${COLOR_ESTADO[estado] || "secondary"}">${texto}</span>`;
}

/**
 * Escapa texto que viene de la base antes de insertarlo como HTML.
 */
function esc(valor) {
    if (valor === null || valor === undefined) return "";
    return String(valor)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;");
}

/**
 * Engancha el enlace "Cerrar sesión" y muestra el nombre del usuario conectado.
 */
function prepararNavegacion() {
    document.querySelectorAll('a[href="index.html"]').forEach(enlace => {
        if (enlace.textContent.toLowerCase().includes("cerrar")) {
            enlace.addEventListener("click", evento => {
                evento.preventDefault();
                cerrarSesion();
            });
        }
    });
}
