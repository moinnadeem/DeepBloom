package edu.mit.BloomFilter.Swing;

import javax.swing.*;

import edu.mit.BloomFilter.StandardBloomFilter.StandardBloomFilterUtils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class Swing extends JFrame {
	private JButton b = new JButton();
	
	public Swing() {
		super();
		
		this.setTitle("HelloApp");
		this.getContentPane().setLayout(null);
		this.setBounds(100, 100, 180, 140);
		
		JLabel label = new JLabel();
		label.setOpaque(true);
		label.setBackground(new Color(248, 213, 131));
		label.setPreferredSize(new Dimension(200, 180));
		this.setContentPane(label);
		
		// TODO: load dataset, run filter, view sizes
		this.add(makeButton());
		
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private JButton makeButton() {
		b.setText("Click me!");
		b.setBounds(40, 40, 100, 30);
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(b, "Hello World!");
				try {
					StandardBloomFilterUtils.getFpr();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
        b.setVerticalTextPosition(AbstractButton.CENTER);
        b.setHorizontalTextPosition(AbstractButton.LEADING); //aka LEFT, for left-to-right locales
        b.setMnemonic(KeyEvent.VK_L);
        b.setActionCommand("clickMe");
		
		return b;
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("BloomFilterSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        Swing contentPane = new Swing();
//        contentPane.setOpaque(true);
        contentPane.setBackground(new Color(248, 213, 131));
        contentPane.setPreferredSize(new Dimension(200, 180));
        
        frame.setContentPane(contentPane);
        
        //Create a yellow label to put in the content pane.
//        JLabel yellowLabel = new JLabel();
//        yellowLabel.setOpaque(true);
//        yellowLabel.setBackground(new Color(248, 213, 131));
//        yellowLabel.setPreferredSize(new Dimension(200, 180));
 
        //Set the menu bar and add the label to the content pane.
//        frame.getContentPane().add(yellowLabel, BorderLayout.CENTER);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
 
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            		new Swing();
//                createAndShowGUI();
            }
        });
    }

}
