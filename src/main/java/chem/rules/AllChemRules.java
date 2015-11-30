package org.openscience.chem.rules;

public final class AllChemRules extends ChemRuleContainer {

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public AllChemRules() {
		this.addRule(new ChemRule1());
		this.addRule(new ChemRule3());
	}
}
