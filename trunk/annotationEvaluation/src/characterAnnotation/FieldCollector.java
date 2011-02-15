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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

//import org.apache.log4j.Logger;
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
		fieldXpath.put("character", "//character");
		fieldXpath.put("modifier", "//character[@modifier]");
		fieldXpath.put("constraint", "//character[@constraint]");
		fieldXpath.put("relation", "//relation");
		readAnnotation(annotationResultFile);		
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
		    while ((strLine = br.readLine()) != null)   {
		      if(strLine.trim().length()==0){//blank line
		    	  if(text.length()>0){
		    		  Document doc = formDocument(text);
		    		  String id = (String)XPath.selectSingleNode(doc.getRootElement(), "statement/[@id]");
		    		  this.sentences.put(id, doc);
		    	  }
		      }
		    }
		    in.close();
		}catch (Exception e){
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
		String xpath = this.fieldXpath.get("field");
		Enumeration en = this.sentences.keys();
		while(en.hasMoreElements()){
			String id = (String)en.nextElement();
			Document doc = this.sentences.get(id);
			Element root =  doc.getRootElement();
			try{
				List<Object> list = XPath.selectNodes(root, xpath);
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
				String value = insertValue(field, id, s);
				insert("structure", value);
			}
		}
	
	}

	private String insertValue(String field, String id, Element s) {
		String values = "";
		try{
			if(field.compareTo("structure")==0){
				String name = (String)XPath.selectSingleNode(s, "/[@name]");
				String constraint = (String)XPath.selectSingleNode(s, "/[@constraint]");
				values = "('"+name+"','"+constraint+"','"+id+"')";
			}else if(field.compareTo("character")==0){
				String name = (String)XPath.selectSingleNode(s, "/[@name]");
				String value = (String)XPath.selectSingleNode(s, "/[@value]");//don't care about range values
				values = "('"+name+"','"+value+"','"+id+"')";
			}else if(field.compareTo("modifier")==0){
				String name = (String)XPath.selectSingleNode(s, "/[@modifier]");
				values = "('"+name+"','"+id+"')";
			}else if(field.compareTo("constraint")==0){
				String name = (String)XPath.selectSingleNode(s, "/[@constraint]");
				values = "('"+name+"','"+id+"')";
			}else if(field.compareTo("relation")==0){
				String rname = (String)XPath.selectSingleNode(s, "/[@name]");
				String from = (String)XPath.selectSingleNode(s, "/[@from]");
				String to = (String)XPath.selectSingleNode(s, "/[@to]");
				Document doc = this.sentences.get(id);
				Element root = doc.getRootElement();
				Element fromorgan = (Element)XPath.selectSingleNode(root, "//structure[@id='"+from+"']");
				Element toorgan = (Element)XPath.selectSingleNode(root, "//structure[@id='"+to+"']");
				String fname = (String)XPath.selectSingleNode(fromorgan, "/[@name]");
				String tname = (String)XPath.selectSingleNode(toorgan, "/[@name]");
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
			stmt.execute("insert into "+this.tablePrefix+"_"+tablename+" values "+values);
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	private void createTableFor(String field) {
		try{
			Statement stmt = conn.createStatement();
			stmt.execute("drop "+field+" if exists");
			if(field.compareTo("structure")==0){				
				stmt.execute("create table "+this.tablePrefix+"_structure (structureName varchar(100), structureConstraint varchar(100), sourceSent varchar(20))");
			}else if(field.compareTo("character")==0){
				stmt.execute("create table "+this.tablePrefix+"_character (characterName varchar(100), characterValue varchar(100), sourceSent varchar(20))");
			}else if(field.compareTo("modifier")==0){
				stmt.execute("create table "+this.tablePrefix+"_modifier (modifier varchar(100), sourceSent varchar(20))");
			}else if(field.compareTo("constraint")==0){
				stmt.execute("create table "+this.tablePrefix+"_constraint (constraint varchar(100), sourceSent varchar(20))");
			}else if(field.compareTo("relation")==0){
				stmt.execute("create table "+this.tablePrefix+"_relation (relatioName varchar(100), fromOrgan varchar(100), toOrgan varchar(100), sourceSent varchar(20))");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String dbname = "annotationEvaluation";
		/*String annotationResults = "C:\\DATA\\annotationResults\\treatiseh-annotation.txt";
		String prefix = "treatiseh";
		String annotationResults = "C:\\DATA\\annotationResults\\bhl-annotation.txt";
		String prefix = "bhl";*/
		String annotationResults = "C:\\DATA\\annotationResults\\fnav19-annotation.txt";
		String prefix = "fnav19";

		FieldCollector fc = new FieldCollector(annotationResults,dbname, prefix);
		fc.collectField("modifier");
	}

}
