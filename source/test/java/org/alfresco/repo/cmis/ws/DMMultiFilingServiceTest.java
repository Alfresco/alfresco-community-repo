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

import javax.xml.namespace.QName;
import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISDictionaryModel;

public class DMMultiFilingServiceTest extends AbstractServiceTest
{

    public final static String SERVICE_WSDL_LOCATION = CmisServiceTestHelper.ALFRESCO_URL + "/cmis/MultiFilingService?wsdl";
    public final static QName SERVICE_NAME = new QName("http://docs.oasis-open.org/ns/cmis/ws/200901", "MultiFilingService");
    private String anotherFolderId;

    public DMMultiFilingServiceTest()
    {
        super();
    }

    public DMMultiFilingServiceTest(String testCase, String username, String password)
    {
        super(testCase, username, password);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        createInitialContent();
        anotherFolderId = helper.createFolder("Test Cmis Folder (" + System.currentTimeMillis() + ")", companyHomeId);

    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        try
        {
            deleteInitialContent();
            helper.deleteFolder(anotherFolderId);
        }
        catch (Exception e)
        {

        }

    }

    protected Object getServicePort()
    {
        return helper.multiFilingServicePort;
    }

    public void testAddObjectToFolder() throws Exception
    {
        MultiFilingServicePort multiFilingServicePort = helper.getMultiFilingServicePort();
        helper.authenticateServicePort(multiFilingServicePort, CmisServiceTestHelper.USERNAME_ADMIN, CmisServiceTestHelper.PASSWORD_ADMIN);
        Holder<CmisExtensionType> holder = new Holder<CmisExtensionType>();
        multiFilingServicePort.addObjectToFolder(repositoryId, documentId, anotherFolderId, false, holder);
        boolean found = false;
        for (CmisObjectInFolderType cmisObjectType : helper.getChildren(anotherFolderId, 0, CMISDictionaryModel.PROP_OBJECT_ID))
        {
            assertNotNull(cmisObjectType);
            assertNotNull(cmisObjectType.getObject());
            assertNotNull(cmisObjectType.getObject().getProperties());
            if ((found = documentId.equals(getIdProperty(cmisObjectType.getObject().getProperties(), CMISDictionaryModel.PROP_OBJECT_ID))))
            {
                break;
            }
        }
        assertTrue("Document was not added to folder", found);
    }

    public void testRemoveObjectFromFolder() throws Exception
    {
        helper.addObjectToFolder(documentId, anotherFolderId);

        try
        {
            // remove object from all folders expects Exception
            ((MultiFilingServicePort) servicePort).removeObjectFromFolder(repositoryId, documentId, null, new Holder<CmisExtensionType>());
            fail("Expects exception");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.NOT_SUPPORTED));
        }

        helper.removeObjectFromFolder(documentId, anotherFolderId);

        try
        {
            // remove object from folder where it is not situated expects Exception
            ((MultiFilingServicePort) servicePort).removeObjectFromFolder(repositoryId, documentId, folderId, new Holder<CmisExtensionType>());
            fail("Expected exception");
        }
        catch (CmisException e)
        {
            assertTrue(e.toString(), e.getFaultInfo().getType().equals(EnumServiceException.STORAGE));
        }

        try
        {
            // remove object from last folder expects Exception
            ((MultiFilingServicePort) servicePort).removeObjectFromFolder(repositoryId, documentId, companyHomeId, new Holder<CmisExtensionType>());
            fail("Expected exception");
        }
        catch (CmisException e)
        {
            assertTrue(e.getFaultInfo().getType().equals(EnumServiceException.NOT_SUPPORTED));
        }
    }
}
