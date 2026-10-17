const API_URL = "http://localhost:8080/api";

async function llamarApi(ruta, opciones = {}) {
  const headers = new Headers(opciones.headers || {});
  headers.set("Accept", "application/json");
  const token = localStorage.getItem("token");
  if (token) headers.set("Authorization", `Bearer ${token}`);

  let body = opciones.body;
  if (body !== undefined && typeof body !== "string") {
    headers.set("Content-Type", "application/json");
    body = JSON.stringify(body);
  }
  const response = await fetch(`${API_URL}${ruta}`, { ...opciones, headers, body });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    if (response.status === 401 && !ruta.startsWith("/auth/") && !["login.html", "registro.html"].includes(location.pathname.split("/").pop())) {
      limpiarSesion();
      const next = `${location.pathname.split("/").pop()}${location.search}`;
      location.href = `login.html?next=${encodeURIComponent(next)}`;
    }
    throw new Error(errorBody?.mensaje || `No se pudo cargar la informacion (${response.status})`);
  }
  if (response.status === 204) return null;
  return response.json();
}

function guardarSesion(auth) {
  localStorage.setItem("token", auth.token);
  localStorage.setItem("usuario", JSON.stringify(auth.usuario));
  actualizarNavegacion();
}

function limpiarSesion() {
  localStorage.removeItem("token");
  localStorage.removeItem("usuario");
  actualizarNavegacion();
}

function leerUsuarioActual() {
  try {
    return JSON.parse(localStorage.getItem("usuario") || "null");
  } catch {
    limpiarSesion();
    return null;
  }
}

function actualizarNavegacion() {
  const nav = document.querySelector("#account-nav");
  if (!nav) return;
  nav.replaceChildren();
  const usuario = leerUsuarioActual();
  if (!localStorage.getItem("token") || !usuario) {
    const login = crearElemento("a", "nav-link", "Ingresar");
    login.href = "login.html";
    const register = crearElemento("a", "nav-link", "Crear cuenta");
    register.href = "registro.html";
    nav.append(login, register);
    return;
  }

  const profile = crearElemento("a", "nav-link", `Perfil · ${usuario.nombre || "Usuario"}`);
  profile.href = "perfil.html";
  const reservations = crearElemento("a", "nav-link", "Mis reservas");
  reservations.href = "mis-reservas.html";
  if (usuario.rol === "ANFITRION") {
    const hostPanel = crearElemento("a", "nav-link", "Panel anfitrion");
    hostPanel.href = "panel-anfitrion.html";
    nav.append(profile, reservations, hostPanel);
  } else if (usuario.rol === "ADMINISTRADOR") {
    const adminPanel = crearElemento("a", "nav-link", "Panel administrador");
    adminPanel.href = "panel-administrador.html";
    nav.append(profile, reservations, adminPanel);
  } else {
    nav.append(profile, reservations);
  }
  const role = crearElemento("span", "nav-link nav-role", String(usuario.rol || "").replaceAll("_", " "));
  role.setAttribute("aria-label", "Rol de la cuenta");
  const logout = crearElemento("button", "nav-link nav-command", "Salir");
  logout.type = "button";
  logout.addEventListener("click", () => {
    limpiarSesion();
    location.href = "index.html";
  });
  nav.append(role, logout);
}

actualizarNavegacion();

function crearElemento(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined && text !== null) element.textContent = text;
  return element;
}

function mostrarEstado(element, message, isError = false) {
  element.textContent = message;
  element.classList.toggle("text-danger", isError);
}

function formatoPrecio(value) {
  return new Intl.NumberFormat("es-CO", { style: "currency", currency: "COP", maximumFractionDigits: 0 }).format(value);
}

function datosPagina(page) {
  return Array.isArray(page) ? { content: page, totalElements: page.length, totalPages: 1, number: 0 } : page;
}

function crearTarjetaAlojamiento(alojamiento) {
  const link = crearElemento("a", "stay-card-link");
  link.href = `detalle-alojamiento.html?id=${encodeURIComponent(alojamiento.id)}`;
  const card = crearElemento("article", "stay-card");
  const imageWrap = crearElemento("div", "stay-image-wrap");
  if (alojamiento.imagenPrincipal) {
    const image = crearElemento("img", "stay-image");
    image.src = alojamiento.imagenPrincipal;
    image.alt = `Imagen de ${alojamiento.nombre}`;
    image.loading = "lazy";
    imageWrap.append(image);
  } else {
    imageWrap.append(crearElemento("div", "stay-image-placeholder", "Imagen no disponible"));
  }

  const body = crearElemento("div", "stay-card-body");
  body.append(crearElemento("h2", "stay-card-title", alojamiento.nombre));
  const metadata = crearElemento("div", "stay-card-meta");
  metadata.append(crearElemento("span", "", `${alojamiento.ciudad}, ${alojamiento.pais}`));
  if (alojamiento.calificacionPromedio !== null && alojamiento.calificacionPromedio !== undefined) {
    metadata.append(crearElemento("span", "", `★ ${alojamiento.calificacionPromedio}`));
  }
  body.append(metadata, crearElemento("p", "stay-card-type", String(alojamiento.tipo || "").replaceAll("_", " ")));
  card.append(imageWrap, body);
  link.append(card);
  return link;
}

function validarEstancia({ entrada, salida, adultos, ninos }) {
  if (entrada && salida && salida <= entrada) return "La salida debe ser posterior a la entrada.";
  if (Number(adultos) < 1 || Number(adultos) + Number(ninos) > 16) return "Indica al menos un adulto y no mas de 16 huespedes.";
  return "";
}

function parametrosEstancia(form) {
  return {
    ciudad: form.elements.ciudad?.value.trim() || "",
    entrada: form.elements.entrada?.value || "",
    salida: form.elements.salida?.value || "",
    adultos: form.elements.adultos?.value || "2",
    ninos: form.elements.ninos?.value || "0"
  };
}

function construirQuery(values, pagina = 0) {
  const params = new URLSearchParams();
  for (const [key, value] of Object.entries(values)) if (value !== "") params.set(key, value);
  params.set("pagina", String(pagina));
  return params.toString();
}