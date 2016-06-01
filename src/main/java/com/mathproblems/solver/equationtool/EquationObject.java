package com.mathproblems.solver.equationtool;

import java.util.LinkedHashSet;

public class EquationObject {

    private final String objectTag;
    private final Integer quantity;
    public EquationObject(String objectTag, Integer quantity) {
        this.objectTag = objectTag;
        this.quantity = quantity;
    }

    public String getObjectTag() {
        return objectTag;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
