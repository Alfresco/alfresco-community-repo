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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.Service;

import org.alfresco.cmis.CMISChangeType;

/**
 * @author Andrey Sokolovsky
 * @author Dmitry Velichkevich
 */
public class DMDiscoveryServiceTest extends AbstractServiceTest
{
    public final static String SERVICE_WSDL_LOCATION = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/DiscoveryService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200908/", "DiscoveryService");
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
        DiscoveryServicePort port = service.getPort(DiscoveryServicePort.class);
        helper.authenticateServicePort(port, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
        return port;
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
                assertNotNull(object);
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
        // TODO: test data creation
        Map<CMISChangeType, Integer> expectedAmounts = new HashMap<CMISChangeType, Integer>();
        List<String> testData = createTestData(18, expectedAmounts);
        Holder<CmisObjectListType> resultHolder = new Holder<CmisObjectListType>();
        Holder<String> changeLogToken = new Holder<String>();
        RepositoryServicePort repositoryServicePort = helper.getRepositoryServicePort();
        helper.authenticateServicePort(repositoryServicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
        CmisRepositoryInfoType repositoryInfo = repositoryServicePort.getRepositoryInfo(repositoryId, null);
        changeLogToken.value = repositoryInfo.getLatestChangeLogToken();
        // TODO: includeACL
        // TODO: includePolicyIds
        DiscoveryServicePort port = (DiscoveryServicePort) getServicePort();
        port.getContentChanges(repositoryId, changeLogToken, true, "*", false, false, null, null, resultHolder);
        assertNotNull(resultHolder.value);
        assertNotNull(resultHolder.value.getObjects());
        assertEquals(18, resultHolder.value.getNumItems());
        assertFalse(resultHolder.value.isHasMoreItems());
        for (CmisObjectType object : resultHolder.value.getObjects())
        {
            assertNotNull(object);
            assertNotNull(object.getProperties());
            String idProperty = getIdProperty(object.getProperties(), "cmis:objectId");
            assertNotNull(idProperty);
            assertTrue(testData.contains(idProperty));
            // TODO: Checking for Change Type (Object Diff etc...)
            switch (object.getChangeEventInfo().getChangeType())
            {
            case DELETED:
            {
                try
                {
                    helper.getObjectProperties(idProperty);
                }
                catch (CmisException e)
                {
                    assertTrue(EnumServiceException.OBJECT_NOT_FOUND == e.getFaultInfo().getType());
                }
                break;
            }
            case UPDATED:
            {
                CmisObjectType objectProperties = helper.getObjectProperties(idProperty);
                assertNotNull(objectProperties.getProperties());
                String nameProperty = getStringProperty(objectProperties.getProperties(), "cmis:name");
                assertNotNull(nameProperty);
                assertTrue(nameProperty.startsWith("Changed"));
                break;
            }
            case SECURITY:
            {
                // FIXME: Uncomment below when ACLService will be implemented
                // ACLServicePort aclService = null;
                // CmisACLType acl = aclService.getACL(repositoryId, idProperty, false, null);
                // assertTrue(acl.getACL().getPermission().contains("DELETE"));
            }
            }
        }
        deleteTestData(testData);
    }

    private List<String> createTestData(int totalAmountOfObjects, Map<CMISChangeType, Integer> expectedAmounts) throws Exception
    {
        RepositoryServicePort repositoryServicePort = helper.getRepositoryServicePort();
        helper.authenticateServicePort(repositoryServicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
        repositoryServicePort.getRepositoryInfo(repositoryId, null);
        List<String> result = new LinkedList<String>();
        Random randomizer = new Random();
        for (int i = 0; i < totalAmountOfObjects; i++)
        {
            String generatedName = generateName();
            String objectId = helper.createDocument(generatedName, companyHomeId);
            result.add(objectId);
            int type = randomizer.nextInt(4);
            CMISChangeType changeType = CMISChangeType.CREATED;
            switch (type)
            {
            case 1:
            {
                changeType = CMISChangeType.UPDATED;
                StringBuilder nameGenerator = new StringBuilder("Changed");
                nameGenerator.append(generatedName);
                helper.updateProperty(objectId, "cmis:name", nameGenerator.toString());
                break;
            }
            case 2:
            {
                changeType = CMISChangeType.SECURITY;
                // FIXME: When AuditingService will be available
                // 
                // ACLServicePort aclServicePort = null;
                // CmisAccessControlListType addACEs = new CmisAccessControlListType();
                // CmisAccessControlEntryType aclEntry = new CmisAccessControlEntryType();
                // CmisAccessControlPrincipalType value = new CmisAccessControlPrincipalType();
                // value.setPrincipalId("admin");
                // aclEntry.setPrincipal(value);
                // aclEntry.setDirect(true);
                // aclEntry.getPermission().add("DELETE");
                // addACEs.getPermission().add(aclEntry);
                // aclServicePort.applyACL(repositoryId, objectId, addACEs, null, EnumACLPropagation.OBJECTONLY, null);
                break;
            }
            case 3:
            {
                changeType = CMISChangeType.DELETED;
                helper.deleteDocument(objectId);
            }
            }
            Integer amount = expectedAmounts.get(changeType);
            amount = (null == amount) ? (Integer.valueOf(1)) : (Integer.valueOf(amount.intValue() + 1));
            expectedAmounts.put(changeType, amount);
        }
        return result;
    }

    private String generateName()
    {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append("TestDocument(").append(System.currentTimeMillis()).append(").txt");
        return nameBuilder.toString();
    }

    private void deleteTestData(List<String> testData)
    {
        for (String id : testData)
        {
            try
            {
                helper.getObjectProperties(id);
                helper.deleteDocument(id);
            }
            catch (Exception e)
            {
                // doing nothing
            }
        }
    }
}
