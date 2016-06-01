package com.mathproblems.solver.equationtool;

import com.mathproblems.solver.partsofspeech.Noun;

public class Triplet {

    private final String subject;
    private final String subjectTag;
    private final int subjectIndex;
    private final String verb;
    private final String verbTag;
    private final int verbIndex;
    private final String object;
    private final String objectTag;
    private final int objectIndex;

    private Integer subjectQuantity;
    private Integer objectQuantity;

    public Triplet(final String subject, final String subjectTag, final int subjectIndex,
                   final String verb, final String verbTag, final int verbIndex,
                   final String object, final String objectTag, final int objectIndex) {
        this.subject = subject;
        this.subjectTag = subjectTag;
        this.subjectIndex = subjectIndex;
        this.verb = verb;
        this.verbTag = verbTag;
        this.verbIndex = verbIndex;
        this.object = object;
        this.objectTag = objectTag;
        this.objectIndex = objectIndex;
    }

    public boolean isEquivalentToPOSNoun(Noun n) {
        boolean isALink = false;
        if(matchesToSubject(n)) {
            //matchingTag = "SBJ";
            isALink = true;
        } else if (matchesToObject(n)) {
            //matchingTag = "OBJ";
            isALink = true;
        }
        return isALink;
    }

    public boolean matchesToSubject(Noun n) {
        boolean match = false;
        if(subject != null && subject.toLowerCase().equals(n.getDependent().toLowerCase())
                && subjectIndex == n.getDependentIndex()) {
            match = true;
        }
        return match;
    }

    public boolean matchesToObject(Noun n) {
        boolean match = false;
        if (object != null && object.toLowerCase().equals(n.getDependent().toLowerCase())
                && objectIndex == n.getDependentIndex()) {
            match = true;
        }
        return match;
    }

    public void setSubjectQuantity(int quantity) {
        subjectQuantity = quantity;
    }

    public void setObjectQuantity(int quantity) {
        objectQuantity = quantity;
    }

    public Integer getSubjectQuantity() {
        return subjectQuantity;
    }

    public Integer getObjectQuantity() {
        return objectQuantity;
    }

    public String getSubject() {
        return subject;
    }

    public String getSubjectTag() {
        return subjectTag;
    }

    public int getSubjectIndex() {
        return subjectIndex;
    }

    public String getVerb() {
        return verb;
    }

    public String getVerbTag() {
        return verbTag;
    }

    public int getVerbIndex() {
        return verbIndex;
    }

    public String getObject() {
        return object;
    }

    public String getObjectTag() {
        return objectTag;
    }

    public int getObjectIndex() {
        return objectIndex;
    }

    @Override
    public String toString() {
        return "Triplet{" +
                "subject='" + subject + '\'' +
                ", subjectTag='" + subjectTag + '\'' +
                ", subjectIndex=" + subjectIndex +
                ", verb='" + verb + '\'' +
                ", verbTag='" + verbTag + '\'' +
                ", verbIndex=" + verbIndex +
                ", object='" + object + '\'' +
                ", objectTag='" + objectTag + '\'' +
                ", objectIndex=" + objectIndex +
                "}\n";
    }
}
