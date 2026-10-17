const panelRoot = document.querySelector("[data-panel]");
const panelStatus = document.querySelector("#panel-status");
const rolEsperado = panelRoot?.dataset.panel === "anfitrion" ? "ANFITRION" : "ADMINISTRADOR";
let alojamientosAnfitrion = [];
let reservasAnfitrion = [];
let resenasAnfitrion = [];

function comprobarAccesoPanel() {
  const usuario = leerUsuarioActual();
  if (!localStorage.getItem("token") || !usuario) {
    const next = `${location.pathname.split("/").pop()}${location.search}`;
    location.href = `login.html?next=${encodeURIComponent(next)}`;
    return false;
  }
  if (usuario.rol !== rolEsperado) {
    location.href = "index.html";
    return false;
  }
  return true;
}

function crearBoton(texto, clase, accion) {
  const button = crearElemento("button", clase, texto);
  button.type = "button";
  button.addEventListener("click", accion);
  return button;
}

function tarjetaGestion(titulo, detalle, estado) {
  const card = crearElemento("article", "management-item");
  card.append(crearElemento("h3", "", titulo));
  if (estado) card.append(crearElemento("span", "management-status", String(estado).replaceAll("_", " ")));
  if (detalle) card.append(crearElemento("p", "", detalle));
  return card;
}

function crearAcciones(card, acciones) {
  const wrapper = crearElemento("div", "management-item-actions");
  for (const [texto, callback] of acciones) wrapper.append(crearBoton(texto, "btn btn-sm btn-outline-primary", callback));
  card.append(wrapper);
}

function errorPanel(error) {
  mostrarEstado(panelStatus, error.message, true);
}

function alojamientoSeleccionado() {
  const id = document.querySelector("#host-property-select")?.value;
  return alojamientosAnfitrion.find((alojamiento) => String(alojamiento.id) === id) || null;
}

function actualizarSelectorPropiedades() {
  const select = document.querySelector("#host-property-select");
  if (!select) return;
  const selected = select.value;
  select.replaceChildren(new Option("Selecciona un alojamiento", ""));
  for (const alojamiento of alojamientosAnfitrion) {
    select.add(new Option(`${alojamiento.nombre} · ${alojamiento.ciudad}`, String(alojamiento.id)));
  }
  if (alojamientosAnfitrion.some((alojamiento) => String(alojamiento.id) === selected)) select.value = selected;
  actualizarContextoAnfitrion();
}

function rellenarFormularioAlojamiento(alojamiento) {
  const form = document.querySelector("#host-property-form");
  form.elements.id.value = alojamiento?.id || "";
  for (const field of ["nombre", "tipo", "descripcion", "ciudad", "pais", "direccion", "reglas", "politicaCancelacion"]) {
    if (alojamiento && form.elements[field]) form.elements[field].value = alojamiento[field] ?? "";
    else if (form.elements[field]) form.elements[field].value = "";
  }
  form.querySelector("button[type='submit']").textContent = alojamiento ? "Guardar cambios" : "Crear alojamiento";
}

function renderAlojamientosAnfitrion() {
  const container = document.querySelector("#host-properties-list");
  container.replaceChildren();
  if (!alojamientosAnfitrion.length) {
    container.append(crearElemento("p", "muted-copy", "Todavia no has registrado alojamientos."));
    return;
  }
  for (const alojamiento of alojamientosAnfitrion) {
    const card = tarjetaGestion(alojamiento.nombre, `${alojamiento.ciudad}, ${alojamiento.pais} · ${alojamiento.habitaciones.length} habitaciones`, alojamiento.estado);
    crearAcciones(card, [["Editar", () => {
      document.querySelector("#host-property-select").value = String(alojamiento.id);
      actualizarContextoAnfitrion();
      document.querySelector("#host-property-form").scrollIntoView({ behavior: "smooth", block: "start" });
    }]]);
    container.append(card);
  }
}

