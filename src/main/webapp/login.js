const API_AUTH =
    "webapi/auth";


const formLogin =
    document.getElementById(
        "formLogin"
    );


const mensajeLogin =
    document.getElementById(
        "mensajeLogin"
    );


document.addEventListener(
    "DOMContentLoaded",
    verificarSesion
);


formLogin.addEventListener(
    "submit",
    iniciarSesion
);


async function verificarSesion() {

    try {

        const response =
            await fetch(
                `${API_AUTH}/session`
            );


        if (!response.ok) {
            return;
        }


        const resultado =
            await response.json();


        if (resultado.autenticado === true) {

            window.location.href =
                "administrador.html";
        }

    } catch (error) {

        console.error(
            "No fue posible verificar la sesión.",
            error
        );
    }
}


async function iniciarSesion(event) {

    event.preventDefault();


    const usuario =
        document
            .getElementById("usuario")
            .value
            .trim();


    const password =
        document
            .getElementById("password")
            .value;


    mostrarMensaje(
        "",
        ""
    );


    try {

        const response =
            await fetch(
                `${API_AUTH}/login`,
                {
                    method:
                        "POST",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body:
                        JSON.stringify({
                            usuario,
                            password
                        })
                }
            );


        const resultado =
            await response.json();


        if (!response.ok) {

            throw new Error(
                resultado.mensaje
                ||
                "No fue posible iniciar sesión."
            );
        }


        window.location.href =
            "administrador.html";


    } catch (error) {

        mostrarMensaje(
            error.message,
            "error"
        );
    }
}


function mostrarMensaje(
    texto,
    tipo
) {

    mensajeLogin.textContent =
        texto;


    mensajeLogin.className =
        "mensaje";


    if (tipo) {

        mensajeLogin.classList.add(
            tipo
        );
    }
}