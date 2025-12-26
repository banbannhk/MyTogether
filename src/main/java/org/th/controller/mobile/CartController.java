package org.th.controller.mobile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.th.dto.AddToCartRequest;
import org.th.dto.ApiResponse;
import org.th.dto.CartDTO;
import org.th.dto.UpdateCartItemRequest;
import org.th.entity.User;
import org.th.service.mobile.CartService;
import org.th.config.ratelimit.RateLimit;
import org.th.config.ratelimit.RateLimit.Tier;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mobile/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "Shopping cart management APIs")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @RateLimit(tier = Tier.IO_INTENSIVE)
    @Operation(summary = "Get cart", description = "Get current user's shopping cart")
    public ResponseEntity<ApiResponse<CartDTO>> getCart(@AuthenticationPrincipal User user) {
        CartDTO cart = cartService.getCart(user);
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cart));
    }

    @PostMapping("/items")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Add to cart", description = "Add an item to the cart (Single shop rule applies)")
    public ResponseEntity<ApiResponse<CartDTO>> addToCart(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddToCartRequest request) {
        CartDTO cart = cartService.addToCart(user, request);
        return ResponseEntity.ok(ApiResponse.success("Item added to cart", cart));
    }

    @PutMapping("/items/{itemId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Update cart item", description = "Update quantity or instructions of a cart item")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Cart Item ID") @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartDTO cart = cartService.updateItem(user, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", cart));
    }

    @DeleteMapping("/items/{itemId}")
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Remove cart item", description = "Remove an item from the cart")
    public ResponseEntity<ApiResponse<CartDTO>> removeItem(
            @AuthenticationPrincipal User user,
            @Parameter(description = "Cart Item ID") @PathVariable Long itemId) {
        CartDTO cart = cartService.removeItem(user, itemId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart", cart));
    }

    @DeleteMapping
    @RateLimit(tier = Tier.WRITE)
    @Operation(summary = "Clear cart", description = "Clear all items from the cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(@AuthenticationPrincipal User user) {
        cartService.clearCart(user);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
