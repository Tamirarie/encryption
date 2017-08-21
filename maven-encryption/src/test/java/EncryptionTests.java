import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EncryptionTests {
//	private InputStream is; OutputStream os;
	
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testInTempFolder() throws IOException {
		File tempFile = testFolder.newFile("file.txt");
		File tempFolder = testFolder.newFolder("folder");
		assertTrue(tempFile.exists());
		assertTrue(tempFolder.exists());
		// test...
	}

	@Test
	public void testEncryptionEmpty() throws IOException {
		File tempFile = testFolder.newFile("file.txt");
		@SuppressWarnings("unused")
		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,false);
		//assertEquals(tempFile.getAbsolutePath(), e.filePath);
		File f = new File("key.bin");
		assertTrue(f.exists());
		assertTrue(f.delete());
		
	}

	@Test
	public void testEncryptionStream() throws IOException {
		/*File tempFile = testFolder.newFile("file.txt");
		Encryption e = new Encryption(tempFile.getAbsolutePath());
		assertEquals(tempFile.getAbsolutePath(), e.filePath);
		File f = new File("key.bin");
		assertTrue(f.exists());
		assertTrue(f.delete());*/

		/*PipedInputStream pipeIn = new PipedInputStream();
		PipedOutputStream pipeOut = new PipedOutputStream();
		pipeIn.connect(pipeOut);
		pipeOut.write(65);
		PrintStream ps = new PrintStream(pipeOut);
		
		
		int c;
		while((c = pipeIn.read() ) != -1)
		{
			System.out.print((char) c);
		}*/
	}
	
	@Test
	public void testCaesarAlgo() throws IOException {
		File tempFile = testFolder.newFile("test.txt");
		PrintStream ps = new PrintStream(tempFile);
		String output = "hi, this is a test!";
		ps.println(output);
		ps.close();
		Encryption e = new Encryption(tempFile.getAbsolutePath(),false,true,false,false);
		try {
			e.caesarAlgo(false);
			e.createKeyFile();
		} catch (KeyException e1) {
			e1.printStackTrace();
		}
		String resPath = e.getResult().getAbsolutePath();
		
		Decryption d = new Decryption(resPath,false,true,false,false);
		String key = "key.bin";
		d.setKeyFile(key);
		d.caesarAlgo(false);	
		Scanner scanner = new Scanner(new File(d.getResult().getAbsolutePath()));
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		assertEquals(output,content);
	}
	


}
