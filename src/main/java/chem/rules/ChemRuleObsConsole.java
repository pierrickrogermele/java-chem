package org.openscience.chem.rules;

import org.openscience.cdk.interfaces.IAtom;

public class ChemRuleObsConsole implements IChemRuleObserver {

	///////////////////////
	// CHEM RULE SUCCESS //
	///////////////////////

	public void chemRuleSuccess(ChemRule rule, IChemRuleSubject subject) {
		System.out.println("SUCCESS of rule " + rule.getName() + " in " + org.openscience.chem.CdkHelper.toString(subject.getAtomContainer()) + ".");
	}

	///////////////////////
	// CHEM RULE FAILURE //
	///////////////////////

	public void chemRuleFailure(ChemRule rule, IChemRuleSubject subject, IChemRuleResult result) {
		System.out.print("FAILURE of rule " + rule.getName() + " for atoms ");
		int i = 0;
		for (IAtom atom: result.getAtoms()) {
			if (i++ > 0)
				System.out.print(", ");
			System.out.print(atom.getSymbol() + " at position " + subject.getAtomContainer().getAtomNumber(atom));
		}
		System.out.println( " in " + org.openscience.chem.CdkHelper.toString(subject.getAtomContainer()) + ".");
	}
}
