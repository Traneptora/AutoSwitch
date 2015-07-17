package thebombzen.mods.autoswitch.installer;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;
import java.util.jar.JarFile;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import thebombzen.mods.thebombzenapi.Constants;

public class ASInstallerFrame extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static ASInstallerFrame instance;
	
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
			
		    if(!destFile.exists()) {
		        destFile.createNewFile();
		    }
	
		    FileChannel source = null;
		    FileChannel destination = null;
		    FileInputStream inStream = null;
		    FileOutputStream outStream = null;
		    
		    try {
		    	inStream = new FileInputStream(sourceFile);
		    	outStream = new FileOutputStream(destFile);
		        source = inStream.getChannel();
		        destination = outStream.getChannel();
	
		        long count = 0;
		        long size = source.size();              
		        while((count += destination.transferFrom(source, count, size-count)) < size);
		    } finally {
		    	if (inStream != null){
		    		inStream.close();
		    	}
		    	if (outStream != null){
		    		outStream.close();
		    	}
		        if(source != null) {
		            source.close();
		        }
		        if(destination != null) {
		            destination.close();
		        }
		    }
		}
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
	
	public static void main(String[] args) throws Exception {
		if (args.length > 0){
			if (args[0].equals("--no-gui") || args[0].equals("-n")){
				installNoGui(getMinecraftClientDirectory());
				return;
			} else {
				System.err.println("Run with --no-gui or -n to install without a gui.");
			}
		}
		new ASInstallerFrame().setVisible(true);
	}
	
	private JTextField textField;
	private JDialog dialog;
	
	public ASInstallerFrame() throws IOException {
		instance = this;
		Box superBox = Box.createHorizontalBox();
		superBox.add(Box.createHorizontalStrut(10));
		
		Box content = Box.createVerticalBox();
		
		content.add(Box.createVerticalStrut(10));
		
		JLabel label = new JLabel("Select minecraft folder:");
		Box labelBox = Box.createHorizontalBox();
		labelBox.add(label);
		labelBox.add(Box.createHorizontalGlue());
		content.add(labelBox);
		
		Box textBox = Box.createHorizontalBox();
		textField = new JTextField();
		textField.setText(getMinecraftClientDirectory());
		textBox.add(textField);
		textBox.add(Box.createHorizontalStrut(10));
		JButton browseButton = new JButton("Browse");
		textBox.add(browseButton);
		content.add(textBox);
		
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
		
		/*JLabel tbzapiLabel = new JLabel("Remember to also install ThebombzenAPI.");
		Box tbzapiLabelBox = Box.createHorizontalBox();
		tbzapiLabelBox.add(tbzapiLabel);
		tbzapiLabelBox.add(Box.createHorizontalGlue());
		content.add(tbzapiLabelBox);
		
		Box linkBox = Box.createHorizontalBox();
		JLabel linkLabel = new JLabel("<html><a href=\"http://is.gd/ThebombzensMods#ThebombzenAPI\">Download ThebombzenAPI Here</a></html>");
		linkBox.add(linkLabel);
		linkBox.add(Box.createHorizontalGlue());
		content.add(linkBox);
		
		content.add(Box.createVerticalStrut(10));*/
		
		JButton install = new JButton("Install AutoSwitch");
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
				new Thread(new Runnable() {
					@Override
					public void run() {
						install();
					}
				}).start();
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
		
		/*linkLabel.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent me){
				try {
					Desktop.getDesktop().browse(new URI("http://is.gd/ThebombzensMods#ThebombzenAPI"));
				} catch (IOException e) {
					
				} catch (URISyntaxException e) {
					
				}
			}
		});*/
		
		browseButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent ae) {
				JFileChooser jfc = new JFileChooser();
				jfc.setMultiSelectionEnabled(false);
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = jfc.showOpenDialog(instance);
				if (result == JFileChooser.APPROVE_OPTION){
					textField.setText(jfc.getSelectedFile().getAbsolutePath());
				}
			}
		});
		
		this.add(superBox);
		
		this.setTitle("Install AutoSwitch");
		this.pack();
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		
		this.setLocation((size.width - getWidth()) / 2, (size.height - getHeight()) / 2);
		
		dialog = new JDialog(this);
		dialog.setLayout(new BorderLayout());
		dialog.add(Box.createVerticalStrut(15), BorderLayout.NORTH);
		dialog.add(Box.createVerticalStrut(15), BorderLayout.SOUTH);
		dialog.add(Box.createHorizontalStrut(25), BorderLayout.WEST);
		dialog.add(Box.createHorizontalStrut(25), BorderLayout.EAST);
		dialog.add(new JLabel("Installing..."), BorderLayout.CENTER);
		dialog.setTitle("Installing");
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.pack();
		dialog.setLocation(this.getX() + (this.getWidth() - dialog.getWidth()) / 2, this.getY() + (this.getHeight() - dialog.getHeight()) / 2);
	}
	
	public static String getThebombzenAPILatestVersion() throws IOException {
		String latestVersion = null;
		BufferedReader br = null;
		try {
			URL versionURL = new URL(
					"https://dl.dropboxusercontent.com/u/51080973/mods/ThebombzenAPI/Latest-" + Constants.MC_VERSION + ".txt");
			br = new BufferedReader(new InputStreamReader(
					versionURL.openStream()));
			latestVersion = br.readLine();
			latestVersion += String.format("%n%s%n", br.readLine());
		} catch (MalformedURLException e) {
			throw new Error();
		} finally {
			br.close();
		}
		return latestVersion;
	}
	
	private void install(){
		try {
			install(textField.getText());
		} catch (Exception e) {
			e.printStackTrace();
			dialog.dispose();
			JOptionPane.showMessageDialog(this,
					"Error installing. Install manually.", "Error Installing",
					JOptionPane.ERROR_MESSAGE);
		}
		System.exit(0);
	}
	
	private static void installNoGui(String directory) throws Exception {
		File dir = new File(directory);
		if (!dir.isDirectory()){
			System.err.println("Invalid directory: " + directory);
			return;
		}
		File modsFolder = new File(directory, "mods");
		modsFolder.mkdir();
		File versionFolder = new File(modsFolder, Constants.MC_VERSION);
		versionFolder.mkdir();
		File file = new File(ASInstallerFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		JarFile jarFile = new JarFile(file);
		if (jarFile.getEntry("thebombzen/mods/autoswitch/installer/ASInstallerFrame.class") == null){
			jarFile.close();
			throw new Exception("Unable to open jar file!");
		}
		jarFile.close();
		installThebombzenAPI(directory);
		File[] mods = modsFolder.listFiles();
		File[] modsVersion = versionFolder.listFiles();
		for (File testMod : mods){
			if (testMod.getName().matches("^AutoSwitch(Mod)?-v\\d\\.\\d(\\.\\d)?-mc(beta)?\\d\\.\\d(\\.\\d)?\\.(jar|zip)$")){
				testMod.delete();
			}
		}
		for (File testMod : modsVersion){
			if (testMod.getName().matches("^AutoSwitch(Mod)?-v\\d\\.\\d(\\.\\d)?-mc(beta)?\\d\\.\\d(\\.\\d)?\\.(jar|zip)$")){
				testMod.delete();
			}
		}
		copyFile(file, new File(versionFolder, file.getName()));
	}

	private void install(String directory) throws Exception {
		File dir = new File(directory);
		if (!dir.isDirectory()){
			JOptionPane.showMessageDialog(this, "Something's wrong with the given folder. Check spelling and try again.", "Hmmm...", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		dialog.setVisible(true);
		installNoGui(directory);
		dialog.dispose();
		JOptionPane.showMessageDialog(this, "Successfully installed AutoSwitch!", "Success!", JOptionPane.INFORMATION_MESSAGE);
	}
	
	public static void installThebombzenAPI(String directory) throws Exception {	
		String latest = getThebombzenAPILatestVersion();
		Scanner scanner = new Scanner(latest);
		scanner.useDelimiter(String.format("%n"));
		String fileName = scanner.next();
		String url = scanner.next();
		scanner.close();
		
		File modsFolder = new File(directory, "mods");
		modsFolder.mkdir();
		File versionFolder = new File(modsFolder, Constants.MC_VERSION);
		versionFolder.mkdir();
		
		File thebombzenAPI = new File(versionFolder, fileName);
		
		if (thebombzenAPI.exists()) {
			return;
		}

		URL downloadURL = new URL(url);

		File[] subFiles = modsFolder.listFiles();
		File[] versionSubFiles = versionFolder.listFiles();
		for (File file : subFiles) {
			if (file.getName()
					.matches(
							"^ThebombzenAPI-v\\d+\\.\\d+(\\.\\d+)?-mc\\d+\\.\\d+(\\.\\d+)?\\.(zip|jar)$")) {
				file.delete();
			}
		}
		for (File file : versionSubFiles) {
			if (file.getName()
					.matches(
							"^ThebombzenAPI-v\\d+\\.\\d+(\\.\\d+)?-mc\\d+\\.\\d+(\\.\\d+)?\\.(zip|jar)$")) {
				file.delete();
			}
		}

		FileOutputStream fos = new FileOutputStream(thebombzenAPI);
		ReadableByteChannel channel = Channels.newChannel(downloadURL
				.openStream());
		fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		channel.close();
		fos.close();
	}
}
