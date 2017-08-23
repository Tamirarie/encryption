import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

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
	
	public static void printOptions(int method,int choose) {
		System.out.println("choose " + ((method == 1) ? "encryption" : "decryption")+" method:");
		System.out.println("1. Caesar algorithm");
		System.out.println("2. Xor algorithm");
		System.out.println("3. Multiplication algorithem");	
		if(choose!=4)System.out.println("4. Double algorithem");		
		if(choose!=5)System.out.println("5. Reverse algorithem");
		if(choose!=6)System.out.println("6. Split algorithm");
		
	}
	
	

	public static void printTime(long estimatedTime,int method) {
		
		System.out.println("Total time of " + ((method == 1) 
				? "encryption" : "decryption")
				+ ": " + (double)estimatedTime/1000000000 + " in seconds");

	}

	public static void printStart(String className,String fileName){
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		System.out.println("Started "+ className +" on File:"+ fileName+" at "+ dateFormat.format(date));
	}
	
	public static void createRandomFile(File name) throws IOException{
		RandomAccessFile f = new RandomAccessFile(name, "rw");
        f.setLength(150 * 150 * 150);
        String text = "this is a large file";
        int len =(int)( f.length());
        
        while(f.getFilePointer() < len)
        {
        	f.writeBytes(text);
        }
        f.close();
	}

	
}
