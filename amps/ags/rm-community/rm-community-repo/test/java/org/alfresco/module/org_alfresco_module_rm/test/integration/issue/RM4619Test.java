/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.test.integration.issue;

import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.GUID;

/**
 * Unit test for RM-4619
 * 
 * When creating regular folders through clients that are not RM aware 
 * the folders must be converted to the appropriate RM container
 * 
 * @author Ana Bozianu
 * @since 2.6
 */
public class RM4619Test extends BaseRMTestCase
{

    /**
     * Given the RM site is created
     * When we create a regular folder in the fileplan
     * Then the folder is immediately converted to a record category
     */
    public void testConvertFolderToCategory() throws Exception
    {
        /*
         * Create a folder in the unfiled record container and check it is immediately converted
         */
        final NodeRef recordCategory = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                FileInfo info = fileFolderService.create(filePlan, GUID.generate(), TYPE_FOLDER);
                return info.getNodeRef();
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertEquals(TYPE_RECORD_CATEGORY, nodeService.getType(recordCategory));
                assertNotNull(nodeService.getProperty(recordCategory, PROP_IDENTIFIER));
                return null;
            }
        }, ADMIN_USER);
    }

    /**
     * Given an existing category
     * When we create a regular folder in the category
     * Then the folder is immediately converted to a record folder
     */
    public void testConvertFolderToRecordFolder() throws Exception
    {
        /*
         * Create a folder in a record category and check it is converted
         */
        final NodeRef recordFolder = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                FileInfo info = fileFolderService.create(rmContainer, GUID.generate(), TYPE_FOLDER);
                return info.getNodeRef();
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertEquals(TYPE_RECORD_FOLDER, nodeService.getType(recordFolder));
                assertNotNull(nodeService.getProperty(recordFolder, PROP_IDENTIFIER));
                return null;
            }
        }, ADMIN_USER);

        /*
         * Check that when the transaction ends the identifier is no longer editable
         * And the record folder has the ASPECT_RM_SEARCH aspect
         */
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertTrue(nodeService.hasAspect(recordFolder, ASPECT_RM_SEARCH));
                return null;
            }
            
        }, ADMIN_USER);
    }

    /**
     * Given the RM site is created
     * When we create a regular folder in the unfiled record container
     * Then the folder is converted to a unfiled record folder
     * 
     * And when we create another regular folder in that unfiled record folder
     * Then the folder is also converted to a unfiled record folder
     */
    public void testConvertFolderToUnfiledRecordFolder() throws Exception
    {
        /*
         * Create a folder in the unfiled record container and check it is converted
         */
        final NodeRef folder1 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                FileInfo folder = fileFolderService.create(unfiledContainer, GUID.generate(), TYPE_FOLDER);
                return folder.getNodeRef();
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertEquals(TYPE_UNFILED_RECORD_FOLDER, nodeService.getType(folder1));
                assertNotNull(nodeService.getProperty(folder1, PROP_IDENTIFIER));
                return null;
            }
        }, ADMIN_USER);

        /*
         * Create a folder in the unfiled record folder and check it is immediately converted
         */
        final NodeRef folder2 = doTestInTransaction(new Test<NodeRef>()
        {
            @Override
            public NodeRef run() throws Exception
            {
                FileInfo folder = fileFolderService.create(folder1, GUID.generate(), TYPE_FOLDER);
                return folder.getNodeRef();
            }
        }, ADMIN_USER);

        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                assertEquals(TYPE_UNFILED_RECORD_FOLDER, nodeService.getType(folder2));
                assertNotNull(nodeService.getProperty(folder2, PROP_IDENTIFIER));
                return null;
            }
        }, ADMIN_USER);
    }
}
