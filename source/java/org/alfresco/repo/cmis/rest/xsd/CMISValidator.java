/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.rest.xsd;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * CMIS Validator
 * 
 * Support for validating CMIS requests/responses against CMIS XSDs
 * 
 * @author davidc
 */
public class CMISValidator
{
    /** XML Schema Validation */
    private DocumentBuilder documentBuilder = null;
    private Validator appValidator = null;
    private Validator atomValidator = null;
    
    
    /**
     * Gets document parser
     * 
     * @return  document parser
     * @throws ParserConfigurationException
     */
    public DocumentBuilder getDocumentBuilder()
        throws ParserConfigurationException
    {
        if (documentBuilder == null)
        {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            documentBuilder = builderFactory.newDocumentBuilder();
        }
        return documentBuilder;
    }
    
    /**
     * Gets CMIS Atom Publishing Protocol XML Validator
     * 
     * @return  APP Validator
     * @throws IOException
     * @throws SAXException
     */
    public Validator getAppValidator()
        throws IOException, SAXException
    {
        if (appValidator == null)
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source APPFile = new StreamSource(getClass().getResourceAsStream("APP.xsd"), getClass().getResource("APP.xsd").toExternalForm());
            Schema schema = factory.newSchema(APPFile);
            appValidator = schema.newValidator();
        }
        return appValidator;
    }
    
    /**
     * Gets CMIS Atom Validator
     * 
     * @return CMIS Atom Validator
     * @throws IOException
     * @throws SAXException
     */
    public Validator getCMISAtomValidator()
        throws IOException, SAXException
    {
        if (atomValidator == null)
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source ATOM4CMISFile = new StreamSource(getClass().getResourceAsStream("ATOM4CMIS.xsd"), getClass().getResource("ATOM4CMIS.xsd").toExternalForm());
            Schema schema = factory.newSchema(ATOM4CMISFile);
            atomValidator = schema.newValidator();
        }
        
        return atomValidator;
    }

    /**
     * Asserts XML complies with specified Validator
     * 
     * @param xml  xml to assert
     * @param validator  validator to assert with
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public void validateXML(String xml, Validator validator)
        throws IOException, ParserConfigurationException, SAXException
    {
        Document document = getDocumentBuilder().parse(new InputSource(new StringReader(xml)));
        validator.validate(new DOMSource(document));
    }
 
    /**
     * Convert SAX Exception to String
     * 
     * @param e  SAX Exception
     * @param xml  related XML (if any)
     * @return  description of SAX Exception
     */
    public String toString(SAXException e, String xml)
    {
        StringBuffer fail = new StringBuffer(e.toString());
        if (e instanceof SAXParseException)
        {
            fail.append("\n");
            fail.append("line: ").append(((SAXParseException)e).getLineNumber()).append("\n");
            fail.append("col: ").append(((SAXParseException)e).getColumnNumber()).append("\n");
        }
        if (xml != null)
        {
            fail.append("\n");
            fail.append(xml);
        }
        return fail.toString();
    }
    
}
