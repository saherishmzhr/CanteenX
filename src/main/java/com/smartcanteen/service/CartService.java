package com.smartcanteen.service;

import com.smartcanteen.model.CartItem;
import com.smartcanteen.model.MenuItem;
import com.smartcanteen.repository.MenuItemRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class CartService {

    public static final String CART_SESSION_KEY = "CANTEEN_CART";

    private final MenuItemRepository menuItemRepository;

    public CartService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    @SuppressWarnings("unchecked")
    public Map<Integer, CartItem> getCartMap(HttpSession session) {
        Object attr = session.getAttribute(CART_SESSION_KEY);
        if (attr instanceof Map<?, ?>) {
            return (Map<Integer, CartItem>) attr;
        }
        Map<Integer, CartItem> freshCart = new LinkedHashMap<>();
        session.setAttribute(CART_SESSION_KEY, freshCart);
        return freshCart;
    }

    public List<CartItem> getCartItems(HttpSession session) {
        return new ArrayList<>(getCartMap(session).values());
    }

    public int getTotalItemCount(HttpSession session) {
        return getCartMap(session).values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public BigDecimal getCartTotal(HttpSession session) {
        return getCartMap(session).values().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(HttpSession session, Integer itemId, int quantityToAdd) {
        if (quantityToAdd <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found."));

        if (!Boolean.TRUE.equals(menuItem.getActive())) {
            throw new IllegalArgumentException("'" + menuItem.getItem_name() + "' is currently unavailable.");
        }

        Map<Integer, CartItem> cart = getCartMap(session);
        CartItem existing = cart.get(itemId);
        int currentQty = (existing != null) ? existing.getQuantity() : 0;
        int desiredQty = currentQty + quantityToAdd;

        if (desiredQty > menuItem.getStock()) {
            throw new IllegalArgumentException(
                "Only " + menuItem.getStock() + " left in stock for '" + menuItem.getItem_name() + "'."
            );
        }

        if (existing != null) {
            existing.setQuantity(desiredQty);
            existing.setUnit_price(menuItem.getPrice());
            existing.setItem_name(menuItem.getItem_name());
            existing.setCategory(menuItem.getCategory());
        } else {
            cart.put(itemId, new CartItem(
                    menuItem.getItem_id(),
                    menuItem.getItem_name(),
                    menuItem.getCategory(),
                    menuItem.getPrice(),
                    desiredQty
            ));
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateQuantity(HttpSession session, Integer itemId, int quantity) {
        Map<Integer, CartItem> cart = getCartMap(session);
        if (!cart.containsKey(itemId)) {
            return;
        }
        if (quantity <= 0) {
            cart.remove(itemId);
            session.setAttribute(CART_SESSION_KEY, cart);
            return;
        }

        MenuItem menuItem = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found."));

        if (!Boolean.TRUE.equals(menuItem.getActive())) {
            cart.remove(itemId);
            session.setAttribute(CART_SESSION_KEY, cart);
            throw new IllegalArgumentException("'" + menuItem.getItem_name() + "' is no longer active and was removed.");
        }

        if (quantity > menuItem.getStock()) {
            throw new IllegalArgumentException(
                "Cannot set quantity to " + quantity + "; only " + menuItem.getStock()
                    + " available for '" + menuItem.getItem_name() + "'."
            );
        }

        CartItem item = cart.get(itemId);
        item.setQuantity(quantity);
        item.setUnit_price(menuItem.getPrice());
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeItem(HttpSession session, Integer itemId) {
        Map<Integer, CartItem> cart = getCartMap(session);
        cart.remove(itemId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }
}
