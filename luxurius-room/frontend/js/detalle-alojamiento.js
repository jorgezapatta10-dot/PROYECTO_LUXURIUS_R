const detailStatus = document.querySelector("#detail-status");
const detailContent = document.querySelector("#detail-content");
const accommodationId = new URLSearchParams(window.location.search).get("id");
let habitacionesDetalle = [];
let solicitudCotizada = null;

function agregarImagenGaleria(container, source, name, index) {
  const image = crearElemento("img", "gallery-image");
  image.src = source;
  image.alt = `${name}, imagen ${index + 1}`;
  image.loading = index > 0 ? "lazy" : "eager";
  container.append(image);
}

function renderHabitaciones(habitaciones) {
  habitacionesDetalle = habitaciones;
  const container = document.querySelector("#detail-rooms");
  const roomSelect = document.querySelector("#booking-room");
  roomSelect.replaceChildren(new Option("Selecciona una habitacion", ""));
  if (!habitaciones.length) {
    container.append(crearElemento("p", "muted-copy", "Este alojamiento aun no tiene habitaciones registradas."));
    return;
  }
  for (const habitacion of habitaciones) {
    const option = new Option(`${habitacion.nombre} · hasta ${habitacion.capacidadMaxima} · ${formatoPrecio(habitacion.precioBaseNoche)}/noche`, String(habitacion.id));
    option.dataset.units = String(habitacion.unidadesTotales);
    roomSelect.add(option);
    const row = crearElemento("article", "room-row");
    const description = document.createElement("div");
    description.append(crearElemento("h3", "", habitacion.nombre));
    description.append(crearElemento("p", "room-meta", `${habitacion.capacidadMaxima} huespedes · ${habitacion.camas || "Camas por confirmar"} · ${habitacion.unidadesTotales} unidades`));
    const price = crearElemento("span", "room-price", `${formatoPrecio(habitacion.precioBaseNoche)} / noche`);
    row.append(description, price);
    container.append(row);
  }
}

function renderResenas(resenas) {
  const container = document.querySelector("#detail-reviews");
  if (!resenas.length) {
    container.append(crearElemento("p", "muted-copy", "Aun no hay resenas para este alojamiento."));
    return;
  }
  for (const resena of resenas) {
    const review = crearElemento("article", "review-item");
    review.append(crearElemento("span", "review-meta", `${resena.usuario || "Huesped"} · ${"★".repeat(Number(resena.calificacion) || 0)}`));
    review.append(crearElemento("p", "", resena.comentario || ""));
    container.append(review);
  }
}

async function cargarDetalle() {
  if (!accommodationId) {
    mostrarEstado(detailStatus, "Falta el identificador del alojamiento.", true);
    return;
  }
  try {
    const detail = await llamarApi(`/alojamientos/${encodeURIComponent(accommodationId)}`);
    let habitaciones = Array.isArray(detail.habitaciones) ? detail.habitaciones : [];
    try {
      habitaciones = await llamarApi(`/alojamientos/${encodeURIComponent(accommodationId)}/habitaciones`);
    } catch (roomsError) {
      mostrarEstado(detailStatus, `Mostrando el detalle; no fue posible cargar la lista separada de habitaciones: ${roomsError.message}`);
    }
    document.title = `${detail.nombre} | Luxurius Room`;
    document.querySelector("#detail-name").textContent = detail.nombre || "Alojamiento";
    document.querySelector("#detail-location").textContent = `${detail.ciudad || ""}, ${detail.pais || ""}`;
    document.querySelector("#detail-address").textContent = detail.direccion || "";
    document.querySelector("#detail-type").textContent = `${String(detail.tipo || "").replaceAll("_", " ")} · Politica ${String(detail.politicaCancelacion || "").toLowerCase()}`;
    document.querySelector("#detail-description").textContent = detail.descripcion || "El anfitrion aun no agrego una descripcion.";
    document.querySelector("#detail-rules").textContent = detail.reglas ? `Reglas: ${detail.reglas}` : "";
    document.querySelector("#detail-rating").textContent = detail.calificacionPromedio ? `★ ${detail.calificacionPromedio}` : "Sin calificacion";

    const gallery = document.querySelector("#detail-gallery");
    const images = Array.isArray(detail.imagenes) ? detail.imagenes.slice(0, 5) : [];
    images.forEach((source, index) => agregarImagenGaleria(gallery, source, detail.nombre, index));
    gallery.classList.toggle("d-none", images.length === 0);

    const services = document.querySelector("#detail-services");
    for (const service of detail.servicios || []) services.append(crearElemento("li", "", service.nombre));
    if (!services.childElementCount) services.append(crearElemento("li", "", "Servicios sin registrar"));

    renderHabitaciones(habitaciones || detail.habitaciones || []);
    const params = new URLSearchParams(window.location.search);
    for (const key of ["entrada", "salida", "adultos", "ninos", "unidades"]) {
      if (params.has(key) && document.querySelector(`#booking-search [name='${key}']`)) {
        document.querySelector(`#booking-search [name='${key}']`).value = params.get(key);
      }
    }
    if (params.has("habitacionId")) document.querySelector("#booking-room").value = params.get("habitacionId");
    renderResenas(detail.resenas || []);
    detailContent.classList.remove("d-none");
    detailStatus.classList.add("d-none");
    cargarDisponibilidad();
    if (params.get("cotizar") === "1") await cotizarReserva();
  } catch (error) {
    mostrarEstado(detailStatus, `No fue posible cargar el alojamiento: ${error.message}`, true);
  }
}

