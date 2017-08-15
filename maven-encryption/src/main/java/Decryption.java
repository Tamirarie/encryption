import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Decryption {

	int method=0;
	String filePath = "";
	Scanner sn ;
	int key=-1;
	File result ,file;
	InputStream is; OutputStream os;

	public Decryption(String name) throws IOException {
		filePath = name;
		sn = new Scanner(System.in);
		file = new File(filePath);

	}

	public void multiplicationAlgo() throws IOException, KeyException{
		init();
		if(key%2==0 || key==0){
			throw new KeyException("key is invalid: "+key);
		}

		byte decrpytedKey=0;
		for(byte i = Byte.MIN_VALUE; i<Byte.MAX_VALUE ; i++){
			if((byte)(i*key)==1) decrpytedKey = i;
		}
		System.out.println("decrypted key is:"+String.valueOf(decrpytedKey));

		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = (byte) (b*decrpytedKey);
				os.write(c);
			}
		}

		is.close();
		os.close();
	}

	public void xorAlgo() throws IOException{
		boolean done = false;
		init();
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = (byte) (b^key);
				os.write(c);
			}
		}

		is.close();
		os.close();
	}

	public void caesarAlgo() throws IOException {
		boolean done = false;
		init();
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = (byte) (b-key);
				if(c<Byte.MIN_VALUE) c = Byte.MAX_VALUE;
				os.write(c);
			}
		}

		is.close();
		os.close();

	}

	public void doubleAlgo() throws IOException, KeyException{
		// encryption first time
		chooseMethod();
		// encrypting second time
		chooseMethod();
	}
	public void reverseAlgo() throws IOException, KeyException{
		UtilFunctions.printOptionsDec();
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		method = sn.nextInt();
		Encryption e = new Encryption(filePath);
		e.setKey(getKeyFromFile());
		boolean done ;
		do{
			done = true;
			switch (method) {
			case 1:
				caesarAlgo();
				break;
			case 2:
				xorAlgo();
				break;
			case 3:
				multiplicationAlgo();
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	
	}

	private void chooseMethod() throws IOException, KeyException {
		UtilFunctions.printOptionsDec();
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		method = sn.nextInt();
		boolean done ;
		do{
			done = true;
			switch (method) {
			case 1:
				caesarAlgo();
				break;
			case 2:
				xorAlgo();
				break;
			case 3:
				multiplicationAlgo();
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);		
	}

	private void init() throws FileNotFoundException{
		if(key == -1){
			System.out.println("Enter key bin file");
			try {
				key = getKeyFromFile();
				System.out.println("key is : " +key);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		result = new File(file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
		is = new FileInputStream(file);
		os = new FileOutputStream(result);
	}

	private int getKeyFromFile() throws IOException {
		File inputFile = new File(getKeyFile());
		double bytes = inputFile.length();
		byte[] data = new byte[(int) bytes];
		FileInputStream fis = new FileInputStream(inputFile);
		fis.read(data, 0, data.length);
		fis.close();
		return UtilFunctions.byteArrayToInt(data);
	}

	private String getKeyFile() {
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
		FileNameExtensionFilter binfilter = new FileNameExtensionFilter
				("BIN FILES (*.bin)", "bin");
		f.setDialogTitle("Choose the binary key file for decryption");
		f.setFileFilter(binfilter);
		
		f.showOpenDialog(null);
		
		return f.getSelectedFile().toString();
	}

	
	
	
}
