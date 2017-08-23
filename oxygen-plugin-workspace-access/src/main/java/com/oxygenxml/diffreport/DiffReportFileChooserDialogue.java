package com.oxygenxml.diffreport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import org.apache.batik.ext.swing.GridBagConstants;

/**
 * 
 * @author intern3
 *
 */
public class DiffReportFileChooserDialogue extends JDialog {
	
	private JButton generateDiffButton;
	private JTextField firstLabelField;
	private JTextField secondLabelField;
	private JTextField thirdLabelField;
	private ReportGenerator reportGenerator; 
	
	/**
	 * 
	 */
	private static volatile DiffReportFileChooserDialogue instance;

	/**
	 * Constructor.
	 */
	private DiffReportFileChooserDialogue() {
		this.setModal(true);
		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				DiffReportFileChooserDialogue.this.setVisible(false);
			}
		});

		this.add(crateMainPanel(), BorderLayout.CENTER);
//		this.setSize(380, 200);
		this.setPreferredSize(new Dimension(400, 200));
//		this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(null);
		this.setTitle("Diff Report Generator");
		
		
	}

	/**
	 * 
	 * @return
	 */
    public static DiffReportFileChooserDialogue getInstance() {
        if (instance == null) {
            synchronized (DiffReportFileChooserDialogue.class) {
                if (instance == null) {
                    instance = new DiffReportFileChooserDialogue();
                }
            }
        }
        return instance;
    }
	

	
	
	//Getters and Setters
	public JTextField getThirdLabelField() {
		return thirdLabelField;
	}

	public void setThirdLabelField(JTextField thirdLabelField) {
		this.thirdLabelField = thirdLabelField;
	}
	
	
	public JButton getCompareButton() {
		return generateDiffButton;
	}


	public JTextField getFirstLabelField() {
		return firstLabelField;
	}

	public void setFirstLabelField(JTextField firstLabelField) {
		this.firstLabelField = firstLabelField;
	}

	
	public JTextField getSecondLabelField() {
		return secondLabelField;
	}


	public void setSecondLabelField(JTextField secondLabelField) {
		this.secondLabelField = secondLabelField;
	}



	/**
	 * In this function the Panel that contains the file choosers and
	 * the one that has the buttons are unified into one panel
	 * @return the resulted Panel
	 */
	
	private JPanel crateMainPanel(){
		BorderLayout layout = new BorderLayout();
		JPanel mainPanel = new JPanel(layout);	
//		GridBagConstraints constraints = new GridBagConstraints();
		JPanel fileChooserPanel = createFileChooserPanel();
		JPanel buttonPanel = createButtons();
		mainPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
//		//add fileChooserPanel to main panel with constraints
//		constraints.fill = GridBagConstraints.BOTH;
////		constraints.anchor = GridBagConstraints.WEST;
//		constraints.gridx = 0;
//		constraints.gridy = 0;
////		constraints.weightx = 1;
		fileChooserPanel.setBorder(new EmptyBorder(15, 5, 15, 5));
		mainPanel.add(fileChooserPanel, BorderLayout.CENTER);
		
//		//add buttonPanel to main panel with constraints
//		constraints.gridy++;
//		constraints.anchor = GridBagConstraints.SOUTHEAST;
//		constraints.fill = GridBagConstraints.HORIZONTAL;
////		constraints.weightx = 1;
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		
		return mainPanel;
	}
	
	/**
	 * This method creates the Panel that contains the whole file chooser
	 * components: Label + Shown Path + Browse Button (with File Chooser)
	 * showing the chosen path in a TextField  
	 * @return the panel with all the given components
	 */
	private JPanel createFileChooserPanel(){
		BorderLayout layout = new BorderLayout();
		JPanel panel = new JPanel(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		
		JLabel fileOne_Label = new JLabel("Left File:  ");
		JLabel fileTwo_Label = new JLabel("Right File: ");
		JLabel fileThree_Label = new JLabel("Output:  ");
		
		firstLabelField = new JTextField(20);
//		firstLabelField.
		secondLabelField = new JTextField(20);
		thirdLabelField = new JTextField(20);
		
		JButton browseButton1 = createBrowseButton(firstLabelField);
		JButton browseButton2 = createBrowseButton(secondLabelField);
		JButton browseButton3 = createBrowseButton(thirdLabelField);
		
		firstLabelField.setText("C:/Users/intern3/Desktop/myFiles/diffSample/EngliGB.xml");
		secondLabelField.setText("C:/Users/intern3/Desktop/myFiles/diffSample/EngliUS.xml");
		thirdLabelField.setText("C:/Users/intern3/Desktop/myFiles/diffSample/htmlFile.html");
		
		firstLabelField.setEditable(true);
		firstLabelField.setBackground(Color.LIGHT_GRAY);
		firstLabelField.setHorizontalAlignment(JTextField.CENTER);
		
		secondLabelField.setEditable(true);
		secondLabelField.setBackground(Color.LIGHT_GRAY);
		secondLabelField.setHorizontalAlignment(JTextField.CENTER);
		
		thirdLabelField.setEditable(true);
		thirdLabelField.setBackground(Color.LIGHT_GRAY);
		thirdLabelField.setHorizontalAlignment(JTextField.CENTER);
		
		JPanel auxiliaryPanel;
		
		// add the first label-----------------
//		constraints.gridx = 0;
//		constraints.gridy = 0;
//		constraints.anchor = GridBagConstraints.WEST;
		auxiliaryPanel = createFilePannel(fileOne_Label, firstLabelField, browseButton1, 16);
		panel.add(auxiliaryPanel, BorderLayout.PAGE_START);
		
		//add the second label----------------------
//		constraints.gridx = 0;
//		constraints.gridy ++ ;
		auxiliaryPanel = createFilePannel(fileTwo_Label, secondLabelField, browseButton2, 13);
		panel.add(auxiliaryPanel, BorderLayout.CENTER);
		
		
		//add the third label---------------------
//		constraints.gridx = 0;
//		constraints.gridy++;
		auxiliaryPanel = createFilePannel(fileThree_Label, thirdLabelField, browseButton3, 20);
		panel.add(auxiliaryPanel, BorderLayout.PAGE_END);

		

		
		return panel;
	}
	
	private JPanel createFilePannel(JLabel file, JTextField field, JButton button, int ipadxMargin) {
		GridBagLayout layout = new GridBagLayout();
		JPanel panel = new JPanel(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.anchor = GridBagConstants.WEST;
		constraints.fill = GridBagConstants.NONE;
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 0;
		constraints.weighty = 0;
		constraints.ipadx = ipadxMargin;
		panel.add(file, constraints);
		
		
		constraints.anchor = GridBagConstants.CENTER;
		constraints.fill = GridBagConstants.BOTH;
		constraints.gridx ++;
		constraints.weightx = 1;
		constraints.weighty = 1;
		panel.add(field, constraints);
		
		
		constraints.anchor = GridBagConstants.EAST;
		constraints.fill = GridBagConstants.NONE;
		constraints.gridx ++;
		constraints.weightx = 0;
		constraints.ipadx = 0;
		constraints.weighty = 0;
		constraints.insets = new Insets(0, 17, 0, 0);
		panel.add(button, constraints);
		
		return panel;
	}
	
	/**
	 * Creates a Browse button and remembers the given path
	 * in the field param
	 * @param field -> The field responsible with remembering the Path
	 * @return the browsing Button for each of the files
	 */
	private JButton createBrowseButton(final JTextField field){
		JButton browse = new JButton("Browse");
		
		ActionListener ac = new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
				FileNameExtensionFilter filter = new FileNameExtensionFilter("XML files","xml", "html");
				fileChooser.setFileFilter(filter);

				int returnValue = fileChooser.showOpenDialog(null);

				if (returnValue == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					field.setText(selectedFile.toString());
					
				}
			}
		};
		
		browse.addActionListener(ac);
		return browse;
	}
	
	/**
	 * Creating the panel which contains the two functions "Generate Diff" and
	 * "Cancel" aligned at the right bottom corner 
	 * @return The panel with the two buttons: Compare and Cancel
	 */
	private JPanel createButtons(){
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		Component box1 = Box.createRigidArea(new Dimension(7,0));
		Component box2= Box.createRigidArea(new Dimension(7,0));
		box1.setBackground(Color.WHITE);
		box2.setBackground(Color.WHITE);
		
		
		//Generate Diff button starts here-------------------------------------------------------
		generateDiffButton = new JButton("Generate Diff");
		generateDiffButton.setPreferredSize(new Dimension(97, 25));
		generateDiffButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generateDiff();
			}
		});
		panel.add(generateDiffButton);
		panel.add(box1);
		
		//ends here--------------------------------------------------------------------
		
		
		
		
		//cancel button starts here ----------------------------------------------------
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(e.getActionCommand() != null)
					setVisible(false);
			}
		});
		cancelButton.setPreferredSize(new Dimension(75, 25));
		panel.add(cancelButton);
		panel.add(box2);
		//ends here----------------------------------------------------------------------
		
		return panel;
	}

	
	public static void main(String[] args) {
		 try {
		        UIManager.setLookAndFeel(
		            UIManager.getSystemLookAndFeelClassName());
		    } catch (Exception e) { }
		 DiffReportFileChooserDialogue dial = new DiffReportFileChooserDialogue();
		 dial.setVisible(true);
	}
	
	/**
	 * 
	 */
	private void generateDiff() {

		try {
//			myDialog.getDialog().setModal(false);
			String leftFile = getFirstLabelField().getText();
			String rightFile = getSecondLabelField().getText();
			// String rightFile =
			// myDialog.getSecondLabelField().getText();
			File outputFile = new File(getThirdLabelField().getText());
			reportGenerator.generateHTMLReport(new File(leftFile).toURI().toURL(), new File(rightFile).toURI().toURL(),
					outputFile);

			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(outputFile.toURI());
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			setVisible(false);
		}
	
	
	}

	public void setReportGenerator(ReportGenerator reportGenerator) {
		this.reportGenerator = reportGenerator;
	}

	public Object getReportGenerator() {
		// TODO Auto-generated method stub
		return this.reportGenerator;
	}
}
