import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;

public class TestCdkHelper {

	////////////////////
	// TEST TO STRING //
	////////////////////

	@Test
	public void test_toString() {
		IAtomContainer acetonep = null;
		IAtomContainer acetonem = null;
		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			acetonep  = sp.parseSmiles("C[C+](C)O");
			acetonem  = sp.parseSmiles("C[C-](C)O");
		} catch (InvalidSmilesException e) {
			System.err.println(e.getMessage());
		}

		assertTrue(fr.cea.chem.CdkHelper.toString(acetonep).endsWith("+"));
		assertTrue(fr.cea.chem.CdkHelper.toString(acetonem).endsWith("-"));
	}
}
