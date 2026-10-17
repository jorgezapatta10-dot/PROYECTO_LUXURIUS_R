const params = new URLSearchParams(window.location.search);
const searchForm = document.querySelector("#results-search");
const resultsList = document.querySelector("#results-list");
const resultCount = document.querySelector("#result-count");
const pageCount = document.querySelector("#page-count");
const emptyState = document.querySelector("#empty-state");
let currentPage = Math.max(0, Number(params.get("pagina") || 0));

for (const key of ["ciudad", "entrada", "salida", "adultos", "ninos", "tipo", "precioMinimo", "precioMaximo", "calificacionMinima", "capacidad"]) {
  const value = params.get(key);
  if (value && searchForm.elements[key]) searchForm.elements[key].value = value;
}
for (const key of ["wifi", "estacionamiento", "desayuno", "aireAcondicionado"]) {
  searchForm.elements[key].checked = params.get(key) === "true";
}
document.querySelector("#results-title").textContent = params.get("ciudad") ? `Estancias en ${params.get("ciudad")}` : "Alojamientos";

async function cargarResultados() {
  const city = searchForm.elements.ciudad.value.trim();
  const query = new URLSearchParams({ pagina: String(currentPage), tamano: "9" });
  const values = leerFiltros();
  for (const [key, value] of Object.entries(values)) if (value !== "") query.set(key, value);
  mostrarEstado(resultCount, "Buscando...");
  try {
    const page = datosPagina(await llamarApi(`/alojamientos?${query}`));
    resultsList.replaceChildren(...page.content.map(crearTarjetaAlojamiento));
    emptyState.classList.toggle("d-none", page.content.length !== 0);
    mostrarEstado(resultCount, `${page.totalElements} ${page.totalElements === 1 ? "alojamiento encontrado" : "alojamientos encontrados"}`);
    pageCount.textContent = page.totalPages > 0 ? `Pagina ${page.number + 1} de ${page.totalPages}` : "";
    document.querySelector("#previous-page").disabled = currentPage <= 0;
    document.querySelector("#next-page").disabled = currentPage + 1 >= page.totalPages;
  } catch (error) {
    resultsList.replaceChildren();
    emptyState.classList.add("d-none");
    mostrarEstado(resultCount, `No fue posible cargar resultados: ${error.message}`, true);
  }
}

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const values = parametrosEstancia(searchForm);
  const filters = leerFiltros();
  const error = validarEstancia(values) || validarRangoFiltros(filters);
  document.querySelector("#search-error").textContent = error;
  if (error) return;
  currentPage = 0;
  const url = new URL(window.location.href);
  url.search = new URLSearchParams({ ...values, ...filters, pagina: String(currentPage) }).toString();
  window.history.replaceState({}, "", url);
  document.querySelector("#results-title").textContent = values.ciudad ? `Estancias en ${values.ciudad}` : "Alojamientos";
  cargarResultados();
});

function leerFiltros() {
  const filters = {
    tipo: searchForm.elements.tipo.value,
    precioMinimo: searchForm.elements.precioMinimo.value,
    precioMaximo: searchForm.elements.precioMaximo.value,
    calificacionMinima: searchForm.elements.calificacionMinima.value,
    capacidad: searchForm.elements.capacidad.value
  };
  for (const key of ["wifi", "estacionamiento", "desayuno", "aireAcondicionado"]) {
    if (searchForm.elements[key].checked) filters[key] = "true";
  }
  return filters;
}

function validarRangoFiltros(filters) {
  if (filters.precioMinimo && filters.precioMaximo && Number(filters.precioMinimo) > Number(filters.precioMaximo)) {
    return "El precio minimo no puede superar el maximo.";
  }
  return "";
}

document.querySelector("#previous-page").addEventListener("click", () => { currentPage -= 1; cargarResultados(); });
document.querySelector("#next-page").addEventListener("click", () => { currentPage += 1; cargarResultados(); });
cargarResultados();