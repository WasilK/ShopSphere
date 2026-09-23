package com.wasil.ShopSphere.controller;

import com.wasil.ShopSphere.dto.inventory.InventoryResponse;
import com.wasil.ShopSphere.dto.inventory.RestockRequest;
import com.wasil.ShopSphere.dto.inventory.StockMovementResponse;
import com.wasil.ShopSphere.services.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Tag(name = "Inventory API's", description = "All operations are admin only.")
@RestController
@RequestMapping("/inventory")
public class InventoryController {
    private final InventoryService inventoryService;
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    @Operation(summary = "To restock a product using product id.")
    @PostMapping("/restock")
    public InventoryResponse restock(
            @Valid @RequestBody RestockRequest request) {
        return inventoryService.restock(request);
    }
    @Operation(summary = "Gets the inventory of a product using product id.")
    @GetMapping("/{id}")
    public InventoryResponse getInventory(@PathVariable Long id){
        return inventoryService.getProductInventory(id);
    }
    @Operation(summary = "Gets the stock movements of a products using product id.")
    @GetMapping("{id}/stocks")
    public List<StockMovementResponse> getStockMovements(@PathVariable Long id){
        return inventoryService.getStockMovements(id);
    }
}
