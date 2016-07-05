package com.mathproblems.solver.partsofspeech;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Rajpals on 7/3/16.
 */
public enum FeatureGramlet {

    /*NPNVQN,
    NVQNNVQNCNVQNPNN,
    QNPVVWNVNV,
    NVQNPNNCQNPN,
    PNNVQN,
    NNVNQN,
    NVQNCQN,
    NVQNPPNNCVQNVN,
    WNVNVV,
    NVNPNN,
    NNVNQNCNNVNQN,
    NVNPNP,
    NVQNCNVQNPNN,
    NVVPNP,
    NVPNN,
    NVVPNN,
    WNPVNV,
    NVNWNEVPN,
    NVQNVPN,
    WVN,
    NVVNCVQN,
    NNVQNPN,
    PNNNNVQNCQN,
    PNNVQNPPNVN,
    NVQNPN,
    NVVQN,
    NNVQN,
    WNV,
    PNNVQNPN,
    NVQNV,
    WNVNVVN,
    EVQNPPN,
    PNPNVQN,
    WVNVPN,
    WNVPN,
    EVQNP,
    NVNPN,
    NVQNP,
    QNPVVNWNVVN,
    NVQNN,
    NVQNPNPN,
    NVVPN,
    WNNVNVP,
    NVQNCQNN,
    NVN,
    WNVNV,
    WVNVVN,
    PNNNVQN,
    NVQNPNN,
    NVQNPNP,
    EVNPN,
    WNVVEVPN,
    NNVQNCQN,
    NPNVQNN,
    WNNVWNVV,
    WNVNVPN,
    PNNVQNCEVN,
    NVPNCVNV,
    NVVN,
    NVQNPNCQNPN,
    NVPNWNVQNPN,
    WNVNVPNN,
    EVQNPN,
    NVNN,
    WNVEPN,
    NVQNPNCQNPNN,
    NVNP,
    EVQN,
    NVNV,
    WVNVN,
    NVQNPNWNNV,
    WNVPNN,
    WNVVN,
    QNVV,
    VNNVQNV,
    WVNVV,
    WNVEPNN,
    NV,
    NVQNPPN,
    WVNV,
    NVNPNNPN,
    NVQNPNPCVQNPNN,
    NVQNCQNPNNN,
    PNWNVNV,
    WNVPWNVV,
    NVQNNVQN,
    NVPN,
    NVVQNVV,
    WNVNVNPN,
    NVNQNPWNVNVP,
    NPNPVQNV,
    NVQNCVQNP,
    NPNVN,
    WNVP,
    NVQNCNVQNPN,
    WNVVPN,
    WNVV,
    PNPNNVQN,
    WNVNVN,
    WNNVNV,
    PNNNVQNPPNVN,
    NVVPNNN,
    PNNPNNVQN,
    NNNVQN,
    WNVVEVNPN,
    WNNNVNVP,
    WNNVNVNPN,
    NVNQNPWNNVNVP,
    NVNNN,
    WNNV,
    NVQNPNNWNNV,
    WNNVNVVN,
    NVNPQN,
    NVQNPNNN,
    NVQN,*/

