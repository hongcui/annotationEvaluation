package characterAnnotation;
import java.io.File;
import java.io.IOException;

/**
 * Used to change the file name for Edward's and Prasad's annotation
 * For Edward use the first commented part. For Edward the naming format was _24.txt-2.xml and changed was 24.txt-2.xml
 * For Prasad use the second part. for prasad the naming format was 24-txt-2.xml and converted format is 24.txt-2.xml
 */

/**
 * @author prasad
 *
 */
public class FileNameChange {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		
		File dir = new File("C:\\my-prgms\\input");

		String[] children = dir.list();
		if (children == null) {
		    // Either dir does not exist or is not a directory
		} else {
		    for (int i=0; i<children.length; i++) {
		        // Get filename of file or directory
		        String filename = children[i];
		      //  System.out.println(filename);
		        if(filename.startsWith("_"))//for edward
		        {/*
		        	System.out.println("present");
		        	File oldFile = new File("C:\\my-prgms\\input\\"+filename);
		        	File newFile = new File("C:\\my-prgms\\output\\"+filename.substring(1));
		        	oldFile.renameTo(newFile);
		        	
		        */}
		        //// for prasad the naming format was 24-txt-2.xml and converted format is 24.txt-2.xml
		        int firstDash = filename.indexOf("-");
		        String firstPart = filename.substring(0,firstDash);
		        String secondPart = filename.substring(firstDash+1);
		        String newName= firstPart.concat(".").concat(secondPart);
		        if(firstDash!=0)
		        {
		       // 	System.out.println("present");
		        	File oldFile = new File("C:\\my-prgms\\input\\"+filename);
		        	File newFile = new File("C:\\my-prgms\\output\\"+newName);
		        	oldFile.renameTo(newFile);
		        	
		        }
		    }
		}

	}

}
