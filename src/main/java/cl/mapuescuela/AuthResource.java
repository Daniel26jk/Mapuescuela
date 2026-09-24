package cl.mapuescuela;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final String ADMIN_USUARIO =
            System.getenv().getOrDefault(
                    "MAPUESCUELA_ADMIN_USER",
                    "admin"
            );

    private static final String ADMIN_PASSWORD =
            System.getenv().getOrDefault(
                    "MAPUESCUELA_ADMIN_PASSWORD",
                    "mapuescuela2026"
            );


    public static class LoginRequest {

        private String usuario;
        private String password;

        public LoginRequest() {
        }

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }


    @POST
    @Path("/login")
    public Response login(
            LoginRequest datos,
            @Context HttpServletRequest request) {

        if (datos == null ||
                datos.getUsuario() == null ||
                datos.getPassword() == null) {

            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity(
                            "{\"mensaje\":\"Debe ingresar usuario y contraseña\"}"
                    )
                    .build();
        }


        boolean usuarioCorrecto =
                ADMIN_USUARIO.equals(
                        datos.getUsuario().trim()
                );

        boolean passwordCorrecto =
                ADMIN_PASSWORD.equals(
                        datos.getPassword()
                );


        if (!usuarioCorrecto ||
                !passwordCorrecto) {

            return Response
                    .status(Response.Status.UNAUTHORIZED)
                    .entity(
                            "{\"mensaje\":\"Usuario o contraseña incorrectos\"}"
                    )
                    .build();
        }


        HttpSession session =
                request.getSession(true);

        session.setAttribute(
                "mapuescuelaAdmin",
                true
        );


        return Response
                .ok(
                        "{\"autenticado\":true}"
                )
                .build();
    }


    @POST
    @Path("/logout")
    public Response logout(
            @Context HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        if (session != null) {
            session.invalidate();
        }


        return Response
                .ok(
                        "{\"autenticado\":false}"
                )
                .build();
    }


    @GET
    @Path("/session")
    public Response estadoSesion(
            @Context HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);


        boolean autenticado =
                session != null
                        &&
                Boolean.TRUE.equals(
                        session.getAttribute(
                                "mapuescuelaAdmin"
                        )
                );


        return Response
                .ok(
                        "{\"autenticado\":"
                                + autenticado
                                + "}"
                )
                .build();
    }
}