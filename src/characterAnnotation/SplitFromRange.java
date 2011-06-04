package characterAnnotation;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Used for Edward's annotation.

 * This class is used to split the xml file when a range of sentences are given. The name of the file to be stored is taken from the database.

 *
 * after running this program, need to rename some files:
 * 1338.txt-3 <-> 1338.txt-6
 * 1381/9 ->1381/5
 * 1381/5 -> 1381/6
 * 1381/6 ->1381/9
 * 1480/1 <-> 1480/3
 * 1575/3 <-> 1575/0
 * 1607/5 <->1607/2
 * 1719/5 <-> 1719/2
 * 1840/5 <-> 1840/4
 * 1940/1 <-> 1940/0
 */

/**
 * @author prasad
 * 
 */
public class SplitFromRange {

	/**
	 * @param args
	 * @throws Exception
	 */
	static int srcCnt = 0;

	public static void main(String[] args) throws Exception {
		// put files to one file: all
		// 1336.txt-0' and '1409.txt.4,
		// 1412.txt-7 to 1484.txt-0 , 1491.txt-3 to 1569.txt-4, 1572.txt-1 to
		// 1642.txt-2, 1645.txt-8 to 1723.txt-1, 1724.txt-1 to 1834.txt-3,
		// 1835.txt-3 to 1913.txt-3, 1919.txt-2 to 2033.txt-1
		
		List sourceNames = new ArrayList();
		sourceNames = getSrcList(sourceNames);
		SAXBuilder builder = new SAXBuilder();
		Document testcase = builder
				.build("Z:\\DATA\\TESTDATA-character-anno\\character-annotations-manual-treatise\\treatise-annotation-Edward\\all.xml");
		Element testroot = testcase.getRootElement();
		List testli = testroot.getChildren("statement");
		System.out.println("total statements are " + testli.size());
		for (int j = 0; j < testli.size(); j++) {
			// since the list size is getting reduced, added the check
			// testli.size()-j<=2 to iterate the entire list.
			System.out.println("j:"+j+" "+sourceNames.get(srcCnt));
			Element statement = (Element) testli.get(j);
			Element statecopy = (Element)statement.clone();
			statecopy.detach();
			File f = new File(
					"Z:\\DATA\\TESTDATA-character-anno\\character-annotations-manual-treatise\\normalized-edward\\"
							+ sourceNames.get(srcCnt) + ".xml");
			srcCnt = srcCnt + 1;
			Element desc = new Element("description");
			desc.addContent(statecopy);
			ParsingUtil.outputXML(desc, f);
		}
	}

	public static List getSrcList(List sourceNames)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "manual_annotation_assigned";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "root";
		String password = "root";
		Class.forName(driver).newInstance();
		Connection conn = DriverManager.getConnection(url + dbName, userName,
				password);

		// Get a statement from the connection
		Statement stmt = conn.createStatement();

		// Execute the query
		ResultSet rs = stmt
				.executeQuery("select source from manual_annotation_assigned.manual_character_annotation_treatise_edward");

		// Loop through the result set
		while (rs.next()) {
			System.out.println(rs.getString(1));
			sourceNames.add(rs.getString(1));

		}
		rs.close();
		stmt.close();
		conn.close();
		return sourceNames;
	}

}
