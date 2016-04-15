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
}
