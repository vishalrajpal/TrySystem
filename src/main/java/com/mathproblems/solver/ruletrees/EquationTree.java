package com.mathproblems.solver.ruletrees;

import com.mathproblems.solver.classifier.SVMClassifier;
import com.mathproblems.solver.equationtool.Triplet;
import com.mathproblems.solver.facttuple.Question;
import com.mathproblems.solver.partsofspeech.Noun;
import se.lth.cs.srl.languages.Language;

import java.util.*;

/**
 * Created by Rajpals on 6/3/16.
 */
public class EquationTree {

    private LinkedHashMap<Noun, Triplet> triplets;
    private Node bottomMostNode = null;
    private Node topMostNode;
    private SVMClassifier svmClassifier;
    private int noOfOperatorNodes = 0;
    private Question question;
    public EquationTree(final Question question, final SVMClassifier svmClassifier) {
        triplets = new LinkedHashMap<>();
        this.question = question;
        this.svmClassifier = svmClassifier;
    }

    public void addTriplets(LinkedHashMap<Noun, Triplet> usefulTriplets) {

        Node operatorNode = null;

        for(Map.Entry<Noun, Triplet> entry: usefulTriplets.entrySet()) {
            final Node currentNode = new Node(entry.getKey(), entry.getValue(), false, null, svmClassifier, false);
            if(operatorNode == null) {
                operatorNode = new Node(null, null, true, currentNode.getVerbSign(), null, false);
                noOfOperatorNodes++;
                if(topMostNode != null) {
                    operatorNode.addChild(topMostNode);
                    topMostNode.setParentNode(operatorNode);
                }
                topMostNode = operatorNode;
            }

            if(bottomMostNode == null) {
                bottomMostNode = currentNode;
            }

            operatorNode.addChild(currentNode);
            currentNode.setParentNode(operatorNode);
        }
    }

    public void prettyPrintTree() {

        LinkedList<Node> nodeQueue = new LinkedList<>();
        nodeQueue.offerFirst(topMostNode);
        int nextLevelSize = 0;
        int currentLevelSize = 1;
        int currentLevelCounter = 0;
        while (nodeQueue.size() > 0) {
            Node currentNode = nodeQueue.pollFirst();
            currentLevelCounter++;
            System.out.print(currentNode + " ");
            nodeQueue.addAll(currentNode.getChildren());
            nextLevelSize += currentNode.getChildren().size();
            if(currentLevelCounter == currentLevelSize) {
                System.out.println();
                currentLevelSize = nextLevelSize;
                nextLevelSize = 0;
                currentLevelCounter = 0;
            }
        }
        System.out.println();
    }

    public void generateEquationTrees() {
        LinkedList<EquationTree> combinationTrees = new LinkedList<>();
        List<List<String>> operatorCombinations = prepareOperatorCombinations(this.noOfOperatorNodes);
        for(List<String> currentCombination: operatorCombinations) {
            EquationTree copiedTreeAsPerCurrentCombination = copyTree(currentCombination);
            combinationTrees.add(copiedTreeAsPerCurrentCombination);
            copiedTreeAsPerCurrentCombination.prettyPrintTree();
            solveTree(copiedTreeAsPerCurrentCombination);
            System.out.println(copiedTreeAsPerCurrentCombination.getTopMostNode().getValue());
        }
    }

    private EquationTree copyTree(List<String> currentCombination) {
        EquationTree newEquationTree = new EquationTree(question, svmClassifier);
        Node newTopMostNode;
        int currentCounter = 0;
        if(topMostNode.isOperatorNode()) {
            newTopMostNode = new Node(null, null, true, currentCombination.get(currentCounter++), svmClassifier, true);
        } else {
            newTopMostNode = new Node(topMostNode.getNoun(), topMostNode.getTriplet(), false, null, svmClassifier, true);
        }
        newEquationTree.topMostNode = newTopMostNode;

        if(topMostNode.equals(bottomMostNode)) {
            newEquationTree.bottomMostNode = newTopMostNode;
        }
        for(Node child: topMostNode.getChildren()) {
            Node newChild = copyNode(child, currentCombination, currentCounter, newEquationTree);
            newChild.setParentNode(newTopMostNode);
            newTopMostNode.addChild(newChild);
        }
        return newEquationTree;
    }

