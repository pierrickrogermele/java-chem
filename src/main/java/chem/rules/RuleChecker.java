package org.openscience.chem.rules;

import javax.swing.*;  
import java.awt.event.*;
import java.awt.*;
import org.openscience.cdk.inchi.InChIToStructure;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import net.sf.jniinchi.INCHI_RET;
import org.openscience.cdk.silent.*;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.renderer.*;
import org.openscience.cdk.renderer.font.*;
import org.openscience.cdk.renderer.generators.*;
import org.openscience.cdk.renderer.visitor.*;
import org.openscience.cdk.templates.*;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.Margin;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator.ZoomFactor;

public class RuleChecker extends JFrame implements IChemRuleObserver {

	////////////////////
	// MOLECULE PANEL //
	////////////////////

	class MoleculePanel extends JPanel {

		private IAtomContainer ac = null;

		void setAtomContainer(IAtomContainer ac) {
			this.ac = ac;
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			Dimension size = getSize();
			Insets insets = getInsets();
			int w = size.width - insets.left - insets.right;
			int h = size.height - insets.top - insets.bottom;
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, w, h);

		if (this.ac != null) {
		IAtomContainerSet acset = org.openscience.cdk.graph.ConnectivityChecker.partitionIntoMolecules(this.ac);
		if (acset.getAtomContainerCount() > 0) {
			try {
			IAtomContainer ac = acset.getAtomContainer(0);
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.setMolecule(ac);
			sdg.generateCoordinates();
			ac = sdg.getMolecule();

			java.util.List generators = new java.util.ArrayList();
			generators.add(new BasicSceneGenerator());
			generators.add(new BasicBondGenerator());
			generators.add(new BasicAtomGenerator());
			Rectangle drawArea = new Rectangle(w, h);
			AtomContainerRenderer renderer = new AtomContainerRenderer(generators, new AWTFontManager());
			renderer.setup(ac, drawArea);
			renderer.paint(ac, new AWTDrawVisitor(g2));
			}
			catch (CDKException e) {}
		}
		}
		}
	}

	//////////////////////////////
	// ATOM CONTAINER TO STRING //
	//////////////////////////////

	private static String atomContainerToString(IAtomContainer c) {
		IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(c);
		String s = MolecularFormulaManipulator.getString(formula);
		int charge = formula.getCharge();
		if (charge != 0) {
			char[] strcharge = new char[Math.abs(charge)];
			java.util.Arrays.fill(strcharge, charge > 0 ? '+' : '-');
			s = "(" + s + ")" + new String(strcharge);
		}
		return s;
	}

	//////////////
	// OBSERVER //
	//////////////

	public void chemRuleSuccess(ChemRule rule, IChemRuleSubject subject) {
		String s = "SUCCESS of rule " + rule.getName() + " in " + atomContainerToString(subject.getAtomContainer()) + ".";
		System.out.println(s);
		this.rule_text.setText(this.rule_text.getText() + s + "\n");
	}

	public void chemRuleFailure(ChemRule rule, IChemRuleSubject subject, IChemRuleResult result) {
		String s = "FAILURE of rule " + rule.getName() + " for atoms ";
		int i = 0;
		for (IAtom atom: result.getAtoms()) {
			if (i++ > 0)
				s += ", ";
			s += atom.getSymbol() + " at position " + subject.getAtomContainer().getAtomNumber(atom);
		}
		s += " in " + atomContainerToString(subject.getAtomContainer()) + ".";
		System.out.println(s);
		this.rule_text.setText(this.rule_text.getText() + s + "\n");
	}
	
	//////////////////////////////
	// SMILES TO ATOM CONTAINER //
	//////////////////////////////

	private static IAtomContainer smilesToAtomContainer(String smiles) {

		IAtomContainer container = null;

		try {
			SmilesParser   sp  = new SmilesParser(DefaultChemObjectBuilder.getInstance());
			container   = sp.parseSmiles(smiles);
		} catch (InvalidSmilesException e) {
			System.err.println(e.getMessage());
		}

		return container;
	}

	/////////////////////////////
	// INCHI TO ATOM CONTAINER //
	/////////////////////////////

	private static IAtomContainer inchiToAtomContainer(String inchi) {
		IAtomContainer container = null;
		try {

		InChIToStructure parser = InChIGeneratorFactory.getInstance().getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());

		INCHI_RET ret = parser.getReturnStatus();
		if (ret == INCHI_RET.WARNING) {
			// Structure generated, but with warning message
			System.err.println("InChI warning: " + parser.getMessage());
		} else if (ret != INCHI_RET.OKAY) {
			// Structure generation failed
			//throw new CDKException("Structure generation failed: " + ret.toString() + " [" + parser.getMessage() + "]");
			return null;
		}
		container = parser.getAtomContainer();
		} catch (CDKException e) {
		}

		return container;
	}

	/////////////////
	// CONSTRUCTOR //
	/////////////////

	MoleculePanel mol_panel = null;
	JTextArea rule_text = new JTextArea();

	ChemRule rules = new AllChemRules();

	RuleChecker() {

		rules.addObserver(this);

		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		JLabel mol_label = new JLabel("Molecule:");
		final JTextField mol_field = new JTextField(10);
		JButton process_btn = new JButton("Process");
		mol_panel = new MoleculePanel();
		mol_panel.setSize(300, 300);

//		setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
c.fill = GridBagConstraints.HORIZONTAL;
c.weightx = 0;
c.weighty = 0;
c.gridx = 0;
c.gridy = 0;
		add(mol_label, c);
c.weightx = 0.5;
c.gridx = 1;
c.gridwidth = 2;
		add(mol_field, c);
c.weightx = 0;
c.gridx = 3;
c.gridwidth = 1;
		add(process_btn, c);
c.gridx = 0;
c.gridy = 1;
c.gridwidth = 2;
c.weightx = 0.5;
c.weighty = 1.0;
//c.anchor = GridBagConstraints.PAGE_END;
//c.insets = new Insets(10,0,0,0);
c.fill = GridBagConstraints.BOTH;
		add(mol_panel, c);
c.gridx = 2;
		add(rule_text, c);
		this.pack();

		process_btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rule_text.setText("");
				String mol_str = mol_field.getText();
				IAtomContainer ac = null;
				if (mol_str.length() > 6 && mol_str.substring(0, 6).equals("InChI=")) {
					ac = inchiToAtomContainer(mol_str);
				}
				else {
					ac = smilesToAtomContainer(mol_str);
				}
				if (ac != null) {
					mol_panel.setAtomContainer(ac);
					mol_panel.invalidate();

					rules.check(new ChemRuleSubject(ac));
				}
			}          
		});

		setVisible(true);//now frame will be visible, by default not visible 
	}
	
	//////////
	// MAIN //
	//////////

	public static void main(String[] args) {
		new RuleChecker();  
	}

}
