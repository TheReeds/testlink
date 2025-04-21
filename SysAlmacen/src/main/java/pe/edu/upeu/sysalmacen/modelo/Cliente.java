package pe.edu.upeu.sysalmacen.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "upeu_cliente")
public class Cliente {

    @Id
    @Column(name = "dniruc", nullable = false, length = 12)
    private String dniruc;

    @Column(name = "nombres", nullable = false, length = 160)
    private String nombres;

    @Column(name = "rep_legal", length = 160)
    private String representanteLegal;  // Nombre más descriptivo

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false, length = 12)
    private TipoDocumento tipoDocumento;  // Usar enum en lugar de String

    @Column(name = "direccion", length = 200)
    private String direccion;

    // Opcional: Añadir enum para tipos de documento
    public enum TipoDocumento {
        DNI,
        RUC,
        CARNET_EXTRANJERIA,
        PASAPORTE
    }
}