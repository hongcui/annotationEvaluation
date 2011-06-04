package characterAnnotation;

import java.io.File;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

/**
 * Used for Alyssa's annotation
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
				.build("Z:\\DATA\\TESTDATA-character-anno\\character-annotations-manual-treatise\\treatise-annotation-Alyssa.xml");
		Element testroot = testcase.getRootElement();
		List testli = testroot.getChildren("statement");

		for (int j = 0; j < testli.size(); j++) {
			Element statement = (Element) testli.get(j);
			Element statecopy = (Element)statement.clone();
			Attribute id = statecopy.getAttribute("id");
			statecopy.detach();

			String source = id.getValue();

			File f = new File(
					"Z:\\DATA\\TESTDATA-character-anno\\character-annotations-manual-treatise\\normalized-alyssa\\"
							+ source + ".xml");

			Element desc = new Element("description");
			desc.addContent(statecopy);
			ParsingUtil.outputXML(desc, f);

		}

	}

}