function actualizarContextoAnfitrion() {
  const alojamiento = alojamientoSeleccionado();
  rellenarFormularioAlojamiento(alojamiento);
  const servicesForm = document.querySelector("#host-services-form");
  if (servicesForm) {
    const codigos = new Set(alojamiento?.servicios || []);
    servicesForm.querySelectorAll("input[type='checkbox']").forEach((input) => { input.checked = codigos.has(input.value); });
  }
  const images = document.querySelector("#host-image-list");
  if (images) {
    images.replaceChildren();
    for (const url of alojamiento?.imagenes || []) {
      const link = crearElemento("a", "", url);
      link.href = url;
      link.target = "_blank";
      link.rel = "noopener noreferrer";
      images.append(link);
    }
  }
  actualizarHabitacionesAnfitrion(alojamiento);
}

function actualizarHabitacionesAnfitrion(alojamiento) {
  const rooms = alojamiento?.habitaciones || [];
  const roomSelect = document.querySelector("#host-room-select");
  roomSelect.replaceChildren(new Option("Nueva habitacion", ""));
  for (const room of rooms) roomSelect.add(new Option(room.nombre, String(room.id)));
  document.querySelector("#rate-room").replaceChildren(...rooms.map((room) => new Option(room.nombre, String(room.id))));
  document.querySelector("#block-room").replaceChildren(...rooms.map((room) => new Option(room.nombre, String(room.id))));
  renderHabitacionesAnfitrion(rooms);
  renderTarifasBloqueos(rooms);
}

function renderHabitacionesAnfitrion(rooms) {
  const container = document.querySelector("#host-room-list");
  container.replaceChildren();
  if (!rooms.length) container.append(crearElemento("p", "muted-copy", "No hay habitaciones registradas."));
  for (const room of rooms) {
    container.append(tarjetaGestion(room.nombre,
      `${room.capacidadMaxima} huespedes · ${room.unidadesTotales} unidades · ${formatoPrecio(room.precioBaseNoche)} por noche`,
      room.activa ? "ACTIVA" : "INACTIVA"));
  }
}

function renderTarifasBloqueos(rooms) {
  const container = document.querySelector("#host-rates-blocks");
  container.replaceChildren();
  const elements = [];
  for (const room of rooms) {
    for (const rate of room.tarifas || []) {
      elements.push(tarjetaGestion(`${room.nombre} · ${formatoPrecio(rate.precioNoche)}/noche`,
        `${rate.fechaInicio} a ${rate.fechaFin}${rate.descripcion ? ` · ${rate.descripcion}` : ""}`, "TARIFA"));
    }
    for (const block of room.bloqueos || []) {
      const card = tarjetaGestion(`${room.nombre} · ${block.unidadesBloqueadas} unidad(es)`,
        `${block.fechaInicio} a ${block.fechaFin}${block.motivo ? ` · ${block.motivo}` : ""}`, "BLOQUEO");
      crearAcciones(card, [["Desbloquear", async () => {
        try {
          await llamarApi(`/anfitrion/bloqueos/${block.id}`, { method: "DELETE" });
          await cargarPanelAnfitrion();
          mostrarEstado(panelStatus, "Bloqueo eliminado.");
        } catch (error) { errorPanel(error); }
      }]]);
      elements.push(card);
    }
  }
  if (!elements.length) container.append(crearElemento("p", "muted-copy", "No hay tarifas ni bloqueos para este alojamiento."));
  else container.replaceChildren(...elements);
}

