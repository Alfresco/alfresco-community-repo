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
package org.alfresco.repo.cmis.rest;

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

import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Base CMIS Web Script Test
 * 
 * @author davidc
 */
public class CMISWebScriptTest extends BaseWebScriptTest
{
    /** XML Schema Validation */
    private static DocumentBuilder documentBuilder = null;
    private static Validator appValidator = null;
    private static Validator atomValidator = null;
    
    
    /**
     * Gets document parser
     * 
     * @return  document parser
     * @throws ParserConfigurationException
     */
    protected static DocumentBuilder getDocumentBuilder()
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
    protected static Validator getAppValidator()
        throws IOException, SAXException
    {
        if (appValidator == null)
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(new ClassPathResource("org/alfresco/repo/cmis/rest/xsd/APP.xsd").getFile());
            Schema schema = factory.newSchema(schemaFile);
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
    protected static Validator getCMISAtomValidator()
        throws IOException, SAXException
    {
        if (atomValidator == null)
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Source schemaFile = new StreamSource(new ClassPathResource("org/alfresco/repo/cmis/rest/xsd/ATOM4CMIS.xsd").getFile());
            Schema schema = factory.newSchema(schemaFile);
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
    public void assertValidXML(String xml, Validator validator)
        throws IOException, ParserConfigurationException
    {
        try
        {
            Document document = getDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            validator.validate(new DOMSource(document));
        }
        catch (SAXException e)
        {
            fail(e.toString() + "\n\n" + xml);
        }
    }    
    
    /**
    public static void main(String[] args) throws IOException, ParserConfigurationException
    {
        try
        {
            Document document = getDocumentBuilder().parse(new ClassPathResource("test.xml").getFile());
            getAppValidator().validate(new DOMSource(document));
        }
        catch (SAXException e)
        {
            fail(e.toString());
        }
        
    }
    */
    
}
