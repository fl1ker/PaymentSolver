package model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Order {
    @JsonProperty("id")
    private String id;

    @JsonProperty("value")
    private double value;

    @JsonProperty("promotions")
    private List<String> promotions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public List<String> getPromotions() {
        return promotions != null ? promotions : List.of();
    }

    public void setPromotions(List<String> promotions) {
        this.promotions = promotions;
    }
}