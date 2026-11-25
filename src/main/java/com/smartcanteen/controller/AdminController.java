package com.smartcanteen.controller;

import com.smartcanteen.model.MenuItem;
import com.smartcanteen.service.MenuService;
import com.smartcanteen.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MenuService menuService;
    private final OrderService orderService;

    public AdminController(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        LocalDate today = LocalDate.now();
        BigDecimal todayRevenue = orderService.calculateTotalRevenue(today, today);
        long todayOrders = orderService.countCompletedOrActiveOrders(today, today);
        BigDecimal totalRevenue = orderService.calculateTotalRevenue(null, null);
        long totalOrders = orderService.countCompletedOrActiveOrders(null, null);
        var menuItems = menuService.getAllMenuItemsForAdmin();
        long activeMenuCount = menuItems.stream().filter(i -> Boolean.TRUE.equals(i.getActive())).count();

        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("todayOrders", todayOrders);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("activeMenuCount", activeMenuCount);
        model.addAttribute("totalMenuCount", menuItems.size());
        model.addAttribute("popularItems", orderService.getPopularItems(today, today));
        return "admin/dashboard";
    }

    @GetMapping("/menu")
    public String menuManagement(@RequestParam(value = "editId", required = false) Integer editId, Model model) {
        MenuItem formItem = new MenuItem();
        formItem.setActive(true);
        formItem.setStock(20);
        formItem.setCategory("VEG");

        if (editId != null) {
            try {
                formItem = menuService.getMenuItemById(editId);
                model.addAttribute("isEditing", true);
            } catch (IllegalArgumentException ex) {
                model.addAttribute("errorMessage", ex.getMessage());
                model.addAttribute("isEditing", false);
            }
        } else {
            model.addAttribute("isEditing", false);
        }

        model.addAttribute("menuItemForm", formItem);
        model.addAttribute("menuItems", menuService.getAllMenuItemsForAdmin());
        return "admin/menu";
    }

    @PostMapping("/menu/save")
    public String saveMenuItem(@RequestParam(value = "item_id", required = false) Integer itemId,
                               @RequestParam("item_name") String itemName,
                               @RequestParam("category") String category,
                               @RequestParam("price") BigDecimal price,
                               @RequestParam("stock") Integer stock,
                               @RequestParam(value = "active", defaultValue = "true") Boolean active,
                               RedirectAttributes redirectAttributes) {
        try {
            MenuItem item = new MenuItem();
            item.setItem_id(itemId);
            item.setItem_name(itemName);
            item.setCategory(category);
            item.setPrice(price);
            item.setStock(stock);
            item.setActive(active);

            MenuItem saved = menuService.saveMenuItem(item);
            redirectAttributes.addFlashAttribute("successMessage",
                    (itemId == null ? "Added menu item: " : "Updated menu item: ") + saved.getItem_name());
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            if (itemId != null) {
                return "redirect:/admin/menu?editId=" + itemId;
            }
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/menu/{id}/delete")
    public String softDeleteMenuItem(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            menuService.softDeleteMenuItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item marked as inactive (soft-deleted).");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/menu/{id}/restore")
    public String restoreMenuItem(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            menuService.restoreMenuItem(id);
            redirectAttributes.addFlashAttribute("successMessage", "Menu item restored to active menu.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/menu";
    }

    @PostMapping("/menu/{id}/toggle-category")
    public String toggleCategory(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            menuService.toggleCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Toggled VEG / NON-VEG category.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/menu";
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(value = "startDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                          @RequestParam(value = "endDate", required = false)
                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                          Model model) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            LocalDate tmp = startDate;
            startDate = endDate;
            endDate = tmp;
            model.addAttribute("errorMessage", "End date was earlier than start date; dates were swapped.");
        }

        BigDecimal totalRevenue = orderService.calculateTotalRevenue(startDate, endDate);
        long orderCount = orderService.countCompletedOrActiveOrders(startDate, endDate);
        var popularItems = orderService.getPopularItems(startDate, endDate);
        var salesOrders = orderService.getSalesOrders(startDate, endDate);

        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("popularItems", popularItems);
        model.addAttribute("salesOrders", salesOrders);
        return "admin/reports";
    }
}
