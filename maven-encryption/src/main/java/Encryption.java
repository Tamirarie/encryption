import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;

import lombok.Data;


public @Data class Encryption {

	private String filePath = "";
	private File result ,file;
	private InputStream is; OutputStream os;
	private Random r;
	private int method ;
	private int numOfEnc ;
	private Vector<Node> keysAlgo = new Vector<>();
	public boolean isEncrypting;
	public Decryption d ; // used for reverseAlgorithm
	long startTime,endTime;
	private Clock clock;
	private int extraKey;
	private int currentKey;

	public Encryption(String name) {
		setNumOfEnc(0);
		setMethod(0);
		filePath = name;
		file = new File(filePath);
		r=new Random();
		isEncrypting = true;
		startTime=endTime=0;

	}

	public void multiplicationAlgo(boolean split) throws KeyException, IOException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		long start = System.nanoTime();
		setStartTime(start);
		initFiles();
		initKeys(name,split);
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
						c = (byte) (b*currentKey);
					}
					else{
						if(count%2 == 1) c=(byte) (b*currentKey);
						else c = (byte)(b*extraKey);
					}
					count++;
					os.write(c);
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}

		}

		try {
			is.close();
			os.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
		long end = System.nanoTime() - start;
		setEndTime(end);

	}

	public void xorAlgo(boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		initFiles();
		initKeys(name, split);

		int count = 1;
		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = 0;
				if(!split) c = (byte) (b^currentKey);
				else{
					if(count%2==1) c= (byte) (b^currentKey);
					else c = (byte) (b^ extraKey);
				}
				count++;
				os.write(c);
			}
		}

		is.close();
		os.close();
		setEndTime(System.nanoTime() - startTime);

	}

	public void caesarAlgo(boolean split) throws IOException, KeyException {
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		setStartTime(System.nanoTime());
		initFiles();
		initKeys(name, split);

		int count = 1;
		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = 0;
				if(!split){
					c = (byte) (b+currentKey);
				}
				else{
					if(count%2 == 1) c = (byte) (b+currentKey);
					else c = (byte) (b+extraKey);
				}
				count++;
				if(c > Byte.MAX_VALUE) c = Byte.MIN_VALUE;
				os.write(c);
			}
		}

		is.close();
		os.close();
		setEndTime(System.nanoTime() - startTime);

	}
	public void doubleAlgo(boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		// encryption first time
		chooseMethod(4,split);
		long temp = getEndTime();
		// encrypting second time
		numOfEnc++;
		//setKey(r.nextInt());
		chooseMethod(4,split);
		setEndTime(getEndTime()+ temp);

	}

	public void reverseAlgo(int choose,boolean split) throws IOException, KeyException{
		String name = new Object(){}.getClass().getEnclosingMethod().getName();
		UtilFunctions.printStart(name);
		UtilFunctions.printOptions(1,5);
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		boolean done ;
		do{
			setMethod(sn.nextInt());
			done = true;
			switch (getMethod()) {
			case 1:
				keysAlgo.add(new Node(r.nextInt(),"caesarAlgo"));
				d.setKeysAlgo(getKeysAlgo());
				d.caesarAlgo(split);
				break;
			case 2:
				keysAlgo.add(new Node(r.nextInt(),"xorAlgo"));
				d.setKeysAlgo(getKeysAlgo());
				d.xorAlgo(split);
				break;
			case 3:
				keysAlgo.add(new Node(r.nextInt(),"multiplicationAlgo"));
				d.setKeysAlgo(getKeysAlgo());
				d.multiplicationAlgo(split);
				break;
			case 4:
				reverseAlgo(4,split);
				d.setNumOfDec(d.getNumOfDec()+1);
				reverseAlgo(4,split);
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	

		try {
			createKeyFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void chooseMethod(int choose,boolean split) throws IOException, KeyException {

		if(choose!=4)UtilFunctions.printOptions(1,0);
		else UtilFunctions.printOptions(1, 4);
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);

		boolean done ;
		do{
			setMethod(sn.nextInt());
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
				if(choose == 4){
					System.out.println("Error on input for method! try again");
					done = false;
				}
				else{
					doubleAlgo(split);
				}
				break;
			case 5:
				if(choose == 5){
					System.out.println("Error on input for method! try again");
					done = false;
				}
				else{
					d = new Decryption(getFilePath());
					d.setDecrypting(false);
					reverseAlgo(5,split);
				}
				break;

			case 6: //Split algorithm
				keysAlgo.addElement(new Node(r.nextInt(), "Split"));
				chooseMethod(6,true); //boolean value that indicates split key
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	

		try {
			createKeyFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void createKeyFile() throws IOException {

		File f = new File("key.bin");
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
			result = new File(file.getName()+".encrypted");
		else{
			result = new File(file.getName()+"_decrypted."+UtilFunctions.getFileExtension(file));
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
