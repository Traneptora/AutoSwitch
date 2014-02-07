package thebombzen.mods.autoswitch.installer;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.util.jar.JarFile;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ASInstallerFrame extends JFrame {
	
	private static final long serialVersionUID = 316486537931642186L;
	private JTextField textField;
	private JButton install;
	
	public static String getMinecraftClientDirectory() throws IOException {
		String name = System.getProperty("os.name");
		if (name.toLowerCase().contains("windows")){
			return new File(System.getenv("appdata") + "\\.minecraft").getCanonicalPath();
		} else if (name.toLowerCase().contains("mac") || name.toLowerCase().contains("osx") || name.toLowerCase().contains("os x")){
			return new File(System.getProperty("user.home") + "/Library/Application Support/minecraft").getCanonicalPath();
		} else {
			return new File(System.getProperty("user.home") + "/.minecraft").getCanonicalPath();
		}
	}
	
	private void install(){
		try {
			install(textField.getText());
		} catch (Exception e){
			JOptionPane.showMessageDialog(this, "Error installing. Install manually.", "Error Installing", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@SuppressWarnings("resource")
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;
	    
	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();

	        // previous code: destination.transferFrom(source, 0, source.size());
	        // to avoid infinite loops, should be:
	        long count = 0;
	        long size = source.size();              
	        while((count += destination.transferFrom(source, count, size-count))<size);
	    }
	    
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}
	
	private void install(String directory) throws Exception {
		File dir = new File(directory);
		if (!dir.isDirectory()){
			JOptionPane.showMessageDialog(this, "Something's wrong with the given folder. Check spelling and try again.", "Hmmm...", JOptionPane.ERROR_MESSAGE);
			return;
		}
		File modsFolder = new File(directory, "mods");
		modsFolder.mkdir();
		File file = new File(ASInstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		JarFile jarFile = new JarFile(file);
		if (jarFile.getEntry("thebombzen/mods/autoswitch/installer/ASInstallerFrame.class") == null){
			jarFile.close();
			throw new Exception();
		}
		jarFile.close();
		File[] mods = modsFolder.listFiles();
		for (File testMod : mods){
			if (testMod.getName().matches("^AutoSwitch(Mod)?-v\\d\\.\\d(\\.\\d)?-mc\\d\\.\\d(\\.\\d)?\\.(jar|zip)$")){
				testMod.delete();
			}
		}
		copyFile(file, new File(modsFolder, file.getName()));
		JOptionPane.showMessageDialog(this, "Successfully installed AutoSwitch!", "Success!", JOptionPane.INFORMATION_MESSAGE);
		System.exit(0);
	}
	
	public static void main(String[] args) throws IOException {
		new ASInstallerFrame().setVisible(true);
	}

	public ASInstallerFrame() throws IOException {
		
		Box superBox = Box.createHorizontalBox();
		superBox.add(Box.createHorizontalStrut(10));
		
		Box content = Box.createVerticalBox();
		
		content.add(Box.createVerticalStrut(10));
		
		JLabel label = new JLabel("Select minecraft folder:");
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(label);
		labelBox.add(Box.createHorizontalGlue());
		content.add(labelBox);
		
		textField = new JTextField();
		textField.setText(getMinecraftClientDirectory());
		content.add(textField);
		
		content.add(Box.createVerticalStrut(10));
		
		JLabel forgeLabel = new JLabel("Remember to also install Minecraft Forge.");
		Box forgeLabelBox = Box.createHorizontalBox();
		forgeLabelBox.add(forgeLabel);
		forgeLabelBox.add(Box.createHorizontalGlue());
		content.add(forgeLabelBox);
		
		Box forgeLinkBox = Box.createHorizontalBox();
		JLabel forgeLinkLabel = new JLabel("<html><a href=\"http://files.minecraftforge.net/\">Download Minecraft Forge Here</a></html>");
		forgeLinkBox.add(forgeLinkLabel);
		forgeLinkBox.add(Box.createHorizontalGlue());
		content.add(forgeLinkBox);
		
		content.add(Box.createVerticalStrut(10));
		
		JLabel tbzapiLabel = new JLabel("Remember to also install ThebombzenAPI.");
		Box tbzapiLabelBox = Box.createHorizontalBox();
		tbzapiLabelBox.add(tbzapiLabel);
		tbzapiLabelBox.add(Box.createHorizontalGlue());
		content.add(tbzapiLabelBox);
		
		Box linkBox = Box.createHorizontalBox();
		JLabel linkLabel = new JLabel("<html><a href=\"http://is.gd/ThebombzensMods#ThebombzenAPI\">Download ThebombzenAPI Here</a></html>");
		linkBox.add(linkLabel);
		linkBox.add(Box.createHorizontalGlue());
		content.add(linkBox);
		
		content.add(Box.createVerticalStrut(10));
		
		install = new JButton("Install AutoSwitch");
		Box installBox = Box.createHorizontalBox();
		installBox.add(Box.createHorizontalGlue());
		installBox.add(install);
		installBox.add(Box.createHorizontalGlue());
		content.add(installBox);
		
		content.add(Box.createVerticalStrut(10));
		
		superBox.add(content);
		superBox.add(Box.createHorizontalStrut(10));

		
		install.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae){
				install();
			}
		});
		
		forgeLinkLabel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent me){
				try {
					Desktop.getDesktop().browse(new URI("http://files.minecraftforge.net/"));
				} catch (IOException e) {
					
				} catch (URISyntaxException e) {
					
				}
			}
		});
		
		linkLabel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent me){
				try {
					Desktop.getDesktop().browse(new URI("http://is.gd/ThebombzensMods#ThebombzenAPI"));
				} catch (IOException e) {
					
				} catch (URISyntaxException e) {
					
				}
			}
		});
		
		this.add(superBox);
		
		this.setTitle("Install AutoSwitch");
		this.pack();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
	}
	
}
