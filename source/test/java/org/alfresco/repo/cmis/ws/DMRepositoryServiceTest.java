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
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.alfresco.repo.cmis.ws.RepositoryServicePort;

/**
 * @author Michael Shavnev
 */
public class DMRepositoryServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = "http://localhost:8080/alfresco/cmis/RepositoryService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "RepositoryService");

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
        return service.getPort(RepositoryServicePort.class);
    }

    public void testGetRepositories()
    {
        try
        {
        	List<RepositoryType> repositories = ((RepositoryServicePort) servicePort).getRepositories();
            assertTrue(repositories.size() == 1);
            assertFalse(repositories.get(0).getRepositoryID() == null);
            assertFalse(repositories.get(0).getRepositoryName() == null);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }

    }

    public void testGetRepositoryInfo()
    {
        try
        {
        	List<RepositoryType> repositories = ((RepositoryServicePort) servicePort).getRepositories();
        	RepositoryInfoType repositoryInfo = ((RepositoryServicePort) servicePort).getRepositoryInfo(repositories.get(0).getRepositoryID());
        	
            assertTrue(repositoryInfo.getRepositoryId().equals(repositories.get(0).getRepositoryID()));
            assertTrue(repositoryInfo.getRepositoryName().equals(repositories.get(0).getRepositoryName()));
            assertTrue("Alfresco".equals(repositoryInfo.getVendorName()));
            assertTrue(repositoryInfo.getVendorName().indexOf("Alfresco Repository (") > -1 );
            CapabilitiesType capabilities = repositoryInfo.getCapabilities();
            assertTrue(capabilities.isCapabilityMultifiling() && capabilities.isCapabilityPWCUpdatable());
            assertFalse(capabilities.isCapabilityUnfiling() && capabilities.isCapabilityVersionSpecificFiling());

        }
        catch (Throwable e)
        {
            e.printStackTrace();
            fail();
        }

    }
}
