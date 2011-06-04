/**
 * 
 */
package characterAnnotation;

/**
 * @author Hong Updates
 *
 */
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.SliderUI;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 * This class is used to change the annotated files for every occurence of "_or_" and checking if there is space,underscore or dash
 * in the machine annotated file and changing them accordingly.
 */

/**
 * @author prasad
 *
 */
public class ReplaceCharacters {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		File ansdirectory = new File("C:\\RA\\Comparator\\machine_annotated_fnav19\\output");//
		String ansfilename[] = ansdirectory.list();

		//URL of folder containing the machine generated files
		File testdirectory = new File("C:\\my-prgms\\input");
		String testfilename[] = testdirectory.list();

		for (int i = 0; i < ansfilename.length; i++) {
			for (int j = 0; j < testfilename.length; j++) {
				if(ansfilename[i].compareTo(testfilename[j])==0){
					SAXBuilder builder = new SAXBuilder();
					//System.out.println(ansfilename[i]);
					Document anskey = builder.build("C:\\RA\\Comparator\\machine_annotated_fnav19\\output\\"+ansfilename[i]);
					Element ansroot = anskey.getRootElement();
					ansroot = ansroot.getChild("statement");
					Document testcase = builder.build("C:\\my-prgms\\input\\"+testfilename[j]);
					Element testroot = testcase.getRootElement();
					testroot = testroot.getChild("statement");
					replaceInStruct(ansroot,testroot,testcase,testfilename[j]);
				}
			}
		}


	}

	private static void replaceInStruct(Element ansroot, Element testroot, Document doc, String testfilename) throws Exception {

		List ansli = ansroot.getChildren("structure");
		List testli = testroot.getChildren("structure");
		int counter;
		if(ansli.size()<testli.size())
			counter = ansli.size();
		else
			counter = testli.size();

		for(int i=0;i<counter;i++)
		{
				Element test = (Element)testli.get(i); //structure

				for(int j=0;j<ansli.size();j++)
				{
					Element machine = (Element)ansli.get(j);//structure
					if(machine.getAttributeValue("name").contains(test.getAttributeValue("name")))
					{
						//structure name matches, so get the characters list
						List chartestli = test.getChildren("character");
						List charmachli = machine.getChildren("character");
						for(int k=0;k<chartestli.size();k++)
						{
							Element charhuman=(Element) chartestli.get(k);
							for(int p=0;p<charmachli.size();p++)
							{
								Element charmach=(Element) charmachli.get(p);
								//System.out.println("machine value is "+charmach.getAttributeValue("name")+":"+charmach.getAttributeValue("value"));
								//System.out.println("human vlaue is "+charhuman.getAttributeValue("name")+":"+charhuman.getAttributeValue("value"));

								if(charmach.getAttributeValue("name").equalsIgnoreCase(charhuman.getAttributeValue("name")))
								{
									//do nothing
									//continue;
								}

								/*else if(charmach.getAttributeValue("name").contains(charhuman.getAttributeValue("name"))&&charmach.getAttributeValue("name").contains("_or_")&&charhuman.getAttributeValue("value")==null||
										charmach.getAttributeValue("name").contains(charhuman.getAttributeValue("name"))&&charmach.getAttributeValue("name").contains("_or_")&&
										//charmach.getAttributeValue("value")!=null&&charhuman.getAttributeValue("value")!=null&&
										//charhuman.getAttributeValue("value")!=null&&
										charmach.getAttributeValue("value").contains(charhuman.getAttributeValue("value")))*/
								else if(charmach.getAttributeValue("name").contains(charhuman.getAttributeValue("name"))&&charmach.getAttributeValue("name").contains("_or_")&&
										( charhuman.getAttributeValue("char_type")==null)
										&&charmach.getAttributeValue("value").indexOf(charhuman.getAttributeValue("value"))!=-1)
								{
									/*
									 * charmach.getAttributeValue("range_value")==null&&charhuman.getAttributeValue("range_value")==null&&
									 */

									//need to change
									charhuman.removeAttribute("name");
									charhuman.setAttribute("name",charmach.getAttributeValue("name"));

									System.out.println(testfilename+"::changed::"+charhuman.getAttributeValue("name"));

									File f = new File(
											"C:\\my-prgms\\output\\"
													+ testfilename );
									BufferedOutputStream out = new BufferedOutputStream(
											new FileOutputStream(f));
									XMLOutputter xmlOutputter = new XMLOutputter();
								    xmlOutputter.output(doc, out);

									//ParsingUtil.outputXML(charhuman.getParentElement().getParentElement(), f);

								     }
								else
								{
									String machvalue = charmach.getAttributeValue("value");
									String humanvalue = charhuman.getAttributeValue("value");
									if(machvalue !=null && humanvalue!=null){
									String[] splittedMachVal =	machvalue.split("-");
									String[] splittedHumanVal = humanvalue.split("_");
									String[] machunderscore = machvalue.split("_");
									String[] humahyphen = humanvalue.split("-");
									String[] machspace = machvalue.split(" ");
									String[] humanspace = humanvalue.split(" ");

									List machinesplit = new ArrayList();
									List humansplit = new ArrayList();
									boolean present = false;

									for(int i1=0;i1<splittedMachVal.length;i1++)
									{
										machinesplit.add(splittedMachVal[i1]);
									}
									for(int i1=0;i1<machunderscore.length;i1++)
									{
										machinesplit.add(machunderscore[i1]);
									}
									for(int i1=0;i1<machspace.length;i1++)
									{
										machinesplit.add(machspace[i1]);
									}

									for(int i1=0;i1<splittedHumanVal.length;i1++)
									{
										humansplit.add(splittedHumanVal[i1]);
									}

									for(int i1=0;i1<humahyphen.length;i1++)
									{
										humansplit.add(humahyphen[i1]);
									}
									for(int i1=0;i1<humanspace.length;i1++)
									{
										humansplit.add(humanspace[i1]);
									}

									for(int i1=0;i1<humansplit.size();i1++)
									{
										for(int j1=0;j1<machinesplit.size();j1++)
										{
											if(((String) humansplit.get(i1)).equalsIgnoreCase((String) machinesplit.get(j1)))
												present =true;
										}
									}

									if((present)&&
											charmach.getAttributeValue("name").contains(charhuman.getAttributeValue("name"))&&charmach.getAttributeValue("name").contains("_or_")&&
											( charhuman.getAttributeValue("char_type")==null)	)
									{

										/*
										 * charmach.getAttributeValue("range_value")==null&&charhuman.getAttributeValue("range_value")==null&&
										 */

										//need to change
										charhuman.removeAttribute("name");
										charhuman.setAttribute("name",charmach.getAttributeValue("name"));

										System.out.println(testfilename+"  changed::"+charhuman.getAttributeValue("name"));

										File f = new File(
												"C:\\my-prgms\\output\\"
														+ testfilename );
										BufferedOutputStream out = new BufferedOutputStream(
												new FileOutputStream(f));
										XMLOutputter xmlOutputter = new XMLOutputter();
									    xmlOutputter.output(doc, out);

										//ParsingUtil.outputXML(charhuman.getParentElement().getParentElement(), f);



									}
									}
								}


								}
							}

						}


					}
				}





		}

	}


