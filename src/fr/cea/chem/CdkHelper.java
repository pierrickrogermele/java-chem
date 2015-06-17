package fr.cea.chem;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.DefaultChemObjectBuilder;
import net.sf.jniinchi.INCHI_RET;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;

public final class CdkHelper {

	//////////////////
	// CONSTRUCTORS //
	//////////////////

	public CdkHelper() {
	}

	public CdkHelper(IChemObserver obs) {
		this.obs = obs;
	}

	//////////////
	// OBSERVER //
	//////////////

	IChemObserver obs;

	///////////////
	// TO STRING //
	///////////////

	public static String toString(IAtomContainer c) {
		IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(c);
		return toString(formula);
	}

	public static String toString(IMolecularFormula formula) {

		String s = "";

		try {
        IsotopeFactory ifac = Isotopes.getInstance();

		for (IIsotope i: formula.isotopes()) {
			if (i.getMassNumber() != ifac.getMajorIsotope(i.getSymbol()).getMassNumber()) {
				if (s.length() != 0)
					s += ":";
				s += i.getMassNumber();
			}
			s += i.getSymbol();
			if (formula.getIsotopeCount(i) > 1)
				s += formula.getIsotopeCount(i);
		}
        } catch (java.io.IOException e) {
			s = MolecularFormulaManipulator.getString(formula);
	    }
		
		Integer charge = formula.getCharge();
		if (charge != null && charge != 0) {
			char[] strcharge = new char[Math.abs(charge)];
			java.util.Arrays.fill(strcharge, charge > 0 ? '+' : '-');
			s = "(" + s + ")" + new String(strcharge);
		}
		return s;
	}

	/////////////////////////
	// MAKE ATOM CONTAINER //
	/////////////////////////

	public IAtomContainer makeAtomContainer(String s) {

		IAtomContainer ac = null;

		// InChI
		if (s.startsWith("InChI=")) {
			InChIToStructure parser = null;
			try {
				parser = InChIGeneratorFactory.getInstance().getInChIToStructure(s, DefaultChemObjectBuilder.getInstance());
			} catch (CDKException e) {
				 if (this.obs != null)
					 this.obs.chemError("ERROR: Can not instantiate InChI parser: " + e.getMessage() + ".");
			}

			INCHI_RET ret = parser.getReturnStatus();
			switch(ret) {
				case WARNING: if (this.obs != null) this.obs.chemWarning("WARNING: Structure generation from InChI encountered issues: " + parser.getMessage()); // This is just a warning, so let's set the atom container anyway.
				case OKAY: ac = parser.getAtomContainer(); break;
				default: if (this.obs != null) this.obs.chemError("ERROR: Structure generation from InChI failed: " + ret.toString() + " [" + parser.getMessage() + "]");
			}
		}

		// Smiles
		else {
			try {
				SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
				ac = sp.parseSmiles(s);
			} catch (InvalidSmilesException e) {
				if (this.obs != null)
					this.obs.chemError("ERROR: Structure generation from SMILES failed: " + e.getMessage() + ".");
			}
		}

		return ac;
	}
}
