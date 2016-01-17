import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.chem.CdkHelper;
import org.openscience.chem.ChemObsConsole;
import org.openscience.cdk.exception.CDKException;

public class TestCdkHelper {

	//////////////////////////////
	// TEST MAKE ATOM CONTAINER //
	//////////////////////////////

	@Test
	public void test_makeAtomContainer() {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		assertNotNull(hlp.makeAtomContainer("C[C+](C)O"));
		assertNotNull(hlp.makeAtomContainer("C[C-](C)O"));
		assertNotNull(hlp.makeAtomContainer("InChI=1S/CH4/h1H4"));
	}

	@Test
	public void test_makeAtomContainers() {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		String[] strings = {"C[C+](C)O", "C[C-](C)O", "InChI=1S/CH4/h1H4"};
		IAtomContainer[] ac = hlp.makeAtomContainers(strings);
		assertNotNull(ac);
		assertEquals(strings.length, ac.length);
	}

	////////////////////////////////
	// TEST CONTAINS SUBSTRUCTURE //
	////////////////////////////////

	@Test
	public void test_containsSubstructure() throws CDKException {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		assertTrue(hlp.containsSubstructure(hlp.makeAtomContainer("InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3"), hlp.makeFunctionalGroup("ALCOHOL")));
		assertFalse(hlp.containsSubstructure(hlp.makeAtomContainer("InChI=1S/C2H6/c1-2/h1-2H3"), hlp.makeFunctionalGroup("ALCOHOL")));
		assertTrue(hlp.containsSubstructure(hlp.makeAtomContainer("InChI=1/C13H18Cl2N2O2/c14-5-7-17(8-6-15)11-3-1-10(2-4-11)9-12(16)13(18)19/h1-4,12H,5-9,16H2,(H,18,19)/t12-/m0/s1"), hlp.makeFunctionalGroup("CARBOXYL")));
	}

	@Test
	public void test_containSubstructure() throws CDKException {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		String[] mols = {"InChI=1S/C2H6O/c1-2-3/h3H,2H2,1H3", "InChI=1S/C2H6/c1-2/h1-2H3"};
		boolean[] contain = hlp.containSubstructure(hlp.makeAtomContainers(mols), hlp.makeFunctionalGroup("ALCOHOL"));
		assertNotNull(contain);
		assertEquals(contain.length, mols.length);
	}

	////////////////////////////////
	// TEST MAKE FUNCTIONAL GROUP //
	////////////////////////////////

	@Test
	public void test_makeFunctionalGroup() {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		assertNotNull(hlp.makeFunctionalGroup(CdkHelper.FunctionalGroup.ALCOHOL));
		assertNotNull(hlp.makeFunctionalGroup(CdkHelper.FunctionalGroup.CARBOXYL));
		assertNotNull(hlp.makeFunctionalGroup("ALCOHOL"));
		assertNotNull(hlp.makeFunctionalGroup("CARBOXYL"));
	}

	////////////////////
	// TEST GET INCHI //
	////////////////////

	@Test
	public void test_getInchi() {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		String inchi = "InChI=1S/CH4/h1H4";
		assertTrue(hlp.getInchi(hlp.makeAtomContainer(inchi)).equals(inchi));
	}

	////////////////////
	// TEST TO STRING //
	////////////////////

	@Test
	public void test_toString() {
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		IAtomContainer acetonep = hlp.makeAtomContainer("C[C+](C)O");
		IAtomContainer acetonem = hlp.makeAtomContainer("C[C-](C)O");

		assertTrue(org.openscience.chem.CdkHelper.toString(acetonep).endsWith("+"));
		assertTrue(org.openscience.chem.CdkHelper.toString(acetonem).endsWith("-"));
	}

	///////////////////
	// TEST LOAD SDF //
	///////////////////

	@Test
	public void test_loadSdf() {
		java.net.URL url = Thread.currentThread().getContextClassLoader().getResource("metfrag-chemspider-output.sdf");
		java.io.File file = new java.io.File(url.getFile());
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		IAtomContainer[] mols = hlp.loadSdf(file);
		assertNotNull(mols);
		assertTrue(mols.length > 0);
	}
}
