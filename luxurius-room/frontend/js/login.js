const loginForm = document.querySelector("#login-form");
const loginStatus = document.querySelector("#auth-status");
const loginParams = new URLSearchParams(window.location.search);

if (loginParams.get("email")) loginForm.elements.email.value = loginParams.get("email");
if (loginParams.get("registered") === "1") mostrarEstado(loginStatus, "Cuenta creada. Ingresa con tus datos.");

loginForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const submit = loginForm.querySelector("button[type='submit']");
  submit.disabled = true;
  mostrarEstado(loginStatus, "Verificando acceso...");
  try {
    const auth = await llamarApi("/auth/login", {
      method: "POST",
      body: { email: loginForm.elements.email.value.trim(), password: loginForm.elements.password.value }
    });
    guardarSesion(auth);
    const next = loginParams.get("next");
    const validNextPages = ["index.html", "alojamientos.html", "detalle-alojamiento.html", "perfil.html", "mis-reservas.html"];
    const nextPage = next ? next.split(/[?#]/)[0] : "";
    window.location.href = validNextPages.includes(nextPage) ? next : "index.html";
  } catch (error) {
    mostrarEstado(loginStatus, error.message, true);
  } finally {
    submit.disabled = false;
  }
});