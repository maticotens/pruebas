package presentacion.gestorCuentas;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import modelo.personas.*;
import org.jetbrains.annotations.NotNull;
import persistencia.RepositorioColaboradores;
import utils.GeneradorModel;

import java.util.HashMap;
import java.util.Map;

public class PerfilController implements Handler {

    RepositorioColaboradores repoColaboradores = RepositorioColaboradores.getInstancia();

    public PerfilController() {
        super();
    }

    @Override
    public void handle(@NotNull Context context) throws Exception {
        Map<String, Object> model = GeneradorModel.getModel(context);

        NotificacionCambio notificacionCambio = context.sessionAttribute("notificacionCambio");
        if(notificacionCambio != null){
            model.put("notificacionCambio", notificacionCambio);
        }
        context.consumeSessionAttribute("notificacionCambio");

        Integer idPersona = context.sessionAttribute("idPersona");
        TipoPersona tipoPer = context.sessionAttribute("tipoPersona");
        model.put("nombreUsuario", context.sessionAttribute("nombreUsuario"));

        if (tipoPer == TipoPersona.PH) {
            PersonaHumana persona = repoColaboradores.traerPersonaPorIdFisica(idPersona);
            String nombreApellido = persona.getNombre() + " " + persona.getApellido();
            model.put("esPersonaHumana", true);
            model.put("nombre", nombreApellido);
            model.put("tipoDoc", persona.getDocumento().getTipoDoc());
            model.put("nroDoc", persona.getDocumento().getNumeroDoc());
            model.put("fechaNacimiento", persona.getFechaNacimiento());
            model.put("direccion", persona.getDireccion());
            model.put("telefono", persona.getTelefono());
            model.put("email", persona.getEmail());
        }
        if (tipoPer == TipoPersona.PJ) {
            PersonaJuridica persona = repoColaboradores.traerPersonaPorIdJuridica(idPersona);
            model.put("esPersonaJuridica", true);
            model.put("nombre", persona.getRazonSocial());
            model.put("cuit", persona.getCUIT());
            model.put("tipoJuridico", persona.getTipoJuridico());
            model.put("rubro", persona.getRubro());
            model.put("direccion", persona.getDireccion());
            model.put("telefono", persona.getTelefono());
            model.put("email", persona.getEmail());
        }

        context.render("templates/perfil.mustache", model);
    }
}
