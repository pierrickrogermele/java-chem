package org.openscience.chem.rules;

public abstract class ChemRule {

	///////////////
	// OBSERVERS //
	///////////////

	protected java.util.List<IChemRuleObserver> observers = new java.util.ArrayList<IChemRuleObserver>();

	public void addObserver(IChemRuleObserver obs) {
		this.observers.add(obs);
	}

	///////////
	// CHECK //
	///////////

	public abstract boolean check(IChemRuleSubject subject);

	//////////
	// NAME //
	//////////

	public abstract String getName();

	//////////
	// TEXT //
	//////////

	public abstract String getText();
}
