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

	private void handleTypeMethod() {
		try {
			if(!isHasSet()  && isEncrypting()){
				initSetOfActions(-1);
				System.out.println("Generated set of actions as follows!:\n");
				System.out.println(setOfActions.toString());
				System.out.println("With the following keys:\n");
				System.out.println(keysAlgo.toString());
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
				chooseMethod(setOfActions, false);	
			}

		} catch (IOException | KeyException e) {
			e.printStackTrace();
		}		
	}

	private void handleFolderASync() {

		long start = System.nanoTime();
		setStartTime(start);
		String folderName = file.getAbsolutePath();
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
		setEndTime(System.nanoTime() - startTime);
		System.out.println("Finished encrypting folder: "+ folderName );
		UtilFunctions.printTime(getEndTime(), getEncrypt());
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

	/*public void initKeys(){
		for (int i = 0; i < setOfActions.size(); i++) {
			keysAlgo.add(new Node(r.nextInt(),String.valueOf(setOfActions.get(i))));
		}
	}*/
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

	public void multiplicationAlgo(boolean split) throws KeyException, IOException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		long start = System.nanoTime();
		setStartTime(start);
		initFiles();

		int curr = keysAlgo.get(numOfEnc).getKey();
		int extra = -1;
		if(split){ 
			numOfEnc++;
			extra = keysAlgo.get(numOfEnc).getKey();	
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
	public void doubleAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name,getFilePath());
		// encryption first time
		chooseMethod(set,split);
		long temp = getEndTime();
		// encrypting second time
		if(!split) {
			setNumOfEnc(getNumOfEnc()+1); // or else we'll get an out of bounds
		}
		/*else{
			swap(getKeysAlgo().get(numOfEnc-1),getKeysAlgo().get(numOfEnc)); // made bugs for some reason
		}*/
		chooseMethod(set,split);
		setEndTime(getEndTime()+ temp);

	}
	/*private void swap(Node node, Node node2) {
		Node temp = new Node(getKeysAlgo().get(numOfEnc-1));
		getKeysAlgo().get(numOfEnc-1).setKey(node2.key);
		getKeysAlgo().get(numOfEnc-1).setAlgoName(node2.algoName);
		getKeysAlgo().get(numOfEnc).setKey(temp.key);
		getKeysAlgo().get(numOfEnc).setAlgoName(temp.algoName);
		
	}*/

	public void reverseAlgo(LinkedList<Integer> set,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		initFiles();
		UtilFunctions.printStart(name,getFilePath());
		int choose = set.pop();
		switch (choose) {
		case 1: // caesar
			d.setKeysAlgo(getKeysAlgo());
			d.caesarAlgo(split);
			break;
		case 2: // xoor
			d.setKeysAlgo(getKeysAlgo());
			d.xorAlgo(split);
			break;
		case 3: // multiplication
			d.setKeysAlgo(getKeysAlgo());
			d.multiplicationAlgo(split);
			break;
		case 4: //double
			reverseAlgo(set, split);
			d.setNumOfDec(d.getNumOfDec()+1);
			reverseAlgo(set, split);
			break;
		case 6: // split algo
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
			d = new Decryption(file.getAbsolutePath(),false,false,false,true);
			//d.setSetOfActions(setOfActions); // no need because we handle one method each time
			reverseAlgo(set,split);
			break;

		case 6: //Split algorithm
			chooseMethod(set,true); //boolean value that indicates split key
			break;
		default:
			System.out.println("Error on input for method!");
			break;
		}

	}

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

	private void closeStreams() {
		try {
			is.close();
			os.close();		

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
private void initKeys(String name,boolean split) {
	keysAlgo.add(new Node(r.nextInt(),name));
	setCurrentKey(keysAlgo.get(numOfEnc).key);
	if(name == "multiplicationAlgo"){
		while(currentKey%2==0 ||currentKey==0){
			invalidCurrKey(name);
			//throw new KeyException("key is invalid: "+currentKey);
		}
	}
	else if(name == "xorAlgo" || name =="caesarAlgo"){
		while(currentKey == 0) {
			invalidCurrKey(name);
		}

	}
	System.out.println("Generated random key: " + currentKey);
	if(split) {
		setExtraKey(keysAlgo.get(numOfEnc+1).key);
		if(name == "multiplicationAlgo"){
			while(extraKey%2==0 ||extraKey==0){
				invalidExtraKey(name);
				//throw new KeyException("key is invalid: "+extraKey);
			}
		}
		else if(name == "xorAlgo" || name =="caesarAlgo"){
			while(extraKey == 0) {
				invalidExtraKey(name);
			}

		}
		System.out.println("found about split algorithm!\nAdding extrakey: "+extraKey);
	}

}

private void invalidExtraKey(String name) {
	System.out.println("key is invalid: "+extraKey);
	keysAlgo.remove(numOfEnc+1);
	keysAlgo.add(new Node(r.nextInt(),"Split"));
	setExtraKey(keysAlgo.get(numOfEnc+1).key);
}

private void invalidCurrKey(String name) {
	System.out.println("key is invalid: "+currentKey);
	keysAlgo.remove(numOfEnc);
	keysAlgo.add(new Node(r.nextInt(),name));
	setCurrentKey(keysAlgo.get(numOfEnc).key);		
}
	 */
}
