const reservationsList = document.querySelector("#reservations-list");
const reservationsStatus = document.querySelector("#reservations-status");
const reservationsEmpty = document.querySelector("#reservations-empty");
const reviewModal = new bootstrap.Modal(document.querySelector("#review-modal"));
const reviewForm = document.querySelector("#review-form");
let reservaParaResena = null;

function textoEstado(value) {
  return String(value || "").replaceAll("_", " ").toLowerCase();
}

function tarjetaReserva(reserva) {
  const card = crearElemento("article", "reservation-card");
  const header = crearElemento("div", "reservation-card-header");
  const heading = document.createElement("div");
  heading.append(crearElemento("p", "eyebrow", reserva.codigo));
  heading.append(crearElemento("h2", "", reserva.alojamiento));
  heading.append(crearElemento("p", "muted-copy", `${reserva.ciudad} · ${reserva.habitacion}`));
  const status = crearElemento("span", `reservation-status status-${String(reserva.estadoReserva || "").toLowerCase()}`, textoEstado(reserva.estadoReserva));
  header.append(heading, status);

  const details = crearElemento("dl", "reservation-details");
  const fields = [
    ["Entrada", reserva.fechaEntrada], ["Salida", reserva.fechaSalida],
    ["Huespedes", `${reserva.adultos} adultos · ${reserva.ninos} ninos · ${reserva.unidades} unidades`],
    ["Noches", reserva.noches], ["Subtotal", formatoPrecio(reserva.subtotal)],
    ["Cargos adicionales", formatoPrecio(reserva.cargosAdicionales)], ["Total", formatoPrecio(reserva.total)],
    ["Pago", textoEstado(reserva.estadoPago)]
  ];
  for (const [label, value] of fields) {
    details.append(crearElemento("dt", "", label));
    details.append(crearElemento("dd", "", String(value ?? "")));
  }
  if (reserva.cancelacionTardia) details.append(crearElemento("p", "late-cancellation", "Cancelacion fuera del plazo gratuito"));

  const actions = crearElemento("div", "reservation-actions");
  if (["PENDIENTE_CONFIRMACION", "CONFIRMADA"].includes(reserva.estadoReserva)) {
    const cancel = crearElemento("button", "btn btn-outline-primary", "Cancelar reserva");
    cancel.type = "button";
    cancel.addEventListener("click", () => cancelarReserva(reserva.codigo, cancel));
    actions.append(cancel);
  }
  if (reserva.estadoReserva === "COMPLETADA" && !reserva.resenaCreada) {
    const review = crearElemento("button", "btn btn-primary", "Calificar");
    review.type = "button";
    review.addEventListener("click", () => abrirFormularioResena(reserva));
    actions.append(review);
  }
  card.append(header, details, actions);
  return card;
}

function abrirFormularioResena(reserva) {
  reservaParaResena = reserva;
  reviewForm.reset();
  reviewForm.elements.codigoReserva.value = reserva.codigo;
  document.querySelector("#review-property").textContent = `${reserva.alojamiento} · ${reserva.habitacion}`;
  mostrarEstado(document.querySelector("#review-status"), "");
  reviewModal.show();
}

reviewForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  if (!reservaParaResena) return;
  const form = event.currentTarget;
  const button = form.querySelector("button[type='submit']");
  button.disabled = true;
  try {
    await llamarApi("/resenas", { method: "POST", body: {
      codigoReserva: reservaParaResena.codigo,
      calificacion: Number(form.elements.calificacion.value),
      comentario: form.elements.comentario.value.trim()
    } });
    reviewModal.hide();
    await cargarReservas();
    mostrarEstado(reservationsStatus, "Gracias por calificar tu estancia.");
  } catch (error) {
    mostrarEstado(document.querySelector("#review-status"), error.message, true);
  } finally {
    button.disabled = false;
  }
});

async function cargarReservas() {
  try {
    const reservas = await llamarApi("/usuario/reservas");
    reservationsList.replaceChildren(...reservas.map(tarjetaReserva));
    reservationsEmpty.classList.toggle("d-none", reservas.length !== 0);
    mostrarEstado(reservationsStatus, reservas.length ? `${reservas.length} reservas` : "");
  } catch (error) {
    mostrarEstado(reservationsStatus, error.message, true);
  }
}

async function cancelarReserva(codigo, button) {
  if (!window.confirm(`Vas a solicitar la cancelacion de ${codigo}. Continuar?`)) return;
  button.disabled = true;
  try {
    await llamarApi(`/reservas/${encodeURIComponent(codigo)}/cancelar`, { method: "PATCH" });
    await cargarReservas();
    mostrarEstado(reservationsStatus, `La reserva ${codigo} fue cancelada.`);
  } catch (error) {
    mostrarEstado(reservationsStatus, error.message, true);
    button.disabled = false;
  }
}

cargarReservas();