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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    public InventoryService(InventoryRepository inventoryRepository, StockMovementRepository stockMovementRepository, ProductRepository productRepository) {
        this.inventoryRepository = inventoryRepository;
        this.stockMovementRepository = stockMovementRepository;
        this.productRepository = productRepository;
    }
    @Transactional
    public InventoryResponse restock(RestockRequest restockRequest) {
        Product product = productRepository.findById(restockRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + restockRequest.getProductId()));
        Inventory inventory = inventoryRepository.findByProduct(product).orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + product.getProdName()));
        inventory.setCurrentStock(inventory.getCurrentStock() + restockRequest.getQuantity());
        inventoryRepository.save(inventory);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setInventory(inventory);
        stockMovement.setQuantity(restockRequest.getQuantity());
        stockMovement.setMovementType(MovementType.RESTOCK);
        stockMovementRepository.save(stockMovement);

        return convertToResponse(inventory);
    }

    public InventoryResponse getProductInventory(Long prodId){
        Product product = productRepository.findById(prodId).orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + prodId));
        Inventory inventory = inventoryRepository.findByProduct(product).orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + prodId));
        return convertToResponse(inventory);
    }
    public List<StockMovementResponse> getStockMovements(Long inventoryId){
        Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow(() -> new InventoryNotFoundException("Inventory not found for id: " + inventoryId));
        List<StockMovement> stms = stockMovementRepository.findByInventory(inventory);
        return stms.stream().map(this::convertToResponse).toList();
    }
    private InventoryResponse convertToResponse(Inventory inventory) {

        InventoryResponse response = new InventoryResponse();

        response.setInventoryId(inventory.getInventoryId());
        response.setProductId(inventory.getProduct().getProdId());
        response.setCurrentStock(inventory.getCurrentStock());
        response.setUpdatedAt(inventory.getUpdatedAt());

        return response;
    }

    private StockMovementResponse convertToResponse(StockMovement stm) {
        StockMovementResponse response = new StockMovementResponse();
        response.setStockId(stm.getStockId());
        response.setQuantity(stm.getQuantity());
        response.setMovementType(stm.getMovementType());
        response.setCreatedAt(stm.getCreatedAt());
        return response;
    }
}
