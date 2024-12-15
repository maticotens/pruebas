package modelo.colaboracion;

import modelo.elementos.SolicitudApertura;
import lombok.Getter;
import lombok.Setter;
import modelo.elementos.Heladera;
import modelo.excepciones.ExcepcionValidacion;
import modelo.personas.Colaborador;
import modelo.personas.TipoPersona;
import persistencia.RepositorioSolicitudes;
import javax.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("DISTRIBUCION")
public class DistribucionDeViandas extends Colaboracion {

    @ManyToOne
    @JoinColumn(name = "heladera_origen_id", referencedColumnName = "id")
    private Heladera heladeraOrigen;

    @ManyToOne
    @JoinColumn(name = "heladera_destino_id", referencedColumnName = "id")
    private Heladera heladeraDestino;

    @Column
    private int viandasDistribuidas;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "distribucion_x_vianda",
            joinColumns = @JoinColumn(name = "distribucion_id"),
            inverseJoinColumns = @JoinColumn(name = "vianda_id")
    )
    @Setter private List<Vianda>viandas;

    @Enumerated(EnumType.STRING)
    private MotivoDistribucion motivoDistribucion;

    @Transient
    @Setter private static Double coeficiente = 1.0;

    @OneToOne
    @JoinColumn(name = "solicitud_apertura_id", referencedColumnName = "id")
    @Getter private SolicitudApertura solicitud;

    // CONSTRUCTOR PRINCIPAL
    public DistribucionDeViandas(Heladera heladeraOrigen, Heladera heladeraDestino, MotivoDistribucion motivoDistribucion, LocalDate fechaDistribucion) {
        this.tiposPersonasHabilitadas = Arrays.asList(TipoPersona.PH);
        this.fechaColaboracion = fechaDistribucion;

        this.heladeraOrigen = heladeraOrigen;
        this.heladeraDestino = heladeraDestino;
        this.viandas = new ArrayList<>();
        this.motivoDistribucion = motivoDistribucion;
        this.viandasDistribuidas = 0;
    }

    // CONSTRUCTOR PARA IMPORTADOR CSV
    public DistribucionDeViandas(LocalDate fechaDistribucion, Integer cantidadViandas)
    {
        this.tiposPersonasHabilitadas = Arrays.asList(TipoPersona.PH);
        this.fechaColaboracion  = fechaDistribucion;
        this.viandas = new ArrayList<>();

        this.heladeraDestino = null;
        this.heladeraOrigen = null;
        this.motivoDistribucion = null;
        this.viandasDistribuidas = 0;
        this.porCSV = Boolean.TRUE;
    }

    public DistribucionDeViandas() {

    }

    @Override
    public void hacerColaboracion(Colaborador colaborador) {
//        if(this.solicitud.getAperturaFehaciente() == null){
//            System.out.println("No se ha solicitado la apertura de la heladera destino");
//            return;
//        }else if ( Duration.between(this.solicitud.getFechaSolicitud(), this.solicitud.getAperturaFehaciente()).toHours() >= this.solicitud.getHorasDeApertura()){
//            System.out.println("El usuario carece de permisos para realizar dicha acción.");
//            return;
//        } //TODO
        //this.efectuarApertura(colaborador);
        //String text = validar(colaborador);
        //if(text == null){
            colaborador.agregarColaboracion(this);

            switch (motivoDistribucion) {
                case DESPERFECTO:

                    for (Vianda vianda : viandas) {
                        if(!heladeraDestino.hayLugar()){
                            throw new ExcepcionValidacion("No hay lugar en la heladera destino");
                        }
                        heladeraOrigen.retirarVianda(heladeraOrigen.getViandas().indexOf(vianda));
                        heladeraDestino.agregarVianda(vianda);
                        vianda.setDisponibleEn(heladeraDestino);
                        viandasDistribuidas += 1;
                    }
                    break;
                case FALTA_VIANDAS:
                    if(!heladeraDestino.hayLugar()){
                        throw new ExcepcionValidacion("No hay lugar en la heladera destino");
                    }else {
                        for (Vianda vianda : viandas) {
                            heladeraOrigen.retirarVianda(heladeraOrigen.getViandas().indexOf(vianda));
                            heladeraDestino.agregarVianda(vianda);
                            vianda.setDisponibleEn(heladeraDestino);
                            viandasDistribuidas += 1;
                        }
                        break;
                    }
                default:
                    break;
            }

            incrementarPuntos(colaborador);

        //else {
         //   System.out.println("Error!!!");
          //  System.out.println(text);
        //}
    }

    @Override
    public String validar(Colaborador colaborador) {
        if(heladeraOrigen.getHabilitado() && heladeraOrigen.getActiva() && this.tiposPersonasHabilitadas.contains(colaborador.getTipoPersona())){
            return null;
        }
        else if(heladeraOrigen.getHabilitado()){
            return "La heladera no está habilitada";
        }
        else if(heladeraOrigen.getActiva()){
            return "La heladera no está activa";
        } else {
            return "Ese Tipo de Persona no puede realizar este tipo de Colaboración!";
        }
    }

    @Override
    public void incrementarPuntos(Colaborador colaborador) {
        colaborador.incrementarPuntaje(this.viandasDistribuidas * coeficiente);
    }

    public void solicitarAperturaHeladera(Colaborador colaborador, Heladera heladera){
        if( !heladera.permitirIngreso() ) {
            System.out.println("La heladera ya se encuentra inhabilitada");
            return;
        }
        SolicitudApertura seguimientoApertura = new SolicitudApertura(heladera, colaborador);
        RepositorioSolicitudes repositorioSolicitudes = RepositorioSolicitudes.getInstancia();
        repositorioSolicitudes.agregarSolicitud(seguimientoApertura);
        this.solicitud = seguimientoApertura;

        if( colaborador.getTarjeta() == null ){
            colaborador.solicitarTarjeta();
        }
    }

    public void efectuarApertura(Colaborador colaborador){
        if(this.solicitud != null){
            RepositorioSolicitudes repositorioSolicitudes = RepositorioSolicitudes.getInstancia();
            repositorioSolicitudes.cambiarEstadoAFehaciente(this.solicitud);
        }
        else {
            System.out.println("No se ha solicitado la apertura de la heladera");
            return;
        }
    }

    @Override
    public Double conocerPuntaje(){return this.viandasDistribuidas * coeficiente; }

    @Override
    public String getClassName() {
        return "Donacion de Dinero";
    }
}