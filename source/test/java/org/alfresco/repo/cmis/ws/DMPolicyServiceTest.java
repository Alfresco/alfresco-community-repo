/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.cmis.ws;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class DMPolicyServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/PolicyService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "PolicyServicePort");

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
        try
        {
            ((PolicyServicePort) servicePort).applyPolicy(repositoryId, "policyId", documentId, null);
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.RUNTIME));
        }
    }

    public void testGetAppliedPolicies() throws Exception
    {
        try
        {
            @SuppressWarnings("unused")
            List<CmisObjectType> response = ((PolicyServicePort) servicePort).getAppliedPolicies(repositoryId, documentId, "", null);
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.RUNTIME));
        }
    }

    public void testRemovePolicy() throws Exception
    {
        try
        {
            ((PolicyServicePort) servicePort).removePolicy(repositoryId, "policyId", documentId, null); // TODO policyId
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.RUNTIME));
        }
    }
}
