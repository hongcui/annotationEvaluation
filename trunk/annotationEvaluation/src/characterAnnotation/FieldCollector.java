/**
 * 
 */
package characterAnnotation;



import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.sql.*;
import com.mysql.jdbc.Driver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

//import org.apache.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;


/**
 * @author Hong Updates
 * This class takes a type of field (one of modifier, constraint, character, structure, or relation) as the input and output all occurrences of that field from annotation result 
 * The assumed annotation result is a text file containing annotated sentences separated by a bland line

 * output for 
 * structure: structure name, structure constraint, source sentence
 * character: character name, character value, source sentence
 * modifier: modifier value,  source sentence
 * constraint: constraint value, source sentence
 * relation: from organ name, relation name, to organ name, source sentence
 * 
 * all output are saved in database tables
 */
public class FieldCollector {
	private Hashtable<String, Document> sentences = new Hashtable<String, Document>();
	private Connection conn = null;
	private String driverPath="com.mysql.jdbc.Driver";
	private Hashtable<String, String> fieldXpath = new Hashtable<String,String>();
	private String tablePrefix = "";
		/**
	 * 
	 */
	
	public FieldCollector(String annotationResultFile, String dbname, String tableprefix) {
		//establish database connection
		this.tablePrefix = tableprefix;
		try{			
			Class.forName(driverPath);
			String url = "jdbc:mysql://localhost/"+dbname+"?user=termsuser&password=termspassword";
			conn = DriverManager.getConnection(url);			
		}catch(Exception e){
			e.printStackTrace();
		}
		fieldXpath.put("structure", "//structure");
		fieldXpath.put("character", "//character[@name and @value]");
		fieldXpath.put("modifier", "//character[@modifier]");
		fieldXpath.put("constraint", "//character[@constraint]");
		fieldXpath.put("relation", "//relation");
		readAnnotation(annotationResultFile);		
	}
	
	public FieldCollector(File[] files, String dbname, String tableprefix) {
		//establish database connection
		this.tablePrefix = tableprefix;
		try{			
			Class.forName(driverPath);
			String url = "jdbc:mysql://localhost/"+dbname+"?user=termsuser&password=termspassword";
			conn = DriverManager.getConnection(url);			
		}catch(Exception e){
			e.printStackTrace();
		}
		fieldXpath.put("structure", "//structure");
		fieldXpath.put("character", "//character[@name and @value]");
		fieldXpath.put("modifier", "//character[@modifier]");
		fieldXpath.put("constraint", "//character[@constraint]");
		fieldXpath.put("relation", "//relation");
		readAnnotation(files);		
	}
	
