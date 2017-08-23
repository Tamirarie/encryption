import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

public class UtilFunctions {


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
				+ ": " + estimatedTime/1000 + " Seconds");

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

	public static LinkedList<Integer> initSetList(int curr,int with) {
		LinkedList<Integer> t = new LinkedList<Integer>();
		t.add(curr);		// adding the current algo and the rest
		t.add(with);
		if(with == 4){ // double
			//t.add(1); t.add(2);
			t.add(5) ; t.add(6) ; t.add(1) ; t.add(2);
			
		}
		else if(with == 5){ // reverse 
			//t.add(1);
			t.add(4) ; t.add(1); t.add(2);
		}
		else if(with == 6){ // split
			t.add(1);
		}
		return t;
	}
	
	public static int initRandomKey(){
		return (new Random().nextInt());
	}
	public static boolean checkIfInvalid(Vector<Node> keys){
		for (int i = 0; i < keys.size() ; i++) {
			int key = keys.get(i).getKey();
			if(key % 2 == 0 || key == 0){
				return true;
			}
		}
		return false;
	}

}
