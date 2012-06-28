package org.opennms.maven.plugins.karaf;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.XMLUnit;
import org.xml.sax.SAXException;

public class Validator {
	static {
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setNormalize(true);
	}

	public static void assertXmlEquals(final File xmlFileA, final File xmlFileB) throws SAXException, IOException {
		assertXmlEquals(slurp(xmlFileA), slurp(xmlFileB));
	}
	
	protected static void assertXmlEquals(final String xmlA, final String xmlB) throws SAXException, IOException {
    	System.err.println("--------");
    	System.err.println("1st XML:");
    	System.err.println("--------");
    	for (final String line : xmlA.trim().split("\n")) {
    		System.err.println("  " + line);
    	}
    	System.err.println("--------");
    	System.err.println("2nd XML:");
    	System.err.println("--------");
    	for (final String line : xmlB.trim().split("\n")) {
    		System.err.println("  " + line);
    	}

    	final List<Difference> differences = getDifferences(xmlA, xmlB);
    	assertEquals("number of XMLUnit differences between the example xml and the generated xml should be 0", 0, differences.size());
    }

    protected static List<Difference> getDifferences(final String xmlA, final String xmlB) throws SAXException, IOException {
    	final DetailedDiff myDiff = new DetailedDiff(XMLUnit.compareXML(xmlA, xmlB));
    	final List<Difference> retDifferences = new ArrayList<Difference>();
    	@SuppressWarnings("unchecked")
    	final List<Difference> allDifferences = myDiff.getAllDifferences();
    	if (allDifferences.size() > 0) {
    		for (final Difference d : allDifferences) {
    			if (d.getDescription().equals("namespace URI")) {
    				System.err.println("Ignoring namspace difference: " + d);
    			} else {
    				System.err.println("Found difference: " + d);
    				retDifferences.add(d);
    			}
    		}
    	}
    	return retDifferences;
    }

    private static String slurp(final File xmlFile) throws IOException {
    	FileReader reader = null;
    	BufferedReader br = null;
    	try {
    		reader = new FileReader(xmlFile);
    		br = new BufferedReader(reader);
    		final StringBuffer xmlContents = new StringBuffer();
    		String line = null;
    		while ((line = br.readLine()) != null) {
    			xmlContents.append(line).append("\n");
    		}
    		return xmlContents.toString();
    	} finally {
    		if (br != null) {
    			try {
    				br.close();
    			} catch (final Throwable t) {
    				// ignore close error
    			}
    		}
    		if (reader != null) {
	    		try {
	    			reader.close();
	    		} catch (final Exception e) {
	    			// ignore close error
	    		}
    		}
    	}
	}
}
