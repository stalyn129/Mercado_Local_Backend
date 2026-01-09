package com.mercadolocalia.dto;

import java.util.List;
import jakarta.validation.constraints.*;

public class PedidoRequest {

	    @NotNull(message = "El consumidor es obligatorio")
	    private Integer idConsumidor;

	    // ⚠️ Opcional si luego usas multi-vendedor
	    private Integer idVendedor;

	    @NotBlank(message = "El método de pago es obligatorio")
	    private String metodoPago;

	    @NotEmpty(message = "El pedido debe tener al menos un producto")
	    private List<
	        @NotNull(message = "El detalle no puede ser nulo")
	        DetallePedidoAddRequest
	    > detalles;

	    // getters y setters
	
    public Integer getIdConsumidor() {
        return idConsumidor;
    }

    public void setIdConsumidor(Integer idConsumidor) {
        this.idConsumidor = idConsumidor;
    }

    public Integer getIdVendedor() {
        return idVendedor;
    }

    public void setIdVendedor(Integer idVendedor) {
        this.idVendedor = idVendedor;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<DetallePedidoAddRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoAddRequest> detalles) {
        this.detalles = detalles;
    }
}