async function cargarDisponibilidad() {
  const container = document.querySelector("#detail-availability");
  try {
    const disponibilidad = await llamarApi(`/alojamientos/${encodeURIComponent(accommodationId)}/disponibilidad`);
    const habitaciones = disponibilidad.habitaciones || [];
    if (habitaciones.length === 0) {
      container.replaceChildren(crearElemento("p", "muted-copy", "No hay reservas ni bloqueos registrados."));
      return;
    }
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const vistas = habitaciones.map((habitacion) => {
      const group = crearElemento("section", "availability-room");
      group.append(crearElemento("h3", "", `${habitacion.nombre} · ${habitacion.unidadesTotales} unidades`));
      const days = crearElemento("div", "availability-days");
      for (let offset = 0; offset < 30; offset += 1) {
        const date = new Date(hoy);
        date.setDate(hoy.getDate() + offset);
        const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
        const ocupadas = (habitacion.periodos || [])
          .filter((periodo) => periodo.fechaInicio <= key && periodo.fechaFin >= key)
          .reduce((total, periodo) => total + Number(periodo.unidades || 0), 0);
        const disponibles = Math.max(0, Number(habitacion.unidadesTotales || 0) - ocupadas);
        const cell = crearElemento("div", `availability-day ${disponibles === 0 ? "is-full" : "is-open"}`);
        cell.append(crearElemento("time", "", new Intl.DateTimeFormat("es-CO", { day: "2-digit", month: "short" }).format(date)));
        cell.append(crearElemento("span", "", disponibles === 0 ? "Completo" : `${disponibles} disp.`));
        cell.title = `${key}: ${disponibles} de ${habitacion.unidadesTotales} unidades disponibles`;
        days.append(cell);
      }
      group.append(days);
      return group;
    });
    container.replaceChildren(...vistas);
  } catch (error) {
    container.replaceChildren(crearElemento("p", "muted-copy", `No fue posible cargar el calendario: ${error.message}`));
  }
}

document.querySelector("#booking-search").addEventListener("submit", async (event) => {
  event.preventDefault();
  await cotizarReserva();
});

document.querySelector("#booking-room").addEventListener("change", (event) => {
  const selected = event.currentTarget.selectedOptions[0];
  const units = document.querySelector("#units");
  units.max = selected?.dataset.units || "16";
  if (Number(units.value) > Number(units.max)) units.value = units.max;
  ocultarCotizacion();
});
for (const selector of ["#checkin", "#checkout", "#adults", "#children", "#units"]) {
  document.querySelector(selector).addEventListener("change", ocultarCotizacion);
}
document.querySelector("#accept-conditions").addEventListener("change", (event) => {
  document.querySelector("#confirm-booking").disabled = !event.currentTarget.checked || !solicitudCotizada;
});
document.querySelector("#confirm-booking").addEventListener("click", confirmarReserva);

function leerSolicitud() {
  const form = document.querySelector("#booking-search");
  return {
    habitacionId: Number(form.elements.habitacionId.value),
    fechaEntrada: form.elements.entrada.value,
    fechaSalida: form.elements.salida.value,
    adultos: Number(form.elements.adultos.value),
    ninos: Number(form.elements.ninos.value),
    unidades: Number(form.elements.unidades.value),
    aceptoCondiciones: false
  };
}

