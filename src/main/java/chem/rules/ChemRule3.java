package org.openscience.chem.rules;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

public class ChemRule3 extends ChemRule {

	final java.util.List<String> neighbours = new java.util.ArrayList<String>(java.util.Arrays.asList("O", "N", "P", "S")); 

	//////////
	// NAME //
	//////////

	public String getName() {
		return "3";
	}

	//////////
	// TEXT //
	//////////

	public String getText() {
		return "A positive charge is forbidden on an α carbon of an X atom, where X = O, N, P or S.";
	}

	///////////
	// CHECK //
	///////////

	public boolean check(IChemRuleSubject subject) {

		boolean success = true;

		// Loop on all atoms.
		for (IAtom atom: subject.getAtomContainer().atoms()) {

			// Check the charge of the atom.
			if (atom.getFormalCharge() > 0) {

				// Loop on first level neighbours of the atom.
				for (IAtom neighbour: subject.getAtomContainer().getConnectedAtomsList(atom)) {

					// Check the second level neighbours of the atom.
					if (neighbour.getSymbol().equals("C")) {
						System.out.println("Carbon neighbour.");
							for (IAtom neighbour2: subject.getAtomContainer().getConnectedAtomsList(neighbour)) {
								if (neighbour2 != atom) {
						System.out.println("A second neighbour which is different from the start atom.");
						if (subject.getAtomContainer().getBond(neighbour, neighbour2).getOrder().equals(IBond.Order.DOUBLE)) {
						System.out.println("With a double bond.");
									if (this.neighbours.contains(neighbour.getSymbol())) {
										success = false;
										for (IChemRuleObserver obs: this.observers)
											obs.chemRuleFailure(this, subject, new ChemRuleResult(atom, neighbour));
									}
								}
							}
						}
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

