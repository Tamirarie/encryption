import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.*;

import javax.swing.JFileChooser;

public class Menu {
	Scanner sn;
	private int method = 0; //1 equals to encryption, 2 to decryption
	private String fileName = "";
	private String filePath = "";

	public Menu() {
		System.out.println("Welcome to our program!\nPlease Enter number "
				+ "to Choose method:\n"
				+"1: Encryption\n2: Decryption\n"
				);
		sn = new Scanner(System.in);
		//sn.useDelimiter("");
		do{
			if(!sn.hasNextInt()) {
				System.out.println("invalid input, please enter valid one...");
			}
			else{
				method = sn.nextInt();
				if(method !=1 && method != 2)
					System.out.println("invalid input, please enter valid one...");
			}
			sn.nextLine();
		} while(method !=1 && method != 2);

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

	private void encryptHandle(String filePath) {
		try{
			Encryption e = new Encryption(filePath);
			//UtilFunctions.printOptions(method);
			e.chooseMethod(0,false);
			UtilFunctions.printTime(e.getEndTime(), method);
		}

		catch(IOException | KeyException e){
			System.out.println("Error: "+e);
		}

	}

	private void decryptHandle(String filePath) {
		try{
			Decryption d = new Decryption(filePath);
			//long startTime = System.nanoTime();
			d.chooseMethod(0,false);

			///estimate the time that it took now:
			UtilFunctions.printTime(d.getEndTime(), method);
			//long endTime = System.nanoTime() - startTime;
			//	UtilFunctions.printTime(estimatedTime,method);
		}
		catch(IOException | KeyException e){
			System.out.println("Error: "+e);
		}

	}

	private static String getPathFromUser() {
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
		f.setFileSelectionMode(JFileChooser.FILES_ONLY); 
		int ret = f.showOpenDialog(null);
		if(ret ==JFileChooser.APPROVE_OPTION)
			return f.getSelectedFile().toString();
		else{
			System.out.println("File chooser was canclled!");
			System.exit(0);
			return "";
		}
	}


	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Menu m = new Menu();
	}
}