function renderReservasAnfitrion() {
  const container = document.querySelector("#host-reservations");
  container.replaceChildren();
  if (!reservasAnfitrion.length) {
    container.append(crearElemento("p", "muted-copy", "No has recibido reservas."));
    return;
  }
  for (const reserva of reservasAnfitrion) {
    const card = tarjetaGestion(`${reserva.codigo} · ${reserva.alojamiento}`,
      `${reserva.ciudad} · ${reserva.habitacion} · ${reserva.fechaEntrada} a ${reserva.fechaSalida} · ${reserva.adultos + reserva.ninos} huespedes · ${formatoPrecio(reserva.total)} · Pago: ${String(reserva.estadoPago).replaceAll("_", " ")}`,
      reserva.estadoReserva);
    const actions = [];
    if (reserva.estadoReserva === "PENDIENTE_CONFIRMACION") {
      actions.push(["Confirmar", () => cambiarEstadoReserva(reserva.codigo, "confirmar")]);
      actions.push(["Cancelar", () => cambiarEstadoReserva(reserva.codigo, "cancelar")]);
    }
    if (reserva.estadoReserva === "CONFIRMADA") {
      actions.push(["Cancelar", () => cambiarEstadoReserva(reserva.codigo, "cancelar")]);
      actions.push(["Completar", () => cambiarEstadoReserva(reserva.codigo, "completar")]);
      actions.push(["No presentado", () => cambiarEstadoReserva(reserva.codigo, "no-presentado")]);
    }
    if (["CONFIRMADA", "COMPLETADA"].includes(reserva.estadoReserva) && reserva.estadoPago === "PENDIENTE_EN_SITIO") {
      actions.push(["Registrar pago en sitio", () => cambiarEstadoReserva(reserva.codigo, "pago")]);
    }
    if (reserva.cancelacionTardia) card.append(crearElemento("p", "late-cancellation", "Cancelacion fuera del plazo gratuito."));
    if (actions.length) crearAcciones(card, actions);
    container.append(card);
  }
}

async function cambiarEstadoReserva(codigo, accion) {
  try {
    await llamarApi(`/anfitrion/reservas/${encodeURIComponent(codigo)}/${accion}`, { method: "PATCH" });
    await cargarPanelAnfitrion();
    mostrarEstado(panelStatus, "Reserva actualizada.");
  } catch (error) { errorPanel(error); }
}

function renderResenasAnfitrion() {
  const container = document.querySelector("#host-reviews-list");
  container.replaceChildren();
  if (!resenasAnfitrion.length) {
    container.append(crearElemento("p", "muted-copy", "Todavia no hay reseñas para tus alojamientos."));
    return;
  }
  for (const resena of resenasAnfitrion) {
    const card = tarjetaGestion(`${resena.alojamiento} · ${"★".repeat(resena.calificacion)}`, resena.comentario || "Sin comentario", resena.reportada ? "REPORTADA" : "VISIBLE");
    card.append(crearElemento("p", "", `${resena.usuario} · ${resena.codigoReserva}`));
    if (!resena.reportada) crearAcciones(card, [["Reportar", async () => {
      const motivo = window.prompt("Motivo del reporte (maximo 255 caracteres):");
      if (!motivo?.trim()) return;
      try {
        await llamarApi(`/anfitrion/resenas/${resena.id}/reportar`, { method: "POST", body: { motivo: motivo.trim() } });
        await cargarPanelAnfitrion();
        mostrarEstado(panelStatus, "Reseña reportada para revisión.");
      } catch (error) { errorPanel(error); }
    }]]);
    container.append(card);
  }
}

async function cargarPanelAnfitrion() {
  try {
    const [properties, reservations, reviews] = await Promise.all([
      llamarApi("/anfitrion/alojamientos"),
      llamarApi("/anfitrion/reservas"),
      llamarApi("/anfitrion/resenas")
    ]);
    alojamientosAnfitrion = properties;
    reservasAnfitrion = reservations;
    resenasAnfitrion = reviews;
    actualizarSelectorPropiedades();
    renderAlojamientosAnfitrion();
    renderReservasAnfitrion();
    renderResenasAnfitrion();
    mostrarEstado(panelStatus, `${properties.length} alojamientos · ${reservations.length} reservas`);
  } catch (error) { errorPanel(error); }
}

