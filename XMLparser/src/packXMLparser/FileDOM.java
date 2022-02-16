package packXMLparser;

import java.io.File;
import java.io.IOException;
import java.util.stream.DoubleStream;
import java.text.DateFormatSymbols;
import java.util.Locale;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class FileDOM {
	private String filename;
	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document doc;
	
	// DOM parser: "In order to be well-formed, XML must have exactly one root element". Therefore, we have
	// added <root> element to all files located in the folder testData.
	
	// More info: https://stackoverflow.com/questions/46355454/how-to-fix-error-the-markup-in-the-document-following-the-root-element-must-be
	
	// Constructor
	public FileDOM(File file) {
		this.filename = file.getName();
		
		// Instantiate the Factory
		this.dbf = DocumentBuilderFactory.newInstance();

		try {
	        // Process XML securely (recommended)
			this.dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

	        // Parse XML file
	        this.db = dbf.newDocumentBuilder();
	        this.doc = db.parse(file);

	        // Normalization (Explained in Link below):
	        // http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	        this.doc.getDocumentElement().normalize();

		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	// -------------------------------
	//   FileDOM Class Methods
	// -------------------------------
	
	public String getFilename() {
		return this.filename;
	}
	
	public String outputHTML() {
		String output = "";
		double total = this.getBalance();
		double[] sums = this.getBalanceByMonth();

		DateFormatSymbols dfs = new DateFormatSymbols(Locale.US);
		String[] months = dfs.getShortMonths();
		
		double noAttr = total-DoubleStream.of(sums).sum();
				
		// Format output
		output += "<h2>Total Balance</h2>";
		output += "<b>Balance = </b>" + String.format("%.2f", total);
		
				
		output += "<h2>Total Balance by Month</h2>";
		output += "<ul>";
		for (int i=0; i<months.length-1; i++) {
			output += "<li>" + months[i] + " = " + String.format("%.2f",sums[i+1]) + "</li>";
		}
		output += "<li>Invalid Month: " + sums[0] + "</li>";
		output += "<li>No Attr: " + noAttr + "</li>";
		output += "</ul>";
		
		//testString = "<table class='styled-table'><thead><tr><th>Name</th><th>Points</th></tr></thead><tbody><tr><td>Dom</td><td>6000</td></tr><tr class='active-row'><td>Melissa</td><td>5150</td></tr><!-- and so on... --></tbody></table>";
		
		return output;
	}
	
	public double getBalance() {
		double total = 0;
    	
    	NodeList creditList = this.doc.getElementsByTagName("credit");
    	NodeList debitList = this.doc.getElementsByTagName("debit");
    	
    	for (int i=0; i<creditList.getLength(); i++) {
	    	Node node = creditList.item(i);
	        if (node.getNodeType() == Node.ELEMENT_NODE) {
	        	Element element = (Element) node;
	        	total += Double.parseDouble(element.getTextContent());
	        }
    	}
    	for (int i=0; i<debitList.getLength(); i++) {
	    	Node node = debitList.item(i);
	        if (node.getNodeType() == Node.ELEMENT_NODE) {
	        	Element element = (Element) node;
	        	total += Double.parseDouble(element.getTextContent());
	        }
    	}
	    return total;
	}
	
	private double[] getBalanceByMonth() {
		double[] sums = new double[13];
		
		// Get <month> NodeList
		NodeList monthList = this.doc.getElementsByTagName("month");
		
		// Loop through <month> NodeList
		for (int i=0; i<monthList.getLength(); i++) {
			Node node = monthList.item(i);
    		
	        if (node.getNodeType() == Node.ELEMENT_NODE) {
	        	Element element = (Element) node;
	        	
	        	int iMonth = Integer.parseInt(element.getAttribute("number"));
	        	
	        	NodeList creditList = element.getElementsByTagName("credit");
	        	NodeList debitList = element.getElementsByTagName("debit");
	        	
	        	for (int j=0; j<creditList.getLength(); j++) {
	        		if (iMonth <=12 && iMonth > 0) {
	        			sums[iMonth] += Double.parseDouble(creditList.item(j).getTextContent());
	        		} else {
	        			sums[0] += Double.parseDouble(creditList.item(j).getTextContent());
	        		}
	        	}
	        	
	        	for (int j=0; j<debitList.getLength(); j++) {
	        		if (iMonth <=12 && iMonth > 0) {
	        			sums[iMonth] += Double.parseDouble(debitList.item(j).getTextContent());
	        		} else {
	        			sums[0] += Double.parseDouble(debitList.item(j).getTextContent());
	        		}
	        	}
	        } 
		}
		
		return sums;
	}

}
