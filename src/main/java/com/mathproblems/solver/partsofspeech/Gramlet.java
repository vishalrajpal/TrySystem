package com.mathproblems.solver.partsofspeech;

import java.util.*;

public enum Gramlet {
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
    NVQN;

    public static void addToLexicon(Map<String,
            Integer> lexicon) {

        for(Gramlet g: Gramlet.values()) {
            lexicon.put(g.toString(), g.toString().hashCode());
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
    Map<Character,
            Integer> counts;
    Gramlet() {
        counts = new HashMap<>();
        for(Character c: this.toString().toCharArray()) {
            int count = 1;
            if(counts.containsKey(c)) {
                count += counts.get(c);
            }
            counts.put(c,
                    count);
        }
    }

    public char firstChar() {
        return this.toString().charAt(0);
    }

    public String lastChar() {
        String gramletStirng = this.toString();
        return gramletStirng.substring(gramletStirng.length() - 1);
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

    public static Gramlet valueOfGramlet(String gramletString) {
        Gramlet gramlet = Gramlet.NVQN;
        try {
            gramlet = valueOf(gramletString);
        } catch (Exception e) {
            unknownGramlets.add(gramletString);
        }
        return gramlet;
    }

    public static void printUnknownGramlets() {
        System.out.print(unknownGramlets);
    }
}
