package com.replay.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository);

        category = new Category();
        category.setId(1L);
        category.setName("Consoles");
        category.setSlug("consoles");

        product = new Product();
        product.setId(1L);
        product.setName("Super Nintendo");
        product.setSlug("super-nintendo");
        product.setPrice(new BigDecimal("89.99"));
        product.setCondition(ProductCondition.BON_ETAT);
        product.setConsoleType(ConsoleType.SNES);
        product.setStockQuantity(5);
        product.setArchived(false);
        product.setCategory(category);
    }

    @Test
    void getProductsWithPagination() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Product> result = productService.getProducts(null, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Super Nintendo", result.getContent().get(0).getName());
    }

    @Test
    void getProductsFilteredByConsole() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Product> result = productService.getProducts(null, "SNES", null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductsFilteredByPriceRange() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Product> result = productService.getProducts(null, null, null,
                new BigDecimal("50"), new BigDecimal("100"), pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getProductBySlugFound() {
        when(productRepository.findBySlug("super-nintendo")).thenReturn(Optional.of(product));

        Product result = productService.getProductBySlug("super-nintendo");

        assertNotNull(result);
        assertEquals("Super Nintendo", result.getName());
    }

    @Test
    void getProductBySlugNotFound() {
        when(productRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                productService.getProductBySlug("non-existent")
        );
    }

    @Test
    void getCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<Category> result = productService.getCategories();

        assertEquals(1, result.size());
        assertEquals("Consoles", result.get(0).getName());
    }
}
