import java.io.File;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * This class can be used to split one xml file with multiple sentences into different xml files. The names for new xml files are taken 
 * from the sentence_id field.
 */

/**
 * @author prasad
 * 
 */
public class SplitFromOneFile {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		SAXBuilder builder = new SAXBuilder();
		Document testcase = builder
				.build("C:\\RA\\Comparator\\character-annotations-manual-treatise\\character-annotations-manual-treatise\\treatise-annotation-Alyssa.xml");
		Element testroot = testcase.getRootElement();
		List testli = testroot.getChildren("statement");

		for (int j = 0; j <= testli.size(); j++) {
			if (j == testli.size() / 2)
				j = 0;
			if (testli.size() == 1)
				j = 0;

			Element statement = (Element) testli.get(j);
			Attribute id = statement.getAttribute("id");
			statement.detach();

			String source = id.getValue();

			File f = new File(
					"C:\\RA\\Comparator\\character-annotations-manual-treatise\\combined\\"
							+ source + ".xml");

			Element desc = new Element("description");
			desc.addContent(statement);
			ParsingUtil.outputXML(desc, f);

		}

	}

}
