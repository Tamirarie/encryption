import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.*;

import javax.swing.JFileChooser;

public class Menu {
	private int method = 0; //1 equals to encryption, 2 to decryption
	private String fileName = "";
	private String filePath , folderPath;
	private int entireDirectory; //1 equals to Entire, 2 to file
	private int sync ; // 1 equals to sync,  2 to async
	private Scanner sn;

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
			else{
				handleASyncFolder();
			}
		}
		else{
			filePath = getPathFromUser();
			File f = new File(filePath);
			while(!f.exists() || !f.isFile()) {
				filePath = getPathFromUser();
				f = new File(filePath);
			}
			fileName = f.getName();
			System.out.println(((method == 1) 
					? "encryption" : "decryption") + " simulation of file $"+fileName+"$");


			if(method == 1) {	
				encryptHandle(filePath);
			}
			else if (method == 2){	
				decryptHandle(filePath);	
			}
		}

		sn.close();

	}

	private void encryptHandle(String filePath) {
		Encryption e = new Encryption(filePath,false,true);	
	}

	private void decryptHandle(String filePath) {
		try{
			Decryption d = new Decryption(filePath,false,true);
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


	public class workerThread implements Runnable{
		String fileName;
		int algo=-1;
		public workerThread(String name, int algo) {
			this.fileName=name;
			this.algo = algo;
		}

		@Override
		public void run() {
			if(method == 1){ // encrypting
				Encryption e = new Encryption(fileName, true,true);
				//	e.setMethod(algo);
				try {
					e.chooseMethod(0, false);
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (KeyException e1) {
					e1.printStackTrace();
				}
			}
			else if (method == 2){ // decrypting

			}
		}

	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Menu m = new Menu();
	}


	private void handleSyncFolder() throws IOException, KeyException { // not working
		folderPath = getPathFromUser();

		Encryption e = null; Decryption d = null;
		if(method == 1) e = new Encryption(folderPath,true,true);
		else if(method == 2) d = new Decryption(folderPath, true,true);

	}

	private void handleASyncFolder(){ // not working
		folderPath = getPathFromUser();
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();
		Vector<String> filesAtFolder = new Vector<>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				filesAtFolder.add(listOfFiles[i].getAbsolutePath());
			}
		}	
		if(listOfFiles.length == 0) {
			System.out.println("Folder is empty! exit now!");
			System.exit(0);
		}
		UtilFunctions.printOptions(method, 0);
		int algo = sn.nextInt();
		workerThread[] wt = new workerThread [listOfFiles.length];
		for (int i = 0; i < filesAtFolder.size(); i++) {
			wt[i] = new workerThread(filesAtFolder.get(i), algo);
			new Thread(wt[i]).start();
		}
	}


}
