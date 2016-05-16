package com.mathproblems.solver;

public enum PennPOSTags {
	PRP,
	PRP$,
	CD,
	VBD,
	VBN,
	VBP,
	VB,
	RP,
	DT,
	VBZ,
	VBG,
	JJ,
	JJR,
	//nouns
	NN,
	NNS,
	NNP,
	NNPS,
	WRB,
	NOT_FOUND;

	public static PennPOSTags valueOfNullable(String value) {
		PennPOSTags tag;
		try {
			tag = PennPOSTags.valueOf(value);
		} catch (final IllegalArgumentException e) {
			tag = PennPOSTags.NOT_FOUND;
		}
		return tag;
	}
}
