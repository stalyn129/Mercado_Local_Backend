package com.mercadolocalia.services.impl;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mercadolocalia.dto.CarritoItemResponse;
import com.mercadolocalia.dto.CarritoResponse;
import com.mercadolocalia.dto.ProductoSimpleResponse;
import com.mercadolocalia.entities.Carrito;
import com.mercadolocalia.entities.CarritoItem;
import com.mercadolocalia.entities.Producto;
import com.mercadolocalia.repositories.CarritoItemRepository;
import com.mercadolocalia.repositories.CarritoRepository;
import com.mercadolocalia.repositories.ConsumidorRepository;
import com.mercadolocalia.repositories.ProductoRepository;
import com.mercadolocalia.services.CarritoService;

import jakarta.transaction.Transactional;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private CarritoItemRepository carritoItemRepository;

    @Autowired
    private ConsumidorRepository consumidorRepository;

    @Autowired
    private ProductoRepository productoRepository;
    


    // ==================================================
    // 🔒 MÉTODO INTERNO (NO SE EXPONE AL CONTROLLER)
    // ==================================================
    private Carrito obtenerCarrito(Integer idConsumidor) {

        return carritoRepository
            .findByConsumidorIdConsumidor(idConsumidor)
            .orElseGet(() -> {
                Carrito carrito = new Carrito();
                carrito.setConsumidor(
                    consumidorRepository.findById(idConsumidor).orElseThrow()
                );
                return carritoRepository.save(carrito);
            });
    }

    // ==================================================
    // 🟢 CARRITO EN FORMATO SEGURO PARA FRONTEND
    // ==================================================
    @Override
    public CarritoResponse obtenerCarritoResponse(Integer idConsumidor) {

        Carrito carrito = obtenerCarrito(idConsumidor);

        CarritoResponse response = new CarritoResponse();
        response.setIdCarrito(carrito.getIdCarrito());

        response.setItems(
            carrito.getItems()
                .stream()
                .map(item -> {

                    var p = item.getProducto();

                    ProductoSimpleResponse producto =
                        new ProductoSimpleResponse(
                            p.getIdProducto(),
                            p.getNombreProducto(),
                            p.getPrecioProducto(),
                            p.getImagenProducto()
                        );

                    CarritoItemResponse dto = new CarritoItemResponse();
                    dto.setIdItem(item.getIdItem());
                    dto.setCantidad(item.getCantidad());
                    dto.setProducto(producto);

                    return dto;
                })
                .collect(Collectors.toList())
        );

        return response;
    }

    // ==================================================
    // 🛒 AGREGAR ITEM (CORREGIDO - EVITA DUPLICADOS)
    // ==================================================
    @Override
    @Transactional
    public String agregarItem(Integer idConsumidor, Integer idProducto, Integer cantidad) {

        // Paso 1: Obtener o crear el carrito del consumidor
        Carrito carrito = obtenerCarrito(idConsumidor);
        
        // Paso 2: Validar que el producto existe
        Producto producto = productoRepository.findById(idProducto)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // 🔍 PASO CLAVE: Verificar si el producto YA existe en el carrito
        // Buscamos en los items del carrito si ya hay uno con este idProducto
        CarritoItem itemExistente = carrito.getItems()
            .stream()
            .filter(item -> item.getProducto().getIdProducto().equals(idProducto))
            .findFirst()
            .orElse(null);

        if (itemExistente != null) {
            // ✅ CASO 1: El producto YA existe en el carrito
            // → Actualizamos la cantidad (sumamos la nueva cantidad a la existente)
            Integer nuevaCantidad = itemExistente.getCantidad() + cantidad;
            itemExistente.setCantidad(nuevaCantidad);
            carritoItemRepository.save(itemExistente);
            
            System.out.println("✅ [Backend] Producto existente actualizado:");
            System.out.println("   - idItem: " + itemExistente.getIdItem());
            System.out.println("   - Cantidad anterior: " + (nuevaCantidad - cantidad));
            System.out.println("   - Cantidad agregada: " + cantidad);
            System.out.println("   - Cantidad nueva: " + nuevaCantidad);
            
            return "Cantidad actualizada en el carrito. Nueva cantidad: " + nuevaCantidad;
        } else {
            // ➕ CASO 2: El producto NO existe en el carrito
            // → Creamos un nuevo CarritoItem
            CarritoItem nuevoItem = new CarritoItem();
            nuevoItem.setCarrito(carrito);
            nuevoItem.setProducto(producto);
            nuevoItem.setCantidad(cantidad);
            
            carritoItemRepository.save(nuevoItem);
            
            System.out.println("➕ [Backend] Nuevo producto agregado:");
            System.out.println("   - idProducto: " + idProducto);
            System.out.println("   - Cantidad: " + cantidad);
            
            return "Producto agregado al carrito.";
        }
    }

    // ==================================================
    // ❌ ELIMINAR ITEM
    // ==================================================
    @Override
    public String eliminarItem(Integer idItem) {

        if (!carritoItemRepository.existsById(idItem)) {
            throw new RuntimeException("Item no encontrado");
        }

        carritoItemRepository.deleteById(idItem);
        return "Item eliminado del carrito.";
    }


    // ==================================================
    // 🧹 VACIAR CARRITO
    // ==================================================
    @Override
    public String vaciarCarrito(Integer idConsumidor) {

        Carrito carrito = obtenerCarrito(idConsumidor);

        carritoItemRepository.deleteAllByCarrito_IdCarrito(
            carrito.getIdCarrito()
        );

        return "Carrito vaciado.";
    }
    
    // ==================================================
    // ACTUALIZAR CANTIDAD
    // ==================================================
    
    @Override
    @Transactional
    public void actualizarCantidad(Integer idItem, Integer cantidad) {

        CarritoItem item = carritoItemRepository.findById(idItem)
            .orElseThrow(() -> new RuntimeException("Item de carrito no encontrado"));

        if (cantidad <= 0) {
            // regla de negocio: cantidad 0 = eliminar
            carritoItemRepository.delete(item);	
            return;
        }

        item.setCantidad(cantidad);
        carritoItemRepository.save(item);
    }


}