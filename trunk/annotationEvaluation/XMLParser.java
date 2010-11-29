import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLParser {

 public static void main(String argv[]) {

  try {
  File file = new File("C:\\RA\\Comparator\\xml files\\alyssa\\fnav19-Second Assignment-Alyssa.xml");
  DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
  DocumentBuilder db = dbf.newDocumentBuilder();
  Document doc = db.parse(file);
  doc.getDocumentElement().normalize();
  System.out.println("Root element " + doc.getDocumentElement().getNodeName());
  NodeList nodeLst = doc.getElementsByTagName("description");
  System.out.println("Statement tag found");

  for (int s = 0; s < nodeLst.getLength(); s++) {

    Node fstNode = nodeLst.item(s);
    
    if (fstNode.getNodeType() == Node.ELEMENT_NODE) {
  
           Element fstElmnt = (Element) fstNode;
      NodeList fstNmElmntLst = fstElmnt.getElementsByTagName("statement");
      fstNmElmntLst.getLength();
      fstNmElmntLst.item(0);
      fstNmElmntLst.item(1);
      Element fstNmElmnt = (Element) fstNmElmntLst.item(0);
       NodeList nl =fstNmElmnt.getChildNodes();
       
       ////////////////////////
       DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
       DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
       Document doc1 = docBuilder.newDocument();

       ////////////////////////
       //Creating the XML tree

       //create the root element and add it to the document
       
       Element root = doc1.createElement("description");
       
       doc1.appendChild(root);
       
       //////////////////////////
      for(int k=1;k<nl.getLength();k++)
      {/*
    	  System.out.println(nl.item(k).getNodeName());
    	  String tagNm = nl.item(k).getNodeName();
    	 // System.out.println(tagNm);
    	Element child = doc1.createElement(tagNm);
    	  //System.out.println(nl.item(k).getTextContent());
    	  if(nl.item(k).getAttributes()!=null){
    	  //System.out.println(nl.item(k).getAttributes().toString());
    	    for(int l=0;l<nl.item(k).getAttributes().getLength();l++)
    	    {
    	    	System.out.println(nl.item(k).getAttributes().item(l).getNodeName());
    	    	System.out.println(nl.item(k).getAttributes().item(l).getTextContent());
    	    	child.setAttribute(nl.item(k).getAttributes().item(l).getNodeName(),nl.item(k).getAttributes().item(l).getTextContent());
    	    }
    	    root.appendChild(child);
    	  }
      */}
      DocumentBuilderFactory documentBuilderFactory = 
          DocumentBuilderFactory.newInstance();
DocumentBuilder documentBuilder = 
     documentBuilderFactory.newDocumentBuilder();
Document document = documentBuilder.newDocument();
Element rootElement = document.createElement("description");
document.appendChild(rootElement);
for (int i = 1; i <= nl.getLength(); i++){
String element = nl.item(i).getNodeName();
String data = nl.item(i).getTextContent();
Element em = document.createElement(element);
em.appendChild(document.createTextNode(data));
rootElement.appendChild(em);
}
     
      TransformerFactory transfac = TransformerFactory.newInstance();
      Transformer trans = transfac.newTransformer();
      trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      trans.setOutputProperty(OutputKeys.INDENT, "yes");

      //create string from xml tree
      StringWriter sw = new StringWriter();
      StreamResult result = new StreamResult(sw);
      DOMSource source = new DOMSource(document);
      trans.transform(source, result);
      String xmlString = sw.toString();
      System.out.println("---------\n\n\n"+xmlString);
    }
    
  }
  } catch (Exception e) {
    e.printStackTrace();
  }
 }
}

