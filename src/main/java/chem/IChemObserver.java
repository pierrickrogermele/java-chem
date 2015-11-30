package org.openscience.chem;

public interface IChemObserver {

	//////////////////
	// CHEM WARNING //
	//////////////////

	public void chemWarning(String s);

	////////////////
	// CHEM ERROR //
	////////////////

	public void chemError(String s);
}
