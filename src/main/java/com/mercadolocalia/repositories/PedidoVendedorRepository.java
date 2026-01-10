package com.mercadolocalia.repositories;

import com.mercadolocalia.entities.PedidoVendedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoVendedorRepository extends JpaRepository<PedidoVendedor, Integer> {
    
    // 🔍 Esta consulta es la que llena la tabla del Dashboard del vendedor
    List<PedidoVendedor> findByVendedor_IdVendedor(Integer idVendedor);
    
    // 📊 Opcional: Para contar pedidos nuevos en el Dashboard
    long countByVendedor_IdVendedorAndEstado(Integer idVendedor, com.mercadolocalia.entities.EstadoPedidoVendedor estado);
}