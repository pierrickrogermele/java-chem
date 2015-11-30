package fr.cea.chem;

public class ChemObsConsole implements IChemObserver {

	//////////////////
	// CHEM WARNING //
	//////////////////

	public void chemWarning(String s) {
		System.err.println(s);
	}

	////////////////
	// CHEM ERROR //
	////////////////

	public void chemError(String s) {
		System.err.println(s);
	}
}
