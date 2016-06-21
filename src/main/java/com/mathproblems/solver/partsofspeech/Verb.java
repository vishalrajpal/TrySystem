package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;

public class Verb implements PartsOfSpeech {
	private final int index;
	private final String verb;
	private final boolean isConjAndVerb;

	public Verb(final int index,
				final String verb,
				boolean isConjAndTriplet) {
		this.index = index;
		this.verb = verb;
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
	public int getQuantity() {
		return 0;
	}

	@Override
	public String toString() {
		return "Verb{" +
				"index=" + index +
				", verb='" + verb + '\'' +
				'}';
	}
}
