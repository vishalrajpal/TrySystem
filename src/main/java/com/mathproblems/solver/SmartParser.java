package com.mathproblems.solver;

import java.util.*;

import com.mathproblems.solver.facttuple.Question;
import edu.stanford.nlp.trees.TypedDependency;

import com.mathproblems.solver.partsofspeech.Adjective;
import com.mathproblems.solver.partsofspeech.Noun;
import com.mathproblems.solver.partsofspeech.Verb;
import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import srl.mateplus.MatePlusDeprel;
import srl.mateplus.SRL;

public class SmartParser {
	
	private static final Set<String> nounInGovernerList = new HashSet<>();
	private static final Set<String> nounInDependentList = new HashSet<>();;
	private static final Set<String> nounInBothList = new HashSet<>();
	private static final Set<String> typeModifiersList = new HashSet<>();
	private static final Set<String> numberList = new HashSet<>();
	private static final Set<PennRelation> universalNounsList = new HashSet<>();
	private static final Set<PennRelation> universalAdjectiveList = new HashSet<>();
	private static final Set<PennRelation> universalVerbsList = new HashSet<>();
	//All universal dependency tags with certainty greater than 1% according to 
	//http://universaldependencies.org/docs/en/pos/NOUN.html
	//omitting root
	public static void initializeUniversalNouns() {
		universalNounsList.add(PennRelation.nmod);
		universalNounsList.add(PennRelation.dobj);
		universalNounsList.add(PennRelation.compound);
		//universalNounsList.add(PennTreeBankRelations.root);
		universalNounsList.add(PennRelation.nsubj);
		universalNounsList.add(PennRelation.conj);
		universalNounsList.add(PennRelation.nsubjpass);
		universalNounsList.add(PennRelation.parataxis);
		universalNounsList.add(PennRelation.ccomp);
		universalNounsList.add(PennRelation.advcl);
	}
	
	public static void initializeUniversalAdjectives() {
		universalAdjectiveList.add(PennRelation.amod);
	}
	
	public static void initializeUniversalVerbs() {
		universalVerbsList.add(PennRelation.root);
		universalVerbsList.add(PennRelation.cop);
		universalVerbsList.add(PennRelation.advcl);
		universalVerbsList.add(PennRelation.xcomp);
		universalVerbsList.add(PennRelation.ccomp);
		universalVerbsList.add(PennRelation.acl);
		universalVerbsList.add(PennRelation.penncase);
	}
	
	public Collection<Noun> parseNounsAccordingToUniversalDependencyTags(List<TypedDependency> dependencies) {
		String currentRelation;
		List<Noun> nounList = new ArrayList<>();
        Set<Integer> dependentIndices = new HashSet<>();
		for(TypedDependency dependency : dependencies) {
			currentRelation = dependency.reln().getShortName();
			if(universalNounsList.contains(PennRelation.valueOfPennRelation(currentRelation))
                    && PennPOSTagsLists.isANoun(dependency.dep().tag())
                    && !dependentIndices.contains(dependency.dep().index())) {
				//System.out.println(dependency.toString() + " : " + dependency.dep().toString());
				final Noun n = new Noun(dependency);
				//System.out.println(n.getDependent());
				nounList.add(n);
                dependentIndices.add(n.getDependentIndex());
			}
		}
		return nounList;
	}
	
	public List<Adjective> parseAdjectivesAccordingToUniversalDependencyTags(List<TypedDependency> dependencies, Collection<Noun> nounList) {
		String currentRelation;
		List<Adjective> adjectiveList = new ArrayList<>();
		for(TypedDependency dependency : dependencies) {
			currentRelation = dependency.reln().getShortName();
			if(universalAdjectiveList.contains(PennRelation.valueOfPennRelation(currentRelation)) && PennPOSTagsLists.isAAdjective(dependency.dep().tag())) {
				System.out.println(dependency.toString() + " : " + dependency.dep().toString());
				adjectiveList.add(new Adjective(dependency, nounList));
			}
		}
		return adjectiveList;
	}
	
	/*public List<Verb> parseVerbsAccordingToUniversalDependencyTags(List<TypedDependency> dependencies) {
		String currentRelation;
		List<Verb> verbList = new ArrayList<>();
		for(TypedDependency dependency : dependencies) {
			currentRelation = dependency.reln().getShortName();
			if(universalVerbsList.contains(PennRelation.valueOfPennRelation(currentRelation)) && PennPOSTagsLists.isAVerb(dependency.dep().tag())) {
				System.out.println(dependency.toString() + " : " + dependency.dep().toString());
				verbList.add(new Verb(dependency));
			}
		}
		return verbList;
	}*/

