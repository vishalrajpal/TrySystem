package com.mathproblems.solver;

public enum PennRelation {
	det,
	nummod,
	cc,
	neg,
	advmod,
	mark,
	dep,	
	aux,	
	expl,
	appos,
	
	//Verbs
	cop,
	xcomp,
	acl,
	penncase,
	//root
	//advcl
	//ccomp
	
	//com.mathproblems.solver.partsofspeech.Adjective
	amod,
	compound,
	//nouns
	nmod,
	nmodof,
	nmodposs,
	dobj,
	iobj,
	root,
	nsubj,
	conj,
	conjand,
	nsubjpass,
	parataxis,
	ccomp,
	auxpass,
	advcl,
	punct;
	
	public static PennRelation valueOfPennRelation(String str) {
		//str = str.split(":")[0];
		String[] splitStr = str.split(":");
		str = splitStr[0];
		switch(str) {
			case "case":
				str = "penncase";
				break;
			case "conj":
				if(splitStr.length > 1) {
					if(splitStr[1].equals("and")) {
						str = "conjand";
					}
				}
				break;
			case "nmod":
				if(splitStr.length > 1) {
					switch(splitStr[1]) {
						case "of":
							str = "nmodof";
							break;
						case "poss":
							str="nmodposs";
							break;
					}
				}
				break;
		}
		return PennRelation.valueOf(str);		
	}
}
