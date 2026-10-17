const registerForm = document.querySelector("#register-form");
const registerStatus = document.querySelector("#auth-status");

registerForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  const submit = registerForm.querySelector("button[type='submit']");
  submit.disabled = true;
  mostrarEstado(registerStatus, "Creando cuenta...");
  try {
    const usuario = await llamarApi("/auth/registro", {
      method: "POST",
      body: {
        nombre: registerForm.elements.nombre.value.trim(),
        apellido: registerForm.elements.apellido.value.trim(),
        email: registerForm.elements.email.value.trim(),
        telefono: registerForm.elements.telefono.value.trim() || null,
        password: registerForm.elements.password.value,
        rol: registerForm.elements.rol.value
      }
    });
    const params = new URLSearchParams({ registered: "1", email: usuario.email });
    window.location.href = `login.html?${params}`;
  } catch (error) {
    mostrarEstado(registerStatus, error.message, true);
  } finally {
    submit.disabled = false;
  }
});