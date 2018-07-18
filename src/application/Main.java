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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	public static int NumberofDoc=1;
	public static int NumberofWord=1;
	//public static int NumberofToken=0;
	//public static int NumberofNum=0;
	//public static int NumberofSame=0;
	//public static int NumberofEmpty=0;
	//public static int NumberofStop=0;
	public static List<Forward_Index_Record> fi=new LinkedList<Forward_Index_Record>();
	public static Map<Integer,Inverted_Index_Record> ii=new TreeMap<Integer,Inverted_Index_Record>();
	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException{
		// TODO Auto-generated method stub
		long startTime = System.nanoTime();
		Map<String, Integer> DocDict = new TreeMap<String, Integer>();
		Map<String, Integer> WordDict = new TreeMap<String, Integer>();
		List<String> stopWord = new Vector<String>();
		
		File stop=new File("src/stopwordlist.txt");
		FileReader fr=new FileReader(stop);
		BufferedReader br=new BufferedReader(fr);
		String line="";
		while((line = br.readLine()) != null) {
			//int n=stopWord.size();
            stopWord.add(line.trim());
        }
		br.close();
		fr.close();
		String topic_path="src/topics.txt";
		String[][]querys= PreProcQuery(topic_path);
		System.out.println("Building Dictionary.....");
		for(int i=1;i<16;i++){
			String path="src/ft911/ft911_"+(i);
			proc_doc(DocDict,WordDict,path,stopWord);
		}
		System.out.println("Building Dictionary Successed");
		System.out.println("Writing the Parser Output File....");
		BufferedWriter writer = new BufferedWriter(new FileWriter("parser_output.txt", true));		
		for (Entry<String, Integer> entry : WordDict.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    String s=key+" :"+value+"\n";
		    writer.append(s);
		}
		for (Entry<String, Integer> entry : DocDict.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue();
		    String s=key+" :"+value+"\n";
		    writer.append(s);
		}
	    writer.close();
	    System.out.println("Writing the Parser Output File Successed");
	    System.out.println("Building Index.....");
	    for(int i=1;i<16;i++){
	    	String path="src/ft911/ft911_"+(i);
	    	forward_index(DocDict,WordDict,path);
	    }
	    System.out.println("Building Index Successed");
	    System.out.println("Writing the Forward Index File ....");
	    BufferedWriter writer2 = new BufferedWriter(new FileWriter("forward_index.txt", true));
	    writer2.append("Document:\tWord\t\n");
	    for(int i=0;i<fi.size();i++){
	    	writer2.append(fi.get(i).get_DocID()+"\t");
	    	Map<Integer,Integer> records=fi.get(i).get_Records();
	    	for(Entry<Integer,Integer> entry: records.entrySet()){
	    		int key=entry.getKey();
	    		int value=entry.getValue();
	    		String s=key+" "+value+"; ";
	    		writer2.append(s);
	    	}
	    	writer2.append("\n");
	    }
	    writer2.close();
	    System.out.println("Writing the Forward Index File Successed");
	    BufferedWriter writer3 = new BufferedWriter(new FileWriter("inverted_index.txt", true));
	    writer3.append("WordID:\tDocID\t\n");
	    System.out.println("Writing the Inverted Index File ....");
	    for(Entry<Integer, Inverted_Index_Record> entry: ii.entrySet()){
	    		int key=entry.getKey();
	    		Inverted_Index_Record value=entry.getValue();
	    		String s=key+"\t";
	    		writer3.append(s);
	    		//writer3.append(value.get_Word()+"\t");
	    		Map<Integer,Integer> record=value.get_Records();
	    		for(Entry<Integer,Integer> e:record.entrySet()){
	    			int k=e.getKey();
	    			int v=e.getValue();
	    			String out=k+":"+v+", ";
	    			writer3.append(out);
	    		}
	    		writer3.append("\n");
	    }
		writer3.close();
		System.out.println("Writing the Inverted Index File Successed");
		long endTime = System.nanoTime();
		long duration = (endTime - startTime)/1000000000;
		System.out.println("Takes "+duration+" secs!");
		
		////////////////////Project 3 Start Here//////////////////////////////
		
		//String long_query="British Chunnel impact";
		//String topic_path="src/topics.txt";
		List<String[]> all_judge = new Vector<String[]>();
		File judge=new File("src/main.qrels");
		fr=new FileReader(judge);
		br=new BufferedReader(fr);
		line="";
		while((line = br.readLine()) != null) {
			String[] judges=line.split(" ");
			all_judge.add(judges);
        }
		br.close();
		fr.close();
		//String[][]querys= PreProcQuery(topic_path);	
		writer = new BufferedWriter(new FileWriter("vsm_output_title.txt", true));
		int tp=0,fn=0,fp=0;
		for(int i=0;i<querys.length;i++){
			String s=querys[i][0]+"\t";
			int count=1;
			Map<String,Double> finalScores=My_Query(querys[i][1],WordDict,DocDict,stopWord);
	    	for(Entry<String,Double> entry: finalScores.entrySet()){
	    		String key=entry.getKey();
	    		Double value=entry.getValue();
	    		writer.append(s);
	    		writer.append(key+"\t"+Integer.toString(count)+"\t"+Double.toString(value)+"\t\n");
	    		int result=getPerformence(querys[i][0],key,all_judge);
	    		if(count<20){
	    			if(result==1)
	    				tp++;
	    			else if(result==0)
	    				fp++;
	    		}			
	    		else
	    		{
	    			if(result==0)
	    				fn++;
	    		}
	    		count++;
	    	}
	    	
			
			
		}
		writer.close();
		double precision=(double)tp/(tp+fp);
    	double recall=(double)tp/(tp+fn);
    	System.out.println("title_precision: "+Double.toString(precision));
    	System.out.println("title_recall: "+Double.toString(recall));
		writer = new BufferedWriter(new FileWriter("vsm_output_title_desc.txt", true));
		tp=0;fn=0;fp=0;
		for(int i=0;i<querys.length;i++){
			String s=querys[i][0]+"\t";
			int count=1;
			Map<String,Double> finalScores=My_Query(querys[i][2],WordDict,DocDict,stopWord);
	    	for(Entry<String,Double> entry: finalScores.entrySet()){
	    		String key=entry.getKey();
	    		Double value=entry.getValue();
	    		writer.append(s);
	    		writer.append(key+"\t"+Integer.toString(count)+"\t"+Double.toString(value)+"\t\n");
	    		int result=getPerformence(querys[i][0],key,all_judge);
	    		if(count<20){
	    			if(result==1)
	    				tp++;
	    			else if(result==0)
	    				fp++;
	    		}			
	    		else
	    		{
	    			if(result==0)
	    				fn++;
	    		}
	    		count++;
	    	}
			
			
		}
		writer.close();
		precision=(double)tp/(tp+fp);
    	recall=(double)tp/(tp+fn);
    	System.out.println("title_desc_precision: "+Double.toString(precision));
    	System.out.println("title_desc_recall: "+Double.toString(recall));
		writer = new BufferedWriter(new FileWriter("vsm_output_title_narr.txt", true));
		tp=0;fn=0;fp=0;
		for(int i=0;i<querys.length;i++){
			String s=querys[i][0]+"\t";
			int count=1;
			Map<String,Double> finalScores=My_Query(querys[i][3],WordDict,DocDict,stopWord);
	    	for(Entry<String,Double> entry: finalScores.entrySet()){
	    		String key=entry.getKey();
	    		Double value=entry.getValue();
	    		writer.append(s);
	    		writer.append(key+"\t"+Integer.toString(count)+"\t"+Double.toString(value)+"\t\n");
	    		int result=getPerformence(querys[i][0],key,all_judge);
	    		if(count<20){
	    			if(result==1)
	    				tp++;
	    			else if(result==0)
	    				fp++;
	    		}			
	    		else
	    		{
	    			if(result==0)
	    				fn++;
	    		}
	    		count++;
	    	}
						
		}
		writer.close();
		precision=(double)tp/(tp+fp);
    	recall=(double)tp/(tp+fn);
    	System.out.println("title_narr_precision: "+Double.toString(precision));
    	System.out.println("title_narr_recall: "+Double.toString(recall));
		    
	   		
	}
	private static int getPerformence(String string, String key, List<String[]> all_judge) {
		for(int i=0;i<all_judge.size();i++){
			if((all_judge.get(i)[0].trim().equals(string))&&(all_judge.get(i)[2].trim().equals(key))){
					if(all_judge.get(i)[3].trim().equals("1")){
						return 1;
					}
					else
						return 0;
			}
		}
		return -1;
		
	}
	public static String[][] PreProcQuery(String topic_path) throws IOException, ParserConfigurationException, SAXException {
		File f=new File(topic_path);
		FileReader fr=new FileReader(f);
		BufferedReader br=new BufferedReader(fr);
		boolean Title = false;
		boolean Num = false;
		boolean Desc = false;
		boolean Narr = false;
		String xml="<ROOT>";
		int r;
		while((r=br.read())!=-1){
			char ch = (char) r;
			if(ch=='<'){
				xml=xml.trim();
				if(Title){
					xml+="</title>";
				}
				else if(Num){
					xml+="</num>";
				}
				else if(Desc){
					xml+="</desc>";
				}
				else if(Narr){
					xml+="</narr>";
				}
				xml+=ch;
				Title = false;
				Num = false;
				Desc = false;
				Narr = false;
				int n;
				n=br.read();
				
				char next= (char) n;
				xml+=next;
				switch(next){
				case 't':
				{
					char t_next=(char)br.read();
					xml+=t_next;
					if(t_next=='i'){
						Title = true;
					}
					break;
				}
				case 'n':{
					char t_next=(char)br.read();
					xml+=t_next;
					if(t_next=='u'){
						Num = true;
					}
					else if(t_next=='a'){
						Narr = true;
					}
					break;
				}
				case 'd':{
					Desc = true;
					break;
				}
				default:
					break;
				}
			}
			else
				xml+=ch;
			
		}
		xml=xml.trim();
		xml+="</ROOT>";
		br.close();
		fr.close();
		//System.out.println(xml);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		Element rootElement = document.getDocumentElement();
		NodeList Topics=rootElement.getChildNodes();
		int NumberofTopics=Topics.getLength();
		String[][] query= new String[NumberofTopics][4];
		for(int i=0;i<NumberofTopics;i++){
			Node node=Topics.item(i).getFirstChild();
			while(node.getNodeName()!="num"){
				node=node.getNextSibling();	
			}
			query[i][0]=node.getTextContent();
			String temp=query[i][0].substring(8, query[i][0].length());
			query[i][0]=temp.trim();
			while(node.getNodeName()!="title"){
				node=node.getNextSibling();				
			}
			query[i][1]=node.getTextContent();
			while(node.getNodeName()!="desc"){
				node=node.getNextSibling();
			
			}
			temp=node.getTextContent().substring(13, node.getTextContent().length());
			query[i][2]=query[i][1]+" "+temp;
			
			
			while(node.getNodeName()!="narr"){
				node=node.getNextSibling();
				
			}
			temp=node.getTextContent().substring(11, node.getTextContent().length());
			query[i][3]=query[i][1]+" "+temp;
			
			
		}
		return query;
		
	}
	
	
	public static Map<String,Double> My_Query(String query,Map<String, Integer>WordDict,Map<String, Integer>DocDict, List<String>stop) {
		query=query.trim();
		
		ArrayList<String> token_list= new ArrayList<String>();
		Porter stemmer = new Porter();
		String[] tokens=query.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim().split("\\s*[^a-z]\\s*");
		for(int i=0;i<tokens.length;i++){
			if(tokens[i].equals("")){ //skip whitespace
				continue;
			}
			if(stop.contains(tokens[i])){ //skip stopword
				//NumberofStop++;
				continue;
			}
			token_list.add(stemmer.stripAffixes(tokens[i]));
		}		
		Set<String> uniqueSet = new HashSet<String>(token_list);		
		Map<String,Double> query_tf=new TreeMap<String,Double>();
		for (String temp : uniqueSet) {
			query_tf.put(temp, (double) Collections.frequency(token_list, temp));			
		}
		double[] idfs=new double[query_tf.size()];
		double[] query_idf=new double[query_tf.size()];
		int tmp_k=0;
		for(Entry<String,Double> e:query_tf.entrySet()){
			
			String query_word=e.getKey();
			if(WordDict.containsKey(query_word)){ //find the wordID
				//System.out.print("Query:"+query+"\t WordID:"+WordID+"\t");
				double idf=get_idf_value(query_word,WordDict);
				idfs[tmp_k]=idf;
				e.setValue(e.getValue()*idf);
				
			}else{
				idfs[tmp_k]=(double)0;
				e.setValue((double) 0);				
			}
			query_idf[tmp_k]=e.getValue();
			tmp_k++;
		}

		
		
		double[][] Martix= new double[NumberofDoc-1][query_tf.size()];
		for(int i=0;i<NumberofDoc-1;i++){
			int k=0;
			for(Entry<String,Double> e:query_tf.entrySet()){
				String query_word=e.getKey();
				int tf=get_tf_value(query_word, WordDict, i);
				Martix[i][k]=idfs[k]*tf;
				k++;
			}
		}
		
		Map<Integer, String> LookupDoc = new TreeMap<Integer, String>();
		Map<String, Double> DocScore = new HashMap<String, Double>();
		for(Entry<String, Integer> e:DocDict.entrySet()){
			LookupDoc.put(e.getValue(), e.getKey());
		}
		for(int i=0;i<NumberofDoc-1;i++){
			String DocNo=LookupDoc.get(i+1);
			double score=cosineSimilarity(query_idf,Martix[i]);
			if(score!=0)
				DocScore.put(DocNo, score);
		}
		List<Map.Entry<String,Double>> entryList = new ArrayList<Map.Entry<String,Double>>(DocScore.entrySet());
        
        Comparator< Map.Entry<String,Double>> sortByValue = (e1,e2)->{
        	if(e1.getValue()>e2.getValue())
        		return -1;
        	else if(e1.getValue()<e2.getValue())
        		return 1;
        	else
        		return 0;
        	};
        Collections.sort(entryList, sortByValue );
        
        Map<String,Double> SortedScore = new LinkedHashMap<>();
        for(Map.Entry<String,Double> e : entryList)
        	SortedScore.put(e.getKey(),e.getValue());
		return SortedScore;
	}
	

	public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
	    double dotProduct = 0.0;
	    double normA = 0.0;
	    double normB = 0.0;
	    boolean flag = false;
	    for (int i = 0; i < vectorA.length; i++) {
	    	if(vectorB[i]!=0){
	    		flag = true;
	    	}
	        dotProduct += vectorA[i] * vectorB[i];
	        normA += Math.pow(vectorA[i], 2);
	        normB += Math.pow(vectorB[i], 2);
	    }
	    if(flag)
	    	return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	    else
	    	return 0;
	}
	
	public static int get_tf_value(String query,Map<String, Integer>WordDict, int DocID){
		int tf=0;
		Forward_Index_Record fir=fi.get(DocID);
		if(WordDict.containsKey(query)){
			int WordID=WordDict.get(query);
			if (fir.Forward_Record.containsKey(WordID)){
				tf=fir.Forward_Record.get(WordID);
			}
		}
		else{
			tf=0;
		}
		return tf;
	}
	
	public static double get_idf_value(String query,Map<String, Integer>WordDict){
		double idf=0;
		int total_number=NumberofDoc-1;
		int WordID=WordDict.get(query);
		Inverted_Index_Record iir=ii.get(WordID);
		int doc_has_word=iir.get_Records().size();
		//idf=log10(N/df)
		idf=Math.log10((float)total_number/doc_has_word);
		return idf;
	}
	
	public static void forward_index(Map<String,Integer>DocDict,Map<String, Integer>WordDict,String path) throws SAXException, IOException, ParserConfigurationException{
		File f=new File(path);
		FileReader fr=new FileReader(f);
		BufferedReader br=new BufferedReader(fr);
		String xml="<ROOT>";
		String line="";
		while((line = br.readLine()) != null) {
			if(line.equals(""))
				continue;
            if(line.charAt(0)!='<')
            	xml+=" ";
            xml+=line;
        }
		xml+="</ROOT>";
		br.close();
		fr.close();
		//System.out.println(xml);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		Element rootElement = document.getDocumentElement();
		NodeList Docs=rootElement.getChildNodes();
		int NumberofDocs=Docs.getLength();
		for(int i=0;i<NumberofDocs;i++){
			int DocID=DocDict.get(Docs.item(i).getFirstChild().getTextContent());
			Forward_Index_Record fir=new Forward_Index_Record(Integer.toString(DocID));// new record
			Node node=Docs.item(i).getFirstChild();
			while(node.getNodeName()!="TEXT"){
				node=node.getNextSibling();
			}
			String temp=node.getTextContent();
			String[] tokens=temp.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim().split("\\s*[^a-z]\\s*");
			for(int j=0;j<tokens.length;j++){
				String token=tokens[j];
				token=token.trim();
				if(token.equals("")){ //skip whitespace
					continue;
				}
				Porter stemmer = new Porter();
				token=stemmer.stripAffixes(token);
				if(WordDict.containsKey(token)){ //find the wordID
					int WordID=WordDict.get(token);
					fir.add_word(WordID);
					int keyword=WordID;
					if(ii.containsKey(keyword)){
						Inverted_Index_Record iir=ii.get(keyword);
						iir.add_Doc(DocID);
						ii.replace(keyword, iir); 
					}
					else{
						Inverted_Index_Record iir=new Inverted_Index_Record(Integer.toString(WordID),token);
						iir.add_Doc(DocID);
						ii.put(keyword, iir);
					}
				}
			}
			fi.add(fir);
		}
		System.out.println(path+"Retrival Done!");
			
	}
	
	
	
	
	public static void proc_doc(Map<String, Integer> DocDict,Map<String, Integer> WordDict, String fileName, List<String> stop) throws IOException, ParserConfigurationException, SAXException{
		//fileName="src/ft911/ft911_1";
		File f=new File(fileName);
		FileReader fr=new FileReader(f);
		BufferedReader br=new BufferedReader(fr);
		String xml="<ROOT>";
		String line="";
		while((line = br.readLine()) != null) {
			if(line.equals(""))
				continue;
            if(line.charAt(0)!='<')
            	xml+=" ";
            xml+=line;
        }
		xml+="</ROOT>";
		br.close();
		fr.close();
		//System.out.println(xml);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(xml)));
		Element rootElement = document.getDocumentElement();
		NodeList Docs=rootElement.getChildNodes();
		int NumberofDocs=Docs.getLength();
		for(int i=0;i<NumberofDocs;i++){
			DocDict.put(Docs.item(i).getFirstChild().getTextContent(),NumberofDoc);
			NumberofDoc++;
			Node node=Docs.item(i).getFirstChild();
			while(node.getNodeName()!="TEXT"){
				node=node.getNextSibling();
			}
			String temp=node.getTextContent();
			String[] tokens=temp.toLowerCase().replaceAll("\\w*\\d\\w*", "").trim().split("\\s*[^a-z]\\s*");
			for(int j=0;j<tokens.length;j++){
				//NumberofToken++;
				String token=tokens[j];
				token=token.trim();
				if(token.equals("")){ //skip stopword
					//NumberofEmpty++;
					continue;
				}
				if(stop.contains(token)){ //skip stopword
					//NumberofStop++;
					continue;
				}
				Porter stemmer = new Porter();
				token=stemmer.stripAffixes(token);
				if(WordDict.containsKey(token)){ //skip which already in the WordDict
					//NumberofSame++;
					continue;
				}
				//if(WordDict.get("{")!=null)
				//	System.out.println("find{");
				WordDict.put(token, NumberofWord);	
				NumberofWord++;
			}
		}
		System.out.println(fileName+" Done!");
	}

}

