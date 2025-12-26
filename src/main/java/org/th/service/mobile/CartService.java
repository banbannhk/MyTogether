package org.th.service.mobile;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.th.dto.AddToCartRequest;
import org.th.dto.CartDTO;
import org.th.dto.CartItemDTO;
import org.th.dto.UpdateCartItemRequest;
import org.th.entity.Cart;
import org.th.entity.CartItem;
import org.th.entity.User;
import org.th.entity.shops.MenuItem;
import org.th.entity.shops.Shop;
import org.th.exception.ApplicationException;
import org.th.exception.ErrorCode;
import org.th.repository.CartRepository;
import org.th.repository.MenuItemRepository;
import org.th.repository.ShopRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public CartDTO getCart(User user) {
        Cart cart = getOrCreateCart(user);
        return mapToDTO(cart);
    }

    @Transactional
    public CartDTO addToCart(User user, AddToCartRequest request) {
        Cart cart = getOrCreateCart(user);

        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.RESOURCE_NOT_FOUND, "Menu item not found"));

        if (!Boolean.TRUE.equals(menuItem.getIsAvailable())) {
            throw new ApplicationException(ErrorCode.BUSINESS_RULE_VIOLATION, "Item is currently unavailable");
        }

        Shop itemShop = menuItem.getShop();

        // Single Shop Rule Validation
        if (cart.getShop() != null && !cart.getShop().getId().equals(itemShop.getId())) {
            // If cart has items from another shop, verify it actually has items.
            // If items list is empty but shop is set (edge case), we can overwrite.
            if (!cart.getItems().isEmpty()) {
                throw new ApplicationException(ErrorCode.CART_MULTI_SHOP_CONFLICT,
                        "Your cart contains items from " + cart.getShop().getName() +
                                ". Please clear your cart before adding items from " + itemShop.getName());
            }
            // Valid overwrite context (empty cart)
            cart.setShop(itemShop);
        } else if (cart.getShop() == null) {
            cart.setShop(itemShop);
        }

        // Add or Update Item
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            // Optional: update special instructions? Accumulate? Overwrite?
            // For now, let's keep existing instructions or overwrite if new provided?
            // Simple approach: Overwrite if provided, else keep.
            if (request.getSpecialInstructions() != null && !request.getSpecialInstructions().isEmpty()) {
                item.setSpecialInstructions(request.getSpecialInstructions());
            }
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.getQuantity())
                    .specialInstructions(request.getSpecialInstructions())
                    .build();
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    @Transactional
    public CartDTO updateItem(User user, Long cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND, "Item not found in cart"));

        if (request.getQuantity() <= 0) {
            cart.removeItem(item);
            if (cart.getItems().isEmpty()) {
                cart.setShop(null);
            }
        } else {
            item.setQuantity(request.getQuantity());
            if (request.getSpecialInstructions() != null) {
                item.setSpecialInstructions(request.getSpecialInstructions());
            }
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    @Transactional
    public CartDTO removeItem(User user, Long cartItemId) {
        Cart cart = getOrCreateCart(user);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.CART_ITEM_NOT_FOUND, "Item not found in cart"));

        cart.removeItem(item);

        if (cart.getItems().isEmpty()) {
            cart.setShop(null);
        }

        Cart savedCart = cartRepository.save(cart);
        return mapToDTO(savedCart);
    }

    @Transactional
    public void clearCart(User user) {
        Cart cart = getOrCreateCart(user);
        cart.getItems().clear();
        cart.setShop(null);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartDTO mapToDTO(Cart cart) {
        List<CartItemDTO> itemDTOs = cart.getItems().stream()
                .map(this::mapItemToDTO)
                .collect(Collectors.toList());

        BigDecimal subtotal = itemDTOs.stream()
                .map(CartItemDTO::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Placeholder logic for delivery fee
        BigDecimal deliveryFee = BigDecimal.ZERO;

        return CartDTO.builder()
                .id(cart.getId())
                .shopId(cart.getShop() != null ? cart.getShop().getId() : null)
                .shopName(cart.getShop() != null ? cart.getShop().getName() : null)
                .shopImageUrl(cart.getShop() != null ? cart.getShop().getCoverUrl() : null) // Using cover image
                .items(itemDTOs)
                .subtotal(subtotal)
                .deliveryFee(deliveryFee)
                .total(subtotal.add(deliveryFee))
                .totalItems(itemDTOs.stream().mapToInt(CartItemDTO::getQuantity).sum())
                .build();
    }

    private CartItemDTO mapItemToDTO(CartItem item) {
        BigDecimal total = item.getMenuItem().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemDTO.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem().getId())
                .name(item.getMenuItem().getName())
                .price(item.getMenuItem().getPrice())
                .quantity(item.getQuantity())
                .imageUrl(item.getMenuItem().getImageUrl())
                .specialInstructions(item.getSpecialInstructions())
                .itemTotal(total)
                .build();
    }
}
