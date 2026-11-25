package com.smartcanteen.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
    private Integer item_id;
    private String item_name;
    private String category;
    private BigDecimal unit_price;
    private int quantity;

    public CartItem() {
    }

    public CartItem(Integer item_id, String item_name, String category, BigDecimal unit_price, int quantity) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.category = category;
        this.unit_price = unit_price;
        this.quantity = quantity;
    }

    public BigDecimal getSubtotal() {
        if (unit_price == null) {
            return BigDecimal.ZERO;
        }
        return unit_price.multiply(BigDecimal.valueOf(quantity));
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

    public BigDecimal getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(BigDecimal unit_price) {
        this.unit_price = unit_price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
