import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import lombok.Data;


public @Data class Encryption {

	private String filePath = "";
	private File result ,file;
	private InputStream is; OutputStream os;
	private Random r;
	//private int method ;
	private int numOfEnc ;
	private Vector<Node> keysAlgo = new Vector<>();
	public boolean isEncrypting;
	public Decryption d ; // used for reverseAlgorithm
	long startTime,endTime;
	private Clock clock;
	private int extraKey;
	private int currentKey;
	private boolean encFolder;
	private ArrayList<Integer> setOfActions;
	private Scanner sn;
	private int count;

	public Encryption(String name,boolean encFolder,boolean isEncrypting) {
		setNumOfEnc(0);
		setCount(0);
		filePath = name;
		file = new File(filePath);
		r=new Random();
		this.isEncrypting = isEncrypting;
		startTime=endTime=0;
		setOfActions = new ArrayList<Integer>();
		this.encFolder = encFolder;
		try {
			sn = new Scanner(System.in);
			if(setOfActions.isEmpty()  && isEncrypting){
				initSetOfActions(-1);
				System.out.println("Generated set of actions as follows!:\n");
				System.out.println(setOfActions.toString());
				System.out.println("With the following keys:\n");
				System.out.println(keysAlgo.toString());
				//initFiles();
			//	createKeyFile();
			}
			if(encFolder && isEncrypting){
				handleFolderSync();
				createKeyFile();
			}
			else if(isEncrypting) // encrypting one file
			{
				//initFiles();
				chooseMethod(setOfActions.get(count), false);
				createKeyFile();
		//		closeStreams();
			}

		} catch (IOException | KeyException e) {
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
				//int arr[] = convertIntegers(setOfActions);
				file = listOfFiles[i].getAbsoluteFile();
				initFiles();
				chooseMethod(setOfActions.get(0),false);
				count=0;
				numOfEnc=0;
			}
		}
	}
	
	public static int[] convertIntegers(ArrayList<Integer> integers)
	{
	    int[] ret = new int[integers.size()];
	    for (int i=0; i < ret.length; i++)
	    {
	        ret[i] = integers.get(i).intValue();
	    }
	    return ret;
	}
	private void closeStreams() {

		try {
			is.close();
			os.close();		

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initSetOfActions(int choose) {
		int input = -1;
		//System.out.println("Enter input");
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
			keysAlgo.add(new Node(r.nextInt(),String.valueOf(input)));
		}
		else{
			keysAlgo.add(new Node(r.nextInt(),String.valueOf(input)));
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

	public void multiplicationAlgo(boolean split) throws KeyException, IOException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		long start = System.nanoTime();
		setStartTime(start);
		initFiles();

		//initKeys(name,split);
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
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		initFiles();

		//initKeys(name, split);
		int curr = keysAlgo.get(numOfEnc).getKey();
		int extra = -1;
		if(split){ 
			numOfEnc++;
			extra = keysAlgo.get(numOfEnc).getKey();	
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
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		//initKeys(name, split);
		initFiles();
		int curr = keysAlgo.get(numOfEnc).getKey();
		System.out.println("with the following key: "+curr);
		int extra = -1;
		if(split){ 
			numOfEnc++;
			extra = keysAlgo.get(numOfEnc).getKey();	
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

		setEndTime(System.nanoTime() - startTime);

	}
	public void doubleAlgo(boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		// encryption first time
		count++;
		chooseMethod(setOfActions.get(count),split);
		long temp = getEndTime();
		// encrypting second time
		numOfEnc++;
		count++;
		chooseMethod(setOfActions.get(count),split);
		setEndTime(getEndTime()+ temp);

	}

	public void reverseAlgo(int choose,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		//UtilFunctions.printOptions(1,5);
		//@SuppressWarnings("resource")
		//Scanner sn = new Scanner(System.in);
		count++;
		boolean done ;
		do{
			//setMethod(sn.nextInt());
			done = true;
			switch (setOfActions.get(count)) {
			case 1:
			//	keysAlgo.add(new Node(r.nextInt(),"caesarAlgo"));
				d.setKeysAlgo(getKeysAlgo());
				d.caesarAlgo(split);
				break;
			case 2:
				//keysAlgo.add(new Node(r.nextInt(),"xorAlgo"));
				d.setKeysAlgo(getKeysAlgo());
				d.xorAlgo(split);
				break;
			case 3:
				//keysAlgo.add(new Node(r.nextInt(),"multiplicationAlgo"));
				d.setKeysAlgo(getKeysAlgo());
				d.multiplicationAlgo(split);
				break;
			/*case 4:
				//reverseAlgo(4,split);
				d.setNumOfDec(d.getNumOfDec()+1);
				reverseAlgo(4,split);
				break;*/
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	


	}


	public void chooseMethod(int choose,boolean split) throws IOException, KeyException {

		/*if(choose!=4)UtilFunctions.printOptions(1,0);
		else UtilFunctions.printOptions(1, 4);
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);*/

		boolean done ;
		do{
			//setMethod(sn.nextInt());
			done = true;
			switch (setOfActions.get(count)) {
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
				d = new Decryption(file.getAbsolutePath(),true,false);
				//d.setDecrypting(false);
				d.setSetOfActions(setOfActions);
				reverseAlgo(5,split);
				break;

			case 6: //Split algorithm
				count++;
				chooseMethod(setOfActions.get(count),true); //boolean value that indicates split key
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	
		closeStreams();
		/*try {
			createKeyFile();
		} catch (IOException e) {
			e.printStackTrace();
		}*/

	}

	public void createKeyFile() throws IOException {

		File f =null;
		if(encFolder){
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

}
