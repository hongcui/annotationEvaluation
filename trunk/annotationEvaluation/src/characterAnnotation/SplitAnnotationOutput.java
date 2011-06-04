/**
 * 
 */
package characterAnnotation;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.Writer;

/**
 * @author Hong Updates
 * take individual <statement>s from stanfordparser.java output and save them in a xml file
 * for XMLCompator.java
 */
public class SplitAnnotationOutput {
	private String annotation;
	private String outdir;
	private String answerdir;
	private String selectdir;
	
	public SplitAnnotationOutput(String annotationfile, String outdir, String selectdir, String answerdir){
		this.annotation = annotationfile;
		this.outdir = outdir;
		this.selectdir = selectdir;
		this.answerdir = answerdir;
	}
	
	private void split(){
		try{
		    FileInputStream fstream = new FileInputStream(annotation);
		    DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String line;
		    boolean read = false;
		    String fname = "";
		    String text = "";
		    while ((line = br.readLine()) != null){
		    	line = line.trim();
		    	
		      if(line.startsWith("<statement id=")){
		    	  fname = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
		    	  read = true;
		      }
		      if(read){
		    	  text += line+System.getProperty("line.separator");
		      }
		      if(line.startsWith("</statement>") || line.matches("<statement .*?/>")){
		    	  
		    	  output(outdir, fname+".xml", text);
		    	  fname = "";
		    	  text = "";
		    	  read = false;
		    	  
		      }		      
		    }
		    //Close the input stream
		    in.close();
		    }catch (Exception e){
		    	e.printStackTrace();
		    }

	}
	
	/**
	 * use the answerdir to select xml files for evaluation
	 */
	private void select(){
		File answerdir = new File(this.answerdir);
		File[] alist = answerdir.listFiles();
		for(int i = 0; i<alist.length; i++){
			String fname = alist[i].getName();
			String content = read(this.outdir, fname);
			output(this.selectdir, fname, content);
		}
	}

	public static String read(String dir, String fname){
		String content = "";
		try{
			FileInputStream istream = new FileInputStream(new File(dir, fname));
			InputStreamReader inread = new InputStreamReader(istream);
			BufferedReader buff = new BufferedReader(inread);
			String s="";
			while((s = buff.readLine())!=null){
				content += s+System.getProperty("line.separator");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return content;
	}
	public static void output(String dir, String fname, String content){
		try{
			File out = new File(dir, fname);
			FileWriter wrt = new FileWriter(out);
		    wrt.append(content);
		    wrt.flush();
		    wrt.close();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		/*
		String annotation = "C:\\Documents and Settings\\Hong Updates\\Desktop\\temp\\fna-annotation-new-complete.txt";
		String outdir ="C:\\DATA\\evaluation\\fnav19\\UnsupervisedStanford_Benchmark_sentence";
		String selectdir = "C:\\DATA\\evaluation\\fnav19\\UnsupervisedStanford_Benchmark_selected_sentence";
		String answerdir = "C:\\DATA\\evaluation\\fnav19\\AnsKey_Benchmark_selected_sentence";
		*/
		
		String annotation = "C:\\Documents and Settings\\Hong Updates\\Desktop\\temp\\treatise-annotation-new-complete.txt";
		String outdir ="C:\\DATA\\evaluation\\treatise\\UnsupervisedStanford_Benchmark_sentence";
		String selectdir = "C:\\DATA\\evaluation\\treatise\\UnsupervisedStanford_Benchmark_selected_sentence";
		String answerdir = "C:\\DATA\\evaluation\\treatise\\AnsKey_Benchmark_selected_sentence";
		
		SplitAnnotationOutput sao = new SplitAnnotationOutput(annotation, outdir, selectdir, answerdir);
		sao.split();
		sao.select();		
	}

}
