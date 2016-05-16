package com.mathproblems.solver;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Clause {
    private static final String CLAUSE_TYPE_REGEX = "([SVCAOUNKNOWNEXISTENTIAL]{2,})";
    private static final String CONSTITUENTS_PREFIX = "([A-Za-z][:]{1}( ){1}[a-zA-Z0-9]+[@]{1}[0-9]+)";
    private static final String SUBJECT_INITIAL = "S";
    private static final String VERB_INITIAL = "V";
    private static final String OBJECT_INITIAL = "O";
    private static final String COMPLEMENT_INITIAL = "C";
    private static final String ADVERB_INITIAL = "A";

    private final ClauseType clauseType;
    private final Map<String, String> constituents;

    public Clause(final String clauseString) {
        constituents = new HashMap<>();
        clauseType = parseClauseType(clauseString);
        parseConstituents(clauseString);
    }

    private ClauseType parseClauseType(final String clauseString) {
        ClauseType clauseType = ClauseType.UNKNOWN;
        final Pattern clauseTypePattern = Pattern.compile(CLAUSE_TYPE_REGEX);
        final Matcher clauseTypeMatcher = clauseTypePattern.matcher(clauseString);
        if (clauseTypeMatcher.find()) {
            final String currentValue = clauseTypeMatcher.group();
            try {
                clauseType = ClauseType.valueOf(currentValue);
            } catch (final Exception e) {
                System.err.println("unable to parse clause type value: " + currentValue);
            }

        }
        return clauseType;
    }

    private void parseConstituents(final String clauseString) {
        final Pattern constituentsPattern = Pattern.compile(CONSTITUENTS_PREFIX);
        final Matcher constituentsMatcher = constituentsPattern.matcher(clauseString);
        while (constituentsMatcher.find()) {
            final String currentValue = constituentsMatcher.group();
            try {
                final String[] split = currentValue.split(":");
                constituents.put(split[0], split[1]);
            } catch (final Exception e) {
                System.err.println("unable to parse constituent value: " + currentValue);
            }
        }
    }
}
