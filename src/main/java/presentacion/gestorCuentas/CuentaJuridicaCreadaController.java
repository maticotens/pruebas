package presentacion.gestorCuentas;

import accessManagment.Roles;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import modelo.authService.AuthServiceColaborador;
import modelo.authService.AuthServiceUsuario;
import modelo.colaboracion.MotivoDistribucion;
import modelo.excepciones.ExcepcionValidacion;
import modelo.personas.Rubro;
import modelo.personas.TipoJuridico;
import modelo.validador.Usuario;
import org.jetbrains.annotations.NotNull;
import persistencia.RepositorioColaboradores;
import utils.GeneradorModel;

import java.util.HashMap;
import java.util.Map;

public class CuentaJuridicaCreadaController implements Handler {

    private RepositorioColaboradores repoColab = RepositorioColaboradores.getInstancia();


    public CuentaJuridicaCreadaController() {
        super();
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        Map<String, Object> model = GeneradorModel.getModel(context);

        String esPorSSO = context.formParam("esXSSO");
        Boolean esXSSO = Boolean.FALSE;
        if (esPorSSO == "1") {
            esXSSO = Boolean.TRUE;
        }

        String razonSocial = context.formParam("razon-social");
        Integer tipo = Integer.valueOf(context.formParam("tipo"));
        Integer rubro = Integer.valueOf(context.formParam("rubro"));
        String cuit = context.formParam("cuit");
        String telefono = context.formParam("telefono-representante");
        String email = context.formParam("email");
        String username = context.formParam("username");
        String password = context.formParam("password");
        String terminos = context.formParam("terms");
        String direccion = context.formParam("direccion");

        if (razonSocial.equals("") || tipo.equals("") || rubro.equals("") || cuit.equals("") || email.equals("") || username.equals("")
            || ( password.equals("") && !esXSSO ) || terminos.equals("")){
            model.put("errorJuridico", "Debe completar todos los campos");
            context.redirect("/crearCuentaJuridica");
            return;
        }

        if ( !esNumerico(cuit)  ||  !esNumerico(telefono) )  {
            model.put("errorJuridico", "El número de CUIT o el teléfono no son numéricos");
            context.redirect("/crearCuentaJuridica");
            return;
        }

        if ( !cuit.matches("[0-9]{11}") )  {
            model.put("errorJuridico", "El número de CUIT debe tener 11 dígitos");
            context.redirect("/crearCuentaJuridica");
            return;
        }

        if ( !telefono.equals("")  &&  !telefono.matches("[0-9]{8,10}") )  {
            model.put("errorJuridico", "El teléfono debe tener entre 8 y 10 dígitos");
            context.redirect("/crearCuentaJuridica");
            return;
        }

        Rubro rubroJuridico;
        switch (rubro) {
            case 1:
                rubroJuridico = Rubro.GASTRONOMIA;
                break;
            case 2:
                rubroJuridico = Rubro.ELECTRONICA;
                break;
            default:
                rubroJuridico = Rubro.ARTICULOS_HOGAR;
                break;
        }

        TipoJuridico tipoJuridico;
        switch (tipo) {
            case 1:
                tipoJuridico = TipoJuridico.GUBERNAMENTAL;
                break;
            case 2:
                tipoJuridico = TipoJuridico.ONG;
                break;
            case 3:
                tipoJuridico = TipoJuridico.EMPRESA;
                break;
            default:
                tipoJuridico = TipoJuridico.INSTITUCION;
                break;
        }

        try {
            Usuario usuario = new Usuario();
            if ( esXSSO ){
                usuario.setMail(email);
                usuario.setUsername(username);
                usuario.setRol(Roles.USUARIO);
            }else {
                usuario = AuthServiceUsuario.validarUsuario(email, username, password);
            }
            AuthServiceColaborador.registrarColaboradorJuridico(usuario, razonSocial, tipoJuridico, rubroJuridico, cuit, telefono, email, direccion);

        } catch (ExcepcionValidacion e) {
            if(esXSSO){
                model.put("errorJuridico", e.getMessage());
                context.redirect("/crearCuentaJuridicaSSO");
                return;
            }
            model.put("errorJuridico", e.getMessage());
            context.redirect("/crearCuentaJuridica");
            return;
        }

        context.redirect("/cuentaCreada");
    }


    public static boolean esNumerico(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}

