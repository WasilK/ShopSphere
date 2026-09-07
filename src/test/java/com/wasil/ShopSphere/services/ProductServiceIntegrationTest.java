package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.product.ProductRequest;
import com.wasil.ShopSphere.dto.product.ProductResponse;
import com.wasil.ShopSphere.exceptions.CategoryInactiveException;
import com.wasil.ShopSphere.exceptions.CategoryNotFoundException;
import com.wasil.ShopSphere.exceptions.InvalidPaginationException;
import com.wasil.ShopSphere.exceptions.InvalidPriceRangeException;
import com.wasil.ShopSphere.exceptions.InvalidSortFieldException;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.model.Category;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setUp() {
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // =========================================================
    // ADD PRODUCT
    // =========================================================

    @Test
    void addProduct_shouldSaveProductAndCreateInventory() {

        Category category = new Category();
        category.setCategoryName("Electronics");
        category.setCategoryIsActive(true);

        Category savedCategory = categoryRepository.save(category);

        ProductRequest request = new ProductRequest();
        request.setProdName("iPhone 17");
        request.setProdPrice(new BigDecimal("79999"));
        request.setProdDescription(
                "Latest Apple smartphone with advanced features"
        );
        request.setCategoryId(savedCategory.getCategoryId());

        ProductResponse response = productService.addProduct(request);

        assertNotNull(response);
        assertNotNull(response.getProdId());
        assertEquals("iPhone 17", response.getProdName());

        assertEquals(
                0,
                new BigDecimal("79999")
                        .compareTo(response.getProdPrice())
        );

        assertEquals(
                savedCategory.getCategoryId(),
                response.getCategoryId()
        );

        Product savedProduct = productRepository
                .findById(response.getProdId())
                .orElseThrow();

        assertEquals("iPhone 17", savedProduct.getProdName());

        assertEquals(
                0,
                new BigDecimal("79999")
                        .compareTo(savedProduct.getProdPrice())
        );

        assertTrue(savedProduct.getProdIsActive());

        assertEquals(
                savedCategory.getCategoryId(),
                savedProduct.getCategory().getCategoryId()
        );

        Inventory inventory = inventoryRepository
                .findByProduct(savedProduct)
                .orElseThrow();

        assertEquals(0, inventory.getCurrentStock());

        assertEquals(
                savedProduct.getProdId(),
                inventory.getProduct().getProdId()
        );
    }

    @Test
    void addProduct_shouldThrowException_whenCategoryDoesNotExist() {

        ProductRequest request = new ProductRequest();

        request.setProdName("iPhone 17");
        request.setProdPrice(new BigDecimal("79999"));
        request.setProdDescription(
                "Latest Apple smartphone with advanced features"
        );
        request.setCategoryId(99999L);

        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.addProduct(request)
        );

        assertEquals(0, productRepository.count());
        assertEquals(0, inventoryRepository.count());
    }

    @Test
    void addProduct_shouldThrowException_whenCategoryIsInactive() {

        Category category = new Category();
        category.setCategoryName("Old Electronics");
        category.setCategoryIsActive(false);

        Category savedCategory = categoryRepository.save(category);

        ProductRequest request = new ProductRequest();

        request.setProdName("iPhone 17");
        request.setProdPrice(new BigDecimal("79999"));
        request.setProdDescription(
                "Latest Apple smartphone with advanced features"
        );
        request.setCategoryId(savedCategory.getCategoryId());

        assertThrows(
                CategoryInactiveException.class,
                () -> productService.addProduct(request)
        );

        assertEquals(0, productRepository.count());
        assertEquals(0, inventoryRepository.count());
    }

    // =========================================================
    // GET PRODUCT
    // =========================================================

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {

        Category category = createActiveCategory();

        Product product = new Product();
        product.setProdName("MacBook Air");
        product.setProdPrice(new BigDecimal("99999"));
        product.setProdDescription(
                "Apple laptop with powerful performance"
        );
        product.setProdIsActive(true);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        ProductResponse response =
                productService.getProductById(savedProduct.getProdId());

        assertNotNull(response);

        assertEquals(
                savedProduct.getProdId(),
                response.getProdId()
        );

        assertEquals(
                "MacBook Air",
                response.getProdName()
        );

        assertEquals(
                0,
                new BigDecimal("99999")
                        .compareTo(response.getProdPrice())
        );

        assertEquals(
                category.getCategoryId(),
                response.getCategoryId()
        );

        assertEquals(
                "Electronics",
                response.getCategoryName()
        );
    }

    @Test
    void getProductById_shouldThrowException_whenProductDoesNotExist() {

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById(99999L)
        );
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    @Test
    void updateProduct_shouldUpdateProductSuccessfully() {

        Category oldCategory = createActiveCategory();

        Category newCategory = new Category();
        newCategory.setCategoryName("Laptops");
        newCategory.setCategoryIsActive(true);
        newCategory = categoryRepository.save(newCategory);

        Product product = new Product();
        product.setProdName("MacBook Air");
        product.setProdPrice(new BigDecimal("99999"));
        product.setProdDescription(
                "Apple laptop with powerful performance"
        );
        product.setProdIsActive(true);
        product.setCategory(oldCategory);

        Product savedProduct = productRepository.save(product);

        ProductRequest request = new ProductRequest();

        request.setProdName("MacBook Pro");
        request.setProdPrice(new BigDecimal("149999"));
        request.setProdDescription(
                "Professional Apple laptop with powerful performance"
        );
        request.setCategoryId(newCategory.getCategoryId());

        ProductResponse response =
                productService.updateProduct(
                        savedProduct.getProdId(),
                        request
                );

        assertNotNull(response);

        assertEquals(
                savedProduct.getProdId(),
                response.getProdId()
        );

        assertEquals(
                "MacBook Pro",
                response.getProdName()
        );

        assertEquals(
                0,
                new BigDecimal("149999")
                        .compareTo(response.getProdPrice())
        );

        assertEquals(
                newCategory.getCategoryId(),
                response.getCategoryId()
        );

        Product updatedProduct = productRepository
                .findById(savedProduct.getProdId())
                .orElseThrow();

        assertEquals(
                "MacBook Pro",
                updatedProduct.getProdName()
        );

        assertEquals(
                0,
                new BigDecimal("149999")
                        .compareTo(updatedProduct.getProdPrice())
        );

        assertEquals(
                newCategory.getCategoryId(),
                updatedProduct.getCategory().getCategoryId()
        );

        assertTrue(updatedProduct.getProdIsActive());
    }

    @Test
    void updateProduct_shouldThrowException_whenProductDoesNotExist() {

        Category category = createActiveCategory();

        ProductRequest request = new ProductRequest();

        request.setProdName("MacBook Pro");
        request.setProdPrice(new BigDecimal("149999"));
        request.setProdDescription(
                "Professional Apple laptop with powerful performance"
        );
        request.setCategoryId(category.getCategoryId());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(
                        99999L,
                        request
                )
        );
    }

    @Test
    void updateProduct_shouldThrowException_whenCategoryDoesNotExist() {

        Category category = createActiveCategory();

        Product product = new Product();
        product.setProdName("MacBook Air");
        product.setProdPrice(new BigDecimal("99999"));
        product.setProdDescription(
                "Apple laptop with powerful performance"
        );
        product.setProdIsActive(true);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        ProductRequest request = new ProductRequest();

        request.setProdName("MacBook Pro");
        request.setProdPrice(new BigDecimal("149999"));
        request.setProdDescription(
                "Professional Apple laptop with powerful performance"
        );
        request.setCategoryId(99999L);

        assertThrows(
                CategoryNotFoundException.class,
                () -> productService.updateProduct(
                        savedProduct.getProdId(),
                        request
                )
        );

        Product unchangedProduct = productRepository
                .findById(savedProduct.getProdId())
                .orElseThrow();

        assertEquals(
                "MacBook Air",
                unchangedProduct.getProdName()
        );
    }

    // =========================================================
    // DELETE PRODUCT
    // =========================================================

    @Test
    void deleteProduct_shouldSoftDeleteProduct() {

        Category category = createActiveCategory();

        Product product = new Product();
        product.setProdName("Old Phone");
        product.setProdPrice(new BigDecimal("29999"));
        product.setProdDescription(
                "Older smartphone with useful features"
        );
        product.setProdIsActive(true);
        product.setCategory(category);

        Product savedProduct = productRepository.save(product);

        productService.deleteProduct(savedProduct.getProdId());

        Product deletedProduct = productRepository
                .findById(savedProduct.getProdId())
                .orElseThrow();

        assertFalse(deletedProduct.getProdIsActive());
    }

    @Test
    void deleteProduct_shouldThrowException_whenProductDoesNotExist() {

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProduct(99999L)
        );
    }

    // =========================================================
    // SEARCH / FILTER
    // =========================================================

    @Test
    void searchAndFilterProducts_shouldReturnOnlyActiveProducts() {

        Category category = createActiveCategory();

        Product activeProduct = createProduct(
                "iPhone 17",
                "79999",
                category,
                true
        );

        Product inactiveProduct = createProduct(
                "Old iPhone",
                "49999",
                category,
                false
        );

        Page<ProductResponse> result =
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

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "iPhone 17",
                result.getContent().get(0).getProdName()
        );

        assertTrue(
                result.getContent()
                        .stream()
                        .allMatch(ProductResponse::getProdIsActive)
        );
    }

    @Test
    void searchAndFilterProducts_shouldFilterByName() {

        Category category = createActiveCategory();

        createProduct(
                "iPhone 17",
                "79999",
                category,
                true
        );

        createProduct(
                "Samsung Galaxy",
                "69999",
                category,
                true
        );

        Page<ProductResponse> result =
                productService.searchAndFilterProducts(
                        "iPhone",
                        null,
                        null,
                        null,
                        0,
                        10,
                        "prodName",
                        "asc"
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "iPhone 17",
                result.getContent().get(0).getProdName()
        );
    }

    @Test
    void searchAndFilterProducts_shouldFilterByPriceRange() {

        Category category = createActiveCategory();

        createProduct(
                "Cheap Phone",
                "20000",
                category,
                true
        );

        createProduct(
                "Mid Phone",
                "50000",
                category,
                true
        );

        createProduct(
                "Expensive Phone",
                "100000",
                category,
                true
        );

        Page<ProductResponse> result =
                productService.searchAndFilterProducts(
                        null,
                        new BigDecimal("30000"),
                        new BigDecimal("80000"),
                        null,
                        0,
                        10,
                        "prodPrice",
                        "asc"
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "Mid Phone",
                result.getContent().get(0).getProdName()
        );
    }

    @Test
    void searchAndFilterProducts_shouldFilterByCategory() {

        Category electronics = createActiveCategory();

        Category clothing = new Category();
        clothing.setCategoryName("Clothing");
        clothing.setCategoryIsActive(true);
        clothing = categoryRepository.save(clothing);

        createProduct(
                "iPhone 17",
                "79999",
                electronics,
                true
        );

        createProduct(
                "T-Shirt",
                "1999",
                clothing,
                true
        );

        Page<ProductResponse> result =
                productService.searchAndFilterProducts(
                        null,
                        null,
                        null,
                        clothing.getCategoryId(),
                        0,
                        10,
                        "prodName",
                        "asc"
                );

        assertEquals(1, result.getTotalElements());

        assertEquals(
                "T-Shirt",
                result.getContent().get(0).getProdName()
        );
    }

    // =========================================================
    // VALIDATION / ERROR CASES
    // =========================================================

    @Test
    void searchAndFilterProducts_shouldThrowException_whenPageIsNegative() {

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
    }

    @Test
    void searchAndFilterProducts_shouldThrowException_whenPageSizeIsInvalid() {

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
    }

    @Test
    void searchAndFilterProducts_shouldThrowException_whenMinPriceGreaterThanMaxPrice() {

        assertThrows(
                InvalidPriceRangeException.class,
                () -> productService.searchAndFilterProducts(
                        null,
                        new BigDecimal("100000"),
                        new BigDecimal("50000"),
                        null,
                        0,
                        10,
                        "prodName",
                        "asc"
                )
        );
    }

    @Test
    void searchAndFilterProducts_shouldThrowException_whenSortFieldIsInvalid() {

        assertThrows(
                InvalidSortFieldException.class,
                () -> productService.searchAndFilterProducts(
                        null,
                        null,
                        null,
                        null,
                        0,
                        10,
                        "invalidField",
                        "asc"
                )
        );
    }

    // =========================================================
    // TEST DATA HELPERS
    // =========================================================

    private Category createActiveCategory() {

        Category category = new Category();

        category.setCategoryName("Electronics");
        category.setCategoryIsActive(true);

        return categoryRepository.save(category);
    }

    private Product createProduct(
            String name,
            String price,
            Category category,
            boolean active
    ) {

        Product product = new Product();

        product.setProdName(name);
        product.setProdPrice(new BigDecimal(price));
        product.setProdDescription(
                "Product with enough description for testing"
        );
        product.setProdIsActive(active);
        product.setCategory(category);

        return productRepository.save(product);
    }
}