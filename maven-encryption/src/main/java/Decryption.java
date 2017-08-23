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
/**
 * This class consists of decryption algorithms and methods which can be used
 * to decrypt files and folders
 * 
 * @author Tamir Arie
 * 
 */

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


	/**
	 * This constructor initialize the arguments required for decryption,
	 * and checks what should be executed:
	 * @param name the name of file/folder to be decrypted
	 * @param encFolder decrypt file or a folder,
	 * @param sync decrypt them in sync method or async
	 * @param isDecrypting check if it needs to be encrypted or decrypted
	 * @param hasSet check if someone wants to supply the Set of actions or it needs to be created
	 * @throws IOException in case of error on reading/writing file
	 *  
	 */
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


	/**
	 * This method executed if there is no set and we're decrypting
	 * it asks the user what algorithm he wants and acting by that:
	 * if it's 1-3 : add that to setOfActions and create key and finish
	 * if it's 4 (Double Algorithm) : add that and call twice (two another algorithm's) 
	 * if it's 5 (Reverse Algorithm) : add that and call once more (which algorithm need to be reversed)
	 * if it's 6 (Split Algorithm) : add that and one key
	 *  and call once more (which algorithm need to be with 2 keys)
	 *  @param choose representing the choose of last time for printing methods except that
	 *  and not enabling to choose the same method
	 *  
	 */

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
	/**
	 * This method handles the decryption 
	 * by the parameters provided by the user
	 */
	private void handleTypeMethod() throws IOException {
		if(!isHasSet() && isDecrypting()){
			initSetOfActions(-1);
			System.out.println("Generated set of actions as follows!:");
			System.out.println(getSetOfActions().toString());
			System.out.println("With the following keys:");
			System.out.println(getKeysAlgo().toString());
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
				chooseMethod(getSetOfActions(), false);
				//	closeStreams();
			}
		} catch (KeyException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * This method handles the encryption in async method
	 * it getting all the files in that folder and creating thread
	 * for each one of them and starting it and after it started all of them
	 * wait for all of them to finish and then record the time it took
	 */
	private void handleFolderSync() throws IOException, KeyException {
		long start = System.currentTimeMillis();
		File[] listOfFiles = getFile().listFiles();
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
				setNumOfDec(0);
			}
		}
		setEndTime(System.currentTimeMillis() - start);

	}
	/**
	 * This class is used for the implementation
	 *  of thread that execute certain file
	 */
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

	/**
	 * This method handles the encryption in async method
	 * it getting all the files in that folder and creating thread
	 * for each one of them and starting it and after it started all of them
	 * wait for all of them to finish and then record the time it took
	 */
	private void handleFolderASync() {
		long start = System.currentTimeMillis();
		File[] listOfFiles = getFile().listFiles();

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
		workerThread[] wt = new workerThread [filesAtFolder.size()];
		Thread threads[] = new Thread[wt.length];
		for (int i = 0; i < filesAtFolder.size(); i++) {
			wt[i] = new workerThread(filesAtFolder.get(i), getSetOfActions(),getKeysAlgo());
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
		setEndTime(System.currentTimeMillis() - start);
	}


	/**
	 * The Multiplication Algorithm used to decrypt file
	 * it print it's name and then start record time for decryption
	 * initialize the files required to read and to write to
	 * getting the keys for that algorithm (if split equals true - add the next key that in keys)
	 * check that the keys are correct and cannot cause data loss
	 * and then read the file byte by byte and decrypting it
	 * after that set the end time to measure time it took to decrypt
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void multiplicationAlgo(boolean split) throws IOException, KeyException{
		initFiles();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.currentTimeMillis());
		int currentKey = getKeysAlgo().get(getNumOfDec()).getKey();
		if(currentKey%2==0 || currentKey==0){
			throw new KeyException("key is invalid: "+currentKey);
		}

		byte decryptedKey=0;
		byte decryptedKey2=0;
		int extraKey = 0;
		int count = 1;

		if(split){
			setNumOfDec(getNumOfDec()+1);
			extraKey = getKeysAlgo().get(getNumOfDec()).getKey();
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


		setEndTime(System.currentTimeMillis() - getStartTime());

	}
	/**
	 * The Xor Algorithm used to decrypt file
	 * it print it's name and then start record time for decryption
	 * initialize the files required to read and to write to
	 * getting the keys for that algorithm (if split equals true - add the next key that in keys)
	 * check that the keys are correct and cannot cause data loss
	 * and then read the file byte by byte and encrypting it
	 * after that set the end time to measure time it took to decrypt
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void xorAlgo(boolean split) throws IOException, KeyException{
		initFiles();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.currentTimeMillis());
		boolean done = false;
		int currentKey = getKeysAlgo().get(getNumOfDec()).getKey();
		int extraKey=0;

		if(split) {
			setNumOfDec(getNumOfDec()+1);;
			extraKey =  getKeysAlgo().get(getNumOfDec()).getKey();
			System.out.println("found about split algorithm!\nadding extrakey: "+extraKey);
		}

		if(currentKey == 0){
			throw new KeyException("Key is invalid: "+currentKey);
		}
		else if(split && extraKey == 0){
			throw new KeyException("Key is invalid: "+extraKey);
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

		setEndTime(System.currentTimeMillis() - getStartTime());
	}

	/**
	 * The Caesar Algorithm used to decrypt file
	 * it print it's name and then start record time for decryption
	 * initialize the files required to read and to write to
	 * getting the keys for that algorithm (if split equals true - add the next key that in keys)
	 * check that the keys are correct and cannot cause data loss
	 * and then read the file byte by byte and encrypting it
	 * after that set the end time to measure time it took to decrypt
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void caesarAlgo(boolean split) throws IOException, KeyException {
		initFiles();
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.currentTimeMillis());
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
		if(currentKey == 0){
			throw new KeyException("Key is invalid: "+currentKey);
		}
		else if(split && extraKey == 0){
			throw new KeyException("Key is invalid: "+extraKey);
		}
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

		setEndTime(System.currentTimeMillis() - getStartTime());

	}

	/**
	 * The Double Algorithm used to decrypt one file
	 * it print it's name and then start record time for decryption
	 * calling the chooseMethod function twice and if split equals true increment the numOfDec
	 * for that (that we will read the next keys)
	 * after that set the end time to measure time it took to decrypt
	 * @param set representing the set of actions that remained undone
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void doubleAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		// encryption first time
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		chooseMethod(set,split);
		long temp = getEndTime();
		if(!split) {
			setNumOfDec(getNumOfDec()+1);
		}
		System.out.println(numOfDec);
		chooseMethod(set,split);
		setEndTime(getEndTime()+ temp);

	}

	/**
	 * The Reverse Algorithm used to decrypt one file
	 * it print it's name
	 * it pops the next action that need to done at the given set and handle it in switch case
	 * it setting the keys of the encryption object to the keys that been generated here
	 * so it uses them and calls the method in encryption
	 * @param set representing the set of actions that remained undone
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void reverseAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		initFiles();
		e = new Encryption(getFile().getAbsolutePath(),isEncFolder(),false,false,true);
		e.setSetOfActions(set);
		e.setKeysAlgo(getKeysAlgo());
		handleReverse(set,split);

	}

	private void handleReverse(LinkedList<Integer> set, boolean split) throws IOException, KeyException {
		int choose = set.pop();
		/*if(choose == 4){
			handleReverse(set,split);
			if(!split)e.setNumOfEnc(e.getNumOfEnc()+1);
			handleReverse(set,split);
			e.getChooseActions()[choose-1].chooseComplex(set, split);
		}*/
		/*else*/ if(choose == 6) handleReverse(set, true);
		else if(choose <= 3 && choose >= 1){
			e.getChooseActions()[choose-1].chooseSimple(split);
		}
		else if (choose <= 6){
			e.getChooseActions()[choose-1].chooseComplex(set, split);
		}
	}


	/**
	 * The chooseMethod function used to handle set of actions
	 * it pops the next action that need to done at the given set and handle it in switch case
	 * in case of choose=5 it creates encryption object and sending it the current file that
	 * need to be decrypted and if wer'e decrypting it in folder and sending booleans that indicate
	 * 1.false -that wer'e not encrypting (wer'e currently calling it from decryption)
	 * 2.false -that wer'e not in sync (not relevant because wer'e handling one file here)
	 * 3.true - that we have the set for the required operation (the set that wer'e using here)
	 * @param set representing the set of actions that remained undone
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 * 
	 */
	public void chooseMethod(LinkedList<Integer> set,boolean split) throws IOException, KeyException {
		int choose = set.pop();

		if(choose <= 3 && choose >= 1)
			chooseActions[choose-1].chooseSimple(split);
		else if(choose <= 6){
			chooseActions[choose-1].chooseComplex(set, split);
		}

	}

	/**
	 * simple Method that checks if the keys are empty and wer'e decrypting and wer'e
	 * not having set of actions yet - we ask for bin file and setting the keys to that
	 * @throws FileNotFoundExecption in case of key.bin not found
	 */
	private void initKey() throws FileNotFoundException{
		if(getKeysAlgo().isEmpty() && isDecrypting() && !isHasSet()){
			System.out.println("Enter key bin file");
			try {
				if(keyFile == ""){
					setKeyFile(getKeyFile());
				}
				setKeysAlgo(getKeyFromFile());
				System.out.println("keys are : " +getKeysAlgo());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Function required for initialize files in case of encrypting or decrypting
	 * and then checks whether we decrypt in folder or at current location
	 * and then creating file input and output streams for them
	 * @throws FileNotFoundExecption in case file is not found
	 */
	private void initFiles() throws FileNotFoundException{
		if(isDecrypting())
			if(isEncFolder()){
				File folder = new File("decryption");
				if(!folder.exists()){
					folder.mkdir();
				}
				result = new File("decryption/"+file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
			}
			else
				result = new File(file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
		else{// wer'e encrypting
			if(isEncFolder()){
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

	/**
	 * Function that extracting the keys from the bin file provided by the user
	 * @return vector of node object that contains the keys
	 * @throws IOException in case of error on reading/writing file
	 */
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

	/**
	 * Function that open up a gui that asking the user for the key file - showing only bin files
	 * @return string representing the key file path
	 */
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

	/**
	 * simple method to get one int user input
	 * @return the user input int
	 */
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


	/**
	 * method called at the end of handling the encryption/decryption:
	 * closing the streams inputstream and outputstream
	 */
	private void closeStreams() {
		try {
			is.close();
			os.close();		

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * this is test to do design pattern
	 */


	private ChooseAction[] chooseActions = new ChooseAction[] {
			new ChooseAction() { public void chooseSimple(boolean split) throws IOException, KeyException { caesarAlgo(split);; }
			@Override
			public void chooseComplex(LinkedList<Integer> set, boolean split) {				
			} },
			new ChooseAction() { public void chooseSimple(boolean split) throws IOException, KeyException { xorAlgo(split);}

			@Override
			public void chooseComplex(LinkedList<Integer> set, boolean split) {

			} },
			new ChooseAction() { public void chooseSimple(boolean split) throws IOException, KeyException { multiplicationAlgo(split); }

			@Override
			public void chooseComplex(LinkedList<Integer> set, boolean split) {

			} },
			new ChooseAction() { public void chooseComplex(LinkedList<Integer> set,boolean split) throws IOException, KeyException { doubleAlgo(set, split);}

			@Override
			public void chooseSimple(boolean split) throws IOException, KeyException {				
			} },
			new ChooseAction() { public void chooseComplex(LinkedList<Integer> set,boolean split) throws IOException, KeyException { reverseAlgo(set, split);}

			@Override
			public void chooseSimple(boolean split) throws IOException, KeyException {				
			} },
			new ChooseAction() { public void chooseComplex(LinkedList<Integer> set,boolean split) throws IOException, KeyException { splitAlgo(set, split);}

			@Override
			public void chooseSimple(boolean split) throws IOException, KeyException {				
			} },
	};
	public void chooseSimple(int i,boolean split) throws IOException, KeyException {
		chooseActions[i].chooseSimple(split);
	}
	private void splitAlgo(LinkedList<Integer> set, boolean split) throws IOException, KeyException {
		chooseMethod(set, true);
	}


	public void chooseComplex(int i,LinkedList<Integer> set,boolean split) throws IOException, KeyException {
		chooseActions[i].chooseComplex(set, split);
	}


}
