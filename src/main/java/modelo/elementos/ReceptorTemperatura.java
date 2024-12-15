package modelo.elementos;

import modelo.personas.Tecnico;
import persistencia.RepositorioIncidentes;
import persistencia.RepositoriosTecnicos;

public class ReceptorTemperatura {
    private Heladera heladera;
    private Sensoreo ultimoRegistro;

    public ReceptorTemperatura(Heladera heladera){
        this.heladera = heladera;
    }

    public void evaluar(Sensoreo sensor){ //TODO:TERMINAR

        if ( sensor.getFechaYhora().toLocalDate() == ultimoRegistro.getFechaYhora().toLocalDate() ){ // En ese caso, se reporta una FALLA DE CONEXION.
            heladera.marcarComoInactiva();
            Alerta alerta = new Alerta(TipoAlerta.FALLA_EN_CONEXION, heladera);
            RepositorioIncidentes repo = RepositorioIncidentes.getInstancia();
            repo.agregarIncidente(alerta);
            RepositoriosTecnicos tecnicos = RepositoriosTecnicos.getInstancia();
            try {
                Tecnico tecnico = tecnicos.obtenerTecnicoCercano(heladera.getPuntoEstrategico().getAreas(), heladera);
                tecnico.notificarAlerta(heladera, alerta.getTipoAlerta());
            } catch (Exception e) { //TODO verificar como volver de este error
                System.out.println("LOG SENSOR TEMPERATURA: Se reporto una falla de conexion pero no se pudo contactar con tecnico.");
                // log
            }
        }

        if(sensor.getTempRegistrada() > heladera.getTemperaturaMaxima()
            || sensor.getTempRegistrada() < heladera.getTemperaturaMinima()){

            heladera.marcarComoInactiva();
            Alerta alerta = new Alerta(TipoAlerta.FALLA_TEMPERATURA, heladera);

            RepositorioIncidentes repo = RepositorioIncidentes.getInstancia();
            repo.agregarIncidente(alerta);
        }
    }

   /* public void leerSensoreo(String path){
        // recibirlo desde el broker
        //TODO
        // cuando lee uno nuevo, corrobora que la hora no sea igual a la ultima registrada
        // en caso que sea igual, se reporta una FALLA DE CONEXION
    }*/

    public void actualizar(Sensoreo sensor) {
        this.ultimoRegistro = sensor;
        evaluar(sensor);
    }

}