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
		// 1336.txt-0' and '1409.txt.4,
		// 1412.txt-7 to 1484.txt-0 , 1491.txt-3 to 1569.txt-4, 1572.txt-1 to
		// 1642.txt-2, 1645.txt-8 to 1723.txt-1, 1724.txt-1 to 1834.txt-3,
		// 1835.txt-3 to 1913.txt-3, 1919.txt-2 to 2033.txt-1
		
		List sourceNames = new ArrayList();
		sourceNames = getSrcList(sourceNames);
		SAXBuilder builder = new SAXBuilder();
		Document testcase = builder
				.build("C:\\RA\\Comparator\\character-annotations-manual-treatise\\character-annotations-manual-treatise\\treatise-annotation-Edward\\1.xml");
		Element testroot = testcase.getRootElement();
		List testli = testroot.getChildren("statement");
		System.out.println("total statements are " + testli.size());
		for (int j = 0; j <= testli.size(); j++) {
			// since the list size is getting reduced, added the check
			// testli.size()-j<=2 to iterate the entire list.

			if (testli.size() - j <= 2)
				j = 0;
			System.out.println("size and j are :" + testli.size() + " " + j);
			Element statement = (Element) testli.get(j);
			statement.detach();

			File f = new File(
					"C:\\RA\\Comparator\\character-annotations-manual-treatise\\edward_with_description\\"
							+ sourceNames.get(srcCnt) + ".xml");
			srcCnt = srcCnt + 1;
			Element desc = new Element("description");
			desc.addContent(statement);
			ParsingUtil.outputXML(desc, f);

		}

	}

	public static List getSrcList(List sourceNames)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException, SQLException {
		String url = "jdbc:mysql://localhost:3306/";
		String dbName = "manual_annotation";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "termsuser";
		String password = "termspassword";
		Class.forName(driver).newInstance();
		Connection conn = DriverManager.getConnection(url + dbName, userName,
				password);

		// Get a statement from the connection
		Statement stmt = conn.createStatement();

		// Execute the query
		ResultSet rs = stmt
				.executeQuery("select source from manual_annotation_assigned.manual_character_annotation_treatise_edward where source between '1336.txt-0' and '1409.txt.4';");

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
