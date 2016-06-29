package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;

public class Conjunction implements PartsOfSpeech {
    private int index;
    private String conjunction;
    private PennPOSTags tag;

    public Conjunction(final int index, final String conjunction, final String posTag) {
        this.index = index;
        this.conjunction = conjunction;
        this.tag = PennPOSTags.valueOfNullable(posTag);
    }

    @Override
    public int getGovernerIndex() {
        return 0;
    }

    @Override
    public int getDependentIndex() {
        return index;
    }

    @Override
    public String getGoverner() {
        return null;
    }

    @Override
    public String getDependent() {
        return conjunction;
    }

    @Override
    public double getQuantity() {
        return 0;
    }

    @Override
    public PennPOSTags getTag() {
        return tag;
    }

    @Override
    public String getGramletCharacter() {
        return "C";
    }

    public String getDependentWithQuantity() {
        return getDependent();
    }
}
