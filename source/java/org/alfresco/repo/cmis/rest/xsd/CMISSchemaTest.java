/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * CMIS XSD Tests
 * 
 * @author davidc
 */
public class CMISSchemaTest extends TestCase
{
    private CMISValidator cmisValidator = new CMISValidator();


    /**
     * Gets XML from file specified by class path
     * 
     * @param classPath  XML file
     * @return  XML
     * @throws IOException
     */
    private String getXML(String classPath)
        throws IOException
    {
        InputStream input = getClass().getResourceAsStream(classPath);
        if (input == null)
        {
            throw new IOException(classPath + " not found.");
        }

        InputStreamReader reader = new InputStreamReader(input);
        StringWriter writer = new StringWriter();

        try
        {
            char[] buffer = new char[4096];
            int bytesRead = -1;
            while ((bytesRead = reader.read(buffer)) != -1)
            {
                writer.write(buffer, 0, bytesRead);
            }
            writer.flush();
        }
        finally
        {
            reader.close();
            writer.close();
        }
        
        return writer.toString();
    }

    /**
     * Assert XML is valid according to specified validator
     *  
     * @param xml  document to test
     * @param validator  validator to test with
     * @throws IOException
     * @throws ParserConfigurationException 
     */
    private void assertValidXML(String xml, Validator validator)
        throws IOException, ParserConfigurationException
    {
        try
        {
            Document document = cmisValidator.getDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            validator.validate(new DOMSource(document));
        }
        catch (SAXException e)
        {
            fail(cmisValidator.toString(e, xml));
        }
    }

    
//    public void testRelaxNG()
//        throws Exception
//    {
//        String xml = getXML("address.xml");
//        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI);
//        Source schemaFile = new StreamSource(getClass().getResourceAsStream("address.rng"), getClass().getResource("address.rng").toExternalForm());
//        Schema schema = factory.newSchema(schemaFile);
//        assertValidXML(xml, schema.newValidator());
//    }
    
    public void testAllowableActions()
        throws Exception
    {
        String xml = getXML("examples/AllowableActions.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testChangeLog()
        throws Exception
    {
        String xml = getXML("examples/ChangeLog.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testDocumentEntry()
        throws Exception
    {
        String xml = getXML("examples/DocumentEntry.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testDocumentEntryPWC()
        throws Exception
    {
        String xml = getXML("examples/DocumentEntryPWC.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testDocumentEntryWithChanges()
        throws Exception
    {
        String xml = getXML("examples/DocumentEntryWithChanges.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }
    
    public void testFolderChildren()
        throws Exception
    {
        String xml = getXML("examples/FolderChildren.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testFolderDescendants()
        throws Exception
    {
        String xml = getXML("examples/FolderDescendants.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testFolderEntry()
        throws Exception
    {
        String xml = getXML("examples/FolderEntry.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testPolicyEntry()
        throws Exception
    {
        String xml = getXML("examples/PolicyEntry.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testQuery()
        throws Exception
    {
        String xml = getXML("examples/Query.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testRelationshipEntry()
        throws Exception
    {
        String xml = getXML("examples/RelationshipEntry.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testService()
        throws Exception
    {
        String xml = getXML("examples/Service.xml");
        assertValidXML(xml, cmisValidator.getAppValidator());
    }

    public void testTypeDocumentWith()
        throws Exception
    {
        String xml = getXML("examples/TypeDocumentWith.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testTypeDocumentWithout()
        throws Exception
    {
        String xml = getXML("examples/TypeDocumentWithout.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testTypeFolderWith()
        throws Exception
    {
        String xml = getXML("examples/TypeFolderWith.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testTypeFolderWithOut()
        throws Exception
    {
        String xml = getXML("examples/TypeFolderWithOut.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testTypeRelationshipWith()
        throws Exception
    {
        String xml = getXML("examples/TypeRelationshipWith.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testTypeRelationshipWithOut()
        throws Exception
    {
        String xml = getXML("examples/TypeRelationshipWithout.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }
    
    public void testFolderChildrenAlfresco()
        throws Exception
    {
        String xml = getXML("examples/example_folderchildren_alfresco.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

    public void testAtomEntry()
        throws Exception
    {
        String xml = getXML("examples/example_atomentry.xml");
        assertValidXML(xml, cmisValidator.getCMISAtomValidator());
    }

}
