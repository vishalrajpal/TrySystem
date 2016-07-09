package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;
import edu.stanford.nlp.trees.TypedDependency;

public class Verb implements PartsOfSpeech {
	private final int index;
	private String verb;
	private final boolean isConjAndVerb;
	private String mergedVerbs;
	public Verb(final int index,
				final String verb,
				boolean isConjAndTriplet) {
		this.index = index;
		this.verb = verb;
		this.mergedVerbs = verb;
		this.isConjAndVerb = isConjAndTriplet;
	}

	public int getIndex() {
		return index;
	}

	public String getVerb() {
		return verb;
	}



	@Override
	public PennPOSTags getTag() {
		if(this.isConjAndVerb) {
			return PennPOSTags.CVB;
		} else {
			return PennPOSTags.VB;
		}
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
		return verb;
	}

	@Override
	public double getQuantity() {
		return 0;
	}

	@Override
	public String toString() {
		return "Verb{" +
				"index=" + index +
				", verb='" + verb + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Verb verb1 = (Verb) o;

		if (getIndex() != verb1.getIndex()) return false;
		return getVerb().equals(verb1.getVerb());

	}

	@Override
	public int hashCode() {
		int result = getIndex();
		result = 31 * result + getVerb().hashCode();
		return result;
	}

	@Override
	public String getGramletCharacter() {
		return "V";
	}

	public String getDependentWithQuantity() {
		return getMergedVerbs();
	}

	public void mergeAuxiliaryVerb(TypedDependency dependency) {
		String auxVerb = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
		this.mergedVerbs = auxVerb + " " + this.mergedVerbs;
	}

	public String getMergedVerbs() {
		return mergedVerbs;
	}
}
