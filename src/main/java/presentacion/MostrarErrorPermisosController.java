package presentacion;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;
import utils.GeneradorModel;

import java.util.Map;

public class MostrarErrorPermisosController implements Handler {
    @Override
    public void handle(@NotNull Context context) throws Exception {
        Map<String, Object> model = GeneradorModel.getModel(context);

        model.put("nombreUsuario", context.sessionAttribute("nombreUsuario"));
        context.render("templates/404NotFound.mustache", model);
    }
}
