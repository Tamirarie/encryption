import java.io.File;

public class UtilFunctions {
	

public static byte[] intToByteArray(int a)
{
    return new byte[] {
        (byte) ((a >> 24) & 0xFF),
        (byte) ((a >> 16) & 0xFF),   
        (byte) ((a >> 8) & 0xFF),   
        (byte) (a & 0xFF)
    };
}
	public static int byteArrayToInt(byte[] b) 
	{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	public static String getFileExtension(File file) {
		String fileName = file.getName();
		if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0
				&& (fileName.indexOf(".") != fileName.lastIndexOf(".")))
			return fileName.substring(fileName.indexOf(".")+1,fileName.lastIndexOf("."));        
		else return "";
	}
	
	public static void printOptionsDec() {
		System.out.println("choose encryption method:");
		System.out.println("1. Caesar algorithm");
		System.out.println("2. Xor algorithm");
		System.out.println("3. Multiplication algorithem");		
	}
	
	public static void printOptionsEnc() {
		System.out.println("choose encryption method:");
		System.out.println("1. Caesar algorithm");
		System.out.println("2. Xor algorithm");
		System.out.println("3. Multiplication algorithem");		
	}

	public static void printTime(long estimatedTime,int method) {
		System.out.println("Total time of " + ((method == 1) 
				? "encryption" : "decryption")
				+ ": " + estimatedTime);

	}

	
}
