package srl.mateplus;

import se.lth.cs.srl.CompletePipeline;
import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.CompletePipelineCMDLineOptions;
import se.lth.cs.srl.options.FullPipelineOptions;

import java.util.Arrays;

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
        String[] tokens = pipeline.pp.tokenize(text); // tokenize
        Sentence s = pipeline.parse(Arrays.asList(tokens)); // process the text (tokens)

        System.out.println();

        // a sentence is just a list of words
        int size = s.size();
        for(int i = 1; i<size; i++) {
            Word w = s.get(i); // skip word number 0 (ROOT token)
            // each word object contains information about a word's actual word form / lemma / POS
            System.out.println(w.getForm() + "\t " + w.getLemma() + "\t" + w.getPOS());
        }

        System.out.println();

        // some words in a sentence are recognized as predicates
        for(Predicate p : s.getPredicates()) {
            // every predicate has a sense that defines its semantic frame
            System.out.println(p.getForm() + " (" + p.getSense()+ ")");
            // show arguments from the semantic frame that are instantiated in a sentence
            for(Word arg : p.getArgMap().keySet()) {
                System.out.print("\t" + p.getArgMap().get(arg) + ":");
                // "arg" is just the syntactic head word; let's iterate through all words in the argument span
                for(Word w : arg.getSpan())
                    System.out.print(" " + w.getForm());
                System.out.println();
            }

            System.out.println();

        }

    }
}
