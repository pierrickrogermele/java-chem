package org.openscience.chem.rules;

public class ChemRuleContainer extends ChemRule {

	private java.util.List<ChemRule> rules = new java.util.ArrayList<ChemRule>();

	///////////////
	// OBSERVERS //
	///////////////

	@Override
	public void addObserver(IChemRuleObserver obs) {
		super.addObserver(obs);

		for (ChemRule rule: this.rules)
			rule.addObserver(obs);
	}

	///////////
	// RULES //
	///////////

	public void addRule(ChemRule rule) {

		// add to list of rules	
		this.rules.add(rule);

		// add observers to this new rule
		for (IChemRuleObserver obs: this.observers)
			rule.addObserver(obs);
	}

	///////////
	// CHECK //
	///////////

	@Override
	public boolean check(IChemRuleSubject subject) {

		boolean result = true;

		for (ChemRule rule: this.rules)
			if  ( ! rule.check(subject))
				result = false;

		return result;
	}

	//////////
	// NAME //
	//////////

	@Override
	public String getName() {
		return "Container rule.";
	}

	//////////
	// TEXT //
	//////////

	@Override
	public String getText() {
		return "This rule has for object to gather a set of rules together.";
	}

}
