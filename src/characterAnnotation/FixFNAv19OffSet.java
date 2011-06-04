/**
 * 
 */
package characterAnnotation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * @author Hong Updates
 * Change data in two tables: fnav19_filename2taxon and fnav19_sentence
 */
public class FixFNAv19OffSet {
	Connection conn;
	
	public FixFNAv19OffSet(){
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
	}
	
	/*
	 * in Sentence table:
	 * from sentid 2824 and on, deduct source file number by 1
	 * file 295.txt is non-existent and has the same taxon name as 294.txt in filename2taxon table
	 * sentid 2824 corresponds to 296.txt-0, which should be changed to 295.txt-0
	 * 
	 * in filename2taxon table
	 * remove filename 295.xml
	 * from filename 296.xml and on, deduct file number by 1
	 */
	public void fix(){
		try{
			//sentence table
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("select sentid, source from fnav19_sentence");
			while(rs.next()){
				String source = rs.getString("source");
				int sentid = rs.getInt("sentid");
				String fnumber = source.replaceFirst("\\.txt.*", "");
				String posfix = source.replaceFirst(fnumber, "");
				int fnum = Integer.parseInt(fnumber);
				if(fnum>295){
					source = (fnum-1) +posfix;
					Statement stmt1 = conn.createStatement();
					stmt1.execute("update fnav19_sentence set source='"+source+"' where sentid="+sentid);
				}
			}
			
			//filename2taxon table
			stmt = conn.createStatement();
			stmt.execute("delete from fnav19_filename2taxon where filename='295.xml'");
			rs = stmt.executeQuery("select filename from fnav19_filename2taxon order by filename+0 limit 294, 1000");
			while(rs.next()){
				String filename = rs.getString("filename");
				String fnumber = filename.replaceFirst("\\.xml.*", "");
				String posfix = filename.replaceFirst(fnumber, "");
				String newfilename = (Integer.parseInt(fnumber)-1)+posfix;
				Statement stmt1 = conn.createStatement();
				stmt1.execute("update fnav19_filename2taxon set filename='"+newfilename+"' where filename='"+filename+"'");
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		FixFNAv19OffSet fo = new FixFNAv19OffSet();
		fo.fix();

	}

}
