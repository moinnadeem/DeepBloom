package edu.mit.BloomFilter.Swing;

import javax.swing.*;

import edu.mit.BloomFilter.SandwichedBloomFilter.SandwichedBloomFilterImp;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

/**
 * - Input a file and set a FPR
- Click a learn button
- It learns the file in both sandwiched and regular implementation
- It prints out the size of the filters for both implementations
 *
 */
public class Swing extends JFrame {
	private static final long serialVersionUID = 1L;

	private static final double BACKUP_FPR = 0.01;
	private static final double INITIAL_FPR = 0.1;
	private static final int APPROX_N = 344821;
	private static final File DEFAULT_FILE = new File("model_training/data.csv");
	private SandwichedBloomFilterImp sandwichedBf = new SandwichedBloomFilterImp();
	
	private JButton fileButton = new JButton("Choose File");
	private JFileChooser chooser;
	private JTextField fileField = new JTextField(200);
	private String filePath = "";
	private File selectedFile;
	private double fpr = 0.0;
	private JTextArea textArea = new JTextArea();
	
	public Swing() {
		super();
		
		this.setTitle("DeepBloom");
		
		JPanel label = new JPanel();
		label.setOpaque(true);
		label.setBackground(new Color(248, 213, 131));
		label.setPreferredSize(new Dimension(500, 250));
		this.setContentPane(label);
		this.setGridLayout();
		
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private GridBagConstraints getGridConstraints(int gridx, int gridy) {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.PAGE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = gridx;
		c.gridy = gridy;
		return c;
	}
	
	private void setGridLayout() {
		boolean rightToLeft = false;
		
		Container pane = this.getContentPane();
		
		GridBagLayout layout = new GridBagLayout();
		layout.columnWeights = new double[] {1, 1, 1};
		pane.setLayout(layout);
		
		if (rightToLeft) {
			pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		}
		
		GridBagConstraints c1 = getGridConstraints(0, 0);
		c1.weightx = 0.5;
		c1.gridwidth = 3;
		pane.add(this.makeFileTextField(), c1);
		
	    GridBagConstraints c2 = getGridConstraints(3, 0);
	    c2.weightx = 0.5;
	    c2.gridwidth = 1;
	    pane.add(this.makeChooseFileButton(), c2);

		GridBagConstraints c3 = getGridConstraints(0, 1);
		c3.weightx = 0.5;
		c3.gridwidth = GridBagConstraints.REMAINDER;
		pane.add(this.makeFprField(), c3);
	    
	    GridBagConstraints c4 = getGridConstraints(0, 2);
	    c4.ipady = 40;      //make this component tall
	    c4.weightx = 0.0;
	    c4.gridwidth = GridBagConstraints.REMAINDER;
	    pane.add(this.makeLearnButton(), c4);
	 
	    GridBagConstraints c5 = getGridConstraints(0, 3);
	    c5.ipady = 0;       //reset to default
	    c5.weighty = 1.0;   //request any extra vertical space
	    c5.gridheight = GridBagConstraints.REMAINDER;
//	    c5.insets = new Insets(10,0,0,0);  //top padding
	    c5.gridwidth = GridBagConstraints.REMAINDER;   //2 columns wide
	    pane.add(this.makeTextPane(), c5);
	}
	
	private JPanel makeFileTextField() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setLayout(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		
	    JLabel fileTextFieldLabel = new JLabel("File Path: ");
	    fileTextFieldLabel.setLabelFor(this.fileField);
	    c.anchor = GridBagConstraints.EAST;
        c.gridwidth = GridBagConstraints.RELATIVE; //next-to-last
        c.fill = GridBagConstraints.NONE;      //reset to default
        c.weightx = 0.0;                       //reset to default
        panel.add(fileTextFieldLabel, c);
		
	    this.fileField.setActionCommand("JTextField");
	    this.fileField.addActionListener((e) -> {
	    	
	    });
        c.gridwidth = GridBagConstraints.REMAINDER;     //end row
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        panel.add(this.fileField, c);
	    
//	    JLabel actionLabel = new JLabel("Type text in a field and press Enter.");
//	    actionLabel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
        return panel;
	}
	
	private JButton makeChooseFileButton() {
		fileButton.setText("Browse...");
		fileButton.setBounds(40, 40, 100, 30);
		fileButton.setVerticalTextPosition(AbstractButton.CENTER);
        fileButton.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        fileButton.setMnemonic(KeyEvent.VK_C);
        
        fileButton.addActionListener((e) -> {
        		chooser = new JFileChooser();
        		chooser.setCurrentDirectory(new java.io.File("."));
        		chooser.setDialogTitle("File Directory");
        		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        		chooser.setAcceptAllFileFilterUsed(false);
        		
        		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        			System.out.println("getCurrentDirectory(): " + chooser.getCurrentDirectory());
        			System.out.println("getSelectedFile(): " + chooser.getSelectedFile());
        			this.selectedFile = chooser.getSelectedFile();
        			String filepath = selectedFile.getAbsolutePath();
        			this.fileField.setText(filepath);
        			this.filePath = filepath;
        		} else {
        			System.out.println("no selection");
        		}
        });
		return fileButton;
	}
	
	private JPanel makeFprField() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
//		panel.setLayout(new BorderLayout());

        SpinnerModel model = new SpinnerNumberModel(INITIAL_FPR, //initial value
                                       0, //min
                                       1, //max
                                       0.01);                //step
        JSpinner spinner = new JSpinner(model);
        //Make the year be formatted without a thousands separator.
        spinner.setEditor(new JSpinner.NumberEditor(spinner, "#%"));
        spinner.addChangeListener((e) -> {
        		this.fpr = ((SpinnerNumberModel) spinner.getModel()).getNumber().doubleValue();
        });
        
		JLabel l = new JLabel("False Positive Rate - FPR (%)");
        l.setLabelFor(spinner);
        
        panel.add(l);
        panel.add(spinner);
        return panel;
	}
	
	private JButton makeLearnButton() {
		JButton b = new JButton("Learn Bloom Filter");
	    b.setVerticalTextPosition(AbstractButton.CENTER);
        b.setHorizontalTextPosition(AbstractButton.LEADING); 
		
		b.addActionListener((e) -> {
			this.textArea.setText("Bloom filter has been learned from file: " + this.filePath 
					+ " with fpr of " + this.fpr);
			try {
				this.sandwichedBf.initAndLearn(DEFAULT_FILE, APPROX_N, this.fpr, BACKUP_FPR);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//				JOptionPane.showMessageDialog(b, "Hello World!");
			// TODO: ADD CODE HERE TO LEARN BLOOM FILTER
			
		});
//        b.setMnemonic(KeyEvent.VK_L);
//        b.setActionCommand("clickMe");
		return b;
	}
	
	private JTextArea makeTextPane() {
		this.textArea = new JTextArea("Here are the results.\\nd\\nd\\nd\\nd\\nd\\nd\\nd");
        textArea.setFont(new Font("Serif", Font.ITALIC, 16));
        textArea.setLineWrap(true);
        textArea.setOpaque(false);
        textArea.setWrapStyleWord(true);
		textArea.setEditable(false);
		textArea.setBorder(
				BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder("Results"),
                    BorderFactory.createEmptyBorder(5,5,5,5)));
		return this.textArea;
	}
	
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            		new Swing();
            }
        });
    }
}