function leerFormularioPropiedad(form) {
  return {
    nombre: form.elements.nombre.value.trim(), tipo: form.elements.tipo.value,
    descripcion: form.elements.descripcion.value.trim(), ciudad: form.elements.ciudad.value.trim(),
    pais: form.elements.pais.value.trim(), direccion: form.elements.direccion.value.trim(),
    reglas: form.elements.reglas.value.trim(), politicaCancelacion: form.elements.politicaCancelacion.value
  };
}

function vincularFormularioAnfitrion() {
  const propertySelect = document.querySelector("#host-property-select");
  propertySelect.addEventListener("change", actualizarContextoAnfitrion);
  document.querySelector("#host-new-property").addEventListener("click", () => {
    propertySelect.value = "";
    actualizarContextoAnfitrion();
    document.querySelector("#host-property-form").scrollIntoView({ behavior: "smooth", block: "start" });
  });
  document.querySelector("#host-property-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    const id = form.elements.id.value;
    try {
      await llamarApi(id ? `/anfitrion/alojamientos/${id}` : "/anfitrion/alojamientos", {
        method: id ? "PUT" : "POST", body: leerFormularioPropiedad(form)
      });
      form.reset();
      propertySelect.value = "";
      await cargarPanelAnfitrion();
      mostrarEstado(panelStatus, id ? "Alojamiento actualizado." : "Alojamiento enviado para aprobación.");
    } catch (error) { errorPanel(error); }
  });

  const roomSelect = document.querySelector("#host-room-select");
  roomSelect.addEventListener("change", () => {
    const room = alojamientoSeleccionado()?.habitaciones.find((item) => String(item.id) === roomSelect.value);
    const form = document.querySelector("#host-room-form");
    for (const field of ["nombre", "descripcion", "capacidadMaxima", "camas", "unidadesTotales", "precioBaseNoche", "cargoLimpieza"]) {
      form.elements[field].value = room?.[field] ?? (field === "cargoLimpieza" ? "0" : "");
    }
    form.elements.activa.checked = room ? Boolean(room.activa) : true;
    form.querySelector("button[type='submit']").textContent = room ? "Guardar habitacion" : "Crear habitacion";
  });
  document.querySelector("#host-room-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const property = alojamientoSeleccionado();
    if (!property) return mostrarEstado(panelStatus, "Selecciona primero un alojamiento.", true);
    const form = event.currentTarget;
    const roomId = roomSelect.value;
    const request = {
      nombre: form.elements.nombre.value.trim(), descripcion: form.elements.descripcion.value.trim(),
      capacidadMaxima: Number(form.elements.capacidadMaxima.value), camas: form.elements.camas.value.trim(),
      unidadesTotales: Number(form.elements.unidadesTotales.value), precioBaseNoche: Number(form.elements.precioBaseNoche.value),
      cargoLimpieza: Number(form.elements.cargoLimpieza.value || 0), activa: form.elements.activa.checked
    };
    try {
      await llamarApi(roomId
        ? `/anfitrion/alojamientos/${property.id}/habitaciones/${roomId}`
        : `/anfitrion/alojamientos/${property.id}/habitaciones`, { method: roomId ? "PUT" : "POST", body: request });
      form.reset();
      await cargarPanelAnfitrion();
      mostrarEstado(panelStatus, roomId ? "Habitacion actualizada." : "Habitacion creada.");
    } catch (error) { errorPanel(error); }
  });

  document.querySelector("#host-services-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const property = alojamientoSeleccionado();
    if (!property) return mostrarEstado(panelStatus, "Selecciona primero un alojamiento.", true);
    const codigos = [...event.currentTarget.querySelectorAll("input:checked")].map((input) => input.value);
    try {
      await llamarApi(`/anfitrion/alojamientos/${property.id}/servicios`, { method: "PUT", body: codigos });
      await cargarPanelAnfitrion();
      mostrarEstado(panelStatus, "Servicios actualizados.");
    } catch (error) { errorPanel(error); }
  });

  document.querySelector("#host-image-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const property = alojamientoSeleccionado();
    if (!property) return mostrarEstado(panelStatus, "Selecciona primero un alojamiento.", true);
    try {
      await llamarApi(`/anfitrion/alojamientos/${property.id}/imagenes`, {
        method: "POST", body: { url: event.currentTarget.elements.url.value.trim() }
      });
      event.currentTarget.reset();
      await cargarPanelAnfitrion();
      mostrarEstado(panelStatus, "Foto agregada.");
    } catch (error) { errorPanel(error); }
  });

  document.querySelector("#host-rate-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    if (form.elements.fechaFin.value < form.elements.fechaInicio.value) return mostrarEstado(panelStatus, "La fecha final no puede ser anterior a la inicial.", true);
    const id = form.elements.habitacionId.value;
    try {
      await llamarApi(`/anfitrion/habitaciones/${id}/tarifas`, { method: "POST", body: {
        fechaInicio: form.elements.fechaInicio.value, fechaFin: form.elements.fechaFin.value,
        precioNoche: Number(form.elements.precioNoche.value), descripcion: form.elements.descripcion.value.trim()
      } });
      form.reset();
      await cargarPanelAnfitrion();
      mostrarEstado(panelStatus, "Tarifa guardada.");
    } catch (error) { errorPanel(error); }
  });

  document.querySelector("#host-block-form").addEventListener("submit", async (event) => {
    event.preventDefault();
    const form = event.currentTarget;
    if (form.elements.fechaFin.value < form.elements.fechaInicio.value) return mostrarEstado(panelStatus, "La fecha final no puede ser anterior a la inicial.", true);
    const id = form.elements.habitacionId.value;
    try {
      await llamarApi(`/anfitrion/habitaciones/${id}/bloqueos`, { method: "POST", body: {
        fechaInicio: form.elements.fechaInicio.value, fechaFin: form.elements.fechaFin.value,
        unidadesBloqueadas: Number(form.elements.unidadesBloqueadas.value), motivo: form.elements.motivo.value.trim()
      } });
      form.reset();
      await cargarPanelAnfitrion();
      mostrarEstado(panelStatus, "Fechas bloqueadas.");
    } catch (error) { errorPanel(error); }
  });
}

