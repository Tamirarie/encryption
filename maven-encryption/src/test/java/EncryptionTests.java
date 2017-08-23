import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Vector;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EncryptionTests {
	//	private InputStream is; OutputStream os;
	Random r = new Random();
	private static final int Algos[] = {1,2,3,4,5,6};
	
	@Mock
	public Encryption eMocked;

	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@InjectMocks
	private Encryption eInjMock = 
	new Encryption("test.txt", false, true, false, true);


	@Test
	public void testInTempFolder() throws IOException {
		File tempFile = testFolder.newFile("file.txt");
		File tempFolder = testFolder.newFolder("folder");
		assertTrue(tempFile.exists());
		assertTrue(tempFolder.exists());
		// test...
	}

	@Test
	public void testCaesarAlgo() throws IOException, KeyException {
		File tempFile = testFolder.newFile("test.txt");
		PrintStream ps = new PrintStream(tempFile);
		String output = "hi, this is a test!";
		ps.println(output);
		ps.close();
		LinkedList<Integer> t = new LinkedList<Integer>();
		t.add(1);		

		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,true);
		e.setSetOfActions(t);
		Vector<Node> keys = new Vector<>();
		keys.add(new Node(50, "caesar"));
		e.setKeysAlgo(keys);
		try {
			e.caesarAlgo(false);
		} catch (KeyException e1) {
			e1.printStackTrace();
		}
		String resPath = e.getResult().getAbsolutePath();

		Decryption d = new Decryption(resPath,false,false,false,false);
		d.setKeysAlgo(e.getKeysAlgo());
		d.caesarAlgo(false);	
		Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		d.getResult().getAbsoluteFile().deleteOnExit();;
		assertEquals(output,content);
	}

	@Test
	public void testXorAlgo() throws IOException, KeyException {
		File tempFile = testFolder.newFile("test.txt");
		PrintStream ps = new PrintStream(tempFile);
		String output = "hi, this is a test!";
		ps.println(output);
		ps.close();
		LinkedList<Integer> t = new LinkedList<Integer>();
		t.add(2);		

		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,true);
		e.setSetOfActions(t);
		Vector<Node> keys = new Vector<>();
		keys.add(new Node(50, "xor"));
		e.setKeysAlgo(keys);
		try {
			e.xorAlgo(false);
		} catch (KeyException e1) {
			e1.printStackTrace();
		}
		String resPath = e.getResult().getAbsolutePath();

		Decryption d = new Decryption(resPath,false,false,false,false);
		d.setKeysAlgo(e.getKeysAlgo());
		d.xorAlgo(false);	
		Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		d.getResult().getAbsoluteFile().deleteOnExit();;
		assertEquals(output,content);
	}

	@Test
	public void testMultiplicationAlgo() throws IOException {
		boolean caughtException = false;
		File tempFile = testFolder.newFile("test.txt");
		PrintStream ps = new PrintStream(tempFile);
		String output = "hi, this is a test!";
		ps.println(output);
		ps.close();
		LinkedList<Integer> t = new LinkedList<Integer>();
		t.add(3);		

		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,true);
		e.setSetOfActions(t);
		Vector<Node> keys = new Vector<>();
		keys.add(new Node(50, "multiplication"));
		e.setKeysAlgo(keys);

		try {
			e.multiplicationAlgo(false);
		} catch (KeyException e1) {
			caughtException = true;
		}
		String resPath = e.getResult().getAbsolutePath();

		Decryption d = new Decryption(resPath,false,false,false,false);
		d.setKeysAlgo(e.getKeysAlgo());
		try {
			d.multiplicationAlgo(false);
		} catch (KeyException e1) {
			caughtException = true;
			//e1.printStackTrace();
		}
		//		Vector<Node> keys = d.getKeysAlgo();
		for (int i = 0; i < keys.size() ; i++) {
			int key = keys.get(i).getKey();
			if(key % 2 == 0 || key == 0){
				assertTrue(caughtException);
				return;
			}
		}
		Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		d.getResult().getAbsoluteFile().deleteOnExit();;
		assertEquals(output,content);
	}


	@Test
	public void testDoubleAlgo() throws IOException {
	//	boolean caughtException = false;
		File tempFile = testFolder.newFile("reverseTest.txt");
		PrintStream ps = new PrintStream(tempFile);
		String output = "hi, this is a test!";
		ps.println(output);
		ps.close();
		LinkedList<Integer> t = new LinkedList<Integer>();
		t.add(4);		// adding double,caesar,and xor algo
		t.add(1);
		t.add(2);

		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,true);
		e.setSetOfActions(t);

		Vector<Node> keys = new Vector<>();
		keys.add(new Node(50, "double_caesar"));
		keys.add(new Node(60, "double_xor"));

		e.setKeysAlgo(keys);

		try {
			e.chooseMethod(t,false);
		} catch (KeyException e1) {
			//caughtException = true;
		}
		String resPath = e.getResult().getAbsolutePath();
		t.add(4);		// adding double,caesar,and xor algo
		t.add(1);
		t.add(2);

		Decryption d = new Decryption(resPath,false,true,false,true);
		d.setKeysAlgo(keys);
		try {
			d.chooseMethod(t, false);
		} catch (KeyException e1) {
		//	caughtException = true;
		}
		

		Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		System.out.println("test:"+d.getResult().getAbsolutePath());
		d.getResult().getAbsoluteFile().deleteOnExit();;
		assertEquals(output,content);
	}

	@Test
	public void testReverseAlgo() throws IOException {
		for(int i=0 ; i < Algos.length ; i++){
			if(Algos[i] == 5) continue;
			boolean caughtException = false;
			//File tempFile = testFolder.newFile("reverseTest.txt");
			File tempFile = testFolder.newFile("largeFile.txt");
			UtilFunctions.createRandomFile(tempFile);
			PrintStream ps = new PrintStream(tempFile);
			String output = "hi, this is a test!";
			ps.println(output);
			ps.close();  //comment this to make test faster
			/*Scanner scanner = new Scanner(tempFile); // uncomment the next two to read the file each time
			String output = scanner.useDelimiter("\\Z").next();
			*/
			LinkedList<Integer> t = new LinkedList<Integer>();
			t=UtilFunctions.initSetList(5,Algos[i]);
			
			Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,true);
			e.setSetOfActions(t);
			Vector<Node> keys = new Vector<>();
			keys.add(new Node(UtilFunctions.initRandomKey(), "reverse_"+ Algos[i]));
			if(Algos[i] == 4 | Algos[i] == 6) 
				{
				keys.add(new Node(UtilFunctions.initRandomKey(), "reverse_"+ Algos[i]));
				}
			e.setKeysAlgo(keys);

			try {
 				e.chooseMethod(t,false);
			} catch (KeyException e1) {
				caughtException = true;
			}
			String resPath = e.getResult().getAbsolutePath();
			t = UtilFunctions.initSetList(5,Algos[i]); // init again after reset it in encrypt
			Decryption d = new Decryption(resPath,false,true,false,true);
			d.setKeysAlgo(keys);
			try {
				d.chooseMethod(t, false);
			} catch (KeyException e1) {
				caughtException = true;
			}
			if(Algos[i] == 3) 
			{
				if(UtilFunctions.checkIfInvalid(keys))
				{
					assertTrue(caughtException);
					continue;
				}
			}
			Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
			String content = scanner.useDelimiter("\\Z").next();
			scanner.close();
			d.getResult().getAbsoluteFile().deleteOnExit();;
			assertEquals(output,content);
			
			
			
		}
	}

	@Test
	public void testSplitAlgo() throws IOException {
		for(int i=0 ; i < Algos.length ; i++){
		if(Algos[i] == 6) continue;
		boolean caughtException = false;
		//File tempFile = testFolder.newFile("reverseTest.txt");
		File tempFile = testFolder.newFile("largeFile.txt");
		UtilFunctions.createRandomFile(tempFile);
		PrintStream ps = new PrintStream(tempFile);
		String output = "hi, this is a test!";
		ps.println(output);
		ps.close();
		LinkedList<Integer> t = new LinkedList<Integer>();
		t = UtilFunctions.initSetList(6, Algos[i]);
		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,true);
		e.setSetOfActions(t);

		Vector<Node> keys = new Vector<>();
		keys.add(new Node(UtilFunctions.initRandomKey(), "split_"+6));
		keys.add(new Node(UtilFunctions.initRandomKey(), "split_"+Algos[i]));
		if(Algos[i] == 4 || Algos[i] == 6)
			{
			keys.add(new Node(UtilFunctions.initRandomKey(), "split_"+ Algos[i]));
			//keys.add(new Node(initRandomKey(), "split_"+ 4));
			System.out.println(keys.toString());
			}
		keys.add(new Node(UtilFunctions.initRandomKey(), "split_"+ Algos[i]));


		e.setKeysAlgo(keys);

		try {
			e.chooseMethod(t,true);
		} catch (KeyException e1) {
			caughtException = true;
		}
		String resPath = e.getResult().getAbsolutePath();
		t = UtilFunctions.initSetList(6, Algos[i]);
		Decryption d = new Decryption(resPath,false,true,false,true);
		d.setKeysAlgo(keys);
		try {
			d.chooseMethod(t, true);
		} catch (KeyException e1) {
			caughtException = true;
		}
		if(Algos[i] == 3) 
		{
			if(UtilFunctions.checkIfInvalid(keys))
			{
				assertTrue(caughtException);
				continue;
			}
		}
		Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		d.getResult().getAbsoluteFile().deleteOnExit();;
		assertEquals(output,content);
		
		}
	}
	
	
}
