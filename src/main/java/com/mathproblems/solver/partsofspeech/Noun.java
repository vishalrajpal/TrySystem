package com.mathproblems.solver.partsofspeech;

import java.util.*;

import com.mathproblems.solver.PennPOSTags;
import com.mathproblems.solver.PennRelation;
import com.mathproblems.solver.logisticregression.LogisticRegression;
import edu.stanford.nlp.trees.TypedDependency;

public class Noun implements PartsOfSpeech {

	private String dependent;
	private final PennPOSTags nounType;
	private final PennRelation relation;
	private final TypedDependency dependency;
	private final int dependentIndex;
	private int governerIndex;
	private final SortedSet<PartsOfSpeech> mergedCompounds;
	private String governer;
	private double quantity;
	private final Map<String, Integer> relatedNouns;
	private final LinkedHashSet<Preposition> prepositions;
	private final SortedSet<Integer> indices;
	private StringBuilder suffixString;
	private String sentenceText;
	public Noun (TypedDependency dependency, String sentenceText, Collection<TypedDependency> dependencies) {
		this.dependency = dependency;
		this.sentenceText=sentenceText;
		relation = PennRelation.valueOfPennRelation(dependency.reln().getShortName());

		governer = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
		governerIndex = dependency.gov().index();
		/*if(!governerExistsInNMod(dependencies)) {
			governer = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
			governerIndex = dependency.gov().index();
		} else {
			governer = "";
			governerIndex = -1;
		}*/
		dependent = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);



		nounType = PennPOSTags.valueOf(dependency.dep().tag());
		dependentIndex = dependency.dep().index();
		mergedCompounds = new TreeSet<PartsOfSpeech>(new Comparator<PartsOfSpeech>() {
			public int compare(PartsOfSpeech o1, PartsOfSpeech o2) {
				return o1.getDependentIndex() - o2.getDependentIndex();
			}
		});

		relatedNouns = new LinkedHashMap<>();
		relatedNouns.put(dependent, dependentIndex);
		indices = new TreeSet<>();
		indices.add(dependentIndex);
		suffixString = new StringBuilder();
		prepositions = new LinkedHashSet<>();


		if(nounType.equals(PennPOSTags.CD) && isANumber(dependent)) {
			associateQuantity(dependent);
		} else {
			mergedCompounds.add(this);
		}
	}

	private boolean governerExistsInNMod(Collection<TypedDependency> dependencies) {
		for(TypedDependency dependency: dependencies) {
			if(PennRelation.valueOfPennRelation(dependency.reln().getShortName()).equals(PennRelation.nmod) && dependency.gov().index()==this.dependency.gov().index()) {
				return true;
			}
		}
		return false;
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

	public String toJoinedString() {
		StringBuilder sb = new StringBuilder();
		for(PartsOfSpeech pos: mergedCompounds) {
			sb.append(pos.getDependent());
			if(!pos.equals(mergedCompounds.last())) {
				sb.append("-");
			}
		}
		return sb.toString();
	}

	public String getSentenceText() {
		return sentenceText;
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
		return getDependent().equals(noun.getDependent())
				&& getDependentIndex() == noun.getDependentIndex()
				&& this.sentenceText.equals(noun.sentenceText);
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
			this.quantity = Double.parseDouble(quantity);
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

	public double getQuantity() {
		return quantity;
	}

	public Set<Integer> getIndices() {
		return indices;
	}

	private LinkedHashMap<Noun, String> relatedAnswerNouns = new LinkedHashMap<>();
	private LinkedHashMap<Noun, String> relatedAnswerMergedNouns = new LinkedHashMap<>();

	public void initializeRelatedNouns(String predictedLabel) {
		relatedAnswerNouns.put(this, predictedLabel);
		relatedAnswerMergedNouns.put(this, predictedLabel);
	}

	public boolean relateNounToAnswerIfMatches(Map.Entry<Noun, String> entry, boolean toAdd) {

		String thisJoinedString = this.toJoinedString();
		String otherJoinedString = entry.getKey().toJoinedString();

		if(thisJoinedString.contains(otherJoinedString) || otherJoinedString.contains(thisJoinedString)) {
			relatedAnswerMergedNouns.put(entry.getKey(), entry.getValue());
		}
		System.out.println(thisJoinedString + " : " + otherJoinedString);

		String smartString = this.toSmartString();
		String otherSmartString = entry.getKey().toSmartString();

		String[] thisSplit = smartString.split(" ");
		//String[] otherSplit = otherSmartString.split(" ");

		for(String splitElement: thisSplit) {
			if(otherSmartString.contains(splitElement)) {
				if(toAdd && entry.getKey().getQuantity() != 0) {
					relatedAnswerNouns.put(entry.getKey(), entry.getValue());
				}
				return true;
			}
		}
		return false;
	}

	public double getAnswer() {
		double result = 0.0;
		int sign;
		for (Map.Entry<Noun, String> relatedAnswerNoun : relatedAnswerNouns.entrySet()) {
			sign = 1;
			String predictedLabel = relatedAnswerNoun.getValue();
			Noun n = relatedAnswerNoun.getKey();
			if (LogisticRegression.operatorToNumberMap.get(predictedLabel) == 2) {
				sign = -sign;
			}
			result = result + (sign * n.getQuantity());
		}

		double mergedResult = 0.0;

		for (Map.Entry<Noun, String> relatedAnswerMergedNoun : relatedAnswerMergedNouns.entrySet()) {
			sign = 1;
			String predictedLabel = relatedAnswerMergedNoun.getValue();
			Noun n = relatedAnswerMergedNoun.getKey();
			if (LogisticRegression.operatorToNumberMap.get(predictedLabel) == 2) {
				sign = -sign;
			}
			mergedResult = mergedResult + (sign * n.getQuantity());
		}
		System.out.println("Answer from merged nouns:" + mergedResult);
		return result;
	}

	public Map<String, Integer> getRelatedNouns() {
		return relatedNouns;
	}

	@Override
	public String getGramletCharacter() {
		String gramletChar = "";
		if(quantity != 0) {
			gramletChar = "Q";
		}
		gramletChar += "N";
		return gramletChar;
	}

	public String getDependentWithQuantity() {
		String initialString = "";
		if(this.quantity!= 0) {
			initialString = this.quantity + " ";
		}
		StringBuilder sb = new StringBuilder(initialString);
		for(PartsOfSpeech pos: mergedCompounds) {
			sb.append(pos.getDependent() + " ");
		}
		return sb.toString();
	}

	public void setDependent(String dependent) {
		this.dependent = dependent;
	}
}
