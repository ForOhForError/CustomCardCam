import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import javax.swing.*;

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

		JPanel options = new JPanel();

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout());
		extOrbs = new JButton("Extract Card Files");
		extOrbs.addActionListener(new OrbExtractAction());
		expSet = new JButton("Save Set");
		expSet.addActionListener(new MakeSetAction());

		JButton openDisplay = new JButton("Open Card Display");
		openDisplay.addActionListener(new OpenCardViewerAction());

		JButton rebuilt = new JButton("Reinject custom sets");
		rebuilt.addActionListener(new ReinsertCustomSets());

		JButton hook = new JButton("Setup Card Display Hook");
		hook.addActionListener(new InsertHook());

		options.add(extOrbs);
		options.add(openDisplay);
		options.add(rebuilt);
		options.add(hook);
		top.add(expSet,BorderLayout.SOUTH);
		top.add(options, BorderLayout.NORTH);

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

	public class OpenCardViewerAction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try{
				new CardWindow();
			}catch(Exception e){}
		}
	}

	public class MakeSetAction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			new SetMakeThread().start();
		}
	}

	public class InsertHook implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {

			int n = JOptionPane.showConfirmDialog(  
					null,
					"This will overwrite any existing OrbCam_match_url value. Continue?",
					"",
					JOptionPane.YES_NO_OPTION);

			if(n == JOptionPane.YES_OPTION)
			{
				FileOperations.insertHook();
				JOptionPane.showMessageDialog(null, "Setup complete. The card viewer will now display cardcam's output. If deckedbuilder is running, restart it to have this apply.");
			}
		}
	}

	public class ReinsertCustomSets implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			FileOperations.updateSetlistFile();
			JOptionPane.showMessageDialog(null, "Custom sets have been reinjected.");
		}
	}

	public class OrbExtractAction implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent arg0) {
			File file = new File(System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\orbs");
			new CardExtractThread(file).start();
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

			String setname = JOptionPane.showInputDialog("Enter set name");
			String blockname = JOptionPane.showInputDialog("Enter category name");

			String orbname = System.getenv("LOCALAPPDATA")+"\\deckedbuilder\\orbs\\"
					+FileOperations.getOrbifiedName(setname)+".orb";


			if(setname != null && blockname != null){

				progress.setValue(2);

				ArrayList<String> m_ids = FileOperations.decklistToIds(field.getText());

				progress.setValue(2);

				FileOperations.writeSet(m_ids, orbname);

				FileOperations.addToCustomSetFile(setname,blockname);
				FileOperations.updateSetlistFile();

				progress.setValue(3);
				JOptionPane.showMessageDialog(null, "Set Created.");
			}else{
				JOptionPane.showMessageDialog(null, "Operation failed: missing set/category name");
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
			JOptionPane.showMessageDialog(null, "Card extraction complete.");

		}
	}


}
