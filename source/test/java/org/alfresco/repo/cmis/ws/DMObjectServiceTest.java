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
package org.alfresco.repo.cmis.ws;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class DMObjectServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = "http://localhost:8080/alfresco/cmis/ObjectService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "ObjectService");
    
    
    private RepositoryServicePort repositoryServicePort;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        repositoryServicePort = (RepositoryServicePort) fContext.getBean("dmRepositoryService");
    }
    
    protected Object getServicePort()
    {
        URL serviceWsdlURL;
        try
        {
            serviceWsdlURL = new URL(SERVICE_WSDL_LOCATION);
        }
        catch (MalformedURLException e)
        {
            throw new java.lang.RuntimeException("Cannot get service Wsdl URL", e);
        }
        Service service = Service.create(serviceWsdlURL, SERVICE_NAME);
        
        return service.getPort(ObjectServicePort.class);
    }


    public void testGetDocumentProperties() throws Exception
    {
        GetProperties request = new GetProperties();
        request.setObjectId(ALFRESCO_TUTORIAL_NODE_REF.toString());
        request.setRepositoryId(repositoryServicePort.getRepositories().get(0).getRepositoryID());
        
        GetPropertiesResponse response = ((ObjectServicePort) servicePort).getProperties(request);
        assertNotNull(response);
    }

    public void testGetPropertiesForInvalidOID() throws Exception
    {

        GetProperties request = new GetProperties();
        request.setObjectId("invalid OID");
        request.setRepositoryId("invalid OID");
        try
        {
            ((ObjectServicePort) servicePort).getProperties(request);
        }
        catch (InvalidArgumentException e)
        {
            return;
        }

        fail("Expects exception");
    }



    public void testGetContentStream() throws Exception
    {
        String documentId = ALFRESCO_TUTORIAL_NODE_REF.toString();
        String repoId = repositoryServicePort.getRepositories().get(0).getRepositoryID();

        GetContentStream contStream = new GetContentStream();
        contStream.setDocumentId(documentId);
        contStream.setRepositoryId(repoId);
        
        CmisContentStreamType result = ((ObjectServicePort) servicePort).getContentStream(repoId, documentId);
        if (result.length.intValue() == 0)
        {
            fail();
        }

        try
        {
            contStream.setDocumentId(documentId + "s");
            {
                result = ((ObjectServicePort) servicePort).getContentStream(repoId, documentId);
            }
        }
        catch (ObjectNotFoundException e)
        {
        }
        catch (Throwable e)
        {
            fail();
        }
    }
}
