import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class EncryptionTests {
	@Rule
	public TemporaryFolder testFolder = new TemporaryFolder();

	@Test
	public void testInTempFolder() throws IOException {
		System.out.println(System.getProperty("user.dir"));
		File tempFile = testFolder.newFile("file.txt");
		File tempFolder = testFolder.newFolder("folder");
		System.out.println("Test folder: " + tempFolder);
		// test...
	}

	@Test
	public void testEncryptionEmpty() throws IOException {
		File tempFile = testFolder.newFile("file.txt");
		Encryption e = new Encryption(tempFile.getAbsolutePath());
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
	public void testMultiAlgo() throws IOException {
		File tempFile = testFolder.newFile("file.txt");
		
		PipedInputStream pipeIn = new PipedInputStream();
		PipedOutputStream pipeOut = new PipedOutputStream();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		pipeIn.connect(pipeOut);
		PrintStream ps = new PrintStream(pipeOut);
		ps.print("hi");
		Encryption e = new Encryption(tempFile.getAbsolutePath());
		try {
			e.multiplicationAlgo(false);
		} catch (KeyException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	


}
