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
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import lombok.Data;

public @Data class Decryption {

	private String filePath = "", keyFile = "";
	private Scanner sn ;
	private File result ,file;
	private InputStream is; OutputStream os;
	private int numOfDec ;
	private Encryption e ; // used for reverse algorithm
	private long startTime,endTime;
	private boolean encFolder,hasSet,isDecrypting,sync;
	private LinkedList<Integer> setOfActions;
	private Vector<Node> keysAlgo ;


	public Decryption(String name,boolean encFolder,boolean isDecrypting,boolean sync,boolean hasSet) throws IOException {

		setFilePath(name);
		setDecrypting(isDecrypting);
		setSn(new Scanner(System.in));
		setFile(new File(name));
		setStartTime(0);
		setEndTime(0);
		setEncFolder(encFolder);
		setHasSet(hasSet);
		setSync(sync);
		setSetOfActions(new LinkedList<Integer>());
		setKeysAlgo(new Vector<Node>());

		initKey();
		handleTypeMethod();

	}


	private void handleTypeMethod() throws IOException {
		if(!isHasSet() && isDecrypting()){
			initSetOfActions(-1);
			System.out.println("Generated set of actions as follows!:\n");
			System.out.println(setOfActions.toString());
			System.out.println("With the following keys:\n");
			System.out.println(keysAlgo.toString());
		}
		try {
			if(!isHasSet() && isEncFolder() && isDecrypting()){
				if(isSync())
					handleFolderSync();
				else
					handleFolderASync();
			}
			else if(!isHasSet() && isDecrypting()){
				initFiles();
				chooseMethod(setOfActions, false);
				//	closeStreams();
			}
		} catch (KeyException e) {
			e.printStackTrace();
		}		
	}


	private void handleFolderSync() throws IOException, KeyException {
		File[] listOfFiles = file.listFiles();
		if(listOfFiles.length == 0) {
			System.out.println("Folder is empty! exit now!");
			System.exit(0);
		}		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				System.out.println("File " + listOfFiles[i].getName());
				file = listOfFiles[i].getAbsoluteFile();
				@SuppressWarnings("unchecked")
				LinkedList<Integer> set = (LinkedList<Integer>) setOfActions.clone();
				initFiles();
				chooseMethod(set,false);
				//count=0;
				setNumOfDec(0);
			}
		}
	}

	public class workerThread implements Runnable{
		String fileName;
		private LinkedList<Integer> ThreadActions;
		private Vector<Node> ThreadKeys ;

		@SuppressWarnings("unchecked")
		public workerThread(String name,LinkedList<Integer> set,Vector<Node> keys) {
			this.fileName=name;
			this.ThreadActions = (LinkedList<Integer>) set.clone();
			this.ThreadKeys = (Vector<Node>) keys.clone();
		}

		@Override
		public void run() {
			try {
				Decryption d = new Decryption(fileName,true,true,false,true);
				d.setOfActions = ThreadActions;
				d.setKeysAlgo(ThreadKeys);
				d.chooseMethod(ThreadActions, false);
				d.closeStreams(); // closing streams for this thread after done
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeyException e1) {
				e1.printStackTrace();
			}
			//System.out.println("Done encrypting file :" +fileName + " in ASync way");
		}

	}

	private void handleFolderASync() {
		File[] listOfFiles = file.listFiles();

		if(listOfFiles.length == 0) {
			System.out.println("Folder is empty! exit now!");
			System.exit(0);
		}		
		Vector<String> filesAtFolder = new Vector<>();
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				//System.out.println("File " + listOfFiles[i].getName());
				filesAtFolder.add(listOfFiles[i].getAbsolutePath());
			}
		}
		workerThread[] wt = new workerThread [listOfFiles.length];
		Thread threads[] = new Thread[wt.length];
		for (int i = 0; i < filesAtFolder.size(); i++) {
			wt[i] = new workerThread(filesAtFolder.get(i), setOfActions,keysAlgo);
			threads[i] = new Thread(wt[i]);
			threads[i].start();
		}
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeStreams() {

		try {
			is.close();
			os.close();		

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void multiplicationAlgo(boolean split) throws IOException, KeyException{
		initFiles();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
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
			numOfDec++;
			extraKey = keysAlgo.get(numOfDec).key;
			if(extraKey%2==0 || extraKey==0){
				throw new KeyException("key is invalid: "+currentKey);
			}
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


		setEndTime(System.nanoTime() - startTime);

	}

	public void xorAlgo(boolean split) throws IOException{
		initFiles();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.nanoTime());
		boolean done = false;
		int currentKey = getKeysAlgo().get(getNumOfDec()).getKey();
		int extraKey=0;

		if(split) {
			setNumOfDec(getNumOfDec()+1);;
			extraKey =  getKeysAlgo().get(getNumOfDec()).getKey();
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

		setEndTime(System.nanoTime() - getStartTime());
	}

	public void caesarAlgo(boolean split) throws IOException {
		initFiles();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.nanoTime());
		boolean done = false;
		int currentKey = getKeysAlgo().get(getNumOfDec()).getKey();
		//System.out.println("with the following key: "+currentKey);
		int extraKey=0;
		if(split) {
			setNumOfDec(getNumOfDec()+1);
			extraKey = getKeysAlgo().get(getNumOfDec()).getKey();
			//System.out.println("found about split algorithm!\nadding extrakey: "+extraKey);
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

		setEndTime(System.nanoTime() - startTime);

	}

	public void doubleAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		// encryption first time
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		chooseMethod(set,split);
		long temp = getEndTime();
		if(!split) {
			setNumOfDec(getNumOfDec()+1);
		}
		/*else{ //// made bugs for some reason
			swap(getKeysAlgo().get(numOfDec-1),getKeysAlgo().get(numOfDec));
		}*/
		chooseMethod(set,split);
		setEndTime(getEndTime()+ temp);

	}
	/*private void swap(Node node, Node node2) {
		Node temp = new Node(getKeysAlgo().get(numOfDec-1));
		getKeysAlgo().get(numOfDec-1).setKey(node2.key);
		getKeysAlgo().get(numOfDec-1).setAlgoName(node2.algoName);
		getKeysAlgo().get(numOfDec).setKey(temp.key);
		getKeysAlgo().get(numOfDec).setAlgoName(temp.algoName);
		
	}*/


	public void reverseAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		initFiles();
		int count = set.pop();
		switch (count) {
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
			reverseAlgo(set,split);
			e.setNumOfEnc(e.getNumOfEnc()+1);
			reverseAlgo(set,split);
			break;
		case 6:
			UtilFunctions.printStart("splitAlgo",getFilePath());
			reverseAlgo(set, true);
			break;
		default:
			System.out.println("Error on input for method!");
			break;
		}

	}

	public void chooseMethod(LinkedList<Integer> set,boolean split) throws IOException, KeyException {
		int choose = set.pop();
		switch (choose) {
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
			doubleAlgo(set,split);
			break;
		case 5:
			e = new Encryption(file.getAbsolutePath(),false,false,false,true);
			e.setSetOfActions(set);
			e.setKeysAlgo(getKeysAlgo());
			reverseAlgo(set,split);
			break;
		case 6:
			chooseMethod(set, true);
			break;
		default:
			System.out.println("Error on input for method!");
			break;
		}

	}

	private void initKey() throws FileNotFoundException{
		if(getKeysAlgo().isEmpty() && isDecrypting && !hasSet){
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

	}

	private void initFiles() throws FileNotFoundException{
		if(isDecrypting)
			if(encFolder){
				File folder = new File("decryption");
				if(!folder.exists()){
					folder.mkdir();
				}
				result = new File("decryption/"+file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
			}
			else
				result = new File(file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
		else{// wer'e encrypting
			if(encFolder){
				File folder = new File("encryption");
				if(!folder.exists()){
					folder.mkdir();
				}
				result = new File("encryption/"+file.getName()+".encrypted");
			}
			else
			{
				result = new File(file.getName()+".encrypted");
			}
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

	private void initSetOfActions(int choose) {
		int input = -1;
		if (choose==4)UtilFunctions.printOptions(1,4);
		else if (choose==5) UtilFunctions.printOptions(1, 5);
		else if (choose==6) UtilFunctions.printOptions(1, 6);
		else UtilFunctions.printOptions(1, 0);
		do{
			input = getInput();
			if(input==choose || (input > 6 || input < 1)){
				System.out.println("Invalid input,please try again");
			}
		}while(input==choose || (input > 6 && input < 1));

		setOfActions.add(input);
		if(input == 4) { // double algo
			initSetOfActions(input);
			initSetOfActions(input);
		}
		else if(input == 5){ // reverse algo
			initSetOfActions(input);
		}
		else if(input == 6){ //split algo
			initSetOfActions(input);
		}


	}

	private int getInput() {
		int input = 0;
		sn.useDelimiter("");
		boolean done = false;
		do{
			if(!sn.hasNextInt()){
				System.out.println("invalid input, please enter valid one...");
			}
			else{
				input = sn.nextInt();
				done = true;
			}
			sn.nextLine();
		} while(!done);

		return input;
	}


}
