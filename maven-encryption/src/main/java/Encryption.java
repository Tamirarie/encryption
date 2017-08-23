import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import lombok.Data;

/**
 * This class consists of encryption algorithms and methods which can be used
 * to encrypt files and folders
 * 
 * @author Tamir Arie
 * 
 */
public @Data class Encryption {

	private String filePath = "";
	private File result ,file;
	private InputStream is; OutputStream os;
	private Random r;
	private int numOfEnc ;
	private boolean isEncrypting,hasSet,encFolder,sync;
	private Decryption d ; // used for reverseAlgorithm
	private long startTime,endTime;
	private Scanner sn;
	private LinkedList<Integer> setOfActions;
	private Vector<Node> keysAlgo;
	private final int Encrypt = 1;

	/**
	 * This constructor initialize the arguments required for encryption,
	 * and checks what should be executed:
	 * @param name the name of file/folder to be encrypted
	 * @param encFolder encrypt file or a folder,
	 * @param sync encrypt them in sync method or async
	 * @param isEncrypting check if it needs to be encrypted or decrypted
	 * @param hasSet check if someone wants to supply the Set of actions or it needs to be created
	 *  
	 */
	public Encryption(String name,boolean encFolder,boolean isEncrypting,boolean sync,boolean hasSet) {

		setNumOfEnc(0);
		setFilePath(name);
		setFile(new File (name));
		setR(new Random());
		setEncrypting(isEncrypting);
		setSync(sync);
		setHasSet(hasSet);
		setStartTime(0);
		setEndTime(0);
		setSetOfActions(new LinkedList<Integer>());
		setKeysAlgo(new Vector<Node>());
		setEncFolder(encFolder);
		setSn(new Scanner(System.in));
		handleTypeMethod();


	}
	/**
	 * This method handles the encryption 
	 * by the parameters provided by the user
	 */
	private void handleTypeMethod() {
		try {
			if(!isHasSet()  && isEncrypting()){
				initSetOfActions(-1);
				System.out.println("Generated set of actions as follows!:");
				System.out.println(getSetOfActions().toString());
				System.out.println("With the following keys:");
				System.out.println(getKeysAlgo().toString());
			}
			if(!isHasSet()  && isEncFolder() && isEncrypting()){
				if(isSync()){
					createKeyFile();
					handleFolderSync();
				}
				else{ // we want to create thread to each file
					createKeyFile();
					handleFolderASync();
				}
			}
			else if(!isHasSet() && isEncrypting()) // encrypting one file
			{
				createKeyFile();
				chooseMethod(getSetOfActions(), false);	
			}

		} catch (IOException | KeyException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * This method handles the encryption in async method
	 * it getting all the files in that folder and creating thread
	 * for each one of them and starting it and after it started all of them
	 * wait for all of them to finish and then record the time it took
	 */
	private void handleFolderASync() {

		long start = System.nanoTime();
		setStartTime(start);
		String folderName = getFile().getAbsolutePath();
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
			wt[i] = new workerThread(filesAtFolder.get(i), getSetOfActions() ,getKeysAlgo());
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
		setEndTime(System.nanoTime() - getStartTime());
		System.out.println("Finished encrypting folder: "+ folderName );
		UtilFunctions.printTime(getEndTime(), getEncrypt());
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
			Encryption e = new Encryption(fileName,true,true,false,true);
			try {
				e.setOfActions = ThreadActions;
				e.setKeysAlgo(ThreadKeys);
				e.chooseMethod(ThreadActions, false);
				e.closeStreams();

			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (KeyException e1) {
				e1.printStackTrace();
			}
			//System.out.println("Done encrypting file :" +fileName + " in ASync way");
		}

	}

	/**
	 * This method handles the encryption in sync method
	 * it getting all the files in that folder and setting the filepath each time
	 * for each one of them and starting chooseMethod function for each one of them
	 * each them it creating a setofActions clone to not harm each other
	 * and reseting the numOfEnc for the same reason
	 * wait for all of them to finish and then record the time it took
	 */
	private void handleFolderSync() throws IOException, KeyException {
		long start = System.nanoTime();
		setStartTime(start);
		File[] listOfFiles = file.listFiles();
		String folderName = file.getAbsolutePath();
		if(listOfFiles.length == 0) {
			System.out.println("Folder is empty! exit now!");
			System.exit(0);
		}		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				//System.out.println("File " + listOfFiles[i].getName());
				file = listOfFiles[i].getAbsoluteFile();
				setFilePath(file.getAbsolutePath());
				initFiles();
				@SuppressWarnings("unchecked")
				LinkedList<Integer> set = (LinkedList<Integer>) setOfActions.clone();
				chooseMethod(set,false);
				numOfEnc=0;
			}
		}
		setEndTime(System.nanoTime() - startTime);
		System.out.println("Finished encrypting folder: "+ folderName );
		UtilFunctions.printTime(getEndTime(), getEncrypt());
	}

	/**
	 * This method executed if there is no set and we're encrypting
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
			keysAlgo.add(new Node(r.nextInt(),String.valueOf(input))); // changed this line from above to upper
			initSetOfActions(input);
		}
		else{
			keysAlgo.add(new Node(r.nextInt(),String.valueOf(input)));
		}


	}

	/**
	 * simple method to get one int user input
	 * @return the user input int
	 */
	public int getInput() {
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
	 * The Multiplication Algorithm used to encrypt file
	 * it print it's name and then start record time for encryption
	 * initialize the files required to read and to write to
	 * getting the keys for that algorithm (if split equals true - add the next key that in keys)
	 * check that the keys are correct and cannot cause data loss
	 * and then read the file byte by byte and encrypting it
	 * after that set the end time to measure time it took to encrypt
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void multiplicationAlgo(boolean split) throws KeyException, IOException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		long start = System.nanoTime();
		setStartTime(start);
		initFiles();

		int curr = getKeysAlgo().get(getNumOfEnc()).getKey();
		int extra = -1;
		if(split){ 
			setNumOfEnc(getNumOfEnc()+1);;
			extra = getKeysAlgo().get(getNumOfEnc()).getKey();	
		}
		if(curr%2==0 ||curr==0){
			throw new KeyException("Key is invalid: "+curr);
		}
		else if(split)
		{
			if(extra%2 == 0 || extra == 0){
				throw new KeyException("Key is invalid: "+extra);
			}
		}
		int count = 1;
		boolean done = false;
		while(!done){
			try{
				int next = is.read();
				if(next == -1) done = true;
				else{
					byte b = (byte) next;
					byte c = 0;
					if(!split){
						c = (byte) (b*curr);
					}
					else{
						if(count%2 == 1) c=(byte) (b*curr);
						else c = (byte)(b*extra);
					}
					count++;
					os.write(c);
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}

		}

		long end = System.nanoTime() - start;
		setEndTime(end);

	}
	
	/**
	 * The Xor Algorithm used to encrypt file
	 * it print it's name and then start record time for encryption
	 * initialize the files required to read and to write to
	 * getting the keys for that algorithm (if split equals true - add the next key that in keys)
	 * check that the keys are correct and cannot cause data loss
	 * and then read the file byte by byte and encrypting it
	 * after that set the end time to measure time it took to encrypt
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void xorAlgo(boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.nanoTime());
		initFiles();

		int curr = getKeysAlgo().get(getNumOfEnc()).getKey();
		int extra = -1;
		if(split){ 
			setNumOfEnc(getNumOfEnc()+1);;
			System.out.println(keysAlgo.toString());
			extra = getKeysAlgo().get(getNumOfEnc()).getKey();	
		}
		if(curr == 0){
			throw new KeyException("Key is invalid: "+curr);
		}
		else if(split && extra == 0){
			throw new KeyException("Key is invalid: "+extra);
		}
		int count = 1;
		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = 0;
				if(!split) c = (byte) (b^curr);
				else{
					if(count%2==1) c= (byte) (b^curr);
					else c = (byte) (b^ extra);
				}
				count++;
				os.write(c);
			}
		}

		setEndTime(System.nanoTime() - startTime);

	}

	/**
	 * The Caesar Algorithm used to encrypt file
	 * it print it's name and then start record time for encryption
	 * initialize the files required to read and to write to
	 * getting the keys for that algorithm (if split equals true - add the next key that in keys)
	 * check that the keys are correct and cannot cause data loss
	 * and then read the file byte by byte and encrypting it
	 * after that set the end time to measure time it took to encrypt
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	public void caesarAlgo(boolean split) throws IOException, KeyException {
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		setStartTime(System.nanoTime());
		initFiles();
		int curr = getKeysAlgo().get(getNumOfEnc()).getKey();
		System.out.println("with the following key: "+curr);
		int extra = -1;
		if(split){ 
			setNumOfEnc(getNumOfEnc()+1);
			extra = getKeysAlgo().get(getNumOfEnc()).getKey();	
		}
		if(curr == 0){
			throw new KeyException("Key is invalid: "+curr);
		}
		else if(split && extra == 0){
			throw new KeyException("Key is invalid: "+extra);
		}
		int count = 1;
		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = 0;
				if(!split){
					c = (byte) (b+curr);
				}
				else{
					if(count%2 == 1) c = (byte) (b+curr);
					else c = (byte) (b+extra);
				}
				count++;
				if(c > Byte.MAX_VALUE) c = Byte.MIN_VALUE;
				os.write(c);
			}
		}

		setEndTime(System.nanoTime() - getStartTime());

	}
	
	/**
	 * The Double Algorithm used to encrypt one file
	 * it print it's name and then start record time for encryption
	 * calling the chooseMethod function twice and if split equals true increment the numOfEnc
	 * for that (that we will read the next keys)
	 * after that set the end time to measure time it took to encrypt
	 * @param set representing the set of actions that remained undone
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	
	public void doubleAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		// encryption first time
		chooseMethod(set,split);
		long temp = getEndTime();
		// encrypting second time
		if(!split) {// or else we'll get an out of bounds
			setNumOfEnc(getNumOfEnc()+1); 
		}
		
		chooseMethod(set,split);
		setEndTime(getEndTime()+ temp);

	}
	/**
	 * The Reverse Algorithm used to encrypt one file
	 * it print it's name
	 * it pops the next action that need to done at the given set and handle it in switch case
	 * it setting the keys of the decryption object to the keys that been generated here
	 * so it uses them and calls the method in decryption
	 * @param set representing the set of actions that remained undone
	 * @param split representing if we're at split algorithm
	 * @throws IOException in case of error on reading/writing file
	 * @throws KeyException in case of invalid key that can cause data loss etc..
	 */
	
	public void reverseAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();		
		UtilFunctions.printStart(name,getFilePath());
		d = new Decryption(file.getAbsolutePath(),isEncFolder(),false,false,true);
		d.setKeysAlgo(getKeysAlgo());
		initFiles();
		//d.setSetOfActions(setOfActions); // no need because we handle one method each time
		handleReverse(set,split);

	}

	private void handleReverse(LinkedList<Integer> set, boolean split) throws IOException, KeyException {
		int choose = set.pop();
		if(choose == 4){
			handleReverse(set,split);
			d.setNumOfDec(d.getNumOfDec()+1);
			handleReverse(set,split);
		}
		else if(choose == 6) handleReverse(set, true);
		else if(choose <= 3 && choose >= 1){
			d.getChooseActions()[choose-1].chooseSimple(split);
		}
		else if (choose <= 6){
			d.getChooseActions()[choose-1].chooseComplex(set, split);
		}
		
	}
	/**
	 * The chooseMethod function used to handle set of actions
	 * it pops the next action that need to done at the given set and handle it in switch case
	 * in case of choose=5 it creates decryption object and sending it the current file that
	 * need to be encrypted and if wer'e encrypting it in folder and sending booleans that indicate
	 * 1.false -that wer'e not decrypting (wer'e currently calling it from encryption)
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
	 * function required for creating a key.bin file in folder encryption
	 * or at current location and print to it the keys required later on to decryption
	 * @throws IOException in case of error on reading/writing file
	 */
	public void createKeyFile() throws IOException {

		File f =null;
		if(encFolder){
			File folder = new File("encryption");
			if(!folder.exists()){
				folder.mkdir();
			}
			f = new File("encryption/key.bin");
		}
		else{
			f = new File("key.bin");
		}
		FileOutputStream fos = new FileOutputStream(f);
		ObjectOutputStream out = new ObjectOutputStream(fos);
		out.writeObject(keysAlgo);

		out.flush();
		out.close();
		fos.flush();
		fos.close();
	}

	/**
	 * Function required for initialize files in case of encrypting or decrypting
	 * and then checks whether we encrypt in folder or at current location
	 * and then creating file input and output streams for them
	 * @throws FileNotFoundException in case file is not found
	 */
	private void initFiles() throws FileNotFoundException{
		if(isEncrypting)
			if(encFolder){
				File folder = new File("encryption");
				if(!folder.exists()){
					folder.mkdir();
				}
				result = new File("encryption/" + file.getName() + ".encrypted");
			}
			else
				result = new File(file.getName()+".encrypted");
		else{ // wer'e decrypting
			if(encFolder){
				File folder = new File("decryption");
				if(!folder.exists()){
					folder.mkdir();
				}
				result = new File("decryption/" + file.getName() + "_decrypted."+UtilFunctions.getFileExtension(file));
			}
			else{
				result = new File(file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
			}
		}

		is = new FileInputStream(file);
		os = new FileOutputStream(result);

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
