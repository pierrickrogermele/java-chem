package org.openscience.chem.rules;

import org.openscience.cdk.interfaces.IAtomContainer;

public class ChemRuleSubject implements IChemRuleSubject {

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public ChemRuleSubject(IAtomContainer container) {
		this.atom_container = container;
	}

	////////////////////
	// ATOM CONTAINER //
	////////////////////

	private IAtomContainer atom_container;

	public IAtomContainer getAtomContainer() {
		return this.atom_container;
	}

}
