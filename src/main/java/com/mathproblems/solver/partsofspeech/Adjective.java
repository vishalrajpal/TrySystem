package com.mathproblems.solver.partsofspeech;

import java.util.Collection;
import java.util.List;

import com.mathproblems.solver.PennRelation;
import edu.stanford.nlp.trees.TypedDependency;
import com.mathproblems.solver.partsofspeech.PartsOfSpeech;

public class Adjective implements PartsOfSpeech {
	
	private final String dependent;
	private final String adjectiveType;
	private final PennRelation relation;
	private final TypedDependency dependency;
	private final int dependentIndex;
	private final int governerIndex;
	private final String governer;
	private final Noun parentNoun;
	public Adjective (TypedDependency dependency, Collection<Noun> nounList) {
		this.dependency = dependency;
		final String[] currentDependency = dependency.dep().toString().split("/");
		governer = dependency.gov().toString().split("/")[0];
		dependent = currentDependency[0];
		relation = PennRelation.valueOfPennRelation(dependency.reln().toString().split(":")[0]);
		adjectiveType = currentDependency[1];
		dependentIndex = dependency.dep().index();
		governerIndex = dependency.gov().index();
		parentNoun = getParentNoun(nounList);
	}
	
	private Noun getParentNoun(Collection<Noun> nounList) {
		Noun parentNoun = null;
		for(Noun n : nounList) {
			if(n.getDependent().equals(governer) && n.getDependentIndex() == governerIndex) {
				parentNoun = n;
				n.mergeAdjective(this);
				break;
			}
		}
		return parentNoun;
	}
	
	@Override
	public String toString() {
		return "Adjective [dependency=" + dependency + ", word=" + dependent
				+ ", adjectiveType=" + adjectiveType + ", index=" + dependentIndex + "]";
	}

	public int getGovernerIndex() {
		return governerIndex;
	}

	public int getDependentIndex() {
		return dependentIndex;
	}

	public String getGoverner() {
		return governer;
	}

	public String getDependent() {
		return dependent;
	}
	
	public int getQuantity() {
		return 0;
	}
}
