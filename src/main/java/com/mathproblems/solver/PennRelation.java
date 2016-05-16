package com.mathproblems.solver;

public enum PennRelation {
	det,
	nummod,
	cc,
	
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
	dobj,	
	root,
	nsubj,
	conj,
	nsubjpass,
	parataxis,
	ccomp,
	advcl;
	
	public static PennRelation valueOfPennRelation(String str) {
		str = str.split(":")[0];
		switch(str) {
		case "case":
			str = "penncase";
			break;
		}
		return PennRelation.valueOf(str);		
	}
}
