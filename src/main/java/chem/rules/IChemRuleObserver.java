package org.openscience.chem.rules;

public interface IChemRuleObserver {

	///////////////////////
	// CHEM RULE SUCCESS //
	///////////////////////

	public void chemRuleSuccess(ChemRule rule, IChemRuleSubject subject);

	///////////////////////
	// CHEM RULE FAILURE //
	///////////////////////

	public void chemRuleFailure(ChemRule rule, IChemRuleSubject subject, IChemRuleResult result);
}
