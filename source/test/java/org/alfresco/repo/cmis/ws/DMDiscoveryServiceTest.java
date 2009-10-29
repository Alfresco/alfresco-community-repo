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

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

public class DMDiscoveryServiceTest extends AbstractServiceTest
{
    public final static String SERVICE_WSDL_LOCATION = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/DiscoveryService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200901", "DiscoveryService");
    public final static String STATEMENT = "SELECT * FROM cmis:document";

    public DMDiscoveryServiceTest()
    {
        super();
    }

    public DMDiscoveryServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
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
        return service.getPort(DiscoveryServicePort.class);
    }

    public void testQuery() throws Exception
    {
        Query request = new Query();
        request.setRepositoryId(repositoryId);
        request.setStatement(STATEMENT);
        QueryResponse response = ((DiscoveryServicePort) servicePort).query(request);
        assertNotNull(response);

        assertNotNull(response.getObjects());
        assertNotNull(response.getObjects().getObjects());

        if (!response.getObjects().getObjects().isEmpty())
        {
            for (CmisObjectType object : response.getObjects().getObjects())
            {
                assertNotNull(object.getProperties());
            }

        }
        else
        {
            fail("The query returned no results");
        }
    }

    public void testGetContentChanges() throws Exception
    {
        try
        {
            Holder<String> changeLogToken = new Holder<String>();
            Holder<CmisObjectListType> resultHolder = new Holder<CmisObjectListType>();
            // TODO: includeACL
            // TODO: includePolicyIds
            ((DiscoveryServicePort) servicePort).getContentChanges(repositoryId, changeLogToken, false, "", false, false, BigInteger.ZERO, null, resultHolder);
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.RUNTIME));
        }
    }
}
