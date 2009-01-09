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

public class DMPolicyServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/PolicyService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://www.cmis.org/ns/1.0", "PolicyServicePort");

    public DMPolicyServiceTest()
    {
        super();
    }

    public DMPolicyServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    protected Object getServicePort()
    {
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
            return service.getPort(PolicyServicePort.class);
        }
    }

    public void testApplyPolicy() throws Exception
    {
        String documentId = helper.createDocument("Test cmis document (" + System.currentTimeMillis() + ")", companyHomeId);
        assertNotNull(documentId);
        ((PolicyServicePort) servicePort).applyPolicy(repositoryId, "policyId", documentId); // TODO policyId
        helper.deleteDocument(documentId);
    }

    public void testGetAppliedPolicies() throws Exception
    {
        String documentId = helper.createDocument("Test cmis document (" + System.currentTimeMillis() + ")", companyHomeId);
        assertNotNull(documentId);

        GetAppliedPolicies request = new GetAppliedPolicies();
        request.setRepositoryId(repositoryId);
        request.setObjectId(documentId);
        request.setFilter(cmisObjectFactory.createGetAppliedPoliciesFilter("")); // TODO
        GetAppliedPoliciesResponse response = ((PolicyServicePort) servicePort).getAppliedPolicies(request);

        // TODO: Uncomment
        // assertNotNull(response);
        // assertNotNull(response.getObject());
        // if (!response.getObject().isEmpty())
        // {
        // for (CmisObjectType object : response.getObject())
        // {
        // assertNotNull(object.getProperties());
        //            }
        //        }

        helper.deleteDocument(documentId);
    }

    public void testRemovePolicy() throws Exception
    {
        String documentId = helper.createDocument("Test cmis document (" + System.currentTimeMillis() + ")", companyHomeId);
        assertNotNull(documentId);

        ((PolicyServicePort) servicePort).removePolicy(repositoryId, "policyId", documentId); // TODO policyId

        helper.deleteDocument(documentId);
    }
}
