package org.openscience.chem.rules;

import org.openscience.cdk.interfaces.IAtom;

public class ChemRule1 extends ChemRule {

	final java.util.List<String> neighbours = new java.util.ArrayList<String>(java.util.Arrays.asList("O", "N", "P", "S")); 

	//////////
	// NAME //
	//////////

	public String getName() {
		return "1";
	}

	//////////
	// TEXT //
	//////////

	public String getText() {
		return "A negative charge is forbidden on an α carbon of a C=X group, where X = O, N, P or S.";
	}

	///////////
	// CHECK //
	///////////

	public boolean check(IChemRuleSubject subject) {

		boolean success = true;

		// Loop on all atoms.
		for (IAtom atom: subject.getAtomContainer().atoms()) {

			// Check the charge of the atom.
			if (atom.getFormalCharge() < 0) {

				// Check the neighbours of the atom.
				for (IAtom neighbour: subject.getAtomContainer().getConnectedAtomsList(atom)) {
					if (this.neighbours.contains(neighbour.getSymbol())) {
						success = false;
						for (IChemRuleObserver obs: this.observers)
							obs.chemRuleFailure(this, subject, new ChemRuleResult(atom, neighbour));
					}
				}
			}
		}

		// Call observers about success
		if (success)
			for (IChemRuleObserver obs: this.observers)
				obs.chemRuleSuccess(this, subject);

		return success;
	}
}
