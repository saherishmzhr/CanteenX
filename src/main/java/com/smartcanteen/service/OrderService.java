package com.smartcanteen.service;

import com.smartcanteen.model.*;
import com.smartcanteen.repository.MenuItemRepository;
import com.smartcanteen.repository.OrderDetailRepository;
import com.smartcanteen.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderDetailRepository orderDetailRepository,
                        MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    public Order placeOrder(Integer userId, List<CartItem> cartItems, PaymentMethod paymentMethod) {
        if (userId == null) {
            throw new IllegalArgumentException("Authenticated user is required to place an order.");
        }
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty. Please add items before checking out.");
        }
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Please select a payment method (CASH or UPI).");
        }

        Order order = new Order();
        order.setUser_id(userId);
        order.setOrder_time(LocalDateTime.now());
        order.setPayment_method(paymentMethod.name());
        order.setPayment_status(PaymentStatus.PAID.name());
        order.setStatus(OrderStatus.PLACED.name());

        int nextToken = generateNextDailyToken(LocalDate.now());
        order.setToken_no(nextToken);

        BigDecimal computedTotal = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            if (cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("Invalid quantity for item '" + cartItem.getItem_name() + "'.");
            }

            MenuItem dbItem = menuItemRepository.findById(cartItem.getItem_id())
                    .orElseThrow(() -> new IllegalArgumentException("Item '" + cartItem.getItem_name() + "' no longer exists."));

            if (!Boolean.TRUE.equals(dbItem.getActive())) {
                throw new IllegalArgumentException("Item '" + dbItem.getItem_name() + "' is currently unavailable.");
            }

            int availableStock = dbItem.getStock() != null ? dbItem.getStock() : 0;
            if (availableStock < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                    "Insufficient stock for '" + dbItem.getItem_name()
                        + "'. Requested: " + cartItem.getQuantity() + ", Available: " + availableStock + "."
                );
            }

            // Decrement stock
            dbItem.setStock(availableStock - cartItem.getQuantity());
            menuItemRepository.save(dbItem);

            BigDecimal unitPrice = dbItem.getPrice();
            BigDecimal lineSubtotal = unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            computedTotal = computedTotal.add(lineSubtotal);

            OrderDetail detail = new OrderDetail();
            detail.setItem_id(dbItem.getItem_id());
            detail.setItem_name(dbItem.getItem_name());
            detail.setQuantity(cartItem.getQuantity());
            detail.setUnit_price(unitPrice);
            detail.setSubtotal(lineSubtotal);

            order.addDetail(detail);
        }

        order.setTotal(computedTotal);
        return orderRepository.save(order);
    }

    public int generateNextDailyToken(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        Integer maxToken = orderRepository.findMaxTokenForRange(startOfDay, endOfDay);
        return (maxToken == null || maxToken < 1) ? 1 : maxToken + 1;
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Integer orderId) {
        return orderRepository.findByIdWithCustomer(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForStudent(Integer userId) {
        return orderRepository.findByUserIdOrderByTimeDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersForStaff(boolean todayOnly, String statusFilter) {
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (todayOnly) {
            LocalDate today = LocalDate.now();
            startTime = today.atStartOfDay();
            endTime = today.plusDays(1).atStartOfDay();
        }
        String normalizedStatus = (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter.trim()))
                ? statusFilter.trim().toUpperCase()
                : null;
        return orderRepository.findFilteredOrders(startTime, endTime, normalizedStatus);
    }

    @Transactional
    public Order updateOrderStatus(Integer orderId, String newStatusStr) {
        Order order = getOrderById(orderId);
        OrderStatus targetStatus;
        try {
            targetStatus = OrderStatus.valueOf(newStatusStr.trim().toUpperCase());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid order status: " + newStatusStr);
        }

        OrderStatus currentStatus = OrderStatus.valueOf(order.getStatus());

        // Restore stock if transitioning into CANCELLED from a non-cancelled state
        if (targetStatus == OrderStatus.CANCELLED && currentStatus != OrderStatus.CANCELLED) {
            for (OrderDetail detail : order.getDetails()) {
                menuItemRepository.findById(detail.getItem_id()).ifPresent(menuItem -> {
                    int currentStock = menuItem.getStock() != null ? menuItem.getStock() : 0;
                    menuItem.setStock(currentStock + detail.getQuantity());
                    menuItemRepository.save(menuItem);
                });
            }
        }

        order.setStatus(targetStatus.name());
        return orderRepository.save(order);
    }

    @Transactional
    public Order markOrderPaid(Integer orderId) {
        Order order = getOrderById(orderId);
        order.setPayment_status(PaymentStatus.PAID.name());
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateTotalRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        List<Order> validOrders = orderRepository.findNonCancelledOrdersInRange(start, end);
        return validOrders.stream()
                .map(Order::getTotal)
                .filter(t -> t != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public long countCompletedOrActiveOrders(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        return orderRepository.findNonCancelledOrdersInRange(start, end).size();
    }

    @Transactional(readOnly = true)
    public List<Order> getSalesOrders(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : LocalDate.of(2000, 1, 1).atStartOfDay();
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        return orderRepository.findNonCancelledOrdersInRange(start, end);
    }

    @Transactional(readOnly = true)
    public List<PopularItemReport> getPopularItems(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;
        return orderDetailRepository.findPopularItems(start, end);
    }
}