    public Collection<Verb> parseVerbsAccordingToSRL(SRL srl, String sentence) {
        Collection<Verb> verbs = new HashSet<>();
        try {
            CompletePipeline pipeline = srl.getPipeline();
            String[] tokens = pipeline.pp.tokenize(sentence);
            Sentence s = pipeline.parse(Arrays.asList(tokens));
            Collection<String> consideredForms = new HashSet<>();
            for(Predicate p : s.getPredicates()) {
                final String posTag = p.getPOS();
                if (PennPOSTagsLists.isAVerb(posTag)) {
                    final int index = p.getIdx();
                    final String verb = p.getForm();
                    consideredForms.add(verb);
                    final String lemma = p.getLemma();
                    MatePlusDeprel subjectDeprel = null;
                    MatePlusDeprel objectDeprel = null;
                    MatePlusDeprel adverbDeprel = null;
                    String verbSubject = null;
                    String verbObject = null;
                    String verbAdverb = null;
                    for(Word arg : p.getArgMap().keySet()) {
                        MatePlusDeprel deprel = MatePlusDeprel.valueOf(arg.getDeprel());
                        StringBuilder deprelValue = new StringBuilder();
                        // "arg" is just the syntactic head word; let's iterate through all words in the argument span
                        for(Word w : arg.getSpan()) {
                            deprelValue.append(w.getForm() + " ");
                        }
                        if (deprel.equals(MatePlusDeprel.ADV)) {
                            verbAdverb = deprelValue.toString();
                            adverbDeprel = deprel;
                        } else if(deprel.equals(MatePlusDeprel.SBJ)) {
                            verbSubject = deprelValue.toString();
                            subjectDeprel = deprel;
                        } else if(deprel.equals(MatePlusDeprel.OBJ)){
                            verbObject = deprelValue.toString();
                            objectDeprel = deprel;
                        }
                    }
                    final Verb v = new Verb(index, verb, PennPOSTags.valueOf(posTag), lemma, verbSubject, subjectDeprel, verbObject, objectDeprel, verbAdverb, adverbDeprel);
                    verbs.add(v);
                }
            }
            int size = s.size();
            for(int i = 1; i<size; i++) {

                Word w = s.get(i);
                if(!consideredForms.contains(w.getForm()) && PennPOSTagsLists.isAVerb(w.getPOS())) {
                    final Verb v = new Verb(i, w.getForm(), PennPOSTags.valueOfNullable(w.getPOS()), w.getLemma(), null, null, null, null, null, null);
                    verbs.add(v);
                    consideredForms.add(w.getForm());
                }
                // each word object contains information about a word's actual word form / lemma / POS
                //System.out.println(w.getForm() + "\t " + w.getLemma() + "\t" + w.getPOS());
            }
        } catch (final Exception e) {

        }
        return verbs;
    }

	public void mergeCompoundsOfParsedNouns(Collection<Noun> nounList) {
        Set<Noun> toRemove = new HashSet<>();
		for(Noun n: nounList) {
			if(!n.getRelation().equals(PennRelation.compound)) {
				final Collection<Noun> remove = n.mergeAllCompounds(nounList);
                toRemove.addAll(remove);
			}
		}
        nounList.removeAll(toRemove);
	}	
	
	public List<TypedDependency> getAllNummods(List<TypedDependency> dependencies) {
		List<TypedDependency> numMods = new ArrayList<>();
		String currentRelation;
		for(TypedDependency dependency: dependencies) {
			currentRelation = dependency.reln().getShortName();
			if(PennRelation.valueOfPennRelation(currentRelation).equals(PennRelation.nummod)) {
				System.out.println(dependency.toString() + " : " + dependency.dep().toString() + ":" + dependency.gov().originalText());
				numMods.add(dependency);
			}
		}
		return numMods;
	}
	
	public void mergeNummodsWithParsedNouns(List<TypedDependency> numMods, Collection<Noun> nounList) {
		String dependencyGoverner;
		for(TypedDependency dependency: numMods) {
			dependencyGoverner = dependency.gov().originalText();
			for(Noun n: nounList) {
				if(dependencyGoverner.equals(n.getDependent()) && dependency.gov().index() == n.getDependentIndex()) {
					n.associateQuantity(dependency.dep().originalText());
				}
			}
			
		}
	}

    public void removeDuplicateNouns(Collection<Noun> nouns) {

    }

	public void printProcessedNouns(Collection<Noun> nounList) {
		for(Noun n : nounList) {
			System.out.println(n.toSmartString());
		}
	}
	
	public void printProcessedAdjectives(List<Adjective> adjectiveList) {
		for(Adjective a : adjectiveList) {
			System.out.println(a.toString());
		}
	}

    public void printProcessedVerbs(Collection<Verb> verbs) {
        for (Verb v : verbs) {
            System.out.println(v);
        }
    }

    public void printProcessedQuestions(final Collection<Question> questions) {
        for (final Question q : questions) {
            System.out.println(q);
        }
    }
}