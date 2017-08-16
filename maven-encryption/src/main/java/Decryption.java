import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import lombok.Data;

public @Data class Decryption {

	int method=0;
	String filePath = "", keyFile = "";
	Scanner sn ;
	File result ,file;
	InputStream is; OutputStream os;
	private int numOfDec ;
	private Vector<Node> keysAlgo = new Vector<>();
	public boolean isDecrypting;
	public Encryption e ; // used for reverse algorithm
	long startTime,endTime;

	public Decryption(String name) throws IOException {
		filePath = name;
		sn = new Scanner(System.in);
		file = new File(filePath);
		isDecrypting = true;
		startTime=endTime=0;

	}

	public void multiplicationAlgo(boolean split) throws IOException, KeyException{
		init();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		int currentKey = keysAlgo.get(numOfDec).key;
		if(currentKey%2==0 || currentKey==0){
			throw new KeyException("key is invalid: "+currentKey);
		}

		byte decryptedKey=0;
		byte decryptedKey2=0;
		int extraKey = 0;
		int count = 1;

		if(split){
			extraKey = keysAlgo.get(numOfDec+1).key;
			System.out.println("found about split algorithm!\nadding extrakey: "+extraKey);
		}

		for(byte i = Byte.MIN_VALUE; i<Byte.MAX_VALUE ; i++){
			if((byte)(i*currentKey)==1){
				decryptedKey = i;
			}
			if(split){
				if((byte)(i*extraKey)==1){
					decryptedKey2 = i;
				}
			}
		}
		System.out.println("decrypted key is:"+String.valueOf(decryptedKey));
		if(split) System.out.println("decrypted key2 is:"+String.valueOf(decryptedKey2));

		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = 0;
				if(!split){
					c = (byte) (b*decryptedKey);
				}
				else{
					if(count%2 == 1) c = (byte) (b*decryptedKey);
					else c = (byte)(b*decryptedKey2);
				}
				count++;
				os.write(c);
			}
		}

		is.close();
		os.close();
		setEndTime(System.nanoTime() - startTime);

	}

	public void xorAlgo(boolean split) throws IOException{
		init();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		boolean done = false;
		int currentKey = keysAlgo.get(numOfDec).key;
		int extraKey=0;
		if(split) {
			extraKey = keysAlgo.get(numOfDec+1).key;
			System.out.println("found about split algorithm!\nadding extrakey: "+extraKey);
		}
		int count = 1;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c =0;
				if(!split){
					c = (byte) (b^currentKey);
				}
				else{
					if(count%2 == 1) c=(byte) (b^currentKey);
					else c = (byte) (b^extraKey);
				}
				count++;
				os.write(c);
			}
		}

		is.close();
		os.close();
		setEndTime(System.nanoTime() - startTime);
	}

	public void caesarAlgo(boolean split) throws IOException {
		init();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		boolean done = false;
		int currentKey = keysAlgo.get(numOfDec).getKey();
		int extraKey=0;
		if(split) {
			extraKey = keysAlgo.get(numOfDec+1).key;
			System.out.println("found about split algorithm!\nadding extrakey: "+extraKey);
		}
		int count = 1;

		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = 0;
				if(!split){
					c = (byte) (b-currentKey);
				}
				else{
					if(count%2 == 1) c=(byte) (b-currentKey);
					else c = (byte) (b-extraKey);
				}
				if(c<Byte.MIN_VALUE) c = Byte.MAX_VALUE;
				os.write(c);
				count++;
			}
		}

		is.close();
		os.close();
		setEndTime(System.nanoTime() - startTime);

	}

	public void doubleAlgo(boolean split) throws IOException, KeyException{
		// encryption first time
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		chooseMethod(4,split);
		long temp = getEndTime();
		numOfDec++;
		// encrypting second time
		chooseMethod(4,split);
		setEndTime(getEndTime()+ temp);

	}
	public void reverseAlgo(int choose,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		UtilFunctions.printOptions(2,5);
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		setMethod(sn.nextInt()); 

		boolean done ;
		do{
			done = true;
			switch (getMethod()) {
			case 1:
				e.caesarAlgo(split);
				break;
			case 2:
				e.xorAlgo(split);
				break;
			case 3:
				e.multiplicationAlgo(split);
				break;
			case 4:
				reverseAlgo(4,split);
				e.setNumOfEnc(e.getNumOfEnc()+1);
				reverseAlgo(4,split);
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	
	}

	public void chooseMethod(int choose,boolean split) throws IOException, KeyException {
		//if(keyFile == "") keyFile=getKeyFile();
		if(choose != 4) UtilFunctions.printOptions(2,0);
		else UtilFunctions.printOptions(2,4);
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		boolean done ;
		do{
			setMethod( sn.nextInt());
			done = true;
			switch (getMethod()) {
			case 1:
				caesarAlgo(split);
				break;
			case 2:
				xorAlgo(split);
				break;
			case 3:
				multiplicationAlgo(split);
				break;
			case 4:
				doubleAlgo(split);
				break;
			case 5:
				e = new Encryption(filePath);
				e.setEncrypting(false);
				init();
				e.setKeysAlgo(getKeysAlgo());
				reverseAlgo(5,split);
				break;
			case 6:
				chooseMethod(6, true);
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);		
	}

	private void init() throws FileNotFoundException{
		if(getKeysAlgo().isEmpty() && isDecrypting){
			System.out.println("Enter key bin file");
			try {
				if(keyFile == ""){
					keyFile = getKeyFile();
				}
				setKeysAlgo(getKeyFromFile());
				System.out.println("keys are : " +getKeysAlgo());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(isDecrypting)
			result = new File(file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
		else{
			result = new File(file.getName()+".encrypted");
		}
		is = new FileInputStream(file);
		os = new FileOutputStream(result);
	}

	@SuppressWarnings("unchecked")
	private Vector<Node> getKeyFromFile() throws IOException {
		File inputFile = new File(keyFile);
		FileInputStream fis = new FileInputStream(inputFile);
		ObjectInputStream in = new ObjectInputStream(fis);
		Vector<Node> keys = new Vector<>();
		try {
			keys = (Vector<Node>) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		fis.close();
		return keys;
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

		int ret = f.showOpenDialog(null);
		if(ret ==JFileChooser.APPROVE_OPTION)
			return f.getSelectedFile().toString();
		else{
			System.out.println("File chooser was canclled!");
			System.exit(0);
			return "";
		}
	}




}
