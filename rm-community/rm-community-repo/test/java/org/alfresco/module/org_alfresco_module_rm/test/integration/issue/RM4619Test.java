/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
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
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                FileInfo info = fileFolderService.create(filePlan, GUID.generate(), TYPE_FOLDER);
                assertEquals(TYPE_RECORD_CATEGORY, info.getType());
                assertNotNull(info.getProperties().get(PROP_IDENTIFIER));
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
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                FileInfo info = fileFolderService.create(rmContainer, GUID.generate(), TYPE_FOLDER);
                assertEquals(TYPE_RECORD_FOLDER, info.getType());
                assertNotNull(info.getProperties().get(PROP_IDENTIFIER));
                return null;
            }
            
        }, ADMIN_USER);
    }

    /**
     * Given the RM site is created
     * When we create a regular folder in the unfiled record container
     * Then the folder is immediately converted to a unfiled record folder
     * 
     * And when we create another regular folder in that unfiled record folder
     * Then the folder is also immediately converted to a unfiled record folder
     */
    public void testConvertFolderToUnfiledRecordFolder() throws Exception
    {
        doTestInTransaction(new Test<Void>()
        {
            @Override
            public Void run() throws Exception
            {
                FileInfo folder1 = fileFolderService.create(unfiledContainer, GUID.generate(), TYPE_FOLDER);
                assertEquals(TYPE_UNFILED_RECORD_FOLDER, folder1.getType());
                assertNotNull(folder1.getProperties().get(PROP_IDENTIFIER));

                FileInfo folder2 = fileFolderService.create(folder1.getNodeRef(), GUID.generate(), TYPE_FOLDER);
                assertEquals(TYPE_UNFILED_RECORD_FOLDER, folder2.getType());
                assertNotNull(folder2.getProperties().get(PROP_IDENTIFIER));
                return null;
            }
            
        }, ADMIN_USER);
    }
}