async function cargarPanelAdministrador() {
  try {
    const [stats, users, properties, reservations, reviews] = await Promise.all([
      llamarApi("/admin/estadisticas"), llamarApi("/admin/usuarios"), llamarApi("/admin/alojamientos"),
      llamarApi("/admin/reservas"), llamarApi("/admin/resenas/reportadas")
    ]);
    renderEstadisticas(stats);
    renderUsuarios(users);
    renderAlojamientosAdmin(properties);
    renderReservasAdmin(reservations);
    renderResenasAdmin(reviews);
    mostrarEstado(panelStatus, `${users.length} usuarios · ${properties.length} alojamientos · ${reservations.length} reservas`);
  } catch (error) { errorPanel(error); }
}

function renderEstadisticas(stats) {
  const container = document.querySelector("#admin-stats-grid");
  const metrics = [
    ["Usuarios", stats.usuariosTotales], ["Pendientes", stats.alojamientosPorEstado.PENDIENTE_APROBACION],
    ["Alojamientos activos", stats.alojamientosPorEstado.ACTIVO], ["Reservas del mes", stats.reservasDelMes],
    ...Object.entries(stats.reservasPorEstado).map(([state, count]) => [state.replaceAll("_", " "), count])
  ];
  container.replaceChildren(...metrics.map(([label, value]) => {
    const card = crearElemento("div", "admin-metric");
    card.append(crearElemento("span", "", label), crearElemento("strong", "", String(value)));
    return card;
  }));
}

