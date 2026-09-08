package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.inventory.InventoryResponse;
import com.wasil.ShopSphere.dto.inventory.RestockRequest;
import com.wasil.ShopSphere.dto.inventory.StockMovementResponse;
import com.wasil.ShopSphere.exceptions.InventoryNotFoundException;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.model.Category;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.MovementType;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.StockMovement;
import com.wasil.ShopSphere.repositories.CategoryRepository;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import com.wasil.ShopSphere.repositories.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InventoryServiceIntegrationTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    @BeforeEach
    void setUp() {
        stockMovementRepository.deleteAll();
        inventoryRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }


    // =========================================================
    // RESTOCK
    // =========================================================

    @Test
    void restock_shouldIncreaseStockAndCreateMovement() {

        Product product = createProduct();

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setCurrentStock(10);

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        RestockRequest request = new RestockRequest();
        request.setProductId(product.getProdId());
        request.setQuantity(5);

        InventoryResponse response =
                inventoryService.restock(request);

        // Verify response
        assertNotNull(response);

        assertEquals(
                savedInventory.getInventoryId(),
                response.getInventoryId()
        );

        assertEquals(
                product.getProdId(),
                response.getProductId()
        );

        assertEquals(
                15,
                response.getCurrentStock()
        );

        // Verify actual database inventory
        Inventory updatedInventory =
                inventoryRepository
                        .findById(savedInventory.getInventoryId())
                        .orElseThrow();

        assertEquals(
                15,
                updatedInventory.getCurrentStock()
        );

        // Verify stock movement was created
        List<StockMovement> movements =
                stockMovementRepository
                        .findByInventory(updatedInventory);

        assertEquals(1, movements.size());

        StockMovement movement = movements.get(0);

        assertEquals(5, movement.getQuantity());

        assertEquals(
                MovementType.RESTOCK,
                movement.getMovementType()
        );

        assertEquals(
                updatedInventory.getInventoryId(),
                movement.getInventory().getInventoryId()
        );
    }


    @Test
    void restock_shouldThrowException_whenProductDoesNotExist() {

        RestockRequest request = new RestockRequest();

        request.setProductId(99999L);
        request.setQuantity(5);

        assertThrows(
                ProductNotFoundException.class,
                () -> inventoryService.restock(request)
        );

        assertEquals(
                0,
                inventoryRepository.count()
        );

        assertEquals(
                0,
                stockMovementRepository.count()
        );
    }


    @Test
    void restock_shouldThrowException_whenInventoryDoesNotExist() {

        Product product = createProduct();

        RestockRequest request = new RestockRequest();

        request.setProductId(product.getProdId());
        request.setQuantity(5);

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.restock(request)
        );

        assertEquals(
                0,
                inventoryRepository.count()
        );

        assertEquals(
                0,
                stockMovementRepository.count()
        );
    }


    // =========================================================
    // GET PRODUCT INVENTORY
    // =========================================================

    @Test
    void getProductInventory_shouldReturnInventory() {

        Product product = createProduct();

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setCurrentStock(25);

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        InventoryResponse response =
                inventoryService.getProductInventory(
                        product.getProdId()
                );

        assertNotNull(response);

        assertEquals(
                savedInventory.getInventoryId(),
                response.getInventoryId()
        );

        assertEquals(
                product.getProdId(),
                response.getProductId()
        );

        assertEquals(
                25,
                response.getCurrentStock()
        );
    }


    @Test
    void getProductInventory_shouldThrowException_whenProductDoesNotExist() {

        assertThrows(
                ProductNotFoundException.class,
                () -> inventoryService.getProductInventory(99999L)
        );
    }


    @Test
    void getProductInventory_shouldThrowException_whenInventoryDoesNotExist() {

        Product product = createProduct();

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.getProductInventory(
                        product.getProdId()
                )
        );
    }


    // =========================================================
    // GET STOCK MOVEMENTS
    // =========================================================

    @Test
    void getStockMovements_shouldReturnMovements() {

        Product product = createProduct();

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setCurrentStock(20);

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        StockMovement movement1 = new StockMovement();
        movement1.setInventory(savedInventory);
        movement1.setQuantity(10);
        movement1.setMovementType(MovementType.RESTOCK);

        StockMovement movement2 = new StockMovement();
        movement2.setInventory(savedInventory);
        movement2.setQuantity(5);
        movement2.setMovementType(MovementType.RESTOCK);

        stockMovementRepository.save(movement1);
        stockMovementRepository.save(movement2);

        List<StockMovementResponse> responses =
                inventoryService.getStockMovements(
                        savedInventory.getInventoryId()
                );

        assertNotNull(responses);

        assertEquals(2, responses.size());

        assertEquals(
                10,
                responses.get(0).getQuantity()
        );

        assertEquals(
                MovementType.RESTOCK,
                responses.get(0).getMovementType()
        );

        assertEquals(
                5,
                responses.get(1).getQuantity()
        );

        assertEquals(
                MovementType.RESTOCK,
                responses.get(1).getMovementType()
        );
    }


    @Test
    void getStockMovements_shouldReturnEmptyList_whenNoMovementsExist() {

        Product product = createProduct();

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setCurrentStock(0);

        Inventory savedInventory =
                inventoryRepository.save(inventory);

        List<StockMovementResponse> responses =
                inventoryService.getStockMovements(
                        savedInventory.getInventoryId()
                );

        assertNotNull(responses);

        assertTrue(responses.isEmpty());
    }


    @Test
    void getStockMovements_shouldThrowException_whenInventoryDoesNotExist() {

        assertThrows(
                InventoryNotFoundException.class,
                () -> inventoryService.getStockMovements(99999L)
        );
    }


    // =========================================================
    // TEST DATA HELPERS
    // =========================================================

    private Product createProduct() {

        Category category = new Category();
        category.setCategoryName("Electronics");
        category.setCategoryIsActive(true);

        Category savedCategory =
                categoryRepository.save(category);

        Product product = new Product();

        product.setProdName("iPhone 17");
        product.setProdPrice(new BigDecimal("79999"));
        product.setProdDescription(
                "Latest Apple smartphone with advanced features"
        );
        product.setProdIsActive(true);
        product.setCategory(savedCategory);

        return productRepository.save(product);
    }
}
