package com.smartcanteen.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Integer item_id;

    @NotBlank(message = "Item name is required")
    @Size(max = 100, message = "Item name must be at most 100 characters")
    @Column(name = "item_name", nullable = false, length = 100)
    private String item_name;

    @NotBlank(message = "Category is required")
    @Column(name = "category", nullable = false, length = 10)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.50", message = "Price must be at least 0.50")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    public MenuItem() {
    }

    public MenuItem(String item_name, String category, BigDecimal price, Integer stock, Boolean active) {
        this.item_name = item_name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.active = active;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public void setItem_id(Integer item_id) {
        this.item_id = item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
