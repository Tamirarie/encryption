import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.Scanner;
import lombok.Data;


public @Data class Encryption {
	
	private String filePath = "";
	private int key;
	private File result ,file;
	private InputStream is; OutputStream os;
	private Random r;
	private int method = 0;

	public Encryption(String name) {
		
		filePath = name;
		file = new File(filePath);
		r=new Random();
		key = r.nextInt();
		try {
			createKeyFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void multiplicationAlgo() throws KeyException, IOException{
		System.out.println(key);
		if(key%2==0 || key==0){
			throw new KeyException("key is invalid: "+key);
		}
		initFiles();
		boolean done = false;
		while(!done){
			try{
				int next = is.read();
				if(next == -1) done = true;
				else{
					byte b = (byte) next;
					byte c = (byte) (b*key);
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

	public void xorAlgo() throws IOException, KeyException{
		System.out.println(key);
		if(key == 0) {
			throw new KeyException("key is invalid: "+key);
		}
		initFiles();
		boolean done = false;
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

	public void caesarAlgo() throws IOException, KeyException {
		System.out.println(key);

		if(key == 0) {
			throw new KeyException("key is invalid: "+key);
		}
		initFiles();
		boolean done = false;
		while(!done){
			int next = is.read();
			if(next == -1) done = true;
			else{
				byte b = (byte) next;
				byte c = (byte) (b+key);
				if(c > Byte.MAX_VALUE) c = Byte.MIN_VALUE;
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
		key = r.nextInt();
		chooseMethod();

	}

	public void reverseAlgo() throws IOException, KeyException{
		UtilFunctions.printOptionsEnc();
		@SuppressWarnings("resource")
		Scanner sn = new Scanner(System.in);
		method = sn.nextInt();
		Decryption d = new Decryption(filePath);
		d.key = r.nextInt();
		boolean done ;
		do{
			done = true;
			switch (method) {
			case 1:
				d.caesarAlgo();
				break;
			case 2:
				d.xorAlgo();
				break;
			case 3:
				d.multiplicationAlgo();
				break;
			default:
				System.out.println("Error on input for method! try again");
				done = false;
				break;
			}
		}while(!done);	


	}


	private void chooseMethod() throws IOException, KeyException {
		UtilFunctions.printOptionsEnc();
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

	private void createKeyFile() throws IOException {
		ByteBuffer b = ByteBuffer.allocate(4);
		b.putInt(key);		
		byte[] data = b.array();
		File f = new File("key.bin");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(data, 0, data.length);
		fos.flush();
		fos.close();
	}

	

	private void initFiles() throws FileNotFoundException{
		result = new File(file.getName()+".encrypted");
		is = new FileInputStream(file);
		os = new FileOutputStream(result);
		
	}
}
