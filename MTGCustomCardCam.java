import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MTGCustomCardCam {

	volatile JProgressBar progress;
	volatile JButton extOrbs;
	volatile JButton expSet;
	volatile JFrame app;
	JTextArea field;

	public static void main(String[] args){
		new MTGCustomCardCam();
	}

	public MTGCustomCardCam()
	{
		app = new JFrame("Custom Card Cam Utility");


		try {
			// Set System L&F
			UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		extOrbs = new JButton("Extract Card Files");
		extOrbs.addActionListener(new OrbExtractAction());
		expSet = new JButton("Save Set");
		expSet.addActionListener(new MakeSetAction());
		top.add(extOrbs,BorderLayout.NORTH);
		top.add(expSet,BorderLayout.SOUTH);

		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());

		app.setLayout(new BorderLayout());

		field = new JTextArea(20,60);
		JScrollPane listscroll = new JScrollPane(field);

		JLabel label = new JLabel("Decklist: (1 card per line)");

		bottom.add(listscroll,BorderLayout.SOUTH);
		bottom.add(label,BorderLayout.NORTH);

		app.add(top,BorderLayout.NORTH);
		app.add(bottom, BorderLayout.CENTER);

		progress = new JProgressBar();

		app.add(progress,BorderLayout.SOUTH);

		app.pack();

		app.setDefaultCloseOperation(3);
		app.setResizable(false);
		app.setVisible(true);
	}

	public class MakeSetAction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			new SetMakeThread().start();
		}
	}

	public class OrbExtractAction implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			JFileChooser chooser = new JFileChooser(); 

			try{
				chooser.setCurrentDirectory(new java.io.File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder"));
			}catch(Exception e){
				chooser.setCurrentDirectory(new java.io.File("."));
			}

			chooser.setDialogTitle("Select DeckedBuilder's Orbs directory");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) { 
				new CardExtractThread(chooser.getSelectedFile()).start();
			}
		}

	}

	public class SetMakeThread extends Thread{

		public void run(){
			doSetMake();
		}

		public void doSetMake(){
			extOrbs.setEnabled(false);
			expSet.setEnabled(false);

			progress.setMaximum(4);
			
			JFileChooser fileChooser = new JFileChooser();
			
			try{
				fileChooser.setCurrentDirectory(new java.io.File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder"));
			}catch(Exception e){
				fileChooser.setCurrentDirectory(new java.io.File("."));
			}
			
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Card Cam Orb", "orb");
			fileChooser.setFileFilter(filter);
			if (fileChooser.showSaveDialog(app) == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				
				progress.setValue(2);
				
				ArrayList<String> m_ids = FileOperations.decklistToIds(field.getText());
				
				progress.setValue(2);
				
				FileOperations.writeSet(m_ids, file.getAbsolutePath().replace(".orb", "")+".orb");
				
				progress.setValue(3);
				JOptionPane.showMessageDialog(app, "Set Created.");
			}
			progress.setValue(0);
			extOrbs.setEnabled(true);
			expSet.setEnabled(true);
		}
	}

	public class CardExtractThread extends Thread{
		private File dir;


		public void run(){
			doOrbExtract(dir);
		}

		public CardExtractThread(File di){
			dir = di;
		}

		private void doOrbExtract(File dir){

			extOrbs.setEnabled(false);
			expSet.setEnabled(false);

			FilenameFilter filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".orb");
				}
			};

			File[] listOfFiles = dir.listFiles(filter);

			progress.setMaximum(listOfFiles.length);	
			for (int i = 0; i < listOfFiles.length; i++) {
				FileOperations.readOrb(listOfFiles[i].getAbsolutePath());
				progress.setValue(i);
			}
			extOrbs.setEnabled(true);
			expSet.setEnabled(true);
			progress.setValue(0);
			JOptionPane.showMessageDialog(app, "Card extraction complete.");

		}
	}


}
