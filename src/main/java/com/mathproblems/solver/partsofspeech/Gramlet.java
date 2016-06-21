package com.mathproblems.solver.partsofspeech;

import java.util.*;

public enum Gramlet {

    NVQN,
    NVQNVN,
    NVQNNVQN,
    NVQNCQNN,
    NVQNCNVQN,
    NVQNCQN,
    NVQNCVQN,
    ENVQN,
    ENVQNCENVQN,
    ENVQNCQN,
    ENVQNCVQN,
    QNVQN,
    NVQNN,
    VQNPPVQNP,
    NVN,
    NVP,
    NVQNPNP,
    NVV,
    PNVN,
    VQNP,
    NNV,
    VNP,
    VNV,
    NVQNVP,
    NNVV,
    NVVP,
    NVNN,
    NVNP,
    NPNV,
    NNVNV,
    NVNV,
    PNVQN,
    VP,
    VNPNP,
    VQNNCN,
    NVQNVQNP,
    VV,
    NP,
    NVQNCNVQNP,
    NV,
    NPN,
    N,
    VQNPN,
    NVQNPV,
    QNP,
    V,
    NVQNNVQNNVQNPN,
    NVQNPN,
    NVVQN,
    VPV,
    VVNVQNV,
    NNVP,
    NVPN,
    NVQNV,
    VQNV,
    NVPP,
    NVNPPN,
    VQN,
    NVNPPVQNVN,
    NVNPP,
    NVVQNV,
    NVNPN,
    NVQNP,
    QNPP,
    NVQNCNVQNPN,
    NNVNQNCNNVNQN,
    NVQNCNVQNN,
    NN,
    NVQNNNN,
    VVNNVQNV,
    NVNNVQNNN,
    NNNNVQNV,
    NNVNNV,
    NNN,
    QNN,
    NVNNNNN,
    NVNQN,
    NVQNNNVQNNN,
    NNVQNCQN,
    NVNNNNVQNVN,
    NVQNNVQNNVQNNN,
    NVQNNV,
    NVNQNNNVN,
    NVNVQN,
    NNVNQN,
    NNVQN,
    NNVN,
    QN,
    NNNVNVQNCQN,
    NVQNNN,
    NVNVNV,
    NVQNCQNNNN,
    NNNN,
    NNVQNNNN,
    VNNV,
    NNNVN,
    NNNVQN,
    NNNV,
    NVQNNNQNN,
    NNVNNVN,
    NVNNNN,
    NVQNNCN,
    NNVNNN,
    NVNNN,
    NVQNNNNV,
    NNVQNN,
    NVQNCNVQNNN,
    NNVNN,
    QNNN,
    NVVNN,
    NVQNVQNN;

    public static void addToLexicon(Map<String, Integer> lexicon) {

        for(Gramlet g: Gramlet.values()) {
            lexicon.put(g.toString(), g.toString().hashCode());
        }

        Set<String> allGramletChars = new HashSet<>();
        allGramletChars.add("N");
        allGramletChars.add("V");
        allGramletChars.add("P");
        allGramletChars.add("C");
        allGramletChars.add("Q");

        for(String str: allGramletChars) {
            lexicon.put(str, str.hashCode());
        }
    }

    static Set<String> unknownGramlets = new HashSet<>();
    Map<Character, Integer> counts;
    Gramlet() {
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
