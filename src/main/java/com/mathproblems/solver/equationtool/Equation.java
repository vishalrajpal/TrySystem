package com.mathproblems.solver.equationtool;

import com.mathproblems.solver.classifier.SVMClassifier;

import java.util.HashMap;
import java.util.LinkedList;

public class Equation {

    private final String tag;
    private final String leftHandSide;
    private final LinkedList<EquationObject> objectToQuantity;
    private final HashMap<String, Double> objectValues;
    public Equation(String tag) {
        this. tag = tag;
        this.leftHandSide = this.tag;
        objectToQuantity = new LinkedList<>();
        objectValues = new HashMap<>();
    }


    public void associateTriplet(Triplet t, SVMClassifier svmClassifier) {

        double objectQuantity = t.getObjectQuantity();
        if(svmClassifier.libSVMClassify(t.getVerb()) == -1.0) {
            objectQuantity = -objectQuantity;
        }
        EquationObject equationObject = new EquationObject(t.getObjectTag(), objectQuantity);
        objectToQuantity.add(equationObject);
    }

    public void prettyPrintEquation() {
        System.out.print(this.leftHandSide + " = ");
        double currentQuantity;
        String signTag;
        for(EquationObject eo: objectToQuantity) {
            currentQuantity = eo.getQuantity();
            signTag = "+";
            if(currentQuantity < 0) {
                signTag = "-";
                currentQuantity = -currentQuantity;
            }
            System.out.print(signTag + " " + currentQuantity + eo.getObjectTag() + " ");
        }
        System.out.println();
    }

    public void processEquation() {
        for(EquationObject eo: objectToQuantity) {
            if(!objectValues.containsKey(eo.getObjectTag())) {
                objectValues.put(eo.getObjectTag(), eo.getQuantity());
            } else {
                objectValues.put(eo.getObjectTag(), objectValues.get(eo.getObjectTag()) + eo.getQuantity());
            }
        }
        System.out.println(objectValues);
    }
}
