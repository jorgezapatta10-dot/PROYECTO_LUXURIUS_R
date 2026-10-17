const profileStatus = document.querySelector("#profile-status");
const profileForm = document.querySelector("#profile-form");
const passwordForm = document.querySelector("#password-form");
const passwordStatus = document.querySelector("#password-status");

async function cargarPerfil() {
  try {
    const usuario = await llamarApi("/usuario/perfil");
    profileForm.elements.nombre.value = usuario.nombre || "";
    profileForm.elements.apellido.value = usuario.apellido || "";
    profileForm.elements.email.value = usuario.email || "";
    profileForm.elements.telefono.value = usuario.telefono || "";
    document.querySelector("#rol").textContent = String(usuario.rol || "").replaceAll("_", " ");
    mostrarEstado(profileStatus, "Perfil cargado.");
  } catch (error) {
    mostrarEstado(profileStatus, error.message, true);
  }
}

profileForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    const usuario = await llamarApi("/usuario/perfil", {
      method: "PUT",
      body: {
        nombre: profileForm.elements.nombre.value.trim(),
        apellido: profileForm.elements.apellido.value.trim(),
        telefono: profileForm.elements.telefono.value.trim() || null
      }
    });
    mostrarEstado(profileStatus, "Cambios guardados.");
    const cached = leerUsuarioActual();
    if (cached) {
      cached.nombre = usuario.nombre;
      localStorage.setItem("usuario", JSON.stringify(cached));
      actualizarNavegacion();
    }
  } catch (error) {
    mostrarEstado(profileStatus, error.message, true);
  }
});

passwordForm.addEventListener("submit", async (event) => {
  event.preventDefault();
  try {
    await llamarApi("/usuario/perfil/password", {
      method: "PUT",
      body: {
        passwordActual: passwordForm.elements.passwordActual.value,
        passwordNueva: passwordForm.elements.passwordNueva.value
      }
    });
    passwordForm.reset();
    mostrarEstado(passwordStatus, "Contrasena actualizada.");
  } catch (error) {
    mostrarEstado(passwordStatus, error.message, true);
  }
});

cargarPerfil();