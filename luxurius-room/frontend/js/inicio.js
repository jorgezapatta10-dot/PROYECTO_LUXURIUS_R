const homeList = document.querySelector("#home-list");
const homeStatus = document.querySelector("#home-status");

async function cargarDestacados() {
  try {
    const page = datosPagina(await llamarApi("/alojamientos?pagina=0&tamano=6"));
    homeList.replaceChildren(...page.content.map(crearTarjetaAlojamiento));
    mostrarEstado(homeStatus, page.content.length ? `${page.totalElements} alojamientos publicados` : "Aun no hay alojamientos publicados.");
  } catch (error) {
    mostrarEstado(homeStatus, `No fue posible conectar con el servicio: ${error.message}`, true);
  }
}

document.querySelector("#search-form").addEventListener("submit", (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const values = parametrosEstancia(form);
  const error = validarEstancia(values);
  document.querySelector("#search-error").textContent = error;
  if (error) return;
  window.location.href = `alojamientos.html?${construirQuery(values)}`;
});

cargarDestacados();