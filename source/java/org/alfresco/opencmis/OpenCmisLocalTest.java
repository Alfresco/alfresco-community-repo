/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.opencmis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.springframework.context.ApplicationContext;

/**
 * Tests basic local CMIS interaction
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public class OpenCmisLocalTest extends TestCase
{
    private static ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
    
    /**
     * Test class to provide the service factory
     * 
     * @author Derek Hulley
     * @since 4.0
     */
    public static class TestCmisServiceFactory implements CmisServiceFactory
    {
        private static AlfrescoCmisServiceFactory serviceFactory = (AlfrescoCmisServiceFactory) ctx.getBean("CMISServiceFactory");
        @Override
        public void init(Map<String, String> parameters)
        {
        }

        @Override
        public void destroy()
        {
        }

        @Override
        public CmisService getService(CallContext context)
        {
            return serviceFactory.getService(context);
        }
        
    }

    private Repository getRepository(String user, String password)
    {
        // default factory implementation
        SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
        Map<String, String> parameters = new HashMap<String, String>();

        // user credentials
        parameters.put(SessionParameter.USER, "admin");
        parameters.put(SessionParameter.PASSWORD, "admin");

        // connection settings
        parameters.put(SessionParameter.BINDING_TYPE, BindingType.LOCAL.value());
        parameters.put(SessionParameter.LOCAL_FACTORY, "org.alfresco.opencmis.OpenCmisLocalTest$TestCmisServiceFactory");

        // create session
        List<Repository> repositories = sessionFactory.getRepositories(parameters);
        return repositories.size() > 0 ? repositories.get(0) : null;
    }
    
    public void setUp() throws Exception
    {
    }
    
    public void testVoid()
    {
        
    }
    
    public void DISABLED_testSetUp() throws Exception
    {
        Repository repository = getRepository("admin", "admin");
        assertNotNull("No repository available for testing", repository);
    }
    
    public void testBasicFileOps()
    {
        Repository repository = getRepository("admin", "admin");
        Session session = repository.createSession();
        Folder rootFolder = session.getRootFolder();
        // create folder
        Map<String,String> folderProps = new HashMap<String, String>();
        {
            folderProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
            folderProps.put(PropertyIds.NAME, getName() + "-" + GUID.generate());
        }
        Folder folder = rootFolder.createFolder(folderProps, null, null, null, session.getDefaultContext());
        
        Map<String, String> fileProps = new HashMap<String, String>();
        {
            fileProps.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
            fileProps.put(PropertyIds.NAME, "mydoc-" + GUID.generate() + ".txt");
        }
        ContentStreamImpl fileContent = new ContentStreamImpl();
        {
            ContentWriter writer = new FileContentWriter(TempFileProvider.createTempFile(getName(), ".txt"));
            writer.putContent("Ipsum and so on");
            ContentReader reader = writer.getReader();
            fileContent.setMimeType(MimetypeMap.MIMETYPE_TEXT_PLAIN);
            fileContent.setStream(reader.getContentInputStream());
        }
        folder.createDocument(fileProps, fileContent, VersioningState.MAJOR);
    }
}
