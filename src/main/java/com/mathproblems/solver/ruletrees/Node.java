package com.mathproblems.solver.ruletrees;

import com.mathproblems.solver.classifier.SVMClassifier;
import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.partsofspeech.Noun;

import java.util.LinkedList;

public class Node {

    private LinkedList<Node> children;
    private Noun noun;
    private Triplet triplet;
    private boolean isOperatorNode;
    private String operator;
    private int verbSign;
    private SVMClassifier svmClassifier;
    private Node parentNode;
    private double value;
    public Node(Noun noun, Triplet triplet, boolean isOperatorNode, String operator, SVMClassifier svmClassifier, boolean isCopyNode) {
        this.noun = noun;
        this.triplet = triplet;
        this.isOperatorNode = isOperatorNode;
        this.operator = operator;
        this.children = new LinkedList<>();
        this.svmClassifier = svmClassifier;
        this.value = 0;
        if(!isOperatorNode) {
            this.value = this.triplet.getObjectQuantity();
            if(!isCopyNode) {
                this.verbSign = classifyVerb();
            }
        }
    }

    public void addChild(Node child) {
        children.offerLast(child);
    }

    private int classifyVerb() {
        int sign = 1;
        if (svmClassifier.libSVMClassify(triplet.getVerb()) == -1.0) {
            sign = -1;
        }
        return sign;
    }

    public String getVerbSign() {
        String sign = "+";
        if (this.verbSign < 0) {
            sign = "-";
        }
        return sign;
    }

    public void setParentNode(Node parentNode) {
        this.parentNode = parentNode;
    }

    @Override
    public String toString() {
        if(isOperatorNode) {
            return operator;
        } else {
            return String.valueOf(this.triplet.getObjectQuantity());
        }
    }

    public LinkedList<Node> getChildren() {
        return children;
    }

    public Noun getNoun() {
        return noun;
    }

    public Triplet getTriplet() {
        return triplet;
    }

    public boolean isOperatorNode() {
        return isOperatorNode;
    }

    public int getOperator() {
        int sign = 1;
        if(operator.equals("-")) {
            sign = -1;
        }
        return sign;
    }

    public SVMClassifier getSvmClassifier() {
        return svmClassifier;
    }

    public Node getParentNode() {
        return parentNode;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
