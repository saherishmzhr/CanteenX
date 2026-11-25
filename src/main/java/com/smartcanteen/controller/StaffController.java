package com.smartcanteen.controller;

import com.smartcanteen.model.OrderStatus;
import com.smartcanteen.service.OrderService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/staff")
public class StaffController {

    private final OrderService orderService;

    public StaffController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String ordersDashboard(@RequestParam(value = "scope", defaultValue = "TODAY") String scope,
                                  @RequestParam(value = "status", defaultValue = "ALL") String status,
                                  Model model) {
        boolean todayOnly = !"ALL".equalsIgnoreCase(scope);
        model.addAttribute("orders", orderService.getOrdersForStaff(todayOnly, status));
        model.addAttribute("selectedScope", todayOnly ? "TODAY" : "ALL");
        model.addAttribute("selectedStatus", status.toUpperCase());
        model.addAttribute("allStatuses", OrderStatus.values());
        return "staff/orders";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateStatus(@PathVariable("orderId") Integer orderId,
                               @RequestParam("status") String status,
                               @RequestParam(value = "scope", defaultValue = "TODAY") String scope,
                               @RequestParam(value = "statusFilter", defaultValue = "ALL") String statusFilter,
                               RedirectAttributes redirectAttributes) {
        try {
            var updated = orderService.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + updated.getOrder_id() + " (Token #" + updated.getToken_no()
                            + ") updated to " + updated.getStatus() + ".");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/orders?scope=" + scope + "&status=" + statusFilter;
    }

    @PostMapping("/orders/{orderId}/pay")
    public String markPaid(@PathVariable("orderId") Integer orderId,
                           @RequestParam(value = "scope", defaultValue = "TODAY") String scope,
                           @RequestParam(value = "statusFilter", defaultValue = "ALL") String statusFilter,
                           RedirectAttributes redirectAttributes) {
        try {
            var updated = orderService.markOrderPaid(orderId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment marked as PAID for Order #" + updated.getOrder_id()
                            + " (Token #" + updated.getToken_no() + ").");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/staff/orders?scope=" + scope + "&status=" + statusFilter;
    }
}
