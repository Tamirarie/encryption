import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;

import javax.swing.JFileChooser;

public class Menu {
	private int method = 0; //1 equals to encryption, 2 to decryption
	private String fileName = ""; // represnt only the file name such as: "test.txt"
	private String filePath , folderPath; //represent the full file path and the folder path in case of folder
	private int entireDirectory; //1 equals to Entire, 2 to file
	private int sync ; // 1 equals to sync,  2 to async
	private Scanner sn; // scanner to read user input

	
	public Menu() {
		sn = new Scanner(System.in);
		System.out.println("Welcome to our program!\nPlease Enter number "
				+ "to Choose method:\n"
				+"1: Encryption\n2: Decryption\n"
				);
		method=getInput();
		System.out.println("Entire folder or file?\n1:Entire folder\n2:File");
		entireDirectory=getInput();
		if(entireDirectory== 1){ // need to do this on entire folder
			System.out.println("Choose:\n1.Sync - All files encrypted in one thread");
			System.out.println("2.Async - Files are encrypted in threads");
			sync = getInput();
			if(sync == 1){ // sync method one thread to all files
				try {
					handleSyncFolder();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (KeyException e) {
					e.printStackTrace();
				}
			}
			else{ // else we want to do this async way
				try {
					handleASyncFolder();
				} catch (IOException | KeyException e) {
					e.printStackTrace();
				}
				
			}
		}
		else{ // else we want to handle only one file
			filePath = getPathFromUser();
			File f = new File(filePath);
			while(!f.exists() || !f.isFile()) {
				filePath = getPathFromUser();
				f = new File(filePath);
			}
			
			fileName = f.getName();
			System.out.println(((method == 1) 
					? "encryption" : "decryption") + " simulation of file $"+fileName+"$");


			if(method == 1) { // we want to encrypt this file
				encryptHandle(filePath);
			}
			else if (method == 2){	// we want to decrypt this file
				decryptHandle(filePath);	
			}
		}

		sn.close();

	}

	@SuppressWarnings("unused")
	private void handleASyncFolder() throws IOException, KeyException  {
		folderPath = getPathFromUser();
		System.out.println(((method == 1) 
				? "encryption" : "decryption") + " simulation of folder $"+folderPath+"$");
		Encryption e = null; Decryption d = null;
		if(method == 1) e = new Encryption(folderPath,true,true,false,false);
		else if(method == 2) d = new Decryption(folderPath, true,true,false,false);
	}
	
	@SuppressWarnings("unused")
	private void handleSyncFolder() throws IOException, KeyException { // not working
		folderPath = getPathFromUser();
		System.out.println(((method == 1) 
				? "encryption" : "decryption") + " simulation of folder $"+folderPath+"$");
		Encryption e = null; Decryption d = null;
		if(method == 1) e = new Encryption(folderPath,true,true,true,false);
		else if(method == 2) d = new Decryption(folderPath, true,true,true,false);

	}
	
	@SuppressWarnings("unused")
	private void encryptHandle(String filePath) {
		Encryption e = new Encryption(filePath,false,true,true,false);	
	}

	@SuppressWarnings("unused")
	private void decryptHandle(String filePath) {
		try{
			Decryption d = new Decryption(filePath,false,true,false,false);
		}
		catch(IOException e){
			System.out.println("Error: "+e);
		}

	}

	private String getPathFromUser() {
		@SuppressWarnings("serial")
		JFileChooser f = new JFileChooser(){
			protected JDialog createDialog(Component parent) throws HeadlessException {
				JDialog dialog = super.createDialog(parent);
				dialog.setLocationByPlatform(true);
				dialog.setAlwaysOnTop(true);
				return dialog;
			}
		};
		File workingDirectory = new File(System.getProperty("user.dir"));
		f.setCurrentDirectory(workingDirectory);
		if(entireDirectory == 2) // only one file
			f.setFileSelectionMode(JFileChooser.FILES_ONLY); 
		else{
			f.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		int ret = f.showOpenDialog(null);
		if(ret ==JFileChooser.APPROVE_OPTION)
			return f.getSelectedFile().toString();
		else{
			System.out.println("File chooser was canclled!");
			System.exit(0);
			return "";
		}
	}



	private int getInput() {
		int input = 0;
		sn.useDelimiter("");
		do{
			if(!sn.hasNextInt()){
				System.out.println("invalid input, please enter valid one...");
			}
			else{
				input = sn.nextInt();
				if(input !=1 && input != 2)
					System.out.println("invalid input, please enter valid one...");
			}
			sn.nextLine();
		} while(input !=1 && input != 2);

		return input;
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Menu m = new Menu();
	}

	

}
