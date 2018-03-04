/**
 * 
 */
package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
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
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException{
		// TODO Auto-generated method stub
		Map<String, Integer> DocDict = new TreeMap<String, Integer>();
		Map<String, Integer> WordDict = new TreeMap<String, Integer>();
		List<String> stopWord = new Vector<String>();
		File stop=new File("src/stopwordlist.txt");
		FileReader fr=new FileReader(stop);
		BufferedReader br=new BufferedReader(fr);
		String line="";
		while((line = br.readLine()) != null) {
			int n=stopWord.size();
            stopWord.add(line.trim());
        }
		br.close();
		for(int i=1;i<16;i++){
			String path="src/ft911/ft911_"+(i);
			proc_doc(DocDict,WordDict,path,stopWord);
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter("parser_output.txt", true));
		for (Entry<String, Integer> entry : DocDict.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    String s=key+" :"+value+"\n";
		    writer.append(s);
		}
		for (Entry<String, Integer> entry : WordDict.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    String s=key+" :"+value+"\n";
		    writer.append(s);
		}
	    writer.close();
		
	}
	
	public static void proc_doc(Map<String, Integer> DocDict,Map<String, Integer> WordDict, String fileName, List<String> stop) throws IOException, ParserConfigurationException, SAXException{
		//fileName="src/ft911/ft911_1";
		File f=new File(fileName);
		FileReader fr=new FileReader(f);
		BufferedReader br=new BufferedReader(fr);
		String xml="<ROOT>";
		String line="";
		while((line = br.readLine()) != null) {
            xml+=line;
        }
		xml+="</ROOT>";
		br.close();
		//System.out.println(xml);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		Element rootElement = document.getDocumentElement();
		NodeList Docs=rootElement.getChildNodes();
		int NumberofDocs=Docs.getLength();
		for(int i=0;i<NumberofDocs;i++){
			DocDict.put(Docs.item(i).getFirstChild().getTextContent(),(DocDict.size()+1));
			Node node=Docs.item(i).getFirstChild();
			while(node.getNodeName()!="TEXT"){
				node=node.getNextSibling();
			}
			String temp=node.getTextContent();
			temp=temp.toLowerCase();
			temp=temp.replaceAll("\\p{P}", " ");
			temp=temp.trim();
			String[] tokens=temp.split("\\s+");
			for(int j=0;j<tokens.length;j++){
				String token=tokens[j].trim();
				if(token.matches(".*\\d+.*")){ //skip numbers and which has numbers
					continue;
				}
				if(stop.contains(token)){ //skip stopword
					continue;
				}
				Porter stemmer = new Porter();
				token=stemmer.stripAffixes(token);
				if(WordDict.containsKey(token)){ //skip which already in the WordDict
					continue;
				}
				//if(WordDict.get("{")!=null)
				//	System.out.println("find{");
				WordDict.put(token, WordDict.size()+1);					
			}
		}
		System.out.println(fileName+"Done!");
	}

}
