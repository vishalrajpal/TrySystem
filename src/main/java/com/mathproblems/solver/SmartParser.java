package com.mathproblems.solver;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mathproblems.solver.classifier.SVMClassifier;
import com.mathproblems.solver.equationtool.Equation;
import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.facttuple.Question;

import com.mathproblems.solver.logisticregression.LogisticRegression;
import com.mathproblems.solver.partsofspeech.*;
import com.mathproblems.solver.ruletrees.EquationTree;
import edu.stanford.nlp.trees.TypedDependency;
import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import srl.mateplus.SRL;

public class SmartParser {

	private static final Set<PennRelation> universalNounsList = new HashSet<>();
	private static final Set<PennRelation> universalAdjectiveList = new HashSet<>();
	private static final Set<PennRelation> universalVerbsList = new HashSet<>();
	//All universal dependency tags with certainty greater than 1% according to 
	//http://universaldependencies.org/docs/en/pos/NOUN.html
	//omitting root
	public static void initializeUniversalNouns() {
		//universalNounsList.add(PennRelation.nmod);
		universalNounsList.add(PennRelation.nmodof);
		universalNounsList.add(PennRelation.nmodposs);
		universalNounsList.add(PennRelation.dobj);
		universalNounsList.add(PennRelation.compound);
		//universalNounsList.add(PennTreeBankRelations.root);
		universalNounsList.add(PennRelation.nsubj);
		universalNounsList.add(PennRelation.conj);
		universalNounsList.add(PennRelation.nsubjpass);
		universalNounsList.add(PennRelation.parataxis);
		universalNounsList.add(PennRelation.ccomp);
		universalNounsList.add(PennRelation.advcl);
		universalNounsList.add(PennRelation.advmod);
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
	
	public LinkedHashSet<Noun> parseNounsAccordingToUniversalDependencyTags(Collection<TypedDependency> dependencies, String sentenceText) {
		String currentRelation;
		LinkedHashSet<Noun> nounList = new LinkedHashSet<>();
        Set<Integer> dependentIndices = new HashSet<>();
		for(TypedDependency dependency : dependencies) {
			currentRelation = dependency.reln().toString();
			if(universalNounsList.contains(PennRelation.valueOfPennRelation(currentRelation))
                    && PennPOSTagsLists.isANoun(dependency.dep().tag())
                    && !dependentIndices.contains(dependency.dep().index())) {
				//System.out.println(dependency.toString() + " : " + dependency.dep().toString());
				final Noun n = new Noun(dependency, sentenceText, dependencies);
				//System.out.println(n.getDependent());
				nounList.add(n);
                dependentIndices.add(n.getDependentIndex());
			}
		}
		return nounList;
	}

	public Collection<TypedDependency> parserNounsWithConj(Collection<TypedDependency> dependencies) {
		Collection<TypedDependency> conjAndDependencies = new LinkedHashSet<>();
		for(TypedDependency dependency : dependencies) {
			if (PennRelation.valueOfPennRelation(dependency.reln().toString()).equals(PennRelation.conj)) {
				conjAndDependencies.add(dependency);
			}
		}
		return conjAndDependencies;
	}

	public static LinkedHashSet<Conjunction> parserNounsWithConjAnds(Collection<TypedDependency> dependencies) {
		LinkedHashSet<Conjunction> conjAndDependencies = new LinkedHashSet<>();
		for(TypedDependency dependency : dependencies) {
			if (PennRelation.valueOfPennRelation(dependency.reln().toString()).equals(PennRelation.cc)) {
				Conjunction c = new Conjunction(dependency.dep().index(), dependency.dep().toString(), dependency.dep().tag());
				conjAndDependencies.add(c);
			}
		}
		return conjAndDependencies;
	}



	public List<Adjective> mergeAdjectivesOfParsedNouns(Collection<TypedDependency> dependencies, Collection<Noun> nounList) {
		String currentRelation;
		List<Adjective> adjectiveList = new ArrayList<>();
		for(TypedDependency dependency : dependencies) {
			currentRelation = dependency.reln().getShortName();
			if(universalAdjectiveList.contains(PennRelation.valueOfPennRelation(currentRelation)) && PennPOSTagsLists.isAAdjective(dependency.dep().tag())) {
				adjectiveList.add(new Adjective(dependency, nounList));
			}
		}
		return adjectiveList;
	}

	/*public void mergeOtherNouns(Collection<Noun> nouns, Collection<TypedDependency> dependencies) {
		for(TypedDependency dependency: dependencies) {
			if(PennRelation.valueOfPennRelation(dependency.reln().getShortName()).equals(PennRelation.advmod) && )
		}
	}*/
	
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
	}

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
    }*/

	public LinkedHashSet<Triplet> getTripletsFromSRL(SRL srl, String sentence) {
		final LinkedHashSet<Triplet> triplets = new LinkedHashSet<>();
		try {
			CompletePipeline pipeline = srl.getPipeline();
			String[] tokens = pipeline.pp.tokenize(sentence); // this is how you tokenize your text
			Sentence s = pipeline.parse(Arrays.asList(tokens)); // this is how you then process the text (tokens)

			String subject, subjectTag, verb, verbTag, object, objectTag;
			int subjectIndex, verbIndex, objectIndex;
			// some words in a sentence are recognized as predicates
			for (Predicate p : s.getPredicates()) {
				if (p.getPOS().startsWith("VB")) {
					subject = subjectTag = object = objectTag = null;
					subjectIndex = objectIndex = 0;
					verb = p.getForm();
					//verb = p.getLemma();
					verbTag = p.getPOS();
					verbIndex = p.getIdx();
					for (Word arg : p.getArgMap().keySet()) {
						if (arg.getDeprel().equals("SBJ")) {
							subject = arg.getForm();
							subjectTag = p.getArgumentTag(arg);
							subjectIndex = arg.getIdx();
						} else if (arg.getDeprel().equals("OBJ")) {
							object = arg.getForm();
							objectTag = p.getArgumentTag(arg);
							objectIndex = arg.getIdx();
						}
					}
					final Triplet triplet = new Triplet(subject, subjectTag, subjectIndex, verb, verbTag, verbIndex, object, objectTag, objectIndex, false);
					triplets.add(triplet);
				}
			}
			System.out.println(triplets);
		} catch (final Exception e) {
			System.err.println("Error running srl to get triplets.");
		}
		return triplets;
	}

	public LinkedHashMap<String, String> getNounWithTags(SRL srl, String sentence) {
		final LinkedHashMap<String, String> nounWithTags = new LinkedHashMap<>();
		try {
			CompletePipeline pipeline = srl.getPipeline();
			String[] tokens = pipeline.pp.tokenize(sentence);
			Sentence s = pipeline.parse(Arrays.asList(tokens));

			for (Predicate p : s.getPredicates()) {
				for(Map.Entry<Word, String> entry: p.getArgMap().entrySet()) {
					Word w = entry.getKey();
					String tag = entry.getValue();
					nounWithTags.put(w.getForm(), tag);
				}
			}
			System.out.println(nounWithTags);
		} catch (final Exception e) {
			System.err.println("Error running srl to get noun with tags.");
		}
		return nounWithTags;
	}

	public void mergeCompoundsOfParsedNouns(Collection<Noun> nounList) {
        Set<Noun> toRemove = new HashSet<>();
		for(Noun n: nounList) {
			if(!n.getRelation().equals(PennRelation.compound)) {
				final Collection<Noun> remove = n.mergeAllCompounds(nounList);
                toRemove.addAll(remove);
			}
			/*else {
				n.mergeGovernerIndex();
			}*/
		}
        nounList.removeAll(toRemove);
	}
	
	public void mergeNummodsWithParsedNouns(Collection<TypedDependency> dependencies, Collection<Noun> nounList, String sentenceText) {
		//String dependencyGoverner;
		List<TypedDependency> numMods = getAllNummods(dependencies);
		boolean matchFound;
		for(TypedDependency dependency: numMods) {
			matchFound = false;
			for(Noun n: nounList) {
				//if(dependencyGoverner.equals(n.getDependent()) && dependency.gov().index() == n.getDependentIndex()) {
				if(n.getIndices().contains(dependency.gov().index())) {
					String dep = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
					n.associateQuantity(dep);
					matchFound = true;
				}
			}

			if(!matchFound) {
				nounList.add(new Noun(dependency, sentenceText, PennRelation.nummod));
			}
		}
	}

	private List<TypedDependency> getAllNummods(Collection<TypedDependency> dependencies) {
		List<TypedDependency> numMods = new ArrayList<>();
		String currentRelation;
		for(TypedDependency dependency: dependencies) {
			currentRelation = dependency.reln().toString();
			if(PennRelation.valueOfPennRelation(currentRelation).equals(PennRelation.nummod)) {
				//System.out.println(dependency.toString() + " : " + dependency.dep().toString() + ":" + dependency.gov().originalText());
				numMods.add(dependency);
			}
		}
		return numMods;
	}

	public List<TypedDependency> getAllNmods(Collection<TypedDependency> dependencies) {
		List<TypedDependency> nMods = new ArrayList<>();
		String currentRelation;
		for(TypedDependency dependency: dependencies) {
			currentRelation = dependency.reln().toString();
			PennRelation rel = PennRelation.valueOfPennRelation(currentRelation);
			if(rel.equals(PennRelation.nmod) || rel.equals(PennRelation.nmodof) || rel.equals(PennRelation.nmodposs) || rel.equals(PennRelation.nmodin)) {
				String dep = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				String gov = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				System.out.println(dependency.toString() + " : " + dep + ":" + gov);
				nMods.add(dependency);
			}
		}
		return nMods;
	}

	public void mergeNmodsWithParsedNouns(Collection<TypedDependency> dependencies, Collection<Noun> nounList, String sentenceText) {
		List<TypedDependency> nMods = getAllNmods(dependencies);
		String dependent;
		String governer;
		boolean foundMatch;
		for(TypedDependency dependency: nMods) {
			//dependent = dependency.dep().originalText();
			//governer = dependency.gov().originalText();
			String currentRelation = dependency.reln().toString();
			PennRelation rel = PennRelation.valueOfPennRelation(currentRelation);

			foundMatch = false;
			governer = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
			dependent = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
			if(rel.equals(PennRelation.nmodof)) {
				for (Noun n : nounList) {
					if (governer.equals(n.getDependent()) && dependency.gov().index() == n.getDependentIndex()) {
						foundMatch = true;
						//PartsOfSpeech nMod = new Noun(dependency, n.getSentenceText(), dependencies);
						//n.mergeAdjective(nMod);
						n.setDependent(dependent);
						//n.associateQuantity(dependency.dep().originalText());
						//n.associateIndex(dependency.gov().originalText(), dependency.gov().index());
						n.associateIndex(dependent, dependency.gov().index());
					}
				}
			} else if(!rel.equals(PennRelation.nmodin)){
				for (Noun n : nounList) {
					if (governer.equals(n.getDependent()) && dependency.gov().index() == n.getDependentIndex()) {
						foundMatch = true;
						PartsOfSpeech nMod = new Noun(dependency, n.getSentenceText(), dependencies);
						n.mergeAdjective(nMod);
						//n.associateQuantity(dependency.dep().originalText());
						//n.associateIndex(dependency.gov().originalText(), dependency.gov().index());
						n.associateIndex(governer, dependency.gov().index());
					}
				}
			}
			if(!foundMatch) {
				nounList.add(new Noun(dependency, sentenceText, dependencies));
			}
		}
	}

	private Collection<TypedDependency> getPrepositions(Collection<TypedDependency> dependencies) {
		Collection<TypedDependency> prepositions = new LinkedHashSet<>();
		String currentRelation;
		for(TypedDependency dependency: dependencies) {
			currentRelation = dependency.reln().toString();
			if(PennRelation.valueOfPennRelation(currentRelation).equals(PennRelation.penncase)) {
				String dep = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				String gov = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				System.out.println(dependency.toString() + " : " + dep + ":" + gov);
				prepositions.add(dependency);
			}
		}
		return prepositions;
	}

	public Collection<Preposition> mergePrepositionsOfParsedNouns(Collection<TypedDependency> dependencies, Collection<Noun> nouns) {
		Collection<TypedDependency> prepositionDependencies = getPrepositions(dependencies);
		Collection<Preposition> prepositions = new LinkedHashSet<>();
		for(TypedDependency preposition: prepositionDependencies) {
			Preposition p = new Preposition(preposition, nouns);
			prepositions.add(p);
		}
		return prepositions;
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

	public String extractFeatures(Gramlet gramlet, String sentence, int label, LinkedHashSet<Verb> verbs, Collection<TypedDependency> dependencies) {
		final String ifWord = "if";
		final String onlyWord = "only";
		final String toWord = "to";

		char splitChar = ' ';
		char concatChar = ':';
		String gramletString = gramlet.toString();

		StringBuilder featuresString = new StringBuilder();
		featuresString.append(label);


		Map<Integer, Double> featureVector = new HashMap<>();
		featureVector.putAll(LogisticRegression.defaultFeatureVector);

		featureVector.put(gramlet.toString().hashCode(), 1.0);
		featureVector.put(gramlet.lastChar().hashCode(), 1.0);
		featureVector.put(LogisticRegression.NO_OF_VERBS_STRING.hashCode(), (double)gramlet.noOfVerbs());
		featureVector.put(LogisticRegression.NO_OF_PREPOSITIONS_STRING.hashCode(), (double)gramlet.noOfPrepositions());
		featureVector.put(LogisticRegression.CONTAINS_AND_CONJUNCTION_STRING.hashCode(), (double)gramlet.noOfCounjunctions());
		featureVector.put(LogisticRegression.NO_OF_QUANTITIES_STRING.hashCode(), (double)gramlet.noOfQuantities());
		featureVector.put(LogisticRegression.NO_OF_NOUNS_STRING.hashCode(), (double)gramlet.noOfNouns());
		featureVector.put(LogisticRegression.CONTAINS_NVQN_PATTERN_STRING.hashCode(), gramlet.containsPattern("NVQN") ? 1.0 : 0.0);
		featureVector.put(LogisticRegression.CONTAINS_VQ_PATTERN_STRING.hashCode(), gramlet.containsPattern("VQ") ? 1.0 : 0.0);
		featureVector.put(LogisticRegression.CONTAINS_WORD_IF_STRING.hashCode(), sentence.toLowerCase().contains(ifWord) ? 1.0 : 0.0);
		featureVector.put(LogisticRegression.CONTAINS_WORD_ONLY_STRING.hashCode(), sentence.toLowerCase().contains(onlyWord) ? 1.0 : 0.0);
		featureVector.put(LogisticRegression.HAS_ZERO_QUANTITIES_STRING.hashCode(), gramlet.noOfQuantities() == 0 ? 1.0 : 0.0);
		featureVector.put(LogisticRegression.HAS_WHADVERB_STRING.hashCode(), gramlet.hasWHAdverb() ? 1.0 : 0.0);
		featureVector.put(LogisticRegression.CONTAINS_WORD_TO_STRING.hashCode(), sentence.toLowerCase().contains(toWord) ? 1.0 : 0.0);

		featureVector.put(LogisticRegression.CONTAINS_NMOD_OF.hashCode(), 0.0);
		featureVector.put(LogisticRegression.CONTAINS_NMOD_POSS.hashCode(), 0.0);
		Collection<TypedDependency> nMods = getAllNmods(dependencies);
		for(TypedDependency dependency: nMods) {
			PennRelation currentRelation = PennRelation.valueOfPennRelation(dependency.reln().getShortName());
			if(currentRelation.equals(PennRelation.nmodof)) {
				featureVector.put(LogisticRegression.CONTAINS_NMOD_OF.hashCode(), 1.0);
			} else if(currentRelation.equals(PennRelation.nmodposs)) {
				featureVector.put(LogisticRegression.CONTAINS_NMOD_POSS.hashCode(), 1.0);
			}
		}


		Pattern p = Pattern.compile("(some|most|several)");
		Matcher m = p.matcher(sentence.toLowerCase());
		featureVector.put(LogisticRegression.CONTAINS_UNKNOWN_QUANTITY_WORDS_STRING.hashCode(), m.find() ? 1.0 : 0.0);

		Pattern nowPattern = Pattern.compile("\\b(now)\\b");
		Matcher nowMatcher = nowPattern.matcher(sentence.toLowerCase());
		featureVector.put(LogisticRegression.CONTAINS_WORD_NOW_STRING.hashCode(), nowMatcher.find() ? 1.0 : 0.0);

		Pattern onPattern = Pattern.compile("\\b(on)\\b");
		Matcher onMatcher = onPattern.matcher(sentence.toLowerCase());
		featureVector.put(LogisticRegression.CONTAINS_WORD_ON_STRING.hashCode(), onMatcher.find() ? 1.0 : 0.0);

		/**one-hot encoding for verbs */
		for(String category: SVMClassifier.importantCategories) {
			featureVector.put(category.hashCode(), 0.0);
		}

		for(Verb verb: verbs) {
			LinkedHashMap<String, Double> word2VecFeatures = VerbClassificationFeatures.runWord2Vec(verb.getVerb());
			for(Map.Entry<String, Double> word2VecFeature: word2VecFeatures.entrySet()) {
				double currentVal = featureVector.get(word2VecFeature.getKey().hashCode());
				featureVector.put(word2VecFeature.getKey().hashCode(), currentVal + word2VecFeature.getValue());
			}

			LinkedHashMap<String, Double> wordNetFeatures = VerbClassificationFeatures.runWordNet(verb.getVerb());
			for(Map.Entry<String, Double> wordNetFeature: wordNetFeatures.entrySet()) {
				double currentVal = featureVector.get(wordNetFeature.getKey().hashCode());
				featureVector.put(wordNetFeature.getKey().hashCode(), currentVal + wordNetFeature.getValue());
			}
		}
		/** one hot encoding for verbs end */

		for(Map.Entry<Integer, Double> entry: featureVector.entrySet()) {
			featuresString.append(splitChar);
			featuresString.append(entry.getKey());
			featuresString.append(concatChar);
			featuresString.append(entry.getValue());
		}
		return featuresString.toString();
	}

	public String extractFeatures1(Gramlet gramlet, String sentence, int label) {
		/**
		 * Gramlet
		 * GramletLastCharacter
		 * NoOfVerbs
		 * NoOfPrepositions
		 * NoOfConjunctions
		 * NoOfQuantities
		 * NoOfNouns
		 * ContainsNVQNPattern
		 * containsWordIF
		 * containsWordONLY
		 * containsUnknownQuantityWords
		 * IsQuantityAfterVerb
		 */
		final String ifWord = "if";
		final String onlyWord = "only";

		char splitChar = ' ';
		char concatChar = ':';
		String gramletString = gramlet.toString();

		int currentIndex = 1;
		StringBuilder featuresString = new StringBuilder();
		featuresString.append(label);
		featuresString.append(splitChar);
		/**1 Gramlet*/
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		//featuresString.append(gramlet.getFeatureValue());
		featuresString.append(splitChar);

		/**2 Last Character */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.lastChar());
		featuresString.append(splitChar);

		/** 3 No of Verbs */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.noOfVerbs());
		featuresString.append(splitChar);

		/** 4 No of Prepositions */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.noOfPrepositions());
		featuresString.append(splitChar);

		/** 5 No of Conjunctions */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.noOfCounjunctions());
		featuresString.append(splitChar);


		/** 6 No of Quantities */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.noOfQuantities());
		featuresString.append(splitChar);

		/** 7 No of Nouns */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.noOfNouns());
		featuresString.append(splitChar);

		/** 8 Contains Pattern NVQN */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.containsPattern("NVQN") ? 1 : 0);
		featuresString.append(splitChar);

		/** 9 Contains Pattern VQ */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(gramlet.containsPattern("VQ") ? 1 : 0);
		featuresString.append(splitChar);

		/** 10 Contains word 'If' */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(sentence.toLowerCase().contains(ifWord) ? 1 : 0);
		featuresString.append(splitChar);

		/** 11 Contains word 'only' */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		featuresString.append(sentence.toLowerCase().contains(onlyWord) ? 1 : 0);
		featuresString.append(splitChar);

		/** 12 Contains unknown quantity words 'some|most|several' */
		featuresString.append(currentIndex++);
		featuresString.append(concatChar);
		Pattern p = Pattern.compile("(some|most|several)");
		Matcher m = p.matcher(sentence.toLowerCase());
		featuresString.append(m.find() ? 1 : 0);
		featuresString.append(splitChar);

		return featuresString.toString();
	}

	private static SRL srl;
	public static SmartParser sParser;

	public static void initializeSrlDependencies() {
		final String lemmaPath = Thread.currentThread().getContextClassLoader().getResource("models/lemma-eng.model").getPath();
		final String taggerPath = Thread.currentThread().getContextClassLoader().getResource("models/tagger-eng.model").getPath();
		final String parserPath = Thread.currentThread().getContextClassLoader().getResource("models/parse-eng.model").getPath();
		final String srlModelPath = Thread.currentThread().getContextClassLoader().getResource("models/srl-EMNLP14+fs-eng.model").getPath();
		String[] pipelineOptions = new String[]{
				"eng",					// language
				"-lemma", lemmaPath,	// lemmatization mdoel
				"-tagger", taggerPath,	// tagger model
				"-parser", parserPath,	// parsing model
				"-srl", srlModelPath,	// SRL model
				"-tokenize",			// turn on word tokenization
				"-reranker"				// turn on reranking (part of SRL)
		};
		//srl = new SRL(pipelineOptions);
		sParser = new SmartParser();
	}

	public static LinkedHashSet<Triplet> getTriplets(com.mathproblems.solver.facttuple.Sentence sentence, LinkedHashSet<Noun> nouns) {
		Collection<TypedDependency> dependencies = sentence.getDependencies();
		LinkedHashSet<Triplet> triplets = sParser.getTripletsFromSRL(srl, sentence.getSentenceText());
		System.out.println(triplets);

		System.out.println("------Merging Conj And Triplets------");
		Collection<Triplet> conjAndTriplets = getConjAndTriplets(dependencies, triplets);
		triplets.addAll(conjAndTriplets);
		System.out.println("------After Merging Conj And Triplets------");
		System.out.println(triplets);

		Triplet lastObjectTriplet = null;
		Triplet numberTriplet = null;
		for(Triplet triplet: triplets) {
			if(isNumber(triplet.getObject())) {
				numberTriplet = triplet;
				if(lastObjectTriplet != null) {
					break;
				}
			} else {
				lastObjectTriplet = triplet;
			}
		}

		if(numberTriplet != null && lastObjectTriplet != null) {
			for (Noun n : nouns) {
				if (n.getDependentIndex() == numberTriplet.getObjectIndex()) {
					n.associateQuantity(numberTriplet.getObject());
					n.addSuffix(lastObjectTriplet.getObject());
				}
			}
		}
		return triplets;
	}

	public static LinkedHashSet<Verb> getVerbsFromTriplets(LinkedHashSet<Triplet> triplets) {
		LinkedHashSet<Verb> verbs = new LinkedHashSet<>();
		for(Triplet triplet: triplets) {
			verbs.add(new Verb(triplet.getVerbIndex(), triplet.getVerb(), triplet.isConjAndTriplet()));
		}
		return verbs;
	}

	public static Gramlet parsePOSToGramlet(SortedSet<PartsOfSpeech> nounsAndVerbs, LinkedHashMap<PartsOfSpeech, String> gramletCharToStringMapping) {
		StringBuilder sb = new StringBuilder();
		for(PartsOfSpeech pos: nounsAndVerbs) {
			String gramletChar = pos.getGramletCharacter();
			if(gramletChar != null) {
				sb.append(gramletChar);
				if(gramletCharToStringMapping != null) {
					gramletCharToStringMapping.put(pos, pos.getDependentWithQuantity());
				}
			}
			/*if(PennPOSTagsLists.isANoun(pos.getTag().name())) {
				if(pos.getQuantity() != 0) {
					sb.append("Q");
				}
				sb.append("N");
			} else if(PennPOSTagsLists.isAVerb(pos.getTag().name())) {
				if(pos.getTag().equals(PennPOSTags.CVB)) {
					sb.append("C");
				} else {
					sb.append("V");
				}
			} else if(pos.getTag().equals(PennPOSTags.PREP)) {
				sb.append("P");
			} else if(pos.getTag().equals(PennPOSTags.EX)) {
				sb.append("E");
			}*/

		}
		System.out.println(sb.toString());
		return Gramlet.valueOfGramlet(sb.toString());
	}

	public static Collection<Triplet> getConjAndTriplets(Collection<TypedDependency> dependencies, Collection<Triplet> triplets) {
		Collection<TypedDependency> conjAndDependencies = sParser.parserNounsWithConj(dependencies);

		Collection<Triplet> conjAndTriplets = new ArrayList<>();
		for(TypedDependency dependency: conjAndDependencies) {
			Triplet newTriplet;
			for(Triplet triplet: triplets) {
				String gov = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				String dep = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				if(triplet.getSubject() != null && triplet.getSubject().equals(gov) && triplet.getSubjectIndex() == dependency.gov().index()) {
					newTriplet = new Triplet(dep, triplet.getSubjectTag(), triplet.getSubjectIndex(),
							triplet.getVerb(), triplet.getVerbTag(), triplet.getVerbIndex(),
							triplet.getObject(), triplet.getObjectTag(), triplet.getObjectIndex(), true);
					conjAndTriplets.add(newTriplet);
					break;
				} else if(triplet.getObject() != null && triplet.getObject().equals(gov) && triplet.getObjectIndex() == dependency.gov().index()) {
					newTriplet = new Triplet(triplet.getSubject(), triplet.getSubjectTag(), triplet.getSubjectIndex(),
							triplet.getVerb(), triplet.getVerbTag(), triplet.getVerbIndex(),
							dep, triplet.getObjectTag(), dependency.dep().index(), true);
					conjAndTriplets.add(newTriplet);
					break;
				}
			}
		}
		return conjAndTriplets;
	}

	public static LinkedHashMap<Noun, Triplet> mergeNounsAndTriplets(Collection<Noun> nouns, Collection<Triplet> triplets) {
		LinkedHashMap<Noun, Triplet> nounToTripletMap = new LinkedHashMap<>();
		for(Noun sentenceNoun: nouns) {
			for(Triplet triplet: triplets) {
				if(triplet.isEquivalentToPOSNoun(sentenceNoun)) {
					nounToTripletMap.put(sentenceNoun, triplet);
					System.out.println("Possible link found.");
					System.out.println("Sentence:" + sentenceNoun);
					System.out.println("Triplet:" + triplet);
				}
			}
		}
		return nounToTripletMap;
	}

	private static LinkedHashMap<Noun, Triplet> findUsefulTripletsAndTheirNouns(LinkedHashMap<Noun, Triplet> nounToTripletMap, LinkedHashSet<String> distinctSubjects) {
		LinkedHashMap<Noun, Triplet> usefulTriplets = new LinkedHashMap<>();
		// boolean isATripletWithoutObject = false;
		for(Map.Entry<Noun, Triplet> entry: nounToTripletMap.entrySet()) {

			Noun n = entry.getKey();
			Triplet t = entry.getValue();

			if(n.getQuantity() != 0) {
				if(t.matchesToSubject(n)) {
					usefulTriplets.put(n, t);
					t.setSubjectQuantity(n.getQuantity());
					distinctSubjects.add(t.getSubjectTag());
					System.out.println(n.getQuantity() + t.getSubjectTag() + " " + t.getVerb() + " " + t.getObjectTag());
				} else if(t.matchesToObject(n)) {
					usefulTriplets.put(n, t);
					t.setObjectQuantity(n.getQuantity());
					distinctSubjects.add(t.getSubjectTag());
					System.out.println(t.getSubjectTag() + " " + t.getVerb() + " " + n.getQuantity() + t.getObjectTag());
				}
			} else if (isNumber(t.getSubject()) && t.matchesToSubject(n)) {
				usefulTriplets.put(n, t);
				t.setSubjectQuantity(Integer.parseInt(t.getSubject()));
				distinctSubjects.add(t.getSubjectTag());
				System.out.println(t.getSubject() + t.getSubjectTag() + " " + t.getVerb() + " " + t.getObjectTag());
			} else if(isNumber(t.getObject()) && t.matchesToObject(n)) {
				usefulTriplets.put(n, t);
				t.setObjectQuantity(Integer.parseInt(t.getObject()));
				distinctSubjects.add(t.getSubjectTag());
				System.out.println(t.getSubjectTag() + " " + t.getVerb() + " " + t.getObject() + t.getObjectTag());
			}
		}
		return usefulTriplets;
	}

	public static LinkedHashSet<Verb> parseVerbsBasedOnDependencies(Collection<TypedDependency> dependencies) {
		LinkedHashSet<Verb> dependencyVerbs = new LinkedHashSet<>();
		Collection<TypedDependency> auxiliaryVerbs = new ArrayList<>();
		for(TypedDependency dependency: dependencies) {
			String depTag = dependency.dep().tag();
			String govTag = dependency.gov().tag();
			if(depTag != null && PennPOSTagsLists.isAVerb(depTag)) {
				String verb = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				Verb newVerb = new Verb(dependency.dep().index(), verb, false);
				dependencyVerbs.add(newVerb);
			} else if(govTag != null && PennPOSTagsLists.isAVerb(govTag)) {
				String verb = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				Verb newVerb = new Verb(dependency.gov().index(), verb, false);
				dependencyVerbs.add(newVerb);
			}

			if(PennRelation.valueOfPennRelation(dependency.reln().getShortName()).equals(PennRelation.aux)) {
				auxiliaryVerbs.add(dependency);
			}
		}

		for(TypedDependency dependency: auxiliaryVerbs) {
			int govIndex = dependency.gov().index();
			for (Verb dependencyVerb : dependencyVerbs) {
				if(govIndex == dependencyVerb.getIndex()) {
					dependencyVerb.mergeAuxiliaryVerb(dependency);
				}
			}
		}
		return dependencyVerbs;
	}

	public static LinkedHashSet<WHAdverb> parseWHAdverbsBasedOnDependencies(Collection<TypedDependency> dependencies) {
		LinkedHashSet<WHAdverb> dependencyWHAdverbs = new LinkedHashSet<>();
		for(TypedDependency dependency: dependencies) {
			String depTag = dependency.dep().tag();
			if(PennRelation.valueOfPennRelation(dependency.reln().toString()).equals(PennRelation.advmod) &&
					PennPOSTags.valueOfNullable(depTag).equals(PennPOSTags.WRB)) {
				String whAdverb = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				WHAdverb newWhAdverb = new WHAdverb(dependency.dep().index(), whAdverb, depTag);
				dependencyWHAdverbs.add(newWhAdverb);
			}
		}
		return dependencyWHAdverbs;
	}

	public static LinkedHashSet<Expletive> parseExpletivesBasedOnDependencies(Collection<TypedDependency> dependencies) {
		LinkedHashSet<Expletive> dependencyExpletives = new LinkedHashSet<>();
		for(TypedDependency dependency: dependencies) {
			String relation = dependency.reln().toString();
			if(PennRelation.valueOfPennRelation(relation).equals(PennRelation.expl)) {
				String expletive = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				Expletive newExpletive = new Expletive(dependency.dep().index(), expletive, dependency.dep().tag());
				dependencyExpletives.add(newExpletive);
			}
		}
		return dependencyExpletives;
	}

	private static void prepareEquationsForTriplets(LinkedHashMap<Noun, Triplet> usefulTriplets,
													Question question,
													LinkedHashMap<String, Equation> tripletToEquationMap,
													LinkedHashMap<Question, EquationTree> equationTrees) {
        /*for(Map.Entry<Noun, Triplet> entry : usefulTriplets.entrySet()) {
            Triplet t = entry.getValue();

            if(!tripletToEquationMap.containsKey(t.getSubjectTag())) {
                tripletToEquationMap.put(t.getSubjectTag(), new Equation(t.getSubjectTag()));
            }

            Equation e = tripletToEquationMap.get(t.getSubjectTag());
            e.associateTriplet(t, svmClassifier);
        }*/
		try {
			final EquationTree equationTree = equationTrees.get(question);
			equationTree.addTriplets(usefulTriplets);
		} catch (final Exception e) {
			System.err.println("Cannot create equation tree." + question.getQuestion());
		}
	}

	private static boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
		} catch (final Exception e) {
			return false;
		}
		return true;
	}

	public Collection<TypedDependency> getConjunctionDependencies(Collection<TypedDependency> dependencies) {
		Collection<TypedDependency> conjAndDependencies = new LinkedHashSet<>();
		for(TypedDependency dependency : dependencies) {
			if (PennRelation.valueOfPennRelation(dependency.reln().toString()).equals(PennRelation.conj)) {
				String governerText  = dependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				String dependentText = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
				if (!governerText.equals(dependentText)) {
					conjAndDependencies.add(dependency);
				}
			}
		}
		return conjAndDependencies;
	}

	public List<String> getParsedConjAndNoouns(Collection<TypedDependency> dependencies) {

		List<String> conjAndStrings = new ArrayList<>();
		TypedDependency conjAndDependency = null;
		for(TypedDependency dependency : dependencies) {
			if(PennRelation.valueOfPennRelation(dependency.reln().getShortName()).equals(PennRelation.conj)) {
				conjAndDependency = dependency;
				break;
			}
		}

		if(conjAndDependency != null) {
			boolean firstQuantityFound = false;
			boolean secondQuantityFound = false;
			String governerText  = conjAndDependency.gov().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
			String dependentText = conjAndDependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
			String firstDependency =  governerText + " " + dependentText;
			String secondDependency = dependentText;
			int governerIndex = conjAndDependency.gov().index();

			for(TypedDependency dependency: dependencies) {
				PennRelation currentRelation = PennRelation.valueOfPennRelation(dependency.reln().getShortName());
				if(currentRelation.equals(PennRelation.nummod)
						&& dependency.gov().index() == governerIndex) {
					String firstDepText = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
					firstDependency = firstDepText + " " + firstDependency;
					firstQuantityFound = true;
				} else if(currentRelation.equals(PennRelation.amod) && dependency.gov().index() == conjAndDependency.dep().index()) {
					String firstDepText = dependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
					secondDependency = firstDepText + " " + secondDependency;

					for(TypedDependency nModSecondDependency: dependencies) {
						PennRelation secondDepNmodReln = PennRelation.valueOfPennRelation(nModSecondDependency.reln().getShortName());
						if(secondDepNmodReln.equals(PennRelation.nummod)
								&& nModSecondDependency.gov().index() == conjAndDependency.dep().index()) {
							String nModSecondDepText =nModSecondDependency.dep().backingLabel().getString(edu.stanford.nlp.ling.CoreAnnotations.ValueAnnotation.class);
							secondDependency = nModSecondDepText + " " + secondDependency;
							secondQuantityFound = true;
						}
					}
				}
			}

			if(firstQuantityFound && secondQuantityFound) {
				conjAndStrings.add(firstDependency + " ");
				conjAndStrings.add(secondDependency + " ");
			}
		}
		return conjAndStrings;
	}
}
