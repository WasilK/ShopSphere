package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.inventory.InventoryResponse;
import com.wasil.ShopSphere.dto.inventory.RestockRequest;
import com.wasil.ShopSphere.dto.inventory.StockMovementResponse;
import com.wasil.ShopSphere.services.InventoryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @PostMapping("/restock")
    public InventoryResponse restock(
            @Valid @RequestBody RestockRequest request) {
        return inventoryService.restock(request);
    }
    @GetMapping("/{id}")
    public InventoryResponse getInventory(@PathVariable Long id){
        return inventoryService.getProductInventory(id);
    }
    @GetMapping("{id}/stocks")
    public List<StockMovementResponse> getStockMovements(@PathVariable Long id){
        return inventoryService.getStockMovements(id);
    }
}
