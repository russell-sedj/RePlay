package com.replay.admin;

import com.replay.auth.User;
import com.replay.auth.UserRepository;
import com.replay.common.DuplicateResourceException;
import com.replay.common.ResourceNotFoundException;
import com.replay.order.Order;
import com.replay.order.OrderRepository;
import com.replay.order.OrderStatus;
import com.replay.product.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administration (stats, produits, categories, commandes, utilisateurs)")
public class AdminController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    // --- 6.1 Stats ---

    @GetMapping("/stats")
    public ResponseEntity<StatsDTO> getStats() {
        long totalUsers = userRepository.count();
        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmountByStatusConfirmed();
        long totalProducts = productRepository.count();

        List<Object[]> topRaw = orderRepository.findTopProductsByQuantitySold(PageRequest.of(0, 5));
        List<ProductSalesDTO> topProducts = topRaw.stream()
                .map(row -> new ProductSalesDTO((String) row[0], (String) row[1], (Long) row[2]))
                .toList();

        return ResponseEntity.ok(new StatsDTO(totalUsers, totalOrders, totalRevenue, totalProducts, topProducts));
    }

    // --- 6.2 Admin Products ---

    @GetMapping("/products")
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String console,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Product> products = productService.getProducts(category, console, condition, minPrice, maxPrice, pageable);
        if (archived != null) {
            products = productRepository.findAll(PageRequest.of(page, size, sortObj));
        }

        Page<ProductSummaryDTO> dtoPage = products.map(p -> new ProductSummaryDTO(
                p.getId(), p.getName(), p.getSlug(), p.getPrice(),
                p.getCondition().name(), p.getConsoleType().name(),
                p.getImageUrl(), p.getStockQuantity(),
                p.getCategory().getName(), p.getCategory().getSlug()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDetailDTO> createProduct(@RequestBody AdminProductRequest req) {
        if (productRepository.existsBySlug(req.slug())) {
            throw new DuplicateResourceException("Slug already exists: " + req.slug());
        }

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = new Product();
        product.setName(req.name());
        product.setSlug(req.slug());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCondition(req.condition());
        product.setConsoleType(req.consoleType());
        product.setStockQuantity(req.stockQuantity());
        product.setImageUrl(req.imageUrl());
        product.setArchived(false);
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return ResponseEntity.ok(toDetailDTO(saved));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDetailDTO> updateProduct(@PathVariable Long id, @RequestBody AdminProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSlug().equals(req.slug()) && productRepository.existsBySlug(req.slug())) {
            throw new DuplicateResourceException("Slug already exists: " + req.slug());
        }

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setName(req.name());
        product.setSlug(req.slug());
        product.setDescription(req.description());
        product.setPrice(req.price());
        product.setCondition(req.condition());
        product.setConsoleType(req.consoleType());
        product.setStockQuantity(req.stockQuantity());
        product.setImageUrl(req.imageUrl());
        product.setArchived(req.archived());
        product.setCategory(category);

        Product saved = productRepository.save(product);
        return ResponseEntity.ok(toDetailDTO(saved));
    }

    // --- 6.3 Admin Categories ---

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> dtos = categories.stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getImageUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody AdminCategoryRequest req) {
        if (categoryRepository.existsBySlug(req.slug())) {
            throw new DuplicateResourceException("Slug already exists: " + req.slug());
        }

        Category category = new Category();
        category.setName(req.name());
        category.setSlug(req.slug());
        category.setDescription(req.description());
        category.setImageUrl(req.imageUrl());

        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(new CategoryDTO(saved.getId(), saved.getName(), saved.getSlug(), saved.getDescription(), saved.getImageUrl()));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody AdminCategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.getSlug().equals(req.slug()) && categoryRepository.existsBySlug(req.slug())) {
            throw new DuplicateResourceException("Slug already exists: " + req.slug());
        }

        category.setName(req.name());
        category.setSlug(req.slug());
        category.setDescription(req.description());
        category.setImageUrl(req.imageUrl());

        Category saved = categoryRepository.save(category);
        return ResponseEntity.ok(new CategoryDTO(saved.getId(), saved.getName(), saved.getSlug(), saved.getDescription(), saved.getImageUrl()));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        long productCount = productRepository.count();
        if (productCount > 0) {
            throw new IllegalArgumentException("Cannot delete category with associated products");
        }

        categoryRepository.delete(category);
        return ResponseEntity.noContent().build();
    }

    // --- 6.4 Admin Orders & Users ---

    @GetMapping("/orders")
    public ResponseEntity<Page<OrderAdminDTO>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Order> orders;

        if (status != null) {
            orders = orderRepository.findByStatus(OrderStatus.valueOf(status.toUpperCase()), pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        Page<OrderAdminDTO> dtoPage = orders.map(o -> new OrderAdminDTO(
                o.getId(), o.getUser().getId(), o.getUser().getEmail(),
                o.getOrderDate(), o.getStatus().name(), o.getTotalAmount(),
                o.getTransactionId(), o.getShippingAddress()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<OrderAdminDTO> updateOrderStatus(@PathVariable Long id, @RequestBody StatusUpdateRequest req) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus = OrderStatus.valueOf(req.status().toUpperCase());
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);

        return ResponseEntity.ok(new OrderAdminDTO(
                saved.getId(), saved.getUser().getId(), saved.getUser().getEmail(),
                saved.getOrderDate(), saved.getStatus().name(), saved.getTotalAmount(),
                saved.getTransactionId(), saved.getShippingAddress()
        ));
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserAdminDTO>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> users = userRepository.findAllByOrderByCreatedAtDesc(pageable);

        Page<UserAdminDTO> dtoPage = users.map(u -> new UserAdminDTO(
                u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getRole().name(), u.getCreatedAt()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    // --- helpers ---

    private ProductDetailDTO toDetailDTO(Product p) {
        return new ProductDetailDTO(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getPrice(),
                p.getCondition().name(), p.getConsoleType().name(),
                p.getStockQuantity(), p.getImageUrl(), p.isArchived(), p.getCreatedAt(),
                p.getCategory().getId(), p.getCategory().getName(), p.getCategory().getSlug()
        );
    }
}
