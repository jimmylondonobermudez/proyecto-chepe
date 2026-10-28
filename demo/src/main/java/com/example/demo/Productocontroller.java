package com.dondechepe.api.controller; // cambia el package por el de tu proyecto
 
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
/**
 * Controlador de productos de Donde Chepe (todo en un solo archivo).
 * Necesita solo spring-boot-starter-web.
 *
 * Postman:
 *   GET  /api/productos?categoria=hamburguesas
 *   GET  /api/productos/1        Header: Authorization: token-chepe
 *   POST /api/productos          Header: X-User-Role: admin   Body JSON
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {
 
    public record Producto(Long id, String nombre, String categoria, double precio, int stock) {}
 
    private static final String TOKEN = "token-chepe";
 
    private final List<Producto> productos = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();
 
    public ProductoController() {
        agregar("Hamburguesa Chepe", "hamburguesas", 18000, 30);
        agregar("Perro Caliente Especial", "perros calientes", 12000, 40);
        agregar("Salchipapa Mixta", "salchipapas", 16000, 25);
        agregar("Gaseosa 400ml", "bebidas", 4000, 100);
    }
 
    private Producto agregar(String nombre, String categoria, double precio, int stock) {
        Producto p = new Producto(seq.incrementAndGet(), nombre, categoria, precio, stock);
        productos.add(p);
        return p;
    }
 
    // GET /api/productos?categoria=hamburguesas  -> Query Param (opcional)
    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String categoria) {
        return productos.stream()
                .filter(p -> categoria == null || p.categoria().equalsIgnoreCase(categoria))
                .toList();
    }
 
    // GET /api/productos/1  -> Path Variable + Header Authorization
    @GetMapping("/{id}")
    public ResponseEntity<?> obtener(@PathVariable Long id,
                                     @RequestHeader(value = "Authorization", required = false) String token) {
        if (!TOKEN.equals(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token inválido o ausente"));
        }
        return productos.stream().filter(p -> p.id().equals(id)).findFirst()
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Producto no encontrado")));
    }
 
    // POST /api/productos  -> Body JSON + Header X-User-Role
    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Producto p,
                                   @RequestHeader(value = "X-User-Role", required = false) String rol) {
        if (!"admin".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Solo admin puede agregar productos"));
        }
        if (p.nombre() == null || p.nombre().isBlank() || p.precio() <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nombre y precio válidos son obligatorios"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(agregar(p.nombre(), p.categoria(), p.precio(), p.stock()));
    }
}
