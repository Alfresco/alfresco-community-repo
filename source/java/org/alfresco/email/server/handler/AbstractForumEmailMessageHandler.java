package org.alfresco.email.server.handler;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.email.EmailMessage;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * Abstact class implements common logic for forum processing email mesages.
 * 
 * @author maxim
 * @since 2.2
 */
public abstract class AbstractForumEmailMessageHandler extends AbstractEmailMessageHandler
{
    /**
     * Posts content
     * 
     * @param nodeRef   Reference to node
     * @param message    Mail parser
     * @return          Returns the new post node
     */
    protected NodeRef addPostNode(NodeRef nodeRef, EmailMessage message)
    {
        NodeService nodeService = getNodeService();
        Date now = new Date();
        String nodeName = "posted-" + new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss").format(now) + ".html";

        PropertyMap properties = new PropertyMap(3);
        properties.put(ContentModel.PROP_NAME, nodeName);

        NodeRef postNodeRef = nodeService.getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, nodeName);
        if (postNodeRef == null)
        {
            ChildAssociationRef childAssoc = nodeService.createNode(
                    nodeRef,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName),
                    ForumModel.TYPE_POST,
                    properties);
            postNodeRef = childAssoc.getChildRef();
        }

        // Add necessary aspects
        properties.clear();
        properties.put(ContentModel.PROP_TITLE, nodeName);
        nodeService.addAspect(postNodeRef, ContentModel.ASPECT_TITLED, properties);
        properties.clear();
        properties.put(ApplicationModel.PROP_EDITINLINE, true);
        nodeService.addAspect(postNodeRef, ApplicationModel.ASPECT_INLINEEDITABLE, properties);

        // Write content
        if (message.getBody() != null)
        {
            writeContent(
                    postNodeRef,
                    message.getBody().getContent(),
                    message.getBody().getContentType(),
                    message.getBody().getEncoding());
        }
        else
        {
            writeContent(postNodeRef, "<The message was empty>", MimetypeMap.MIMETYPE_TEXT_PLAIN);
        }
        addEmailedAspect(postNodeRef, message);
        
        // Done
        return postNodeRef;
    }

    /**
     * Finds first child with specified name
     * 
     * @param nodeRef Parent node for the search
     * @param name String for search
     * @return Reference to found node or null if node isn't found
     */
    protected NodeRef getTopicNode(NodeRef nodeRef, String name)
    {
        String workingName = encodeSubject(name);
        
        NodeRef ret = getNodeService().getChildByName(nodeRef, ContentModel.ASSOC_CONTAINS, workingName);
        return ret;
    }

    /**
     * Adds topic node into Alfresco repository
     * 
     * @param parentNode        Parent node
     * @param name              Topic name
     * @return                  Reference to created node
     */
    protected NodeRef addTopicNode(NodeRef parentNode, String name)
    {
        String workingName = encodeSubject(name);
        
        NodeService nodeService = getNodeService();
        Map<QName, Serializable> properties = new HashMap<QName, Serializable>(1);
        properties.put(ContentModel.PROP_NAME, workingName);

        NodeRef topicNode = nodeService.getChildByName(parentNode, ContentModel.ASSOC_CONTAINS, workingName);
        if (topicNode == null)
        {
            ChildAssociationRef association = nodeService.createNode(
                    parentNode,
                    ContentModel.ASSOC_CONTAINS,
                    QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, workingName),
                    ForumModel.TYPE_TOPIC,
                    properties);
            topicNode = association.getChildRef();
        }

        // Add necessary aspects
        properties.clear();
        properties.put(ApplicationModel.PROP_ICON, "topic");
        getNodeService().addAspect(topicNode, ApplicationModel.ASPECT_UIFACETS, properties);

        return topicNode;
    }
}
