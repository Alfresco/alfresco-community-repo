/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.virtual.VirtualizationIntegrationTest;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class VirtualPreferenceServiceExtensionTest extends VirtualizationIntegrationTest
{
    private static final String DOCUMENTS_FAVOURITES_KEY = "org.alfresco.share.documents.favourites";

    private static final String EXT_DOCUMENTS_FAVOURITES = "org.alfresco.ext.documents.favourites.";

    private static final String FOLDERS_FAVOURITES_KEY = "org.alfresco.share.folders.favourites";

    private static final String EXT_FOLDERS_FAVOURITES = "org.alfresco.ext.folders.favourites.";

    private static final String CREATED_AT = ".createdAt";

    private PreferenceService preferenceService;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        preferenceService = ctx.getBean("preferenceService",
                                        PreferenceService.class);
    }

    public void testSetFavoritesPreferencesForDocuments() throws Exception
    {
        NodeRef node2 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node2");
        HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_NAME,
                       "testfile.txt");
        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
                                             QName.createValidLocalName("testfile.txt"));

        nodeService.createNode(node2,
                               ContentModel.ASSOC_CONTAINS,
                               assocQName,
                               ContentModel.TYPE_CONTENT,
                               properties);
        NodeRef node2_1 = nodeService.getChildByName(node2,
                                                     ContentModel.ASSOC_CONTAINS,
                                                     "Node2_1");
        nodeService.createNode(node2_1,
                               ContentModel.ASSOC_CONTAINS,
                               assocQName,
                               ContentModel.TYPE_CONTENT,
                               properties);

        NodeRef testfile1 = nodeService.getChildByName(node2_1,
                                                       ContentModel.ASSOC_CONTAINS,
                                                       "testfile-1.txt");
        NodeRef physicalTestfile1 = nodeService.getChildByName(virtualFolder1NodeRef,
                                                               ContentModel.ASSOC_CONTAINS,
                                                               "testfile-1.txt");

        // set preference to one document from one virtual folder and check if
        // the actual nodeRef is present in
        // org.alfresco.share.documents.favourites preference
        Map<String, Serializable> preferences = new TreeMap<String, Serializable>();
        String key = EXT_DOCUMENTS_FAVOURITES + testfile1.toString() + CREATED_AT;
        preferences.put(key,
                        "CREATED");
        preferences.put(DOCUMENTS_FAVOURITES_KEY,
                        testfile1.toString());
        preferenceService.setPreferences("admin",
                                         preferences);

        String preference = (String) preferenceService.getPreference("admin",
                                                                     DOCUMENTS_FAVOURITES_KEY);
        assertFalse(preference.contains(testfile1.toString()));
        assertTrue(preference.contains(physicalTestfile1.toString()));
        assertNull((String) preferenceService.getPreference("admin",
                                                            EXT_DOCUMENTS_FAVOURITES + testfile1.toString()
                                                                        + CREATED_AT));
        assertNotNull((String) preferenceService.getPreference("admin",
                                                               EXT_DOCUMENTS_FAVOURITES + physicalTestfile1.toString()
                                                                           + CREATED_AT));

        // remove favorite for a document from one virtual folder and check that
        // the physical document is not favorite anymore
        // and that the ext keys are removed
        preferences = new TreeMap<String, Serializable>();
        key = EXT_DOCUMENTS_FAVOURITES + testfile1.toString() + CREATED_AT;
        preferences.put(key,
                        null);
        preferences.put(DOCUMENTS_FAVOURITES_KEY,
                        physicalTestfile1.toString());
        preferenceService.setPreferences("admin",
                                         preferences);

        preference = (String) preferenceService.getPreference("admin",
                                                              DOCUMENTS_FAVOURITES_KEY);
        assertTrue(preference.isEmpty());

        assertNull((String) preferenceService.getPreference("admin",
                                                            EXT_DOCUMENTS_FAVOURITES + testfile1.toString()
                                                                        + CREATED_AT));
        assertNull((String) preferenceService.getPreference("admin",
                                                            EXT_DOCUMENTS_FAVOURITES + physicalTestfile1.toString()
                                                                        + CREATED_AT));
    }

    public void testSetFavoritesPreferencesForFolders() throws Exception
    {
        NodeRef physicalFolder = createFolder(testRootFolder.getNodeRef(),
                                              "FOLDER").getChildRef();
        NodeRef virtualFolder = createVirtualizedFolder(testRootFolder.getNodeRef(),
                                                        VIRTUAL_FOLDER_2_NAME,
                                                        TEST_TEMPLATE_6_JSON_SYS_PATH);
        NodeRef node1 = nodeService.getChildByName(virtualFolder,
                                                   ContentModel.ASSOC_CONTAINS,
                                                   "Node1");
        assertNotNull(node1);

        NodeRef physicalFolderInVirtualContext = nodeService.getChildByName(node1,
                                                                            ContentModel.ASSOC_CONTAINS,
                                                                            "FOLDER");
        assertNotNull(physicalFolderInVirtualContext);

        // set preference to one folder from one virtual folder and check if
        // the actual nodeRef is present in
        // org.alfresco.share.folders.favourites preference
        Map<String, Serializable> preferences = new TreeMap<String, Serializable>();
        String key = EXT_FOLDERS_FAVOURITES + physicalFolderInVirtualContext.toString() + CREATED_AT;
        preferences.put(key,
                        "CREATED");
        preferences.put(FOLDERS_FAVOURITES_KEY,
                        physicalFolderInVirtualContext.toString());
        preferenceService.setPreferences("admin",
                                         preferences);

        String preference = (String) preferenceService.getPreference("admin",
                                                                     FOLDERS_FAVOURITES_KEY);
        assertFalse(preference.contains(physicalFolderInVirtualContext.toString()));
        assertTrue(preference.contains(physicalFolder.toString()));
        assertNull((String) preferenceService.getPreference("admin",
                                                            EXT_FOLDERS_FAVOURITES
                                                                        + physicalFolderInVirtualContext.toString()
                                                                        + CREATED_AT));
        assertNotNull((String) preferenceService.getPreference("admin",
                                                               EXT_FOLDERS_FAVOURITES + physicalFolder.toString()
                                                                           + CREATED_AT));

        // remove favorite for a folder from one virtual folder and check that
        // the physical folder is not favorite anymore
        // and that the ext keys are removed
        preferences = new TreeMap<String, Serializable>();
        key = EXT_FOLDERS_FAVOURITES + physicalFolderInVirtualContext.toString() + CREATED_AT;
        preferences.put(key,
                        null);
        preferences.put(FOLDERS_FAVOURITES_KEY,
                        physicalFolder.toString());
        preferenceService.setPreferences("admin",
                                         preferences);

        preference = (String) preferenceService.getPreference("admin",
                                                              FOLDERS_FAVOURITES_KEY);
        assertTrue(preference.isEmpty());

        assertNull((String) preferenceService.getPreference("admin",
                                                            EXT_FOLDERS_FAVOURITES
                                                                        + physicalFolderInVirtualContext.toString()
                                                                        + CREATED_AT));
        assertNull((String) preferenceService.getPreference("admin",
                                                            EXT_FOLDERS_FAVOURITES + physicalFolder.toString()
                                                                        + CREATED_AT));
    }
}