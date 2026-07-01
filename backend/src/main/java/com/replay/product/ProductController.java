package com.replay.product;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Catalogue produits et categories")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<Page<ProductSummaryDTO>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String console,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {

        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(
                sortParams[1].equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC : Sort.Direction.ASC,
                sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<Product> products = productService.getProducts(
                category, console, condition, minPrice, maxPrice, pageable);

        Page<ProductSummaryDTO> dtoPage = products.map(p -> new ProductSummaryDTO(
                p.getId(), p.getName(), p.getSlug(), p.getPrice(),
                p.getCondition().name(), p.getConsoleType().name(),
                p.getImageUrl(), p.getStockQuantity(),
                p.getCategory().getName(), p.getCategory().getSlug()
        ));

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/products/{slug}")
    public ResponseEntity<ProductDetailDTO> getProductBySlug(@PathVariable String slug) {
        Product product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(new ProductDetailDTO(
                product.getId(), product.getName(), product.getSlug(),
                product.getDescription(), product.getPrice(),
                product.getCondition().name(), product.getConsoleType().name(),
                product.getStockQuantity(), product.getImageUrl(),
                product.isArchived(), product.getCreatedAt(),
                product.getCategory().getId(), product.getCategory().getName(),
                product.getCategory().getSlug()
        ));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        List<Category> categories = productService.getCategories();
        List<CategoryDTO> dtos = categories.stream()
                .map(c -> new CategoryDTO(c.getId(), c.getName(), c.getSlug(),
                        c.getDescription(), c.getImageUrl()))
                .toList();
        return ResponseEntity.ok(dtos);
    }
}
