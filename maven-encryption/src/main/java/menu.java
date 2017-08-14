import java.awt.Component;
import java.awt.HeadlessException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import javax.swing.*;

import javax.swing.JFileChooser;

public class menu {
	Scanner sn;
	private int method = 0; //1 equals to encryption, 2 to decryption
	private String fileName = "";
	private String filePath = "";

	public menu() {
		System.out.println("Welcome to our program!\nPlease Enter number "
				+ "to Choose method:\n"
				+"1: Encryption\n2: Decryption\n"
				);
		sn = new Scanner(System.in);
		do{
			method = sn.nextInt();
			if(method !=1 && method != 2)
				System.out.println("invalid input, please enter valid one...");
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
			encryption e = new encryption(filePath);
			System.out.println("choose encryption method:");
			System.out.println("1. Caesar algorithm");
			System.out.println("2. Xor algorithm");
			System.out.println("3. Multiplication algorithem");
			System.out.println("4. Double algorithm");
			
			boolean done ;
			do{
				done = true;
				int input = sn.nextInt();
				switch (input) {
				case 1:
					e.caesarAlgo();
					break;
				case 2:
					e.xorAlgo();
					break;
				case 3:
					e.multiplicationAlgo();
					break;
				case 4:
					e.doubleAlgo();
					break;
				case 5:
					e.reverseAlgo();
					break;
				default:
					System.out.println("Error on input for method! try again");
					done = false;
					break;
				}
			}while(!done);
		}

		catch(IOException | keyException e){
			System.out.println("Error: "+e);
		}

	}

	private void decryptHandle(String filePath) {
		try{
			decryption d = new decryption(filePath);
			System.out.println("choose decryption method:");
			System.out.println("1. Caesar algorithm");
			System.out.println("2. Xor algorithm");
			System.out.println("3. Multiplication algorithem");
			boolean done ;
			int input = sn.nextInt();
			long startTime = System.nanoTime();    
			do{
				done = true;
				// ... the code being measured ...    
				switch (input) {
				case 1:
					d.caesarAlgo();
					break;
				case 2:
					d.xorAlgo();
					break;
				case 3:
					d.multiplicationAlgo();
					break;
				case 4:
					d.doubleAlgo();
					break;
				default:
					System.out.println("Error on input for method! try again");
					done = false;
					break;
				}
			}while(!done);
			///estimate the time that it took now:
			long estimatedTime = System.nanoTime() - startTime;
			printTime(estimatedTime);
		}
		catch(IOException | keyException e){
			System.out.println("Error: "+e);
		}

	}

	private void printTime(long estimatedTime) {
		System.out.println("Total time of " + ((method == 1) 
				? "encryption" : "decryption")
				+ ": " + estimatedTime);

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
		f.showOpenDialog(null);
		return f.getSelectedFile().toString();
	}


	public static void main(String[] args) {
		@SuppressWarnings("unused")
		menu m = new menu();
	}
}
