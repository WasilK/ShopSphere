package com.wasil.ShopSphere.services;

import com.wasil.ShopSphere.dto.inventory.InventoryResponse;
import com.wasil.ShopSphere.dto.inventory.RestockRequest;
import com.wasil.ShopSphere.dto.inventory.StockMovementResponse;
import com.wasil.ShopSphere.exceptions.InventoryNotFoundException;
import com.wasil.ShopSphere.exceptions.ProductNotFoundException;
import com.wasil.ShopSphere.model.Inventory;
import com.wasil.ShopSphere.model.MovementType;
import com.wasil.ShopSphere.model.Product;
import com.wasil.ShopSphere.model.StockMovement;
import com.wasil.ShopSphere.repositories.InventoryRepository;
import com.wasil.ShopSphere.repositories.ProductRepository;
import com.wasil.ShopSphere.repositories.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Inventory inventory;
    private RestockRequest restockRequest;
    private StockMovement stockMovement;


    @BeforeEach
    void setUp() {

        product = new Product();
        product.setProdId(1L);
        product.setProdName("Laptop");

        inventory = new Inventory();
        inventory.setInventoryId(10L);
        inventory.setProduct(product);
        inventory.setCurrentStock(50);

        restockRequest = new RestockRequest();
        restockRequest.setProductId(1L);
        restockRequest.setQuantity(20);

        stockMovement = new StockMovement();
        stockMovement.setStockId(100L);
        stockMovement.setInventory(inventory);
        stockMovement.setQuantity(20);
        stockMovement.setMovementType(MovementType.RESTOCK);
    }


    // =========================================================
    // RESTOCK
    // =========================================================

    @Test
    void shouldRestockInventorySuccessfully() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        when(inventoryRepository.save(any(Inventory.class)))
                .thenReturn(inventory);

        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenReturn(stockMovement);

        // Act
        InventoryResponse response =
                inventoryService.restock(restockRequest);

        // Assert
        assertNotNull(response);

        assertEquals(10L, response.getInventoryId());
        assertEquals(1L, response.getProductId());

        // Original stock = 50
        // Restock quantity = 20
        // New stock = 70
        assertEquals(70, response.getCurrentStock());

        verify(productRepository).findById(1L);
        verify(inventoryRepository).findByProduct(product);
        verify(inventoryRepository).save(inventory);
        verify(stockMovementRepository).save(any(StockMovement.class));
    }


    @Test
    void shouldThrowExceptionWhenRestockProductDoesNotExist() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> inventoryService.restock(restockRequest)
                );

        assertEquals(
                "Product not found with id: 1",
                exception.getMessage()
        );

        verify(productRepository).findById(1L);

        verify(inventoryRepository, never())
                .findByProduct(any(Product.class));

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(stockMovementRepository, never())
                .save(any(StockMovement.class));
    }


    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExistDuringRestock() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        // Act & Assert
        InventoryNotFoundException exception =
                assertThrows(
                        InventoryNotFoundException.class,
                        () -> inventoryService.restock(restockRequest)
                );

        assertEquals(
                "Inventory not found for product: Laptop",
                exception.getMessage()
        );

        verify(productRepository).findById(1L);
        verify(inventoryRepository).findByProduct(product);

        verify(inventoryRepository, never())
                .save(any(Inventory.class));

        verify(stockMovementRepository, never())
                .save(any(StockMovement.class));
    }


    // =========================================================
    // GET PRODUCT INVENTORY
    // =========================================================

    @Test
    void shouldGetProductInventorySuccessfully() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.of(inventory));

        // Act
        InventoryResponse response =
                inventoryService.getProductInventory(1L);

        // Assert
        assertNotNull(response);

        assertEquals(10L, response.getInventoryId());
        assertEquals(1L, response.getProductId());
        assertEquals(50, response.getCurrentStock());

        verify(productRepository).findById(1L);
        verify(inventoryRepository).findByProduct(product);
    }


    @Test
    void shouldThrowExceptionWhenGettingInventoryForNonExistingProduct() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> inventoryService.getProductInventory(1L)
                );

        assertEquals(
                "Product not found with id: 1",
                exception.getMessage()
        );

        verify(productRepository).findById(1L);

        verify(inventoryRepository, never())
                .findByProduct(any(Product.class));
    }


    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExistForProduct() {

        // Arrange
        when(productRepository.findById(1L))
                .thenReturn(Optional.of(product));

        when(inventoryRepository.findByProduct(product))
                .thenReturn(Optional.empty());

        // Act & Assert
        InventoryNotFoundException exception =
                assertThrows(
                        InventoryNotFoundException.class,
                        () -> inventoryService.getProductInventory(1L)
                );

        assertEquals(
                "Inventory not found for product: 1",
                exception.getMessage()
        );

        verify(productRepository).findById(1L);
        verify(inventoryRepository).findByProduct(product);
    }


    // =========================================================
    // GET STOCK MOVEMENTS
    // =========================================================

    @Test
    void shouldGetStockMovementsSuccessfully() {

        // Arrange
        when(inventoryRepository.findById(10L))
                .thenReturn(Optional.of(inventory));

        when(stockMovementRepository.findByInventory(inventory))
                .thenReturn(List.of(stockMovement));

        // Act
        List<StockMovementResponse> responses =
                inventoryService.getStockMovements(10L);

        // Assert
        assertNotNull(responses);
        assertEquals(1, responses.size());

        StockMovementResponse response = responses.get(0);

        assertEquals(100L, response.getStockId());
        assertEquals(20, response.getQuantity());
        assertEquals(
                MovementType.RESTOCK,
                response.getMovementType()
        );

        verify(inventoryRepository).findById(10L);
        verify(stockMovementRepository)
                .findByInventory(inventory);
    }


    @Test
    void shouldReturnEmptyListWhenNoStockMovementsExist() {

        // Arrange
        when(inventoryRepository.findById(10L))
                .thenReturn(Optional.of(inventory));

        when(stockMovementRepository.findByInventory(inventory))
                .thenReturn(List.of());

        // Act
        List<StockMovementResponse> responses =
                inventoryService.getStockMovements(10L);

        // Assert
        assertNotNull(responses);
        assertTrue(responses.isEmpty());

        verify(inventoryRepository).findById(10L);
        verify(stockMovementRepository)
                .findByInventory(inventory);
    }


    @Test
    void shouldThrowExceptionWhenInventoryDoesNotExistForStockMovements() {

        // Arrange
        when(inventoryRepository.findById(10L))
                .thenReturn(Optional.empty());

        // Act & Assert
        InventoryNotFoundException exception =
                assertThrows(
                        InventoryNotFoundException.class,
                        () -> inventoryService.getStockMovements(10L)
                );

        assertEquals(
                "Inventory not found for id: 10",
                exception.getMessage()
        );

        verify(inventoryRepository).findById(10L);

        verify(stockMovementRepository, never())
                .findByInventory(any(Inventory.class));
    }
}
