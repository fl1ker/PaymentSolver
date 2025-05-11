package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentMethod {
    @JsonProperty("id")
    private String id;

    @JsonProperty("discount")
    private int discount;

    @JsonProperty("limit")
    private double limit;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }
}