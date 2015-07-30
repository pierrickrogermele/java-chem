import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import fr.cea.chem.CdkHelper;
import fr.cea.chem.ChemObsConsole;

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

		assertTrue(fr.cea.chem.CdkHelper.toString(acetonep).endsWith("+"));
		assertTrue(fr.cea.chem.CdkHelper.toString(acetonem).endsWith("-"));
	}

	///////////////////
	// TEST LOAD SDF //
	///////////////////

	@Test
	public void test_loadSdf() {
		String root = fr.cea.lib.Meta.getPackageRoot(this.getClass());
		java.io.File file = new java.io.File(new java.io.File(root), "metfrag-chemspider-output.sdf");
		CdkHelper hlp = new CdkHelper(new ChemObsConsole());
		IAtomContainer[] mols = hlp.loadSdf(file);
		assertNotNull(mols);
		assertTrue(mols.length > 0);
	}
}
