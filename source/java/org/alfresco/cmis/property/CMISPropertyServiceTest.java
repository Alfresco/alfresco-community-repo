/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.cmis.property;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.cmis.dictionary.BaseCMISTest;
import org.alfresco.cmis.dictionary.CMISMapping;
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
    public void testBasicFolder()
    {
        NodeRef folder = fileFolderService.create(rootNodeRef, "BaseFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        Map<String, Serializable> properties = cmisPropertyService.getProperties(folder);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), folder);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.FOLDER_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseFolder");

        assertNull(properties.get(CMISMapping.PROP_IS_IMMUTABLE));
        assertNull(properties.get(CMISMapping.PROP_IS_LATEST_VERSION));
        assertNull(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION));
        assertNull(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION));
        assertNull(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT));
        assertNull(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY));
        assertNull(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID));
        assertNull(properties.get(CMISMapping.PROP_CHECKIN_COMMENT));
        assertNull(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED));
        assertNull(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH));
        assertNull(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE));
        assertNull(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME));
        assertNull(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI));

        assertEquals(properties.get(CMISMapping.PROP_PARENT), rootNodeRef);
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

    }

    public void testBasicDocument()
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

    }

    public void testContentProperties()
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        ContentData contentData = new ContentData(null, "text/plain", 0L, "UTF-8", Locale.UK);

        nodeService.setProperty(content, ContentModel.PROP_CONTENT, contentData);

        ContentWriter writer = serviceRegistry.getContentService().getWriter(content, ContentModel.PROP_CONTENT, true);
        writer.setEncoding("UTF-8");
        writer.putContent("The quick brown fox jumped over the lazy dog and ate the Alfresco Tutorial, in pdf format, along with the following stop words;  a an and are"
                + " as at be but by for if in into is it no not of on or such that the their then there these they this to was will with: "
                + " and random charcters \u00E0\u00EA\u00EE\u00F0\u00F1\u00F6\u00FB\u00FF");
        long size = writer.getSize();

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), size);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "text/plain");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);
    }

    public void testLock()
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        serviceRegistry.getLockService().lock(content, LockType.READ_ONLY_LOCK);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);

        serviceRegistry.getLockService().unlock(content);
        properties = cmisPropertyService.getProperties(content);

        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

    }

    public void testCheckOut()
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        NodeRef pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);

        properties = cmisPropertyService.getProperties(pwc);

        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), pwc);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        serviceRegistry.getCheckOutCheckInService().cancelCheckout(pwc);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);

        properties = cmisPropertyService.getProperties(pwc);

        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), pwc);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        serviceRegistry.getCheckOutCheckInService().checkin(pwc, null);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

    }

    public void testVersioning()
    {
        NodeRef content = fileFolderService.create(rootNodeRef, "BaseContent", ContentModel.TYPE_CONTENT).getNodeRef();

        Map<String, Serializable> properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        nodeService.addAspect(content, ContentModel.ASPECT_VERSIONABLE, null);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        NodeRef pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);

        properties = cmisPropertyService.getProperties(pwc);

        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), pwc);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        serviceRegistry.getCheckOutCheckInService().cancelCheckout(pwc);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);

        properties = cmisPropertyService.getProperties(pwc);

        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), pwc);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        Map<String, Serializable> versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "Meep");
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MAJOR);
        serviceRegistry.getCheckOutCheckInService().checkin(pwc, versionProperties);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), "Meep");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        pwc = serviceRegistry.getCheckOutCheckInService().checkout(content);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), true);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), authenticationComponent.getCurrentUserName());
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), pwc);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), "Meep");

        properties = cmisPropertyService.getProperties(pwc);

        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), pwc);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent (Working Copy)");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), null);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent (Working Copy)");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        versionProperties = new HashMap<String, Serializable>();
        versionProperties.put(Version.PROP_DESCRIPTION, "Woof");
        versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
        serviceRegistry.getCheckOutCheckInService().checkin(pwc, versionProperties);

        properties = cmisPropertyService.getProperties(content);
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_ID), content);
        assertNull(properties.get(CMISMapping.PROP_URI));
        assertEquals(properties.get(CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.DOCUMENT_TYPE_ID);
        assertEquals(properties.get(CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_CREATION_DATE));
        assertEquals(properties.get(CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(properties.get(CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(properties.get(CMISMapping.PROP_CHANGE_TOKEN));

        assertEquals(properties.get(CMISMapping.PROP_NAME), "BaseContent");

        assertEquals(properties.get(CMISMapping.PROP_IS_IMMUTABLE), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_VERSION), true);
        assertEquals(properties.get(CMISMapping.PROP_IS_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_IS_LATEST_MAJOR_VERSION), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT), false);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY), null);
        assertEquals(properties.get(CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID), null);
        assertEquals(properties.get(CMISMapping.PROP_CHECKIN_COMMENT), "Woof");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_ALLOWED), true);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_LENGTH), 0L);
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_MIMETYPE), "application/octet-stream");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_FILENAME), "BaseContent");
        assertEquals(properties.get(CMISMapping.PROP_CONTENT_STREAM_URI), null);

        assertNull(properties.get(CMISMapping.PROP_PARENT));
        assertNull(properties.get(CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));
    }
    
    public void testSinglePropertyFolderAccess()
    {   
        NodeRef folder = fileFolderService.create(rootNodeRef, "BaseFolder", ContentModel.TYPE_FOLDER).getNodeRef();
        assertEquals(cmisPropertyService.getProperty(folder, CMISMapping.PROP_OBJECT_ID), folder);
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_URI));
        assertEquals(cmisPropertyService.getProperty(folder, CMISMapping.PROP_OBJECT_TYPE_ID), CMISMapping.FOLDER_TYPE_ID);
        assertEquals(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CREATED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CREATION_DATE));
        assertEquals(cmisPropertyService.getProperty(folder, CMISMapping.PROP_LAST_MODIFIED_BY), authenticationComponent.getCurrentUserName());
        assertNotNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_LAST_MODIFICATION_DATE));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CHANGE_TOKEN));
       
        assertEquals(cmisPropertyService.getProperty(folder, CMISMapping.PROP_NAME), "BaseFolder");

        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_IS_IMMUTABLE));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_IS_LATEST_VERSION));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_IS_MAJOR_VERSION));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_IS_LATEST_MAJOR_VERSION));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_VERSION_SERIES_IS_CHECKED_OUT));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_BY));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_VERSION_SERIES_CHECKED_OUT_ID));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CHECKIN_COMMENT));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CONTENT_STREAM_ALLOWED));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CONTENT_STREAM_LENGTH));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CONTENT_STREAM_MIMETYPE));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CONTENT_STREAM_FILENAME));
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_CONTENT_STREAM_URI));
       
        assertEquals(cmisPropertyService.getProperty(folder, CMISMapping.PROP_PARENT), rootNodeRef);
        assertNull(cmisPropertyService.getProperty(folder, CMISMapping.PROP_ALLLOWED_CHILD_OBJECT_TYPES));

        assertEquals(cmisPropertyService.getProperty(folder, "CM_NAME"), "BaseFolder");
        assertEquals(cmisPropertyService.getProperty(folder, "cm_name"), "BaseFolder");
    }
}
