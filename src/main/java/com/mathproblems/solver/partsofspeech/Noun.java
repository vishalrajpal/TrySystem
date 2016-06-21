package com.mathproblems.solver.partsofspeech;

import java.util.*;

import com.mathproblems.solver.PennPOSTags;
import com.mathproblems.solver.PennRelation;
import edu.stanford.nlp.trees.TypedDependency;

public class Noun implements PartsOfSpeech {

	private final String dependent;
	private final PennPOSTags nounType;
	private final PennRelation relation;
	private final TypedDependency dependency;
	private final int dependentIndex;
	private final int governerIndex;
	private final SortedSet<PartsOfSpeech> mergedCompounds;
	private final String governer;
	private int quantity;
	private final Map<String, Integer> relatedNouns;
	private final LinkedHashSet<Preposition> prepositions;
	final SortedSet<Integer> indices;
	private StringBuilder suffixString;
	public Noun (TypedDependency dependency) {
		this.dependency = dependency;
		governer = dependency.gov().originalText();
		dependent = dependency.dep().originalText();
		relation = PennRelation.valueOfPennRelation(dependency.reln().getShortName());
		nounType = PennPOSTags.valueOf(dependency.dep().tag());
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
		suffixString = new StringBuilder();
		prepositions = new LinkedHashSet<>();

		if(nounType.equals(PennPOSTags.CD) && isANumber(dependent)) {
			associateQuantity(dependent);
		}
	}
//Jason found 49 seashells on the beach . He gave 13 of the seashells to Tim . How many seashells does Jason now have ?
	private boolean isANumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
	public String getDependent() {
		return dependent;
	}
	public String getGoverner() {
		return governer;
	}
	public PennPOSTags getTag() {
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
		return sb.toString() + suffixString.toString();
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

	public void mergeGovernerIndex() {
		addSuffix(governer);
		associateIndex(governer, governerIndex);
	}
	public void addSuffix(String suffix) {
		suffixString.append(" " + suffix);
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

	public void associatePreposition(Preposition p) {
		this.prepositions.add(p);
	}

	public int getQuantity() {
		return quantity;
	}

	public Map<String, Integer> getIndices() {
		return relatedNouns;
	}
}
