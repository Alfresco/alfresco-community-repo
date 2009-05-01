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
import java.util.List;

import javax.xml.ws.Holder;

public class DMRepositoryServiceTest extends AbstractServiceTest
{
    public final static String TYPE_ID = "D/wcm_avmlayeredcontent";

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
        List<CmisRepositoryEntryType> repositories = ((RepositoryServicePort) servicePort).getRepositories();
        assertTrue(repositories.size() == 1);
        assertFalse(repositories.get(0).getRepositoryId() == null);
        assertFalse(repositories.get(0).getRepositoryName() == null);
    }

    public void testGetRepositoryInfo() throws Exception
    {
        List<CmisRepositoryEntryType> repositories = ((RepositoryServicePort) servicePort).getRepositories();
        GetRepositoryInfo parameters = new GetRepositoryInfo();
        parameters.setRepositoryId(repositories.get(0).getRepositoryId());
        CmisRepositoryInfoType cmisRepositoryInfoType = ((RepositoryServicePort) servicePort).getRepositoryInfo(repositoryId);

        assertTrue(cmisRepositoryInfoType.getRepositoryId().equals(repositories.get(0).getRepositoryId()));
        assertTrue(cmisRepositoryInfoType.getRepositoryName().equals(repositories.get(0).getRepositoryName()));
        CmisRepositoryCapabilitiesType capabilities = cmisRepositoryInfoType.getCapabilities();
        assertTrue(capabilities.isCapabilityMultifiling() && capabilities.isCapabilityPWCUpdateable());
        assertFalse(capabilities.isCapabilityUnfiling() && capabilities.isCapabilityVersionSpecificFiling());
    }

    public void testGetTypes() throws Exception
    {
        GetTypes request = new GetTypes();
        BigInteger maxItems = BigInteger.valueOf(0);
        BigInteger skipItems = BigInteger.valueOf(0);
        request.setRepositoryId(repositoryId);
        request.setIncludePropertyDefinitions(cmisObjectFactory.createGetTypesIncludePropertyDefinitions(Boolean.FALSE));
        Holder<List<CmisTypeDefinitionType>> typeHolder = new Holder<List<CmisTypeDefinitionType>>();
        Holder<Boolean> hasMoreElementsHolder = new Holder<Boolean>();
        ((RepositoryServicePort) servicePort).getTypes(repositoryId, "", Boolean.FALSE, maxItems, skipItems, typeHolder, hasMoreElementsHolder);
        assertNotNull(typeHolder.value);
        assertFalse(typeHolder.value.isEmpty());
        CmisTypeDefinitionType element = typeHolder.value.get(0);
        assertNotNull(element);
    }

    public void testGetTypeDefinition() throws Exception
    {
        CmisTypeDefinitionType type = ((RepositoryServicePort) servicePort).getTypeDefinition(repositoryId, TYPE_ID);
        assertNotNull(type);
    }
}