    PNNNNVQNCQN,
    WNNVNVP,
    NNVQNCQN,
    VNNVQNV,
    NVQNCQNPNNN,
    NVQNCNVQNPN,
    PNPNNVQN,
    WNVNVN,
    WNNVNV,
    PNNNVQNPPNVN,
    NVVPNNN,
    PNNPNNVQN,
    NNNVQN,
    WNVVEVNPN,
    WNNNVNVP,
    WNNVNVNPN,
    NVNQNPWNNVNVP,
    NVNNN,
    WNNV,
    NVQNPNNWNNV,
    WNNVNVVN,
    NVNPQN,
    NVQNPNNN,
    NPNVQN,
    NVQNPVN,
    NVN,
    NVQNNVQNCNVQNPNN,
    QNPVVWNVNV,
    WNVNV,
    WVNVVN,
    NVQNCVQNPNNN,
    PNNNVQN,
    NVQNPNN,
    NVQNPNNCQNPN,
    NVQNPNP,
    EVNPN,
    WNVVEVPN,
    PNNVQN,
    NVQNCVNVQNPNN,
    NNVNQN,
    NPNVQNN,
    NNVNQNCVNNVNQN,
    NVQNCQN,
    WNVNVPN,
    PNNVQNCEVN,
    PNNVNNVQNCVQN,
    NVQNCVQN,
    NVPNCVNV,
    NVVN,
    NVQNPNCQNPN,
    NVQNPPNNCVQNVN,
    NVPNWNVQNPN,
    WNVNVV,
    WNVNVPNN,
    EVQNPN,
    NVNN,
    WNVEPN,
    NVQNPNCQNPNN,
    NVNP,
    EVQN,
    NVNPNN,
    WVNVPVN,
    NVNV,
    WVNVN,
    NVNPNP,
    NVQNPNWNNV,
    WNVPNN,
    NVVPNP,
    NVPNN,
    QNVV,
    NVVPNN,
    WVNVV,
    WNVEPNN,
    WNPVNV,
    NV,
    VVNNVQNV,
    NVQNPPN,
    NVNWNEVPN,
    NVQNVPN,
    WVN,
    WVNV,
    NVVNCVQN,
    NNVQNPN,
    NVNPNNPN,
    NVQNPNPCVQNPNN,
    PNWNVNV,
    NVQNCQNVN,
    PNNVQNPPNVN,
    NVQNNVQN,
    NVQNPN,
    NVVQN,
    NNVQN,
    PNNVQNPN,
    NVQNV,
    NVPN,
    NVVQNVV,
    NVNQNPWNVNVP,
    EVQNPPN,
    WNVNVVNVP,
    PNPNVQN,
    NPNPVQNV,
    NVQNCVQNP,
    WVNVPN,
    NNVQNCVQN,
    WNVPN,
    NPNVN,
    EVQNP,
    NVQNCVNVQNPN,
    NVNPN,
    NVQNP,
    QNPVVNWNVVN,
    NVQNN,
    WNVVPN,
    NVQNPNPN,
    NVVPN,
    WNVV,
    NVNVPN,
    EVQNCQNPN,
    EVQNPVQNP,
    NVQNCNVQN,
    WNVE,
    WNVNVNPN,
    WNVPWNVV,
    WNVNVVN,
    WNVVN,
    WVNVNV,
    WNVP,
    WNVVNVV,
    WNNVWNVV,
    WNV,
    NVQNCNVQNPNN,
    NVQNCVNVQN,
    EVQNCQNP,
    NVQNCQNP,
    NVPNPCQNP,
    NVPNP,
    WNPVNVV,
    NVVNVPN,
    NVQNVN,
    NVQNNCQN,
    EVQNPNCQNPN,
    NVQNCQNPN,
    NVQNCQNPNN,
    NNVNQNCNNVNQN,
    CNVQNCQN,
    P,
    PN,
    CNVQNP,
    WVNVPNN,
    NVPVNNCVN,
    WNVN,
    NVQNVV,
    CNVQN,
    CVVPNN,
    NVPNNN,
    WNNVPN,
    CQNPVV,
    NVQNCQNN,
    NVV,
    NVQN;


    public static void addToLexicon(Map<String, Integer> lexicon) {

        for(FeatureGramlet featureGramlet: FeatureGramlet.values()) {
            lexicon.put(featureGramlet.toString(), featureGramlet.toString().hashCode());
        }

        Set<String> allGramletChars = new HashSet<>();
        allGramletChars.add("N");
        allGramletChars.add("V");
        allGramletChars.add("P");
        allGramletChars.add("C");
        allGramletChars.add("Q");
        allGramletChars.add("W");
        allGramletChars.add("E");

        for(String str: allGramletChars) {
            lexicon.put(str, str.hashCode());
        }
    }

    static Set<String> unknownGramlets = new HashSet<>();
    Map<Character, Integer> counts;

    FeatureGramlet() {
        counts = new HashMap<>();
        for(Character c: this.toString().toCharArray()) {
            int count = 1;
            if(counts.containsKey(c)) {
                count += counts.get(c);
            }
            counts.put(c, count);
        }
    }

    public char firstChar() {
        return this.toString().charAt(0);
    }

    public String lastChar() {
        String featureGramletString = this.toString();
        return featureGramletString.substring(featureGramletString.length() - 1);
    }

    public int noOfVerbs() {
        return getCountOf('V');
    }

    public int noOfPrepositions() {
        return getCountOf('P');
    }

    public int noOfNouns() {
        return getCountOf('N');
    }

    public int noOfCounjunctions() {
        return getCountOf('C');
    }

    public int noOfQuantities() {
        return getCountOf('Q');
    }

    public boolean hasWHAdverb() {
        return getCountOf('W') > 0;
    }

    public boolean containsPattern(String pattern) {
        return this.toString().contains(pattern);
    }

    private int getCountOf(char c) {
        int count = 0;
        if(counts.containsKey(c)) {
            count = counts.get(c);
        }
        return count;
    }

    public static FeatureGramlet valueOfFeatureGramlet(String gramletString) {
        FeatureGramlet featureGramlet = FeatureGramlet.NVQN;
        try {
            featureGramlet = valueOf(gramletString);
        } catch (Exception e) {
            unknownGramlets.add(gramletString);
        }
        return featureGramlet;
    }

    public static void printUnknownGramlets() {
        System.out.print(unknownGramlets);
    }
}
