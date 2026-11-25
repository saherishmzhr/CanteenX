package com.smartcanteen.model;

import java.math.BigDecimal;

public class PopularItemReport {
    private final Integer item_id;
    private final String item_name;
    private final long total_quantity;
    private final BigDecimal total_revenue;

    public PopularItemReport(Integer item_id, String item_name, long total_quantity, BigDecimal total_revenue) {
        this.item_id = item_id;
        this.item_name = item_name;
        this.total_quantity = total_quantity;
        this.total_revenue = total_revenue != null ? total_revenue : BigDecimal.ZERO;
    }

    public Integer getItem_id() {
        return item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public long getTotal_quantity() {
        return total_quantity;
    }

    public BigDecimal getTotal_revenue() {
        return total_revenue;
    }
}
