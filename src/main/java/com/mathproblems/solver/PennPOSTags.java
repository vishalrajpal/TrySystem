package com.mathproblems.solver;

public enum PennPOSTags {
	PRP,
	PRP$,
	IN,
	CD,
	VBD,
	VBN,
	VBP,
	VB,
	CVB,
	RP,
	RB,
	WDT,
	DT,
	VBZ,
	VBG,
	JJ,
	JJR,
	JJS,
	//nouns
	NN,
	NNS,
	NNP,
	NNPS,
	WRB,
	NOT_FOUND,
	$,
	EX,
	PREP,
	RBR,
	TO;

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
