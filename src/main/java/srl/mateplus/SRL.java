package srl.mateplus;

import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.options.FullPipelineOptions;

import java.util.*;

public class SRL {

    private CompletePipeline pipeline;

    public SRL(String[] commandlineoptions) {
        FullPipelineOptions options = new CompletePipelineCMDLineOptions();
        options.parseCmdLineArgs(commandlineoptions); // process options
        try {
            pipeline = CompletePipeline.getCompletePipeline(options); // initialize pipeline
        } catch (final Exception e) {
            System.err.println("Unable to initialize SRL.");
            e.printStackTrace();
        }
    }

    public CompletePipeline getPipeline() {
        return pipeline;
    }

    public void parse(String text) throws Exception {
        String[] tokens = pipeline.pp.tokenize(text); // this is how you tokenize your text
        Sentence s = pipeline.parse(Arrays.asList(tokens)); // this is how you then process the text (tokens)

        final HashSet<Map<String, String>> triplets = new LinkedHashSet<>();
        Map<String, String> triplet;
        // some words in a sentence are recognized as predicates
        for(Predicate p : s.getPredicates()) {
            if (p.getPOS().startsWith("VB")) {
                triplet = new LinkedHashMap<>();
                for (Word arg : p.getArgMap().keySet()) {
                    if (arg.getDeprel().equals("SBJ") || arg.getDeprel().equals("OBJ")) {
                        triplet.put(p.getArgumentTag(arg), arg.getLemma());
                    }
                }
                triplet.put(p.getPOS(), p.getLemma());
                triplets.add(triplet);
            }
        }
        System.out.println(triplets);

    }
}
