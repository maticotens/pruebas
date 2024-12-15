package presentacion.colaboraciones;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import lombok.Getter;
import lombok.Setter;
import modelo.elementos.Heladera;
import modelo.excepciones.ExcepcionValidacion;
import org.jetbrains.annotations.NotNull;
import persistencia.RepositorioHeladeras;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DonarViandaController implements Handler {

    @Override
    public void handle(@NotNull Context context) throws Exception {
        Map<String, Object> model = context.sessionAttribute("model");
        if (model == null) {
            model = new HashMap<>();
            context.sessionAttribute("model", model);
        }

        RepositorioHeladeras repoHeladeras = RepositorioHeladeras.getInstancia();
        List<HeladeraConDisponibilidad> heladeras = getHeladerasConDisponibilidad(repoHeladeras.obtenerHeladeras().stream().filter(Heladera::getActiva).toList());

        if (heladeras == null)
            throw new ExcepcionValidacion("No se pudieron obtener las heladeras");


        model.put("heladeras", heladeras);

        model.put("nombreUsuario", context.sessionAttribute("nombreUsuario"));
        context.render("templates/donarVianda.mustache",model);
    }

    private List<HeladeraConDisponibilidad> getHeladerasConDisponibilidad(List<Heladera> heladeras){
        return heladeras.stream()
                .filter(h -> cantidadViandas(h) > 1)
                .map(heladera -> {
                    HeladeraConDisponibilidad heladeraConDisponibilidad = new HeladeraConDisponibilidad();
                    heladeraConDisponibilidad.setNombre(heladera.getNombre());
                    heladeraConDisponibilidad.setActiva(heladera.getActiva());
                    heladeraConDisponibilidad.setDireccion(heladera.getPuntoEstrategico().getDireccion());
                    heladeraConDisponibilidad.setDisponibilidad(cantidadViandas(heladera));
                    heladeraConDisponibilidad.setId(heladera.getId());
                    return heladeraConDisponibilidad;
                }).toList();
    }

    private Integer cantidadViandas(Heladera heladera){
        return heladera.getViandasMaximas() - heladera.getViandas().size();
    }

}

@Getter @Setter
class HeladeraConDisponibilidad {
    private int id;
    private String nombre;
    private Integer disponibilidad;
    private Boolean activa;
    private String direccion;

}
