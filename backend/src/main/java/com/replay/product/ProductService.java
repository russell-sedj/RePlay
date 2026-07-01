package com.replay.product;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<Product> getProducts(String categorySlug, String console, String condition,
                                     BigDecimal minPrice, BigDecimal maxPrice,
                                     Pageable pageable) {
        Specification<Product> spec = Specification.where(isNotArchived());

        if (categorySlug != null) {
            spec = spec.and(hasCategorySlug(categorySlug));
        }
        if (console != null) {
            spec = spec.and(hasConsoleType(console));
        }
        if (condition != null) {
            spec = spec.and(hasCondition(condition));
        }
        if (minPrice != null) {
            spec = spec.and(priceGreaterThanOrEqual(minPrice));
        }
        if (maxPrice != null) {
            spec = spec.and(priceLessThanOrEqual(maxPrice));
        }

        return productRepository.findAll(spec, pageable);
    }

    public Product getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found: " + slug));
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    private Specification<Product> isNotArchived() {
        return (root, query, cb) -> cb.isFalse(root.get("archived"));
    }

    private Specification<Product> hasCategorySlug(String slug) {
        return (root, query, cb) -> {
            var join = root.join("category");
            return cb.equal(join.get("slug"), slug);
        };
    }

    private Specification<Product> hasConsoleType(String console) {
        return (root, query, cb) -> cb.equal(root.get("consoleType"),
                ConsoleType.valueOf(console));
    }

    private Specification<Product> hasCondition(String condition) {
        return (root, query, cb) -> cb.equal(root.get("condition"),
                ProductCondition.valueOf(condition));
    }

    private Specification<Product> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    private Specification<Product> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice);
    }
}
