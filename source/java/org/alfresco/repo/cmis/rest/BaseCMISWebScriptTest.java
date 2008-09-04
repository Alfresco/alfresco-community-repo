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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Validator;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.cmis.rest.xsd.CMISValidator;
import org.alfresco.repo.web.scripts.BaseWebScriptTest;
import org.alfresco.web.scripts.TestWebScriptServer.Request;
import org.alfresco.web.scripts.TestWebScriptServer.Response;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Base CMIS Web Script Test
 * 
 * @author davidc
 */
public class BaseCMISWebScriptTest extends BaseWebScriptTest
{
    private CMISValidator cmisValidator = new CMISValidator();
    private boolean argsAsHeaders = false;
    
    /**
     * Pass URL arguments as headers
     * 
     * @param argsAsHeaders
     */
    protected void setArgsAsHeaders(boolean argsAsHeaders)
    {
        this.argsAsHeaders = argsAsHeaders;
    }
    
    /**
     * Determines if URL arguments are passed as headers
     * 
     * @return
     */
    protected boolean getArgsAsHeaders()
    {
        return argsAsHeaders;
    }
    
    /**
     * Gets CMIS Validator
     * 
     * @return  CMIS Validator
     */
    protected CMISValidator getCMISValidator()
    {
        return cmisValidator;
    }
    
    /**
     * Gets CMIS App Validator
     * 
     * @return  CMIS App Validator
     * 
     * @throws SAXException 
     * @throws IOException 
     */
    protected Validator getAppValidator()
        throws IOException, SAXException
    {
        return getCMISValidator().getAppValidator();
    }

    /**
     * Gets CMIS Atom Validator
     * 
     * @return  CMIS App Validator
     * 
     * @throws SAXException 
     * @throws IOException 
     */
    protected Validator getAtomValidator()
        throws IOException, SAXException
    {
        return getCMISValidator().getCMISAtomValidator();
    }

    /**
     * Asserts XML complies with specified Validator
     * 
     * @param xml  xml to assert
     * @param validator  validator to assert with
     * @throws IOException
     * @throws ParserConfigurationException
     */
    protected void assertValidXML(String xml, Validator validator)
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
     
    /**
     * Load text from file specified by class path
     * 
     * @param classPath  XML file
     * @return  XML
     * @throws IOException
     */
    protected String loadString(String classPath)
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
     * Send Request to Test Web Script Server (as admin)
     * 
     * @param req
     * @param expectedStatus
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus, Validator responseValidator)
        throws IOException
    {
        return sendRequest(req, expectedStatus, responseValidator, null);
    }
    
    /**
     * Send Request
     * 
     * @param req
     * @param expectedStatus
     * @param asUser
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus, Validator responseValidator, String asUser)
        throws IOException
    {
        Response res = sendRequest(req, expectedStatus, asUser);
        if (responseValidator != null)
        {
            try
            {
                // Validate response according to validator
                String resXML = res.getContentAsString();
                assertValidXML(resXML, responseValidator);
            }
            catch (ParserConfigurationException e)
            {
                throw new AlfrescoRuntimeException("Failed to validate", e);
            }
        }
        return res;
    }
    
    /**
     * Send Request to Test Web Script Server
     * @param req
     * @param expectedStatus
     * @param asUser
     * @return response
     * @throws IOException
     */
    protected Response sendRequest(Request req, int expectedStatus, String asUser)
        throws IOException
    {
        if (argsAsHeaders)
        {
            Map<String, String> args = req.getArgs();
            if (args != null)
            {
                Map<String, String> headers = req.getHeaders();
                if (headers == null)
                {
                    headers = new HashMap<String, String>();
                }
                for (Map.Entry<String, String> arg : args.entrySet())
                {
                    headers.put("CMIS-" + arg.getKey(), arg.getValue());
                }
                
                req = new Request(req);
                req.setArgs(null);
                req.setHeaders(headers);
            }
        }
        
        return super.sendRequest(req, expectedStatus, asUser);
    }
    
}