    private Node copyNode(Node currentNode, List<String> currentCombination, int currentCounter, EquationTree currentEquationTree) {
        Node newCopyNode;
        if(currentNode.isOperatorNode()) {
            newCopyNode = new Node(null, null, true, currentCombination.get(currentCounter++), svmClassifier, true);
        } else {
            newCopyNode = new Node(currentNode.getNoun(), currentNode.getTriplet(), false, null, svmClassifier, true);
        }

        if(currentNode.equals(bottomMostNode)) {
            currentEquationTree.bottomMostNode = newCopyNode;
        }
        for(Node child: currentNode.getChildren()) {
            Node newChild = copyNode(child, currentCombination, currentCounter, currentEquationTree);
            newChild.setParentNode(newCopyNode);
            newCopyNode.addChild(newChild);
        }
        return newCopyNode;
    }

    public List<List<String>> prepareOperatorCombinations(int max) {
        if (max == 1) {
            List<List<String>> defaultCombinations = new ArrayList<>();
            List<String> defaultAddList = new ArrayList<>();
            defaultAddList.add("+");
            defaultCombinations.add(defaultAddList);

            List<String> defaultSubtractList = new ArrayList<>();
            defaultSubtractList.add("-");
            defaultCombinations.add(defaultSubtractList);
            return defaultCombinations;
        }
        List<List<String>> restCombinations = prepareOperatorCombinations(max - 1);
        List<List<String>> combinedWithRest = new ArrayList<>();
        for(List<String> currentCombination: restCombinations) {
            List<String> newAddList = new ArrayList<>(currentCombination);
            newAddList.add(0, "+");
            List<String> newSubtractList = new ArrayList<>(currentCombination);
            newSubtractList.add(0, "-");
            combinedWithRest.add(newAddList);
            combinedWithRest.add(newSubtractList);
        }
        return combinedWithRest;
    }

    public void solveTree() {
        solveTree(this);
    }

    private void solveTree(EquationTree tree) {
        Node bottomsParent = tree.bottomMostNode.getParentNode();
        if(bottomsParent.isOperatorNode()) {
            Node parentNode = bottomsParent;

            while(parentNode != null) {
                double currentVal;
                int sign = parentNode.getOperator();
                if(parentNode.getChildren().size() > 0) {
                    currentVal = parentNode.getChildren().get(0).getValue();
                    for(int childCount = 1; childCount<parentNode.getChildren().size(); childCount++) {
                        currentVal += (sign * parentNode.getChildren().get(childCount).getValue());
                    }
                    parentNode.setValue(currentVal);
                }
                parentNode = parentNode.getParentNode();
            }
        }
        return;
    }

    public void setTriplets(LinkedHashMap<Noun, Triplet> triplets) {
        this.triplets = triplets;
    }

    public void setBottomMostNode(Node bottomMostNode) {
        this.bottomMostNode = bottomMostNode;
    }

    public void setTopMostNode(Node topMostNode) {
        this.topMostNode = topMostNode;
    }

    public void setSvmClassifier(SVMClassifier svmClassifier) {
        this.svmClassifier = svmClassifier;
    }

    public void setNoOfOperatorNodes(int noOfOperatorNodes) {
        this.noOfOperatorNodes = noOfOperatorNodes;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public LinkedHashMap<Noun, Triplet> getTriplets() {
        return triplets;
    }

    public Node getBottomMostNode() {
        return bottomMostNode;
    }

    public Node getTopMostNode() {
        return topMostNode;
    }

    public SVMClassifier getSvmClassifier() {
        return svmClassifier;
    }

    public int getNoOfOperatorNodes() {
        return noOfOperatorNodes;
    }

    public Question getQuestion() {
        return question;
    }
}
