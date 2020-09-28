/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.activities;

public interface ActivityType
{
    // pre-defined alfresco activity types
    
    // generic fallback (if specific template is missing)
    public static final String GENERIC_FALLBACK = "org.alfresco.generic";
    
    // site membership
    public static final String SITE_USER_JOINED = "org.alfresco.site.user-joined";
    public static final String SITE_USER_REMOVED = "org.alfresco.site.user-left";
    public static final String SITE_USER_ROLE_UPDATE = "org.alfresco.site.user-role-changed";
    public static final String SITE_GROUP_ADDED = "org.alfresco.site.group-added";
    public static final String SITE_GROUP_REMOVED = "org.alfresco.site.group-removed";
    public static final String SITE_GROUP_ROLE_UPDATE = "org.alfresco.site.group-role-changed";
    public static final String SUBSCRIPTIONS_SUBSCRIBE = "org.alfresco.subscriptions.subscribed";
    public static final String SUBSCRIPTIONS_FOLLOW = "org.alfresco.subscriptions.followed";
    
    public static final String FILE_ADDED   = "org.alfresco.documentlibrary.file-added";
    public static final String FILE_UPDATED = "org.alfresco.documentlibrary.file-updated";
    public static final String FILE_DELETED = "org.alfresco.documentlibrary.file-deleted";
    
    public static final String FILES_ADDED   = "org.alfresco.documentlibrary.files-added";
    public static final String FILES_UPDATED = "org.alfresco.documentlibrary.files-updated";
    public static final String FILES_DELETED = "org.alfresco.documentlibrary.files-deleted";
    
    public static final String FOLDER_ADDED   = "org.alfresco.documentlibrary.folder-added";
    public static final String FOLDER_DELETED = "org.alfresco.documentlibrary.folder-deleted";
    
    public static final String FOLDERS_ADDED   = "org.alfresco.documentlibrary.folders-added";
    public static final String FOLDERS_DELETED = "org.alfresco.documentlibrary.folders-deleted";
    
    public static final String FILE_LIKED = "org.alfresco.documentlibrary.file-liked";
    public static final String FOLDER_LIKED = "org.alfresco.documentlibrary.folder-liked";

    public static final String COMMENT_CREATED = "org.alfresco.comments.comment-created";

    public static final String DOCLINK_CREATED = "org.alfresco.doclink.link-created";
}
