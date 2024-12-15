package modelo.suscripcion;

import modelo.elementos.Heladera;
import lombok.Getter;
import modelo.notificador.Notificador;
import modelo.personas.Colaborador;
import modelo.personas.MedioDeContacto;

import javax.persistence.*;

@Entity
@Table
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Suscripcion {

    @Id
    @GeneratedValue
    @Getter
    private Long id;

    @ManyToOne
    @JoinColumn(name="heladera_id", referencedColumnName = "id")
    @Getter private Heladera heladera;

    @ManyToOne
    @JoinColumn(name="colaborador_id",referencedColumnName = "id")
    private Colaborador colaborador;

    @Enumerated(EnumType.STRING)
    @Getter private TipoSuscripcion tipoSuscripcion;

    @OneToOne(cascade = CascadeType.ALL)
    private MedioDeContacto medioDeContacto;

    public Suscripcion(Heladera heladera, Colaborador colaborador, TipoSuscripcion tipo, MedioDeContacto medio) {
        this.heladera = heladera;
        this.colaborador = colaborador;
        this.tipoSuscripcion = tipo;
        this.medioDeContacto = medio;
    }

    public Suscripcion() {

    }

    // esta validacion debe hacerse en el controller
    public void validarExistenciaMedioDeContacto() {
        if (this.medioDeContacto.getContacto() == null) {
            throw new RuntimeException("No se puede notificar al colaborador, no tiene cargado el medio de contacto");
        }
    }

    public void notificarmeSuscripcion() {
        String texto;
        String asunto;
        switch (this.tipoSuscripcion) {
            case POCO_ESPACIO:
                texto = "La heladera " + heladera.getNombre() + " ubicada en " + heladera.getPuntoEstrategico().getDireccion() + " tiene poco espacio!" +
                        "Corre a distribuir sus viandas y suma puntos!";
                asunto = "Poco espacio en heladera";
                Notificador.notificar(texto, asunto, medioDeContacto);
                break;
            case QUEDAN_POCAS:
                texto = "La heladera " + heladera.getNombre()  + " ubicada en " + heladera.getPuntoEstrategico().getDireccion() + " tiene pocas viandas!" +
                        "Ve a rellenarla con mas viandas y consigue puntos!";
                asunto = "Quedan pocas viandas!";
                Notificador.notificar(texto, asunto, medioDeContacto);
                break;
            case DESPERFECTO:
                Sugerencia sugerencia = new Sugerencia(heladera);
                texto = sugerencia.devolerMensajeSugerencia();
                asunto = "Hubo un Desperfecto en " + heladera.getNombre() +"!";
                Notificador.notificar(texto, asunto, medioDeContacto);
                break;
            default:
                break;

        }

    }
}
