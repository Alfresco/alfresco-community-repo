package org.alfresco.model;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Forums Model Constants
 * 
 * @author gavinc
 */
@AlfrescoPublicApi
public interface ForumModel
{
    //
    // Forums Model Definitions
    //
   
    static final QName TYPE_FORUMS = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "forums");
    static final QName TYPE_FORUM = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "forum");
    static final QName TYPE_TOPIC = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "topic");
    static final QName TYPE_POST = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "post");

    static final QName ASPECT_DISCUSSABLE = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussable");
    
    static final QName ASSOC_DISCUSSION = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion");
    
    static final QName ASPECT_COMMENTS_ROLLUP = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "commentsRollup");
    static final QName PROP_COMMENT_COUNT = QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "commentCount");
}
