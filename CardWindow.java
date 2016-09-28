import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.*;

public class CardWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	private static volatile JLabel label;
	ImageIcon defaultIcon;
	
	public CardWindow() throws Exception {
		super("Card Display");
		this.setLayout(new BorderLayout());
		URL url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=-1&type=card");
		BufferedImage img = ImageIO.read(url);
		ImageIcon defaultIcon = new ImageIcon(img);
		label = new JLabel(defaultIcon);
		JButton button = new JButton(new AbstractAction("Clear") {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				label.setIcon(defaultIcon);
				label.validate();
			}
		});
		this.add(label, BorderLayout.CENTER);
		this.add(button, BorderLayout.SOUTH);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		if(!ImageUpdateThread.created){
			new ImageUpdateThread(label).start();
		}else{
			ImageUpdateThread.setLabel(label);
		}
		label.setTransferHandler(createTransferHandler());
	}

	public static class ImageUpdateThread extends Thread {
		private ServerSocket s;
		private static JLabel lab;
		public static boolean created = false;

		public ImageUpdateThread(JLabel l) {
			super();
			lab = l;
			created = true;
		}
		
		public static void setLabel(JLabel l){
			lab = l;
		}

		public void run() {
			try {
				s = new ServerSocket(7777);
			} catch (IOException e) {
				e.printStackTrace();
			}

			while (true) {
				try {
					Socket sock = s.accept();
					BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

					for (;;) {
						String line = in.readLine();
						if (line != null) {
							if (line.startsWith("GET ")) {
								String id = line.split(" ")[1].replace("/", "");
								URL url = new URL("http://gatherer.wizards.com/Handlers/Image.ashx?multiverseid=" + id
										+ "&type=card");
								BufferedImage img = ImageIO.read(url);
								ImageIcon icon = new ImageIcon(img);
								lab.setIcon(icon);
								sock.close();
								break;
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static TransferHandler createTransferHandler() {
		return new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean importData(JComponent comp, Transferable aTransferable) {
				try {
					String st = (String) aTransferable.getTransferData(DataFlavor.stringFlavor);
					URL url = new URL(st);
					BufferedImage img = ImageIO.read(url);
					ImageIcon icon = new ImageIcon(img);
					label.setIcon(icon);
					label.validate();
				} catch (UnsupportedFlavorException e) {

					try {
						Image i = (Image) aTransferable.getTransferData(DataFlavor.imageFlavor);
						label.setIcon(new ImageIcon(i));
						label.validate();
					} catch (UnsupportedFlavorException a) {
					} catch (IOException a) {
					}
				} catch (IOException e) {
				}
				return true;
			}

			@Override
			public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
				return true;
			}
		};
	}

	public static void main(String[] args) throws Exception {
		new CardWindow();
	}
}
