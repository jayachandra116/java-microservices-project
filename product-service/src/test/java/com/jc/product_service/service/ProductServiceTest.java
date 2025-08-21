package com.jc.product_service.service;

import com.jc.product_service.exception.ProductNotFoundException;
import com.jc.product_service.model.Product;
import com.jc.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("High performance laptop")
                .price(1200.0)
                .stock(10)
                .build();
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(product)).thenReturn(product);

        Product saved = productService.createProduct(product);

        assertNotNull(saved);
        assertEquals("Laptop", saved.getName());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldGetAllProducts() {
        when(productRepository.findAll()).thenReturn(Arrays.asList(product));

        List<Product> products = productService.getAllProducts();

        assertEquals(1, products.size());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void shouldGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product found = productService.getProductById(1L);

        assertNotNull(found);
        assertEquals("Laptop", found.getName());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getProductById(1L));
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void shouldUpdateProduct() {
        Product updated = Product.builder()
                .name("Updated Laptop")
                .description("Updated description")
                .price(1500.0)
                .stock(5)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        Product result = productService.updateProduct(1L, updated);

        assertEquals("Updated Laptop", result.getName());
        assertEquals(1500.0, result.getPrice());
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldDeleteProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
    }

}
