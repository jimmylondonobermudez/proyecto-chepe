
package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private List<producto> productos = new ArrayList<>();
    private AtomicLong contador = new AtomicLong(0);

    public ProductoController() {
        productos.add(new producto(contador.incrementAndGet(),
                "Hamburguesa", 12000, "Comida rapida"));

        productos.add(new producto(contador.incrementAndGet(),
                "Perro caliente", 8000, "Comida rapida"));

        productos.add(new producto(contador.incrementAndGet(),
                "Salchipapa", 15000, "Comida rapida"));
    }

    @GetMapping
    public List<producto> listarProductos() {
        return productos;
    }

    @GetMapping("/{id}")
    public ResponseEntity<producto> buscarProducto(@PathVariable Long id) {
        return productos.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public producto agregarProducto(@RequestBody producto producto) {
        producto.setId(contador.incrementAndGet());
        productos.add(producto);
        return producto;
    }

    @PutMapping("/{id}")
    public ResponseEntity<producto> actualizarProducto(
            @PathVariable Long id,
            @RequestBody producto productoActualizado) {

        for (producto p : productos) {
            if (p.getId().equals(id)) {
                p.setNombre(productoActualizado.getNombre());
                p.setPrecio(productoActualizado.getPrecio());
                p.setCategoria(productoActualizado.getCategoria());

                return ResponseEntity.ok(p);
            }
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(@PathVariable Long id) {
        boolean eliminado = productos.removeIf(p -> p.getId().equals(id));

        if (eliminado) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
