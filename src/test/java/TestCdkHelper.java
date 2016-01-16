import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.chem.CdkHelper;
import org.openscience.chem.ChemObsConsole;

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
