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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DMRepositoryServiceTest extends AbstractServiceTest
{
    public final static String TYPE_ID = "cmis:folder";

    public DMRepositoryServiceTest()
    {
        super();
    }

    public DMRepositoryServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    protected Object getServicePort()
    {
        return helper.repositoryServicePort;
    }

    public void testGetRepositories() throws Exception
    {
        List<CmisRepositoryEntryType> repositories = ((RepositoryServicePort) servicePort).getRepositories(null);
        assertTrue(repositories.size() == 1);
        assertFalse(repositories.get(0).getRepositoryId() == null);
        assertFalse(repositories.get(0).getRepositoryName() == null);
    }

    public void testGetRepositoryInfo() throws Exception
    {
        List<CmisRepositoryEntryType> repositories = ((RepositoryServicePort) servicePort).getRepositories(null);
        GetRepositoryInfo parameters = new GetRepositoryInfo();
        parameters.setRepositoryId(repositories.get(0).getRepositoryId());
        CmisRepositoryInfoType cmisRepositoryInfoType = ((RepositoryServicePort) servicePort).getRepositoryInfo(repositoryId, null);

        assertTrue(cmisRepositoryInfoType.getRepositoryId().equals(repositories.get(0).getRepositoryId()));
        assertTrue(cmisRepositoryInfoType.getRepositoryName().equals(repositories.get(0).getRepositoryName()));
        CmisRepositoryCapabilitiesType capabilities = cmisRepositoryInfoType.getCapabilities();
        assertTrue(capabilities.isCapabilityMultifiling() && capabilities.isCapabilityPWCUpdatable());
        assertFalse(capabilities.isCapabilityUnfiling() && capabilities.isCapabilityVersionSpecificFiling());
    }

    public void testGetTypeChildren() throws Exception
    {
        BigInteger maxItems = BigInteger.valueOf(0);
        BigInteger skipItems = BigInteger.valueOf(0);
        CmisTypeDefinitionListType response = ((RepositoryServicePort) servicePort).getTypeChildren(repositoryId, "", false, maxItems, skipItems, null);
        assertNotNull(response);
        assertNotNull(response.getTypes());
        assertFalse(response.getTypes().isEmpty());
        CmisTypeDefinitionType element = response.getTypes().get(0);
        assertNotNull(element);
    }

    public void testGetTypeDescedants() throws Exception
    {
        List<CmisTypeContainer> result = ((RepositoryServicePort) servicePort).getTypeDescendants(repositoryId, "", null, true, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        CmisTypeContainer element = result.get(0);
        assertNotNull(element);
        result = ((RepositoryServicePort) servicePort).getTypeDescendants(repositoryId, "", BigInteger.valueOf(1), true, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0));
        result = ((RepositoryServicePort) servicePort).getTypeDescendants(repositoryId, TYPE_ID, null, false, null);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotNull(result.get(0));
    }

    public void testGetTypeDefinition() throws Exception
    {
        CmisTypeDefinitionType type = ((RepositoryServicePort) servicePort).getTypeDefinition(repositoryId, TYPE_ID, null);
        assertNotNull(type);
    }

    private static final Map<String, EnumBaseObjectTypeIds> BASE_TYPE_IDS = new HashMap<String, EnumBaseObjectTypeIds>();
    static {
        BASE_TYPE_IDS.put(EnumBaseObjectTypeIds.CMIS_DOCUMENT.value(), EnumBaseObjectTypeIds.CMIS_DOCUMENT);
        BASE_TYPE_IDS.put(EnumBaseObjectTypeIds.CMIS_FOLDER.value(), EnumBaseObjectTypeIds.CMIS_FOLDER);
        BASE_TYPE_IDS.put(EnumBaseObjectTypeIds.CMIS_RELATIONSHIP.value(), EnumBaseObjectTypeIds.CMIS_RELATIONSHIP);
        BASE_TYPE_IDS.put(EnumBaseObjectTypeIds.CMIS_POLICY.value(), EnumBaseObjectTypeIds.CMIS_POLICY);
    }

    // CMIS SOAP webscript [https://issues.alfresco.com/jira/browse/SAIL-225]
    public void testSail225Check() throws Exception
    {
        RepositoryServicePort repositoryServicePort = (RepositoryServicePort) getServicePort();

        // Checking whether RepositoryService.getTypeChildren() returns all base type definitions if typeId is not specified
        CmisTypeDefinitionListType typeChildren = repositoryServicePort.getTypeChildren(repositoryId, null, false, null, null, null);
        assertNotNull(typeChildren);
        assertNotNull(typeChildren.getTypes());
        assertFalse(typeChildren.getTypes().isEmpty());
        assertEquals(4, typeChildren.getTypes().size());
        Set<EnumBaseObjectTypeIds> alreadyFound = new HashSet<EnumBaseObjectTypeIds>();
        for (CmisTypeDefinitionType type : typeChildren.getTypes())
        {
            EnumBaseObjectTypeIds typeId = BASE_TYPE_IDS.get(type.getId());
            assertNotNull("RepositoryService.getTypeChildren() returned superfluous Type Definition with not specified Type Id input parameter", typeId);
            assertFalse(type.getId() + " Base Type Definition was returned twice", alreadyFound.contains(typeId));
            alreadyFound.add(typeId);
        }

        // Checking receiving descendants for Base Types
        for (EnumBaseObjectTypeIds typeId : EnumBaseObjectTypeIds.values())
        {
            assertBaseTypeDescendants(repositoryServicePort, typeId.value());
        }

        // Checking Type Definition receiving for Base Type Ids
        for (EnumBaseObjectTypeIds typeId : EnumBaseObjectTypeIds.values())
        {
            CmisTypeDefinitionType typeDefinition = repositoryServicePort.getTypeDefinition(repositoryId, typeId.value(), null);
            assertNotNull(("Type Definition for " + typeId.value() + " Base Type Id was not returned"), typeDefinition);
            assertEquals(("Invalid Type Definitions was returned for " + typeId.value() + " Base Type Id"), typeId.value(), typeDefinition.getId());
        }
    }

    private void assertBaseTypeDescendants(RepositoryServicePort repositoryServicePort, String typeId) throws CmisException
    {
        List<CmisTypeContainer> typeDescendants = repositoryServicePort.getTypeDescendants(repositoryId, typeId, BigInteger.valueOf(-1), false, null);
        int typeFoundTimes = 0;
        for (CmisTypeContainer container : typeDescendants)
        {
            if (container.getType().getId().equals(typeId))
            {
                typeFoundTimes++;
            }
        }
        assertTrue((typeId + " was found at all"), (0 < typeFoundTimes));
    }
}
