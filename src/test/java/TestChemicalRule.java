import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import net.sf.jniinchi.INCHI_RET;
import org.openscience.chem.CdkHelper;
import org.openscience.chem.rules.ChemRule;
import org.openscience.chem.rules.ChemRule1;
import org.openscience.chem.rules.ChemRuleSubject;
import org.openscience.chem.rules.IChemRuleSubject;
import org.openscience.chem.rules.IChemRuleResult;
import org.openscience.chem.rules.ChemRuleObsConsole;

public class TestChemicalRule {

	/////////////////
	// TEST RULE 1 //
	/////////////////

	@Test
	public void test_ChemicalRule1() {
		
		CdkHelper cdk = new CdkHelper(new org.openscience.chem.ChemObsConsole());

		IAtomContainer good_mol = cdk.makeAtomContainer("InChI=1S/C3H6O/c1-3(2)4/h1-2H3/q+1");
		IAtomContainer acetone = cdk.makeAtomContainer("CC(C)O");
		IAtomContainer acetonep = cdk.makeAtomContainer("C[C+](C)O");
		IAtomContainer acetonem = cdk.makeAtomContainer("C[C-](C)O");

		// Bad molecule
		IAtomContainer bad_mol = new AtomContainer();
		IAtom c1 = new Atom("C");
		IAtom c2 = new Atom("C");
		IAtom c3 = new Atom("C");
		IAtom o = new Atom("O");
		bad_mol.addAtom(c1);
		bad_mol.addAtom(c2);
		bad_mol.addAtom(c3);
		bad_mol.addAtom(o);
		bad_mol.addBond(new Bond(c1, c2, IBond.Order.SINGLE));
		bad_mol.addBond(new Bond(c3, c2, IBond.Order.SINGLE));
		bad_mol.addBond(new Bond(c2, o, IBond.Order.SINGLE));
		c2.setFormalCharge(-1);

		// Apply rule
		ChemRule rule1 = new ChemRule1();
		rule1.addObserver(new ChemRuleObsConsole());
		assertTrue(rule1.check(new ChemRuleSubject(good_mol)));
		assertFalse(rule1.check(new ChemRuleSubject(bad_mol)));
		assertTrue(rule1.check(new ChemRuleSubject(acetone)));
		assertTrue(rule1.check(new ChemRuleSubject(acetonep)));
		assertFalse(rule1.check(new ChemRuleSubject(acetonem)));
	}

	/////////////////
	// TEST RULE 3 //
	/////////////////

	@Test
	public void test_ChemicalRule3() {
	}

}
