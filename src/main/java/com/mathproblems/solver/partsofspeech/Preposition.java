package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;
import com.mathproblems.solver.PennRelation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Preposition implements PartsOfSpeech {

    private final String dependent;
    private final PennRelation relation;
    private final TypedDependency dependency;
    private final int dependentIndex;
    private final int governerIndex;
    private final String governer;
    private final Noun attachedNoun;
    private final String prepositionLabel;
    public Preposition(TypedDependency dependency, Collection<Noun> nouns) {
        //this.dependent = dependency.dep().originalText();
        this.dependent = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
        this.dependentIndex = dependency.dep().index();
        //this.governer = dependency.gov().originalText();
        this.governer = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
        this.governerIndex = dependency.gov().index();
        this.dependency = dependency;
        this.relation = PennRelation.valueOfPennRelation(dependency.reln().getShortName());
        this.prepositionLabel = dependency.dep().getString(CoreAnnotations.PartOfSpeechAnnotation.class);
        this.attachedNoun = findAttachedNoun(nouns);
    }

    private Noun findAttachedNoun(Collection<Noun> nouns) {
        Noun attachedNoun = null;
        for(Noun n: nouns) {
            if(n.getRelatedNouns().containsKey(this.getGoverner()) && n.getRelatedNouns().containsValue(this.governerIndex)) {
                attachedNoun = n;
                n.associatePreposition(this);
                break;
            }
        }
        return attachedNoun;
    }

    @Override
    public int getGovernerIndex() {
        return governerIndex;
    }

    @Override
    public int getDependentIndex() {
        return dependentIndex;
    }

    @Override
    public String getGoverner() {
        return governer;
    }

    @Override
    public String getDependent() {
        return dependent;
    }

    @Override
    public double getQuantity() {
        return 0;
    }

    @Override
    public PennPOSTags getTag() {
        return PennPOSTags.PREP;
    }

    @Override
    public String getGramletCharacter() {
        return "P";
    }

    public String getDependentWithQuantity() {
        return getDependent();
    }

    public Set<Integer> getIndices() {
        return new HashSet<>();
    }
}
