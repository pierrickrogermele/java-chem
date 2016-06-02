package org.openscience.chem;

import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.config.Isotopes;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IChemFile;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.Isotope;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.DefaultChemObjectBuilder;
import net.sf.jniinchi.INCHI_RET;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.io.ReaderFactory;
import org.openscience.cdk.io.ISimpleChemObjectReader;
import org.openscience.cdk.tools.manipulator.ChemFileManipulator;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.graph.matrix.AdjacencyMatrix;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

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

	////////////////////
	// COUNT ISOTOPES //
	////////////////////

	public java.util.Map<IIsotope, Integer> countIsotopes(IAtomContainer molecule) throws CDKException {

		java.util.Map<IIsotope, Integer> count = new java.util.TreeMap<IIsotope, Integer>(
				new java.util.Comparator<IIsotope>() {
					@Override
					public int compare(IIsotope a, IIsotope b) {
						if (a.getAtomicNumber() != b.getAtomicNumber())
							return a.getAtomicNumber().compareTo(b.getAtomicNumber());
						return a.getMassNumber().compareTo(b.getMassNumber());
					}
				}
				);

		// Loop on all atoms
		for (IAtom a: molecule.atoms()) {
			try {
				IIsotope t = a.getMassNumber() == null ? Isotopes.getInstance().getMajorIsotope(a.getAtomicNumber()) : new Isotope(a.getSymbol(), a.getMassNumber());
				if (count.containsKey(t))
					count.put(t, new Integer(count.get(t) + 1));
				else
					count.put(t, new Integer(1));
			} catch(java.io.IOException e) {
				throw new CDKException("Unable to load isotopes data file.");
			}
		}

		return count;
	}

	///////////////////////
	// FIND SUBTRUCTURES //
	///////////////////////

	/**
	 * @param molecule: The molecule in which to search for substructures.
	 * @param mass: Mass of the searched substructure.
	 * @param charge: Charge of the searched substructures.
	 * @param err: Error between specified mass and exact mass of found substructure, in ppm.
	 */
	public void findSubstructures(IAtomContainer molecule, double mz, int charge, double err) throws CDKException {

		// Add atom information into the molecule
		this.setAtomTypes(molecule);
		this.addExplicitHydrogens(molecule);

		// A
		// Find sets of atoms that are in mz.
		java.util.Map<IIsotope, Integer> atom_types = this.countIsotopes(molecule);
		java.util.Iterator i = atom_types.entrySet().iterator();
		while (i.hasNext()) {
			java.util.Map.Entry<IIsotope, Integer> mapEntry = (java.util.Map.Entry<IIsotope, Integer>)i.next();
			System.out.println("Atom " + mapEntry.getKey().getSymbol() + mapEntry.getKey().getMassNumber() + " count is : " + mapEntry.getValue() + ".");
		}
//		java.util.List<String> atoms = AtomContainerManipulator.getAllIDs(molecule);
//		for (String a: atoms)
//			System.err.println("ATOM : " + a);
		//MolecularFormulaRange
		// For each set, generate all possible atom combinations in the molecule.
		// For each atom combination, check the connectivity using org/openscience/cdk/graph/ConnectivityChecker.

		// B
		// Look for subgroups matching with mz and charge, starting from heavy atoms.
/*		int[][] adjm = AdjacencyMatrix.getMatrix(molecule);
		java.util.Stack<Integer> group = new java.util.Stack<Integer>();
		java.util.Stack<Double> mass = new java.util.Stack<Double>(); // Use stack also for mass in order to avoid subtractions.
		for (int i = 0 ; i < adjm.length ; ++i) {
			group.push(i);
			mass.push(molecule.getAtom(i).getExactMass());
			cur_atom = i;
			next_atom = 0;
			while (true) {
				if (adjm[cur_atom][next_atom] == 1) {
				}
			}
		}
		*/

		// Return solutions
	}

	///////////////////////
	// FUNCTIONAL GROUPS //
	///////////////////////

	public enum FunctionalGroup {
		ALCOHOL, CARBOXYL;
		public static final java.util.Map<String, FunctionalGroup> fromString = new java.util.HashMap<String, FunctionalGroup>();
		static {
			for (FunctionalGroup g: FunctionalGroup.values())
			fromString.put(g.toString(), g);
		}
	}

	public IAtomContainer makeFunctionalGroup(String group) {
		return this.makeFunctionalGroup(FunctionalGroup.fromString.get(group));
	}

	public IAtomContainer makeFunctionalGroup(FunctionalGroup group) {

		IAtomContainer grp = null;

		switch(group) {
			case ALCOHOL:
				grp = new AtomContainer();
				grp.addAtom(new Atom("O"));
				grp.addAtom(new Atom("H"));
				grp.addBond(new Bond(grp.getAtom(0), grp.getAtom(1)));
				break;

			case CARBOXYL:
				grp = new AtomContainer();
				grp.addAtom(new Atom("C"));
				grp.addAtom(new Atom("O"));
				grp.addAtom(new Atom("O"));
				grp.addAtom(new Atom("H"));
				grp.addBond(new Bond(grp.getAtom(0), grp.getAtom(1), IBond.Order.DOUBLE));
				grp.addBond(new Bond(grp.getAtom(0), grp.getAtom(2)));
				grp.addBond(new Bond(grp.getAtom(2), grp.getAtom(3)));
				break;
		}

		return grp;
	}

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

	////////////////////
	// SET ATOM TYPES //
	////////////////////

	public void setAtomTypes(IAtomContainer ac) throws CDKException {
		CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(ac.getBuilder());
		for (IAtom atom: ac.atoms()) {
			IAtomType type = matcher.findMatchingAtomType(ac, atom);
			AtomTypeManipulator.configure(atom, type);
		}
	}

	////////////////////////////
	// ADD EXPLICIT HYDROGENS //
	////////////////////////////

	public void addExplicitHydrogens(IAtomContainer ac) throws CDKException {
		CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(ac.getBuilder());
		adder.addImplicitHydrogens(ac);
		AtomContainerManipulator.convertImplicitToExplicitHydrogens(ac);
	}

	//////////////////////////////
	// CONTAIN FUNCTIONAL GROUP //
	//////////////////////////////

	public boolean[] containFunctionalGroup(String[] inchi, String group) throws CDKException {
		return containSubstructure(this.makeAtomContainers(inchi), this.makeFunctionalGroup(group));
	}

	///////////////////////////
	// CONTAINS SUBSTRUCTURE //
	///////////////////////////

	public boolean[] containSubstructure(IAtomContainer[] molecules, IAtomContainer substructure) throws CDKException {

		boolean[] contain = new boolean[molecules.length];

		this.setAtomTypes(substructure);
		UniversalIsomorphismTester tester = new UniversalIsomorphismTester();

		for (int i = 0 ; i < molecules.length ; ++i) {
			this.setAtomTypes(molecules[i]);
			this.addExplicitHydrogens(molecules[i]);
			contain[i] = tester.isSubgraph(molecules[i], substructure);
		}

		return contain;
	}

	public boolean containsSubstructure(IAtomContainer molecule, IAtomContainer substructure) throws CDKException {
		this.setAtomTypes(molecule);
		this.addExplicitHydrogens(molecule);
		this.setAtomTypes(substructure);
		return new UniversalIsomorphismTester().isSubgraph(molecule, substructure);
	}

	/////////////////////////
	// MAKE ATOM CONTAINER //
	/////////////////////////

	public IAtomContainer[] makeAtomContainers(String[] strings) {

		IAtomContainer[] ac = new IAtomContainer[strings.length];

		for (int i = 0 ; i < ac.length ; ++i)
			ac[i] = this.makeAtomContainer(strings[i]);

		return ac;
	}

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

	///////////////
	// GET INCHI //
	///////////////

	public String getInchi(IAtomContainer ac) {

		String inchi = null;

		try {
			/* TODO Calling getInChIGenerator() triggers the loading of the native compiled InChI shared library and the display of an output like this one:
   0    [main] INFO  net.sf.jnati.deploy.artefact.ConfigManager  - Loading global configuration
   7    [main] DEBUG net.sf.jnati.deploy.artefact.ConfigManager  - Loading defaults: jar:file:/Users/pierrick/dev/distrib-metabohub/java-cdk/cdk-1.5.10.jar!/META-INF/jnati/jnati.default-properties
   7    [main] INFO  net.sf.jnati.deploy.artefact.ConfigManager  - Loading artefact configuration: jniinchi-1.03_1
   8    [main] DEBUG net.sf.jnati.deploy.artefact.ConfigManager  - Loading instance defaults: jar:file:/Users/pierrick/dev/distrib-metabohub/java-cdk/cdk-1.5.10.jar!/META-INF/jnati/jnati.instance.default-properties
   11   [main] INFO  net.sf.jnati.deploy.repository.ClasspathRepository  - Searching classpath for: jniinchi-1.03_1-MAC-X86_64
   13   [main] INFO  net.sf.jnati.deploy.repository.LocalRepository  - Searching local repository for: jniinchi-1.03_1-MAC-X86_64
   13   [main] DEBUG net.sf.jnati.deploy.repository.LocalRepository  - Artefact path: /Users/pierrick/.jnati/repo/jniinchi/1.03_1/MAC-X86_64
   14   [main] INFO  net.sf.jnati.deploy.artefact.ManifestReader  - Reading manifest
   124  [main] INFO  net.sf.jnati.deploy.NativeArtefactLocator  - Artefact (jniinchi-1.03_1-MAC-X86_64) location: /Users/pierrick/.jnati/repo/jniinchi/1.03_1/MAC-X86_64
   124  [main] DEBUG net.sf.jnati.deploy.NativeLibraryLoader  - Loading library: /Users/pierrick/.jnati/repo/jniinchi/1.03_1/MAC-X86_64/JniInchi-1.03_1-MAC-X86_64
			TODO How to disable this output ?
			*/

			inchi = InChIGeneratorFactory.getInstance().getInChIGenerator(ac).getInchi();
		} catch (CDKException e) {
				if (this.obs != null)
					this.obs.chemError("ERROR: Failure while trying to generate InChI: " + e.getMessage() + ".");
		}

		return inchi;
	}

	//////////////
	// LOAD SDF //
	//////////////

	public IAtomContainer[] loadSdf(String file) {
		return this.loadSdf(new java.io.File(file));
	}

	public IAtomContainer[] loadSdf(java.io.File file) {

		java.util.List<IAtomContainer> aclist = null;

		ReaderFactory readerFactory = new ReaderFactory();
		try {
			ISimpleChemObjectReader reader = readerFactory.createReader(new java.io.FileReader(file));
			if (reader != null) {
				IChemFile content = (IChemFile) reader.read(DefaultChemObjectBuilder.getInstance().newInstance(IChemFile.class));
				if (content != null) {
					aclist = ChemFileManipulator.getAllAtomContainers(content);
				}
			}
		} catch (java.io.IOException e) {
			if (this.obs != null)
				this.obs.chemError("ERROR: Failure while trying to load SDF file \"" + file.getAbsolutePath() + "\": " + e.getMessage() + ".");
		} catch (CDKException e) {
			if (this.obs != null)
				this.obs.chemError("ERROR: Failure while trying to load SDF file \"" + file.getAbsolutePath() + "\": " + e.getMessage() + ".");
		}

		return aclist != null ? aclist.toArray(new IAtomContainer[0]) : null;
	}
}
