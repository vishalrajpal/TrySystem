package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;
import srl.mateplus.MatePlusDeprel;

public class Verb {
	private final int index;
	private final String verb;
	private final String lemma;
	private final String subject;
	private final MatePlusDeprel subjectDeprel;
	private final String object;
	private final MatePlusDeprel objectDeprel;
	private final String adverb;
	private final MatePlusDeprel adverbDeprel;
	private final PennPOSTags tag;
	public Verb(final int index,
				final String verb,
				final PennPOSTags tag,
				final String lemma,
				final String subject,
				final MatePlusDeprel subjectDeprel,
				final String object,
				final MatePlusDeprel objectDeprel,
				final String adverb,
				final MatePlusDeprel adverbDeprel) {
		this.index = index;
		this.verb = verb;
		this.tag = tag;
		this.lemma = lemma;
		this.subject = subject;
		this.object = object;
		this.adverb = adverb;
		this.subjectDeprel = subjectDeprel;
		this.objectDeprel = objectDeprel;
		this.adverbDeprel = adverbDeprel;
	}

	public int getIndex() {
		return index;
	}

	public String getVerb() {
		return verb;
	}

	public PennPOSTags getTag() {
		return tag;
	}

	public String getLemma() {
		return lemma;
	}

	public String getSubject() {
		return subject;
	}

	public String getObject() {
		return object;
	}

	public MatePlusDeprel getSubjectDeprel() {
		return subjectDeprel;
	}

	public MatePlusDeprel getObjectDeprel() {
		return objectDeprel;
	}

	public String getAdverb() {
		return adverb;
	}

	public MatePlusDeprel getAdverbDeprel() {
		return adverbDeprel;
	}

	@Override
	public String toString() {
		return "Verb{" +
				"index=" + index +
				", verb='" + verb + '\'' +
				", lemma='" + lemma + '\'' +
				", subject='" + subject + '\'' +
				", subjectDeprel=" + subjectDeprel +
				", object='" + object + '\'' +
				", objectDeprel=" + objectDeprel +
				", adverb='" + adverb + '\'' +
				", adverbDeprel=" + adverbDeprel +
				", tag=" + tag +
				'}';
	}
}
