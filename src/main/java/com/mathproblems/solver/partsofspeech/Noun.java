package com.mathproblems.solver.partsofspeech;

import java.util.*;

import com.mathproblems.solver.PennRelation;
import edu.stanford.nlp.trees.TypedDependency;

public class Noun implements PartsOfSpeech {

	private final String dependent;
	private final String nounType;
	private final PennRelation relation;
	private final TypedDependency dependency;
	private final int dependentIndex;
	private final int governerIndex;
	private final SortedSet<PartsOfSpeech> mergedCompounds;
	private final String governer;
	private int quantity;
	private final Map<String, Integer> relatedNouns;
	final SortedSet<Integer> indices;
	public Noun (TypedDependency dependency) {
		this.dependency = dependency;
		governer = dependency.gov().originalText();
		dependent = dependency.dep().originalText();
		relation = PennRelation.valueOfPennRelation(dependency.reln().getShortName());
		nounType = dependency.dep().tag();
		dependentIndex = dependency.dep().index();
		governerIndex = dependency.gov().index();
		mergedCompounds = new TreeSet<PartsOfSpeech>(new Comparator<PartsOfSpeech>() {
			public int compare(PartsOfSpeech o1, PartsOfSpeech o2) {
				return o1.getDependentIndex() - o2.getDependentIndex();
			}
		});
		mergedCompounds.add(this);
		relatedNouns = new LinkedHashMap<>();
		relatedNouns.put(dependent, dependentIndex);
		indices = new TreeSet<>();
		indices.add(dependentIndex);
	}
	
	public String getDependent() {
		return dependent;
	}
	public String getGoverner() {
		return governer;
	}
	public String getNounType() {
		return nounType;
	}
	public PennRelation getRelation() {
		return relation;
	}
	public TypedDependency getDependency() {
		return dependency;
	}

	@Override
	public String toString() {
		return "Noun [dependency=" + dependency + ", word=" + dependent
				+ ", nounType=" + nounType + ", index=" + dependentIndex + ", quantity=" + quantity + "]";
	}
	
	public String toSmartString() {
		StringBuilder sb = new StringBuilder(this.quantity + " ");
		for(PartsOfSpeech pos: mergedCompounds) {
			sb.append(pos.getDependent() + " ");
		}
		return sb.toString();
	}
	
	public int getDependentIndex() {
		return dependentIndex;
	}
	
	public int getGovernerIndex() {
		return governerIndex;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Noun noun = (Noun) o;
		return getDependent().equals(noun.getDependent()) && getDependentIndex() == noun.getDependentIndex();
	}

	@Override
	public int hashCode() {
		return getDependent().hashCode();
	}

	public Collection<Noun> mergeAllCompounds(Collection<Noun> nounList) {
        Collection<Noun> toRemove = new HashSet<>();

		for(Noun n: nounList) {
			if(n.getRelation().equals(PennRelation.compound) && n.getGoverner().equals(this.getDependent()) && n.getGovernerIndex() == this.getDependentIndex()) {
				mergedCompounds.add(n);
                toRemove.add(n);
				indices.add(n.getDependentIndex());
			}
		}
        return toRemove;
	}


	public void mergeAdjective(PartsOfSpeech adjective) {
		mergedCompounds.add(adjective);
		indices.add(adjective.getDependentIndex());
	}
	
	public void associateQuantity(String quantity) {
		try {
			this.quantity = Integer.parseInt(quantity);
		} catch (NumberFormatException e) {
			System.err.println("Error parsing number:" + quantity);
		}
	}

	public void associateIndex(String relatedNoun, Integer index) {
		this.relatedNouns.put(relatedNoun, index);
	}

	public int getQuantity() {
		return quantity;
	}

	public Map<String, Integer> getIndices() {
		return relatedNouns;
	}
}
