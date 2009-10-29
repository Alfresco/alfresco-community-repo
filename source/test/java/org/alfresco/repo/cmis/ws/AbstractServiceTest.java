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

import java.util.List;

import junit.framework.TestCase;

import org.alfresco.cmis.CMISDictionaryModel;

/**
 * @author Michael Shavnev
 * @author Alexander Tsvetkov
 */
public abstract class AbstractServiceTest extends TestCase
{
    // protected ServiceRegistry serviceRegistry;

    public String companyHomeId;
    public String repositoryId;

    protected String documentId;
    protected String folderId;
    protected String documentName;
    protected String folderName;

    protected ObjectFactory cmisObjectFactory = new ObjectFactory();

    protected Object servicePort = null;
    protected CmisServiceTestHelper helper;

    private static boolean testAsUser = false;

    protected abstract Object getServicePort();

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public AbstractServiceTest()
    {
        super();
        if (testAsUser)
        {
            helper = CmisServiceTestHelper.getInstance(CmisServiceTestHelper.USERNAME_USER1, CmisServiceTestHelper.PASSWORD_USER1);
            servicePort = getServicePort();
            helper.authenticateServicePort(servicePort, CmisServiceTestHelper.USERNAME_USER1, CmisServiceTestHelper.PASSWORD_USER1);

            repositoryId = helper.getRepositoryId();
            companyHomeId = helper.getCompanyHomeId(repositoryId);
            // Users could not create any content in Company Home, they should create test content in Users Hone folder
            companyHomeId = helper.getUserHomeId(repositoryId, companyHomeId);
        }
        else
        {
            helper = CmisServiceTestHelper.getInstance();
            servicePort = getServicePort();
            helper.authenticateServicePort(servicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);

            repositoryId = helper.getRepositoryId();
            companyHomeId = helper.getCompanyHomeId(repositoryId);
        }
    }

    public AbstractServiceTest(String testCase, String userName, String password)
    {
        super(testCase);
        helper = CmisServiceTestHelper.getInstance(userName, password);
        servicePort = getServicePort();
        helper.authenticateServicePort(servicePort, userName, password);
        repositoryId = helper.getRepositoryId();
        companyHomeId = helper.getCompanyHomeId(repositoryId);
        // Users could not create any content in Company Home, they should create test content in Users Home folder
        companyHomeId = helper.getUserHomeId(repositoryId, companyHomeId);

    }

    public String getObjectName(CmisObjectType response)
    {
        String property = null;

        if (response != null)
        {
            CmisPropertiesType properties = response.getProperties();
            property = getStringProperty(properties, CMISDictionaryModel.PROP_NAME);
        }
        else
        {
            fail("Response has no results.");
        }
        return property;
    }

    protected String getIdProperty(CmisPropertiesType properties, String propertyName)
    {
        if (null == propertyName)
        {
            return null;
        }

        for (CmisProperty property : properties.getProperty())
        {
            if ((property instanceof CmisPropertyId) && propertyName.equals(getPropertyName(property)))
            {
                return ((CmisPropertyId) property).getValue().iterator().next();
            }
        }

        return null;
    }

    protected String getStringProperty(CmisPropertiesType properties, String propertyName)
    {
        if (null == propertyName)
        {
            return null;
        }

        for (CmisProperty property : properties.getProperty())
        {
            if ((property instanceof CmisPropertyString) && propertyName.equals(getPropertyName(property)))
            {
                return ((CmisPropertyString) property).getValue().iterator().next();
            }
        }

        return null;
    }

    protected Boolean getBooleanProperty(CmisPropertiesType properties, String propertyName)
    {
        if (null == propertyName)
        {
            return null;
        }

        for (CmisProperty property : properties.getProperty())
        {
            if ((property instanceof CmisPropertyBoolean) && propertyName.equals(getPropertyName(property)))
            {
                return ((CmisPropertyBoolean) property).getValue().iterator().next();
            }
        }

        return null;
    }

    private String getPropertyName(CmisProperty property)
    {
        String propertyName = (null != property) ? (property.getPdid()) : (null);
        if (null == propertyName)
        {
            propertyName = property.getLocalname();
            if (null == propertyName)
            {
                propertyName = property.getDisplayname();
            }
        }
        return propertyName;
    }

    protected void assertObjectPropertiesNotNull(CmisObjectType propertiesObject)
    {
        assertNotNull(propertiesObject);
        assertNotNull(propertiesObject.getProperties());
    }

    public String getObjectId(GetPropertiesResponse response)
    {
        String property = null;

        if (response != null && response.getObject() != null)
        {
            CmisObjectType object = response.getObject();
            CmisPropertiesType properties = object.getProperties();
            property = getIdProperty(properties, CMISDictionaryModel.PROP_OBJECT_ID);
        }
        else
        {
            fail("Response has no results.");
        }
        return property;
    }

    public void validateResponse(List<CmisObjectType> objects)
    {
        for (CmisObjectType object : objects)
        {
            CmisPropertiesType properties = object.getProperties();
            String name = null;
            name = getStringProperty(properties, CMISDictionaryModel.PROP_NAME);
            assertNotNull(name);
        }

    }

    public boolean isExistItemWithProperty(List<CmisObjectType> objects, String propertyName, String propertyValue)
    {
        boolean isFound = false;
        for (CmisObjectType object : objects)
        {
            CmisPropertiesType properties = object.getProperties();
            String property = null;
            property = getStringProperty(properties, propertyName);
            if (property.equals(propertyValue))
            {
                isFound = true;
            }
        }
        return isFound;
    }

    public void createInitialContent() throws Exception
    {
        // create initial content
        documentName = "Test cmis document (" + System.currentTimeMillis() + ")";
        documentId = helper.createDocument(documentName, companyHomeId);

        folderName = "Test Cmis Folder (" + System.currentTimeMillis() + ")";
        folderId = helper.createFolder(folderName, companyHomeId);
    }

    public void deleteInitialContent()
    {
        try
        {
            helper.deleteFolder(folderId);
        }
        catch (Exception e)
        {
        }

        try
        {
            helper.deleteDocument(documentId);
        }
        catch (Exception e)
        {
        }
    }

}
