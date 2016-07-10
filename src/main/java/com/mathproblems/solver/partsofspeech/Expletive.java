package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;

import java.util.HashSet;
import java.util.Set;

public class Expletive implements PartsOfSpeech {
    private int index;
    private String expletive;
    private PennPOSTags tag;

    public Expletive(final int index, final String expletive, final String posTag) {
        this.index = index;
        this.expletive = expletive;
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
        return expletive;
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
        return "E";
    }

    public String getDependentWithQuantity() {
        return getDependent();
    }

    public Set<Integer> getIndices() {
        return new HashSet<>();
    }
}
