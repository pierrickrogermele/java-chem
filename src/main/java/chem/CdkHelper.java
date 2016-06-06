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
import org.openscience.cdk.formula.MassToFormulaTool;
import org.openscience.cdk.interfaces.IMolecularFormulaSet;
import org.openscience.cdk.formula.MolecularFormulaRange;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import org.openscience.cdk.formula.rules.IRule;
import org.openscience.cdk.formula.rules.ToleranceRangeRule;
import org.openscience.cdk.formula.rules.ElementRule;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

public final class CdkHelper {

	/////////////////////////
	// IISOTOPE COMPARATOR //
	/////////////////////////

	public static class IIsotopeComparator implements Comparator<IIsotope> {
		@Override
		public int compare(IIsotope x, IIsotope y) {
			if (x.getAtomicNumber() != y.getAtomicNumber())
				return Integer.compare(x.getAtomicNumber(), y.getAtomicNumber());
			return Integer.compare(x.getMassNumber(), y.getMassNumber());
		}
	}

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

	/////////////////////////
	// UPDATE MASS NUMBERS //
	/////////////////////////

	public void updateMassNumbers(IAtomContainer c) throws CDKException {

		try {
			for (IAtom a: c.atoms())
				if (a.getMassNumber() == null)
					a.setMassNumber(Isotopes.getInstance().getMajorIsotope(a.getAtomicNumber()).getMassNumber());
		} catch (java.io.IOException e) {
			throw new CDKException(e.getMessage());
		}
	}

	/////////////////////
	// UPDATE ALL INFO //
	/////////////////////

	public void updateAllInfo(IAtomContainer c) throws CDKException {
		this.setAtomTypes(c);
		this.addExplicitHydrogens(c);
		this.updateMassNumbers(c);
	}

	////////////////
	// FIND ATOMS //
	////////////////

	public List<IAtom> findAtoms(IAtomContainer ac, IIsotope isotope) {

		List<IAtom> atoms = new ArrayList<IAtom>();
		IIsotopeComparator iso_comp = new IIsotopeComparator();

		for (IAtom a: ac.atoms())
			if (iso_comp.compare(a, isotope) == 0)
				atoms.add(a);

		return atoms;
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

		// Add missing atom information into the molecule
		this.updateAllInfo(molecule);

		System.err.println("findSubstructures(): molecule =  " + this.toString(molecule) + ", mz = " + mz + ", charge = " + charge + ", err = " + err);

		// List atoms present inside the molecule
		IMolecularFormula mf = MolecularFormulaManipulator.getMolecularFormula(molecule);
		for (IIsotope i: mf.isotopes())
			System.err.println("Atom " + i.getSymbol() + i.getMassNumber() + " count is : " + mf.getIsotopeCount(i) + ".");

		// Find sets of atoms that are in mz.
		MassToFormulaTool mtft = new MassToFormulaTool(DefaultChemObjectBuilder.getInstance());
		{
			// Add rules
			List<IRule> rules = new ArrayList<IRule>();
			{
				// Add mass tolerance rule
				ToleranceRangeRule tolerance_rule = new ToleranceRangeRule();
				Double[] tolerance_rule_param = { mz, mz * err * 1e-6 };
				tolerance_rule.setParameters(tolerance_rule_param);
				rules.add(tolerance_rule);

				// Add set of isotopes
				ElementRule element_rule = new ElementRule();
				MolecularFormulaRange[] element_rule_param = new MolecularFormulaRange[1];
				element_rule_param[0] = new MolecularFormulaRange();
				try {
					for (IIsotope i: mf.isotopes())
						element_rule_param[0].addIsotope(Isotopes.getInstance().getIsotope(i.getSymbol(), i.getMassNumber()), 0, mf.getIsotopeCount(i));
				} catch (java.io.IOException e) {
					throw new CDKException(e.getMessage());
				}
				element_rule.setParameters(element_rule_param);
				rules.add(element_rule);
			}

			mtft.setRestrictions(rules);
		}
		IMolecularFormulaSet formula_set = mtft.generate(mz);

		// Display TODO --> toward observer ?
		for (IMolecularFormula imf: formula_set.molecularFormulas())
			System.err.println(MolecularFormulaManipulator.getString(imf));

		// For each set, generate all possible atom combinations in the molecule.
		List<java.util.List<IAtom>> combinations = new ArrayList<List<IAtom>>();
		for (IMolecularFormula imf: formula_set.molecularFormulas()) {
			Map<IIsotope, List<IAtom>> isotope_to_atoms = new TreeMap<IIsotope, List<IAtom>>(new IIsotopeComparator());
			for (IIsotope i: imf.isotopes()) {

				// Isotope count
//				int ic = imf.getIsotopeCount(i);

				// Get all atoms equivalent to this isotope inside the molecule
				List<IAtom> atoms = this.findAtoms(molecule, i);
				isotope_to_atoms.put(i, atoms);

			}

			// Get all possible combinations inside the molecule

			// For each atom combination, check the connectivity using org/openscience/cdk/graph/ConnectivityChecker.
		}

		// C
		/* 
		   Create a Stack<IAtom> atom_stack
		   atom_stack.push(heavy atoms of molecule)
		   Stack<IAtom> substructure;
		   while (atom_stack NOT EMPTY)
		   	   cur_atom = atom_stack.pop()
		   	   if (cur_atom NOT IN substructure) then
		  	    	substructure.push(cur_atom)
			    	atom_stack.push(NEW_ATOM)
		   	        atom_stack.push(cur_atom.getConnectedAtomsList())
		        
		*/

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
