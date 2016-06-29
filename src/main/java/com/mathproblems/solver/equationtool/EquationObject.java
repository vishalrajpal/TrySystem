package com.mathproblems.solver.equationtool;

import java.util.LinkedHashSet;

public class EquationObject {

    private final String objectTag;
    private final Double quantity;
    public EquationObject(String objectTag, Double quantity) {
        this.objectTag = objectTag;
        this.quantity = quantity;
    }

    public String getObjectTag() {
        return objectTag;
    }

    public Double getQuantity() {
        return quantity;
    }
}
