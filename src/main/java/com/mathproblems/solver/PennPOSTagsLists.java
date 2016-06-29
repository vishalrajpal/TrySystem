package com.mathproblems.solver;

import java.util.ArrayList;
import java.util.List;

public class PennPOSTagsLists {
	
	public static final List<PennPOSTags> PENN_NOUN_TAGS = new ArrayList<PennPOSTags>();
	public static final List<PennPOSTags> PENN_ADJECTIVE_TAGS = new ArrayList<PennPOSTags>();
	public static final List<PennPOSTags> PENN_VERB_TAGS = new ArrayList<PennPOSTags>();
	public static void initializeTagLists() {
		PENN_NOUN_TAGS.add(PennPOSTags.NN);
		PENN_NOUN_TAGS.add(PennPOSTags.NNS);
		PENN_NOUN_TAGS.add(PennPOSTags.NNP);
		PENN_NOUN_TAGS.add(PennPOSTags.NNPS);
		PENN_NOUN_TAGS.add(PennPOSTags.CD);
		PENN_NOUN_TAGS.add(PennPOSTags.PRP$);
		PENN_NOUN_TAGS.add(PennPOSTags.PRP);

		PENN_ADJECTIVE_TAGS.add(PennPOSTags.JJ);
		PENN_ADJECTIVE_TAGS.add(PennPOSTags.$);

		PENN_VERB_TAGS.add(PennPOSTags.VBD);
		PENN_VERB_TAGS.add(PennPOSTags.VBN);
		PENN_VERB_TAGS.add(PennPOSTags.VB);
		PENN_VERB_TAGS.add(PennPOSTags.CVB);
		PENN_VERB_TAGS.add(PennPOSTags.VBP);
		PENN_VERB_TAGS.add(PennPOSTags.VBG);
		PENN_VERB_TAGS.add(PennPOSTags.VBZ);
	}
	
	public static boolean isANoun(String tag) {
		return PENN_NOUN_TAGS.contains(PennPOSTags.valueOf(tag));
	}
	
	public static boolean isAAdjective(String tag) {
		return PENN_ADJECTIVE_TAGS.contains(PennPOSTags.valueOf(tag));
	}
	
	public static boolean isAVerb(String tag) {
		return PENN_VERB_TAGS.contains(PennPOSTags.valueOfNullable(tag));
	}
}
