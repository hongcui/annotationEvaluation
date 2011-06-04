/**$Id

 * 
 */
package characterAnnotation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/**
 * @author Hong Updates
 * count # of sentences, words/sentence, # of words
 */
public class SentenceCount {
	Connection conn = null;
	String table = null;
	public SentenceCount(Connection conn, String table){
		this.conn = conn;
		this.table = table;
	}

	public void count(){
		HashSet<String> words = new HashSet<String>();
		int totalsent = 0;
		int totallength = 0;
		try{
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select sentence from "+this.table);
			while(rs.next()){
				String sent = rs.getString("sentence").replaceAll("[<{}>]", "");
				totalsent++;
				String [] tokens = sent.split("\\s+");
				totallength += tokens.length;
				for(int i = 0; i < tokens.length; i++){
					words.add(tokens[i]);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		System.out.println(this.table+" sentence: "+totalsent+" uniqueWords: "+words.size()+" averageSentLength: "+(float)totallength/totalsent);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Connection conn = null;
		String database="annotationevaluation";
		String username="root";
		String password="root";
		try{
			if(conn == null){
				Class.forName("com.mysql.jdbc.Driver");
				String URL = "jdbc:mysql://localhost/"+database+"?user="+username+"&password="+password;
				conn = DriverManager.getConnection(URL);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		SentenceCount sc= new SentenceCount(conn, "fnav19_sentence");
		sc.count();
		sc = new SentenceCount(conn, "treatiseh_sentence");
		sc.count();
		
	}

}
