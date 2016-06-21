package com.mathproblems.solver.partsofspeech;

import com.mathproblems.solver.PennPOSTags;

public interface PartsOfSpeech {
	
	public int getGovernerIndex();
	public int getDependentIndex();
	public String getGoverner();
	public String getDependent();
	public int getQuantity();
	public PennPOSTags getTag();
}
