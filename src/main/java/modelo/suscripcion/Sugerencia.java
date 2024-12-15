package modelo.suscripcion;

import modelo.elementos.Heladera;
import persistencia.RepositorioHeladeras;

import java.util.List;

public class Sugerencia {
    private Heladera heladeraAfectada;
    private List<Heladera> heladerasCercanas;
    private static final Integer cantidadHeladerasCercanas = 3; // Acá establecemos que en todas las sugerencias se van a recomendar 3 heladeras como máximo.

    public Sugerencia(Heladera heladeraAfectada) {
        this.heladeraAfectada = heladeraAfectada;
        this.llenarHeladerasCercanas();
    }

    public void llenarHeladerasCercanas(){
        RepositorioHeladeras repositorioHeladeras = RepositorioHeladeras.getInstancia();
        this.heladerasCercanas = repositorioHeladeras.obtenerHeladerasCercanas(this.heladeraAfectada, this.cantidadHeladerasCercanas);
    }

    public String devolerMensajeSugerencia(){
        String mensaje = "La heladera " + heladeraAfectada.getNombre() + " ubicada en " + this.heladeraAfectada.getPuntoEstrategico().getDireccion() + " tiene un desperfecto.\n Te recomendamos que visites las siguientes heladeras cercanas: \n";
        for (Heladera heladera : this.heladerasCercanas) {
            mensaje += "Heladera: "+ heladera.getNombre() + " ubicada en: " + heladera.getPuntoEstrategico().getDireccion() + "\n";
        }
        return mensaje;
    }

}