	/**
	 * save individual annotated sentences into a hashtable indexed by statement id
	 * @param annotationResultFile
	 */
	private void readAnnotation(String annotationResultFile) {
		try{
		    FileInputStream fstream = new FileInputStream(annotationResultFile);
		    DataInputStream in = new DataInputStream(fstream);
		    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    String strLine;
		    StringBuffer text = new StringBuffer();
		    boolean start = false;
		    int count = 1;
		    while ((strLine = br.readLine()) != null)   {
		      if(strLine.trim().compareTo("</statement>")==0 || strLine.trim().matches("<statement .*?/>")){//end of statement
		    	  if(text.length()>0 && text.toString().trim().startsWith("<statement")){
		    		  text.append(strLine+" ");
		    		  Document doc = formDocument(text);
		    		  Element statement = (Element)XPath.selectSingleNode(doc.getRootElement(), "/statement[@id]");
		    		  String id = statement.getAttributeValue("id");
		    		   this.sentences.put(id, doc);
		    		  text = new StringBuffer();
		    		  start = false;
		    		  System.out.println("read "+count+" files");
		    		  count++;
		    	  }
		      }else if(strLine.trim().startsWith("<statement")){
		    	  start = true;
		    	  text.append(strLine+" ");
		      }else if(start){
		    	  text.append(strLine+" ");
		      }
		    }
		    in.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void readAnnotation(File[] files) {
		try{
		    int count = 1;
		    for(int f = 0; f<files.length; f++) {
		    	SAXBuilder builder = new SAXBuilder();
				Document doc = builder.build(files[f]);
				Element statement = (Element)XPath.selectSingleNode(doc.getRootElement(), "./statement[@id]");
				if(statement==null){
					statement = (Element)XPath.selectSingleNode(doc.getRootElement(), "/statement[@id]");
				}
		    	//String id = statement.getAttributeValue("id");
				String id = files[f].getName();
		    	this.sentences.put(id, doc);		    		  
		    	System.out.println("read "+count+" files:"+id);
		    	count++;
		   }
		}catch (Exception e){
			e.printStackTrace();
		}
		
	}
	
	public void outputDocuments(String targetdir){
		try{
			File target = new File(targetdir);
			if(!target.isDirectory()){
				target.mkdir();
			}
			Enumeration<String> en = this.sentences.keys();
			while(en.hasMoreElements()){
				String id = en.nextElement();
				Document doc = this.sentences.get(id);	
				Element root = doc.getRootElement();
				root.detach();
				ParsingUtil.outputXML(root, new File(target, id+".xml"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private Document formDocument(StringBuffer text) {
		try{
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(new StringReader(text.toString()));
			return doc;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	public void collectFields(ArrayList<String> fields){
		Iterator<String> it = fields.iterator();
		while(it.hasNext()){
			String field = it.next();
			collectField(field);
		}		
	}
	
	public void collectField(String field){
		createTableFor(field);
		Hashtable<String, List> collection = new Hashtable<String, List>(); //selected elements/attributes => source sentence
		String xpath = this.fieldXpath.get(field);
		Enumeration en = this.sentences.keys();
		while(en.hasMoreElements()){
			String id = (String)en.nextElement();
			Document doc = this.sentences.get(id);
			Element root =  doc.getRootElement();
			try{
				List<Object> list = XPath.selectNodes(root, xpath);
				if(list.size()>0)
					collection.put(id, list);				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		output(collection, field);
	}


	/**
	 * save results into database table
	 * @param list
	 * @param field
	 */
	private void output(Hashtable<String, List> collection, String field) {
		Enumeration<String> en = collection.keys();
		while(en.hasMoreElements()){
			String id = en.nextElement();
			List<Object> structs = collection.get(id);
			Iterator<Object> it = structs.iterator();
			while(it.hasNext()){
				Element s = (Element)it.next();
				String value = formatValues(field, id, s);
				insert(field, value);
			}
		}
	
	}

	private String formatValues(String field, String id, Element s) {
		String values = "";
		try{
			if(field.compareTo("structure")==0){
				String name = ((Attribute)XPath.selectSingleNode(s, "@name")).getValue();
				values = "('"+name+"',";
				Attribute a = (Attribute)XPath.selectSingleNode(s, "@constraint");
				if(a!=null){
					String constraint = (a).getValue();
					values += "'"+constraint+"',";
				}else{
					values += "'',";
				}
				values = values+"'"+id+"')";
			}else if(field.compareTo("character")==0){
				String name = ((Attribute)XPath.selectSingleNode(s, "@name")).getValue();
				String value = ((Attribute)XPath.selectSingleNode(s, "@value")).getValue();//don't care about range values
				values = "('"+name+"','"+value+"','"+id+"')";
			}else if(field.compareTo("modifier")==0){
				String name = ((Attribute)XPath.selectSingleNode(s, "@modifier")).getValue();
				values = "('"+name+"','"+id+"')";
			}else if(field.compareTo("constraint")==0){
				String name = ((Attribute)XPath.selectSingleNode(s, "@constraint")).getValue();
				values = "('"+name+"','"+id+"')";
			}else if(field.compareTo("relation")==0){
				Attribute a = (Attribute)XPath.selectSingleNode(s, "@name");
				String rname = (a).getValue();
				String fromid = ((Attribute)XPath.selectSingleNode(s, "@from")).getValue();
				String toid = ((Attribute)XPath.selectSingleNode(s, "@to")).getValue();
				Document doc = this.sentences.get(id);
				Element root = doc.getRootElement();
				Element fromorgan = (Element)XPath.selectSingleNode(root, "//structure[@id='"+fromid+"']");
				Element toorgan = (Element)XPath.selectSingleNode(root, "//structure[@id='"+toid+"']");
				String fname = ((Attribute)XPath.selectSingleNode(fromorgan, "@name")).getValue();
				a = (Attribute)XPath.selectSingleNode(fromorgan, "@constraint");
				if(a!=null){
					fname = a.getValue()+" "+fname;
				}
				String tname = ((Attribute)XPath.selectSingleNode(toorgan, "@name")).getValue();
				a = (Attribute)XPath.selectSingleNode(toorgan, "@constraint");
				if(a!=null){
					tname = a.getValue()+" "+tname;
				}
				values = "('"+rname+"','"+fname+"','"+tname+"','"+id+"')";
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return values;
	}

	private void insert(String tablename, String values) {
		try{
			Statement stmt = conn.createStatement();
			System.out.println(values);
			stmt.execute("insert into "+this.tablePrefix+"_"+tablename+" values "+values);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private void createTableFor(String field) {
		try{
			Statement stmt = conn.createStatement();
			stmt.execute("drop table  if exists "+this.tablePrefix+"_"+field);
			if(field.compareTo("structure")==0){				
				stmt.execute("create table "+this.tablePrefix+"_structure (structureName varchar(100), structureConstraint varchar(1000), sourceSent varchar(50))");
			}else if(field.compareTo("character")==0){
				stmt.execute("create table "+this.tablePrefix+"_character (characterName varchar(100), characterValue varchar(1000), sourceSent varchar(50))");
			}else if(field.compareTo("modifier")==0){
				stmt.execute("create table "+this.tablePrefix+"_modifier (modifier varchar(1000), sourceSent varchar(50))");
			}else if(field.compareTo("constraint")==0){
				stmt.execute("create table "+this.tablePrefix+"_constraint (constraintt varchar(1000), sourceSent varchar(50))");
			}else if(field.compareTo("relation")==0){
				stmt.execute("create table "+this.tablePrefix+"_relation (relatioName varchar(1000), fromOrgan varchar(1000), toOrgan varchar(1000), sourceSent varchar(50))");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//make sure db is created before running this program
		String dbname = "annotationEvaluation";
		ArrayList<String> fields = new ArrayList<String> ();
		fields.add("structure");
		fields.add("character");
		fields.add("modifier");
		fields.add("constraint");
		fields.add("relation");
		//String annotationResults = "C:\\DATA\\annotationResults\\treatiseh-annotation.txt";
		//String prefix = "treatiseh";
		//String annotationResults = "C:\\DATA\\annotationResults\\bhl-annotation.txt";
		//String prefix = "bhl";
		//String annotationResults = "C:\\DATA\\annotationResults\\fnav19-annotation.txt";
		
		//String annotationResults = "C:\\Documents and Settings\\Hong Updates\\Desktop\\temp\\fna_ans_annotation.txt";
		
		//File dir = new File("C:\\DATA\\evaluation\\fnav19\\AnsKey_Benchmark_selected_sentence");
		//String prefix = "fnav19_ans";

		
		//String annotationResults = "C:\\Documents and Settings\\Hong Updates\\Desktop\\temp\\fna_utest_annotation.txt";
		
		//File dir = new File("C:\\DATA\\evaluation\\fnav19\\UnsupervisedStanford_Benchmark_selected_sentence");
		//String prefix = "fnav19_utest";
		
		
		//String annotationResults = "C:\\Documents and Settings\\Hong Updates\\Desktop\\temp\\treatise_ans_annotation.txt";
		//File dir = new File("C:\\DATA\\evaluation\\treatise\\AnsKey_Benchmark_selected_sentence");
		//String prefix = "treatise_ans";

		
		//String annotationResults = "C:\\Documents and Settings\\Hong Updates\\Desktop\\temp\\treatise_utest_annotation.txt";
		File dir = new File("C:\\DATA\\evaluation\\treatise\\UnsupervisedStanford_Benchmark_selected_sentence");
		String prefix = "treatise_utest";
		
		//FieldCollector fc = new FieldCollector(annotationResults,dbname, prefix);
		FieldCollector fc = new FieldCollector(dir.listFiles(),dbname, prefix);
		//String targetdir = "C:\\DATA\\annotationResults\\"+prefix;
		//fc.outputDocuments(targetdir);
		//fc.collectField("structure");
		fc.collectFields(fields);
		
	}

}
