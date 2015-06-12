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
}
