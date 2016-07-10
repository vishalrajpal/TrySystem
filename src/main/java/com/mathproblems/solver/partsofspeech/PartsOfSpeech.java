package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;

import java.util.Set;

public interface PartsOfSpeech {
	
	public int getGovernerIndex();
	public int getDependentIndex();
	public String getGoverner();
	public String getDependent();
	public String getDependentWithQuantity();
	public double getQuantity();
	public PennPOSTags getTag();
	public String getGramletCharacter();
	public Set<Integer> getIndices();
}