class Forward_Index_Record{
	String DocID;
	Map<Integer, Integer> Forward_Record;
	public Forward_Index_Record(String docid){
		DocID=docid;
		Forward_Record=new TreeMap<Integer,Integer>();
	}
	public void add_word(Integer wordID){
		if(Forward_Record.containsKey(wordID)){
			int n=Forward_Record.get(wordID);
			n++;
			Forward_Record.replace(wordID,n);
		}
		else{
			Forward_Record.put(wordID, 1);
		}
	}
	public Map<Integer,Integer> get_Records(){
		return Forward_Record;
	}
	public String get_DocID(){
		return DocID;
	}
}


class Inverted_Index_Record{
	String Word;
	String WordID;
	Map<Integer, Integer> Inverted_Record;
	public Inverted_Index_Record(String wordid,String word){
		WordID=wordid;
		Word=word;
		Inverted_Record=new TreeMap<Integer,Integer>();
	}
	public void add_Doc(Integer DocID){
		if(Inverted_Record.containsKey(DocID)){
			int n=Inverted_Record.get(DocID);
			n++;
			Inverted_Record.replace(DocID,n);
		}
		else{
			Inverted_Record.put(DocID, 1);
		}
	}
	public Map<Integer,Integer> get_Records(){
		return Inverted_Record;
	}
	public String get_WordID(){
		return WordID;
	}
	public String get_Word(){
		return Word;
	}
}
