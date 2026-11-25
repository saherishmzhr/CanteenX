package com.smartcanteen.controller;

import com.smartcanteen.model.Order;
import com.smartcanteen.model.PaymentMethod;
import com.smartcanteen.security.CustomUserDetails;
import com.smartcanteen.service.CartService;
import com.smartcanteen.service.MenuService;
import com.smartcanteen.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final MenuService menuService;
    private final CartService cartService;
    private final OrderService orderService;

    public StudentController(MenuService menuService,
                             CartService cartService,
                             OrderService orderService) {
        this.menuService = menuService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @GetMapping("/menu")
    public String menu(@RequestParam(value = "category", required = false) String category,
                       @RequestParam(value = "search", required = false) String search,
                       HttpSession session,
                       Model model) {
        String activeCategory = (category == null || category.isBlank()) ? "ALL" : category.trim().toUpperCase();
        model.addAttribute("menuItems", menuService.browseMenu(activeCategory, search));
        model.addAttribute("selectedCategory", activeCategory);
        model.addAttribute("searchQuery", search != null ? search.trim() : "");
        model.addAttribute("cartCount", cartService.getTotalItemCount(session));
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        return "student/menu";
    }

    @GetMapping("/cart")
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("cartCount", cartService.getTotalItemCount(session));
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        return "student/cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("itemId") Integer itemId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        try {
            cartService.addItem(session, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Added item to your cart.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        if (redirectUrl != null && redirectUrl.startsWith("/student/")) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/student/menu";
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("itemId") Integer itemId,
                             @RequestParam("quantity") int quantity,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            cartService.updateQuantity(session, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("itemId") Integer itemId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        cartService.removeItem(session, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/student/cart";
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (cartService.getCartItems(session).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty. Add items before checking out.");
            return "redirect:/student/cart";
        }
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("cartCount", cartService.getTotalItemCount(session));
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "student/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam("paymentMethod") String paymentMethodStr,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            if (cartService.getCartItems(session).isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot checkout with an empty cart.");
                return "redirect:/student/cart";
            }

            PaymentMethod paymentMethod;
            try {
                paymentMethod = PaymentMethod.valueOf(paymentMethodStr.trim().toUpperCase());
            } catch (Exception e) {
                throw new IllegalArgumentException("Please select a valid payment method: CASH or UPI.");
            }

            Order savedOrder = orderService.placeOrder(
                    userDetails.getUserId(),
                    cartService.getCartItems(session),
                    paymentMethod
            );
            cartService.clearCart(session);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed! Your token number is #" + savedOrder.getToken_no() + ".");
            return "redirect:/student/orders/" + savedOrder.getOrder_id() + "/confirmation";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/cart";
        }
    }

    @GetMapping("/orders/{orderId}/confirmation")
    public String orderConfirmation(@PathVariable("orderId") Integer orderId,
                                    @AuthenticationPrincipal CustomUserDetails userDetails,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (!order.getUser_id().equals(userDetails.getUserId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You can only view confirmation for your own orders.");
                return "redirect:/student/orders";
            }
            model.addAttribute("order", order);
            model.addAttribute("cartCount", cartService.getTotalItemCount(session));
            return "student/confirmation";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/orders";
        }
    }

    @GetMapping("/orders")
    public String orderHistory(@AuthenticationPrincipal CustomUserDetails userDetails,
                               HttpSession session,
                               Model model) {
        model.addAttribute("orders", orderService.getOrdersForStudent(userDetails.getUserId()));
        model.addAttribute("cartCount", cartService.getTotalItemCount(session));
        return "student/orders";
    }
}
