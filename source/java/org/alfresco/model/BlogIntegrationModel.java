package org.alfresco.model;

import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public interface BlogIntegrationModel
{
    static final String MODEL_URL = "http://www.alfresco.org/model/blogintegration/1.0";
    static final String MODEL_PREFIX = "blg";
    
    static final QName ASPECT_BLOG_DETAILS = QName.createQName(MODEL_URL, "blogDetails");
    static final QName PROP_BLOG_IMPLEMENTATION = QName.createQName(MODEL_URL, "blogImplementation");
    static final QName PROP_ID = QName.createQName(MODEL_URL, "id");
    static final QName PROP_NAME = QName.createQName(MODEL_URL, "name");
    static final QName PROP_DESCRIPTION = QName.createQName(MODEL_URL, "description");
    static final QName PROP_URL = QName.createQName(MODEL_URL, "url");
    static final QName PROP_USER_NAME = QName.createQName(MODEL_URL, "userName");
    static final QName PROP_PASSWORD = QName.createQName(MODEL_URL, "password");
        
    static final QName ASPECT_BLOG_POST = QName.createQName(MODEL_URL, "blogPost");
    static final QName PROP_POST_ID = QName.createQName(MODEL_URL, "postId");
    static final QName PROP_PUBLISHED = QName.createQName(MODEL_URL, "published");
    static final QName PROP_LINK = QName.createQName(MODEL_URL, "link");
    static final QName PROP_POSTED = QName.createQName(MODEL_URL, "posted");
    static final QName PROP_LAST_UPDATE = QName.createQName(MODEL_URL, "lastUpdate");
    static final QName ASSOC_BLOG_DETAILS = QName.createQName(MODEL_URL, "blogDetails");
}