function guardarSolicitudEnUrl(solicitud) {
  const url = new URL(window.location.href);
  url.searchParams.set("habitacionId", String(solicitud.habitacionId));
  url.searchParams.set("entrada", solicitud.fechaEntrada);
  url.searchParams.set("salida", solicitud.fechaSalida);
  url.searchParams.set("adultos", String(solicitud.adultos));
  url.searchParams.set("ninos", String(solicitud.ninos));
  url.searchParams.set("unidades", String(solicitud.unidades));
  url.searchParams.set("cotizar", "1");
  history.replaceState({}, "", url);
}

async function cotizarReserva() {
  const solicitud = leerSolicitud();
  const error = !solicitud.habitacionId
    ? "Selecciona una habitacion."
    : validarEstancia({ entrada: solicitud.fechaEntrada, salida: solicitud.fechaSalida, adultos: solicitud.adultos, ninos: solicitud.ninos });
  const errorContainer = document.querySelector("#booking-error");
  errorContainer.textContent = error || "";
  if (error) return;

  if (!localStorage.getItem("token")) {
    guardarSolicitudEnUrl(solicitud);
    window.location.href = `login.html?next=${encodeURIComponent(`${location.pathname.split("/").pop()}${location.search}`)}`;
    return;
  }

  const quoteButton = document.querySelector("#quote-button");
  quoteButton.disabled = true;
  quoteButton.textContent = "Calculando...";
  guardarSolicitudEnUrl(solicitud);
  try {
    const cotizacion = await llamarApi("/reservas/cotizacion", { method: "POST", body: solicitud });
    solicitudCotizada = solicitud;
    renderCotizacion(cotizacion);
  } catch (requestError) {
    mostrarEstado(errorContainer, requestError.message, true);
  } finally {
    quoteButton.disabled = false;
    quoteButton.textContent = "Recalcular total";
  }
}

function renderCotizacion(cotizacion) {
  const summary = document.querySelector("#quote-summary");
  const nights = document.querySelector("#quote-nights");
  const totals = document.querySelector("#quote-totals");
  nights.replaceChildren();
  totals.replaceChildren();
  for (const night of cotizacion.detalleNoches || []) {
    const line = crearElemento("p", "quote-night", `${night.fecha} · ${formatoPrecio(night.precioPorUnidad)} x ${solicitudCotizada.unidades} = ${formatoPrecio(night.importe)}`);
    nights.append(line);
  }
  for (const [label, amount] of [["Noches", cotizacion.noches], ["Subtotal", cotizacion.subtotal], ["Cargos adicionales", cotizacion.cargosAdicionales], ["Total", cotizacion.total]]) {
    const term = crearElemento("dt", "", label);
    const description = crearElemento("dd", label === "Total" ? "quote-total" : "", label === "Noches" ? String(amount) : formatoPrecio(amount));
    totals.append(term, description);
  }
  document.querySelector("#accept-conditions").checked = false;
  document.querySelector("#confirm-booking").disabled = true;
  summary.classList.remove("d-none");
  mostrarEstado(document.querySelector("#booking-result"), "La cotizacion refleja el precio vigente. El servidor validara nuevamente la disponibilidad al confirmar.");
}

async function confirmarReserva() {
  if (!solicitudCotizada || !document.querySelector("#accept-conditions").checked) return;
  const button = document.querySelector("#confirm-booking");
  button.disabled = true;
  button.textContent = "Enviando solicitud...";
  try {
    const reserva = await llamarApi("/reservas", {
      method: "POST",
      body: { ...solicitudCotizada, aceptoCondiciones: true }
    });
    const result = document.querySelector("#booking-result");
    result.replaceChildren();
    result.append(crearElemento("strong", "reservation-confirmed", `Solicitud ${reserva.codigo} creada · ${reserva.estadoReserva}`));
    result.append(crearElemento("p", "payment-note", "Tu reserva queda pendiente de confirmacion. El pago se realiza en el alojamiento."));
    const link = crearElemento("a", "btn btn-outline-primary w-100", "Ver mis reservas");
    link.href = "mis-reservas.html";
    result.append(link);
    document.querySelector("#quote-summary").classList.add("d-none");
  } catch (error) {
    mostrarEstado(document.querySelector("#booking-error"), error.message, true);
    button.disabled = false;
  } finally {
    button.textContent = "Confirmar solicitud";
  }
}

function ocultarCotizacion() {
  solicitudCotizada = null;
  document.querySelector("#quote-summary").classList.add("d-none");
  document.querySelector("#booking-result").replaceChildren();
  document.querySelector("#accept-conditions").checked = false;
  document.querySelector("#confirm-booking").disabled = true;
}

cargarDetalle();