package org.openscience.chem.rules;

import org.openscience.cdk.interfaces.IAtom;

// TODO Bad name "ChemRuleResult". This is not a result. It stores the location where the rule has been applied (atoms).

public class ChemRuleResult implements IChemRuleResult {

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	public ChemRuleResult(IAtom a, IAtom b) {
		this.atoms.add(a);
		this.atoms.add(b);
	}

	///////////
	// ATOMS //
	///////////

	private java.util.List<IAtom> atoms = new java.util.ArrayList<IAtom>();

	public java.util.List<IAtom> getAtoms() {
		return this.atoms;
	}
}
