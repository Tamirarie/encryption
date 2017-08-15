import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

	public Encryption(String name) {
		setNumOfEnc(0);
		setMethod(0);
		filePath = name;
		file = new File(filePath);
		r=new Random();
		isEncrypting = true;
	}

	public void multiplicationAlgo(boolean split) throws KeyException, IOException{
		int currentKey = keysAlgo.get(numOfEnc).key;
		System.out.println(currentKey);//getKey());
		if(currentKey%2==0 ||currentKey==0){
			throw new KeyException("key is invalid: "+currentKey);
		}

		initFiles();
		boolean done = false;
		while(!done){
			try{
				int next = is.read();
				if(next == -1) done = true;
				else{
					byte b = (byte) next;
					byte c = (byte) (b*currentKey);
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

	}

	public void xorAlgo(boolean split) throws IOException, KeyException{
		int currentKey = keysAlgo.get(numOfEnc).key;
		System.out.println(currentKey);
		if(currentKey == 0) {
			throw new KeyException("key is invalid: " + currentKey);
		}
		initFiles();
		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = (byte) (b^currentKey);
				os.write(c);
			}
		}

		is.close();
		os.close();
	}

	public void caesarAlgo(boolean split) throws IOException, KeyException {
		int currentKey = keysAlgo.get(numOfEnc).key;
		System.out.println(currentKey);

		if(currentKey == 0) {
			throw new KeyException("key is invalid: " + currentKey);
		}
		initFiles();
		int extraKey=0;
		if(split) {
			extraKey = keysAlgo.get(numOfEnc+1).key;
			System.out.println("found about split algorithm!\nadding extrakey: "+extraKey);
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
					c = (byte) (b+currentKey);
				}
				else{
					if(count%2 == 1) c = (byte) (b+currentKey);
					else c = (byte) (b+extraKey);
				}
				if(c > Byte.MAX_VALUE) c = Byte.MIN_VALUE;
				os.write(c);
			}
		}

		is.close();
		os.close();
	}
	public void doubleAlgo(boolean split) throws IOException, KeyException{
		// encryption first time
		chooseMethod(4,split);
		// encrypting second time
		numOfEnc++;
		//setKey(r.nextInt());
		chooseMethod(4,split);

	}

	public void reverseAlgo(int choose,boolean split) throws IOException, KeyException{
		UtilFunctions.printOptions(1,5);
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		boolean done ;
		do{
			setMethod(sn.nextInt());
			done = true;
			switch (getMethod()) {
			case 1:
				keysAlgo.add(new Node(r.nextInt(),"caesar"));
				d.setKeysAlgo(getKeysAlgo());
				d.caesarAlgo(split);
				break;
			case 2:
				keysAlgo.add(new Node(r.nextInt(),"xor"));
				d.setKeysAlgo(getKeysAlgo());
				d.xorAlgo(split);
				break;
			case 3:
				keysAlgo.add(new Node(r.nextInt(),"multiplication"));
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
				keysAlgo.add(new Node(r.nextInt(),"caesar"));
				caesarAlgo(split);
				break;
			case 2:
				keysAlgo.add(new Node(r.nextInt(),"xor"));
				xorAlgo(split);
				break;
			case 3:
				keysAlgo.add(new Node(r.nextInt(),"multiplication"));
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
				keysAlgo.addElement(new Node(r.nextInt(), "split"));
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

	private void createKeyFile() throws IOException {

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
}
