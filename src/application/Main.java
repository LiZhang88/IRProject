/**
 * 
 */
package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Li
 *
 */
public class Main {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		// TODO Auto-generated method stub
		File f=new File("src/ft911/ft911_1");
		FileReader fr=new FileReader(f);
		BufferedReader br=new BufferedReader(fr);
		String xml="<ROOT>";
		String line="";
		while((line = br.readLine()) != null) {
            xml+=line;
        }
		xml+="</ROOT>";
		System.out.println(xml);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		Element rootElement = document.getDocumentElement();
		
		
		System.out.println(rootElement.getTagName());
		String word = "gives";
		Porter stemmer = new Porter();
		String stem = stemmer.stripAffixes(word);
		System.out.println(stem);
	}

}
