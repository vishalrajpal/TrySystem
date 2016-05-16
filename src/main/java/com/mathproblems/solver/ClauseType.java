package com.mathproblems.solver;

public enum ClauseType {

    SV(VerbType.INTRANSITIVE),
    SVC(VerbType.COPULAR),
    SVA(VerbType.EXTENDED_COPULAR),
    SVO(VerbType.MONOTRANSITIVE),
    SVOO(VerbType.DITRANSITIVE),
    SVOC(VerbType.COMPLEX_TRANSITIVE),
    SVOA(VerbType.COMPLEX_TRANSITIVE),
    EXISTENTIAL(VerbType.UNKNOWN),
    UNKNOWN(VerbType.UNKNOWN);

    private VerbType associatedVerbType;
    ClauseType(VerbType verbType) {
        associatedVerbType = verbType;
    }

    public VerbType getVerbType() {
        return associatedVerbType;
    }
}
