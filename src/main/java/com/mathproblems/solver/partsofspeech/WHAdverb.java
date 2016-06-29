package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;

public class WHAdverb implements PartsOfSpeech {

    private int index;
    private String whAdverb;
    private PennPOSTags tag;

    public WHAdverb(final int index, final String whAdverb, String posTag) {
        this.index = index;
        this.whAdverb = whAdverb;
        this.tag = PennPOSTags.valueOf(posTag);
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
        return whAdverb;
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
        return "W";
    }

    public String getDependentWithQuantity() {
        return getDependent();
    }
}