function renderUsuarios(users) {
  const container = document.querySelector("#admin-users-list");
  container.replaceChildren();
  for (const user of users) {
    const card = tarjetaGestion(`${user.nombre} ${user.apellido}`, `${user.email} · ${user.rol} · ${user.telefono || "Sin telefono"}`, user.activo ? "ACTIVO" : "INACTIVO");
    const current = leerUsuarioActual();
    const actions = [];
    if (current?.id !== user.id) actions.push([user.activo ? "Desactivar" : "Activar", async () => {
      try {
        await llamarApi(`/admin/usuarios/${user.id}/activo?activo=${!user.activo}`, { method: "PATCH" });
        await cargarPanelAdministrador();
        mostrarEstado(panelStatus, "Estado del usuario actualizado.");
      } catch (error) { errorPanel(error); }
    }]);
    if (actions.length) crearAcciones(card, actions);
    container.append(card);
  }
  if (!users.length) container.append(crearElemento("p", "muted-copy", "No hay usuarios registrados."));
}

function renderAlojamientosAdmin(properties) {
  const container = document.querySelector("#admin-properties-list");
  container.replaceChildren();
  for (const property of properties) {
    const card = tarjetaGestion(property.nombre, `${property.ciudad}, ${property.pais} · ${property.tipo} · ${property.emailAnfitrion}`, property.estado);
    const actions = [];
    if (property.estado !== "ACTIVO") actions.push(["Aprobar / activar", async () => {
      try {
        await llamarApi(`/admin/alojamientos/${property.id}/aprobar`, { method: "PATCH" });
        await cargarPanelAdministrador();
      } catch (error) { errorPanel(error); }
    }]);
    if (property.estado !== "SUSPENDIDO") actions.push(["Suspender", async () => {
      try {
        await llamarApi(`/admin/alojamientos/${property.id}/suspender`, { method: "PATCH" });
        await cargarPanelAdministrador();
      } catch (error) { errorPanel(error); }
    }]);
    if (actions.length) crearAcciones(card, actions);
    container.append(card);
  }
  if (!properties.length) container.append(crearElemento("p", "muted-copy", "No hay alojamientos registrados."));
}

function renderReservasAdmin(reservations) {
  const container = document.querySelector("#admin-reservations-list");
  container.replaceChildren();
  for (const item of reservations) {
    const reserva = item.reserva;
    container.append(tarjetaGestion(`${reserva.codigo} · ${reserva.alojamiento}`,
      `${item.nombreHuesped} (${item.emailHuesped}) · ${reserva.fechaEntrada} a ${reserva.fechaSalida} · ${formatoPrecio(reserva.total)} · Pago: ${reserva.estadoPago}`,
      reserva.estadoReserva));
  }
  if (!reservations.length) container.append(crearElemento("p", "muted-copy", "No hay reservas registradas."));
}

function renderResenasAdmin(reviews) {
  const container = document.querySelector("#admin-reviews-list");
  container.replaceChildren();
  for (const review of reviews) {
    const card = tarjetaGestion(`${review.alojamiento} · ${"★".repeat(review.calificacion)}`,
      `${review.usuario} · ${review.comentario || "Sin comentario"} · Motivo: ${review.motivoReporte || "No indicado"}`, review.estado);
    const action = review.estado === "OCULTA" ? "restaurar" : "ocultar";
    crearAcciones(card, [[action === "ocultar" ? "Ocultar" : "Restaurar", async () => {
      try {
        await llamarApi(`/admin/resenas/${review.id}/${action}`, { method: "PATCH" });
        await cargarPanelAdministrador();
        mostrarEstado(panelStatus, `Reseña ${action === "ocultar" ? "ocultada" : "restaurada"}.`);
      } catch (error) { errorPanel(error); }
    }]]);
    container.append(card);
  }
  if (!reviews.length) container.append(crearElemento("p", "muted-copy", "No hay reseñas reportadas."));
}

if (panelRoot && comprobarAccesoPanel()) {
  if (rolEsperado === "ANFITRION") {
    vincularFormularioAnfitrion();
    cargarPanelAnfitrion();
  } else {
    cargarPanelAdministrador();
  }
}