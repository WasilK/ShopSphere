package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.product.ProductRequest;
import com.wasil.ShopSphere.dto.product.ProductResponse;
import com.wasil.ShopSphere.exceptions.*;
import com.wasil.ShopSphere.model.Category;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
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
    private ProductRepository prodRepo;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;
    private Product inactiveProduct;
    private Inventory inventory;
    private ProductRequest productRequest;


    @BeforeEach
    void setUp() {

        // =========================
        // CATEGORY
        // =========================

        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");
        category.setCategoryIsActive(true);


        // =========================
        // PRODUCT
        // =========================

        product = new Product();
        product.setProdId(1L);
        product.setProdName("Laptop");
        product.setProdPrice(new BigDecimal("1000.00"));
        product.setProdDescription("Gaming Laptop");
        product.setProdIsActive(true);
        product.setCategory(category);


        inactiveProduct = new Product();
        inactiveProduct.setProdId(2L);
        inactiveProduct.setProdName("Old Laptop");
        inactiveProduct.setProdPrice(new BigDecimal("500.00"));
        inactiveProduct.setProdDescription("Inactive product");
        inactiveProduct.setProdIsActive(false);
        inactiveProduct.setCategory(category);


        // =========================
        // INVENTORY
        // =========================

        inventory = new Inventory();
        inventory.setInventoryId(10L);
        inventory.setProduct(product);
        inventory.setCurrentStock(0);


        // =========================
        // PRODUCT REQUEST
        // =========================

        productRequest = new ProductRequest();
        productRequest.setProdName("Laptop");
        productRequest.setProdPrice(new BigDecimal("1000.00"));
        productRequest.setProdDescription("Gaming Laptop");
        productRequest.setCategoryId(1L);
    }


    // =========================================================
    // ADD PRODUCT
    // =========================================================

    @Test
    void shouldAddProductSuccessfully() {

        // Arrange
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(prodRepo.save(any(Product.class)))
                .thenReturn(product);

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        // Act
        ProductResponse response =
                productService.addProduct(productRequest);

        // Assert
        assertNotNull(response);

        assertEquals(1L, response.getProdId());
        assertEquals("Laptop", response.getProdName());
        assertEquals(
                new BigDecimal("1000.00"),
                response.getProdPrice()
        );
        assertEquals("Gaming Laptop", response.getProdDescription());
        assertTrue(response.getProdIsActive());

        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());

        verify(categoryRepository)
                .findById(1L);

        verify(prodRepo)
                .save(any(Product.class));

        verify(inventoryRepository)
                .save(any(Inventory.class));
    }


    @Test
    void shouldThrowExceptionWhenCategoryDoesNotExistWhileAddingProduct() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        CategoryNotFoundException exception =
                assertThrows(
                        CategoryNotFoundException.class,
                        () -> productService.addProduct(productRequest)
                );

        assertEquals(
                "Category not found with id: 1",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .save(any(Product.class));

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }


    @Test
    void shouldThrowExceptionWhenCategoryIsInactive() {

        category.setCategoryIsActive(false);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        CategoryInactiveException exception =
                assertThrows(
                        CategoryInactiveException.class,
                        () -> productService.addProduct(productRequest)
                );

        assertEquals(
                "Cannot create product under an inactive category",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .save(any(Product.class));

        verify(inventoryRepository, never())
                .save(any(Inventory.class));
    }


    @Test
    void shouldCreateInventoryWithZeroStockWhenProductIsAdded() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(prodRepo.save(any(Product.class)))
                .thenReturn(product);

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        productService.addProduct(productRequest);

        ArgumentCaptor<Inventory> captor =
                ArgumentCaptor.forClass(Inventory.class);

        verify(inventoryRepository)
                .save(captor.capture());

        Inventory savedInventory = captor.getValue();

        assertEquals(product, savedInventory.getProduct());
        assertEquals(0, savedInventory.getCurrentStock());
    }


    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================

    @Test
    void shouldGetProductByIdSuccessfully() {

        when(prodRepo.findById(1L))
                .thenReturn(Optional.of(product));

        ProductResponse response =
                productService.getProductById(1L);

        assertNotNull(response);

        assertEquals(1L, response.getProdId());
        assertEquals("Laptop", response.getProdName());
        assertEquals(
                new BigDecimal("1000.00"),
                response.getProdPrice()
        );
        assertEquals("Gaming Laptop", response.getProdDescription());
        assertTrue(response.getProdIsActive());

        assertEquals(1L, response.getCategoryId());
        assertEquals("Electronics", response.getCategoryName());

        verify(prodRepo)
                .findById(1L);
    }


    @Test
    void shouldThrowExceptionWhenGettingNonExistingProduct() {

        when(prodRepo.findById(1L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> productService.getProductById(1L)
                );

        assertEquals(
                "Product not found with id :1",
                exception.getMessage()
        );
    }


    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    @Test
    void shouldUpdateProductSuccessfully() {

        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setProdName("Updated Laptop");
        updateRequest.setProdPrice(new BigDecimal("1200.00"));
        updateRequest.setProdDescription("Updated Description");
        updateRequest.setCategoryId(1L);

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(prodRepo.findById(1L))
                .thenReturn(Optional.of(product));

        when(prodRepo.save(any(Product.class)))
                .thenReturn(product);

        ProductResponse response =
                productService.updateProduct(
                        1L,
                        updateRequest
                );

        assertNotNull(response);

        assertEquals("Updated Laptop", response.getProdName());
        assertEquals(
                new BigDecimal("1200.00"),
                response.getProdPrice()
        );
        assertEquals(
                "Updated Description",
                response.getProdDescription()
        );

        verify(categoryRepository)
                .findById(1L);

        verify(prodRepo)
                .findById(1L);

        verify(prodRepo)
                .save(product);
    }


    @Test
    void shouldThrowExceptionWhenUpdateCategoryDoesNotExist() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.updateProduct(
                        1L,
                        productRequest
                )
        );

        verify(prodRepo, never())
                .findById(anyLong());

        verify(prodRepo, never())
                .save(any(Product.class));
    }


    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingProduct() {

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(prodRepo.findById(1L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> productService.updateProduct(
                                1L,
                                productRequest
                        )
                );

        assertEquals(
                "Product not found with id :1",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .save(any(Product.class));
    }


    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    @Test
    void shouldDeleteProductSuccessfully() {

        when(prodRepo.findById(1L))
                .thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        assertFalse(product.getProdIsActive());

        verify(prodRepo)
                .findById(1L);

        verify(prodRepo)
                .save(product);
    }


    @Test
    void shouldThrowExceptionWhenDeletingNonExistingProduct() {

        when(prodRepo.findById(1L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> productService.deleteProduct(1L)
                );

        assertEquals(
                "Product not found with id :1",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .save(any(Product.class));
    }


    // =========================================================
    // SEARCH / FILTER / PAGINATION
    // =========================================================

    @Test
    void shouldSearchProductsSuccessfully() {

        Page<Product> productPage =
                new PageImpl<>(
                        List.of(product),
                        PageRequest.of(
                                0,
                                10,
                                Sort.by(
                                        Sort.Direction.ASC,
                                        "prodName"
                                )
                        ),
                        1
                );

        when(prodRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        Page<ProductResponse> response =
                productService.searchAndFilterProducts(
                        "Laptop",
                        null,
                        null,
                        null,
                        0,
                        10,
                        "prodName",
                        "asc"
                );

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getContent().size());

        assertEquals(
                "Laptop",
                response.getContent().get(0).getProdName()
        );

        verify(prodRepo)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }


    @Test
    void shouldSearchProductsWithPriceAndCategoryFilters() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(prodRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        Page<ProductResponse> response =
                productService.searchAndFilterProducts(
                        null,
                        new BigDecimal("500"),
                        new BigDecimal("1500"),
                        1L,
                        0,
                        10,
                        "prodPrice",
                        "asc"
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());

        verify(prodRepo)
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }


    @Test
    void shouldSearchProductsWithoutOptionalFilters() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(prodRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        Page<ProductResponse> response =
                productService.searchAndFilterProducts(
                        null,
                        null,
                        null,
                        null,
                        0,
                        10,
                        "prodName",
                        "asc"
                );

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }


    @Test
    void shouldSupportDescendingSort() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(prodRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        productService.searchAndFilterProducts(
                null,
                null,
                null,
                null,
                0,
                10,
                "prodPrice",
                "desc"
        );

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(prodRepo).findAll(
                any(Specification.class),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort()
                        .getOrderFor("prodPrice")
                        .getDirection()
        );
    }


    // =========================================================
    // PAGINATION VALIDATION
    // =========================================================

    @Test
    void shouldThrowExceptionWhenPageIsNegative() {

        InvalidPaginationException exception =
                assertThrows(
                        InvalidPaginationException.class,
                        () -> productService.searchAndFilterProducts(
                                null,
                                null,
                                null,
                                null,
                                -1,
                                10,
                                "prodName",
                                "asc"
                        )
                );

        assertEquals(
                "Page must be greater than or equal to 0",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }


    @Test
    void shouldThrowExceptionWhenPageSizeIsZero() {

        InvalidPaginationException exception =
                assertThrows(
                        InvalidPaginationException.class,
                        () -> productService.searchAndFilterProducts(
                                null,
                                null,
                                null,
                                null,
                                0,
                                0,
                                "prodName",
                                "asc"
                        )
                );

        assertEquals(
                "Page size must be between 1 and 100",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenPageSizeExceeds100() {

        InvalidPaginationException exception =
                assertThrows(
                        InvalidPaginationException.class,
                        () -> productService.searchAndFilterProducts(
                                null,
                                null,
                                null,
                                null,
                                0,
                                101,
                                "prodName",
                                "asc"
                        )
                );

        assertEquals(
                "Page size must be between 1 and 100",
                exception.getMessage()
        );
    }


    // =========================================================
    // PRICE VALIDATION
    // =========================================================

    @Test
    void shouldThrowExceptionWhenMinimumPriceIsGreaterThanMaximumPrice() {

        InvalidPriceRangeException exception =
                assertThrows(
                        InvalidPriceRangeException.class,
                        () -> productService.searchAndFilterProducts(
                                null,
                                new BigDecimal("2000"),
                                new BigDecimal("1000"),
                                null,
                                0,
                                10,
                                "prodName",
                                "asc"
                        )
                );

        assertEquals(
                "Minimum price cannot be greater than maximum price",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }


    // =========================================================
    // SORT VALIDATION
    // =========================================================

    @Test
    void shouldThrowExceptionWhenSortFieldIsInvalid() {

        InvalidSortFieldException exception =
                assertThrows(
                        InvalidSortFieldException.class,
                        () -> productService.searchAndFilterProducts(
                                null,
                                null,
                                null,
                                null,
                                0,
                                10,
                                "password",
                                "asc"
                        )
                );

        assertEquals(
                "Invalid sort field: password",
                exception.getMessage()
        );

        verify(prodRepo, never())
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }


    @Test
    void shouldAcceptAllAllowedSortFields() {

        Page<Product> productPage =
                new PageImpl<>(List.of(product));

        when(prodRepo.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(productPage);

        // prodName
        productService.searchAndFilterProducts(
                null, null, null, null,
                0, 10,
                "prodName",
                "asc"
        );

        // prodPrice
        productService.searchAndFilterProducts(
                null, null, null, null,
                0, 10,
                "prodPrice",
                "asc"
        );

        // prodCreatedAt
        productService.searchAndFilterProducts(
                null, null, null, null,
                0, 10,
                "prodCreatedAt",
                "asc"
        );

        verify(prodRepo, times(3))
                .findAll(
                        any(Specification.class),
                        any(Pageable.class)
                );
    }
}
