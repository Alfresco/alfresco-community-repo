package org.alfresco.util.schemacomp;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.util.schemacomp.model.Schema;
import org.xml.sax.SAXException;

import com.sun.star.uno.RuntimeException;

/**
 * Converts an in-memory Schema to an XML output stream.
 * 
 * @author Matt Ward
 */
public class SchemaToXML
{
    private TransformerHandler xmlOut;
    private Schema schema;
    
    public SchemaToXML(Schema schema, StreamResult streamResult)
    {
        final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
        try
        {
            xmlOut = stf.newTransformerHandler();
        }
        catch (TransformerConfigurationException error)
        {
            throw new RuntimeException("Unable to create TransformerHandler.", error);
        }
        final Transformer t = xmlOut.getTransformer();
        try
        {
            t.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
        }
        catch (final IllegalArgumentException e)
        {
            // It was worth a try
        }
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.setOutputProperty(OutputKeys.ENCODING, SchemaComparator.CHAR_SET);
        xmlOut.setResult(streamResult);
        
        this.schema = schema;
    }
    
    
    public void execute()
    {
        try
        {
            attemptTransformation();
        }
        catch (SAXException e)
        {
            throw new RuntimeException("Unable to complete transformation.", e);
        }
    }
    
    private void attemptTransformation() throws SAXException
    {
        xmlOut.startDocument();
        DbObjectXMLTransformer dboTransformer = new DbObjectXMLTransformer(xmlOut);
        dboTransformer.output(schema);
        xmlOut.endDocument();
    }
  
}
