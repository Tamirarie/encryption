import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class UtilFunctionsTests {
	
	@Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
	
	@Test
	public void testFileExtension() throws IOException {
		File tempFile = testFolder.newFile("file.txt.encrypted");
		assertEquals("txt", UtilFunctions.getFileExtension(tempFile));
		File tempFile2 = testFolder.newFile("file.txt");
		assertEquals("", UtilFunctions.getFileExtension(tempFile2));
		assertTrue(tempFile.exists());
		assertTrue(tempFile2.exists());
	}
	
	@Test
	public void testByteConversion(){
		byte b[] = UtilFunctions.intToByteArray(8);
		assertEquals(8, UtilFunctions.byteArrayToInt(b));
	}

}
