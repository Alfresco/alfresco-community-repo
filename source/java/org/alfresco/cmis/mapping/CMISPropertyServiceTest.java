/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.cmis.mapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.cmis.CMISDictionaryModel;
import org.alfresco.cmis.CMISInvalidArgumentException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.lock.LockType;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

public class CMISPropertyServiceTest extends BaseCMISTest
{
    public void testBasicFolder() throws Exception
    {
        NodeRef folder = fileFolderService.create(cmisService.getDefaultRootNodeRef(), "BaseFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        Map<String, Serializable> properties = cmisService.getProperties(folder);
        assertEquals(folder.toString(), properties.get(CMISDictionaryModel.PROP_OBJECT_ID));
        assertEquals(CMISDictionaryModel.FOLDER_TYPE_ID.getId(), properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID));
        assertEquals(CMISDictionaryModel.FOLDER_TYPE_ID.getId(), properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID));
        assertEquals(authenticationComponent.getCurrentUserName(), properties.get(CMISDictionaryModel.PROP_CREATED_BY));
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(authenticationComponent.getCurrentUserName(), properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY));
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals("BaseFolder", properties.get(CMISDictionaryModel.PROP_NAME));

        assertNull(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE));
        assertNull(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION));
        assertNull(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION));
        assertNull(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION));
        assertNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertNull(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT));
        assertNull(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY));
        assertNull(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT));
        assertNull(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH));
        assertNull(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME));
        assertNull(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_ID));

        assertEquals(cmisService.getDefaultRootNodeRef().toString(), properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));
    }

    public void testBasicDocument() throws Exception
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));
    }

    public void testContentProperties() throws Exception
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        ContentData contentData = new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK);

        nodeService.setProperty(content, ContentModel.PROP_CONTENT, contentData);

        ContentWriter writer = serviceRegistry.getContentService().getWriter(content, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        writer.putContent("The quick brown fox jumped over the lazy dog and ate the Alfresco Tutorial, in pdf format, along with the following stop words;  a an and are"
                + " as at be but by for if in into is it no not of on or such that the their then there these they this to was will with: "
                + " and random charcters \u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF");
        long size = writer.getSize();

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), size);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "text/plain");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_ID));
    }

    public void testLock() throws Exception
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        serviceRegistry.getLockService().lock(content, LockType.READ_ONLY_LOCK);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);

        serviceRegistry.getLockService().unlock(content);
        properties = cmisService.getProperties(content);

        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));
    }

    public void testCheckOut() throws Exception
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        NodeRef pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);

        properties = cmisService.getProperties(pwc);

        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        serviceRegistry.getCheckOutCheckInService().cancelCheckout(pwc);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);

        properties = cmisService.getProperties(pwc);

        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        serviceRegistry.getCheckOutCheckInService().checkin(pwc, null);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));
    }

    public void testVersioning() throws Exception
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        nodeService.addAspect(content, ContentModel.ASPECT_VERSIONABLE, null);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        NodeRef pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);

        properties = cmisService.getProperties(pwc);

        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        serviceRegistry.getCheckOutCheckInService().cancelCheckout(pwc);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);

        properties = cmisService.getProperties(pwc);

        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "Meep");
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        serviceRegistry.getCheckOutCheckInService().checkin(pwc, versionProperties);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL), "current");
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL), "current");
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());

        properties = cmisService.getProperties(pwc);

        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertNotNull(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL));
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "Woof");
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        serviceRegistry.getCheckOutCheckInService().checkin(pwc, versionProperties);

        properties = cmisService.getProperties(content);
        assertEquals(properties.get(CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.DOCUMENT_TYPE_ID.getId());
        assertEquals(properties.get(CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISDictionaryModel.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISDictionaryModel.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_LABEL), "current");
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_ID), content.toString());
        assertEquals(properties.get(CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT), false);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE), "application/octet-stream");
        assertEquals(properties.get(CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME), "BaseContent");

        assertNull(properties.get(CMISDictionaryModel.PROP_PARENT_ID));
        assertNull(properties.get(CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));
    }
    
    public void testSinglePropertyFolderAccess() throws Exception
    {   
        NodeRef folder = fileFolderService.create(rootNodeRef, "BaseFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_OBJECT_ID), folder.toString());
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_OBJECT_TYPE_ID), CMISDictionaryModel.FOLDER_TYPE_ID.getId());
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_BASE_TYPE_ID), CMISDictionaryModel.FOLDER_TYPE_ID.getId());
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(cmisService.getProperty(folder, CMISDictionaryModel.PROP_CREATION_DATE));
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(cmisService.getProperty(folder, CMISDictionaryModel.PROP_LAST_MODIFICATION_DATE));
        assertNull(cmisService.getProperty(folder, CMISDictionaryModel.PROP_CHANGE_TOKEN));
       
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_NAME), "BaseFolder");

        try
        {
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_IS_IMMUTABLE);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_IS_LATEST_VERSION);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_IS_MAJOR_VERSION);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_IS_LATEST_MAJOR_VERSION);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_VERSION_LABEL);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_VERSION_SERIES_ID);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_IS_VERSION_SERIES_CHECKED_OUT);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_BY);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_VERSION_SERIES_CHECKED_OUT_ID);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_CHECKIN_COMMENT);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_CONTENT_STREAM_LENGTH);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_CONTENT_STREAM_MIME_TYPE);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_CONTENT_STREAM_FILENAME);
            cmisService.getProperty(folder, CMISDictionaryModel.PROP_CONTENT_STREAM_ID);
            fail("Failed to catch invalid property on type folder");
        }
        catch(CMISInvalidArgumentException e)
        {
            // NOTE: Invalid property
        }
       
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_PARENT_ID), rootNodeRef.toString());
        assertNull(cmisService.getProperty(folder, CMISDictionaryModel.PROP_ALLOWED_CHILD_OBJECT_TYPE_IDS));

        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_NAME.toUpperCase()), "BaseFolder");
        assertEquals(cmisService.getProperty(folder, CMISDictionaryModel.PROP_NAME.toLowerCase()), "BaseFolder");
    }
}
