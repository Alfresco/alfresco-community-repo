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
package org.alfresco.repo.forum;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;

import org.alfresco.model.ContentModel;
import org.alfresco.model.ForumModel;
import org.alfresco.query.CannedQueryFactory;
import org.alfresco.query.CannedQueryResults;
import org.alfresco.query.EmptyPagingResults;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQueryFactory;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.registry.NamedObjectRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;

/**
 * @author Neil Mc Erlean
 * @since 4.0
 */
// TODO consolidate this and ScriptCommentService and the implementations of comments.* web scripts.
public class CommentServiceImpl extends AbstractLifecycleBean implements CommentService, 
        NodeServicePolicies.BeforeDeleteNodePolicy, 
        NodeServicePolicies.OnUpdatePropertiesPolicy
{
    private static Log logger = LogFactory.getLog(CommentServiceImpl.class);  

    /**
     * Naming convention for Share comment model. fm:forum contains fm:topic
     */
    private static final QName FORUM_TO_TOPIC_ASSOC_QNAME = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "Comments");
    private static final String COMMENTS_TOPIC_NAME = "Comments";

    private static final String CANNED_QUERY_GET_CHILDREN = "commentsGetChildrenCannedQueryFactory";
    
    // Injected services
    private NodeService nodeService;
    private ContentService contentService;
    private ActivityService activityService;
    private SiteService siteService;
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;
    private PermissionService permissionService;
    private LockService lockService;
    private DictionaryService dictionaryService;
    
    private NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry;

	public void setSiteService(SiteService siteService)
    {
		this.siteService = siteService;
	}

	public void setActivityService(ActivityService activityService)
    {
		this.activityService = activityService;
	}
    
	public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

	public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    public void setCannedQueryRegistry(NamedObjectRegistry<CannedQueryFactory<? extends Object>> cannedQueryRegistry)
    {
		this.cannedQueryRegistry = cannedQueryRegistry;
	}

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public void setLockService(LockService lockService)
    {
        this.lockService = lockService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected void onBootstrap(ApplicationEvent event)
    {
        // belts-and-braces (in case of broken spring-bean override)
        ApplicationContext ctx = getApplicationContext();
        if (ctx != null)
        {
            if (this.nodeService == null)
            {
                this.nodeService = (NodeService)ctx.getBean("NodeService");
            }
            if (this.contentService == null)
            {
                this.contentService = (ContentService)ctx.getBean("ContentService");
            }
            if (this.siteService == null)
            {
                this.siteService = (SiteService)ctx.getBean("SiteService");
            }
            if (this.activityService == null)
            {
                this.activityService = (ActivityService)ctx.getBean("activityService");
            }
            if (this.cannedQueryRegistry == null)
            {
                this.cannedQueryRegistry = (NamedObjectRegistry<CannedQueryFactory<? extends Object>>)ctx.getBean("commentsCannedQueryRegistry");
            }
            if (this.policyComponent == null)
            {
                this.policyComponent = (PolicyComponent)ctx.getBean("policyComponent");
            }
            if (this.behaviourFilter == null)
            {
                this.behaviourFilter = (BehaviourFilter)ctx.getBean("policyBehaviourFilter");
            }
            if (this.permissionService == null)
            {
                this.permissionService = (PermissionService)ctx.getBean("PermissionService");
            }
            if (this.lockService == null)
            {
                this.lockService = (LockService)ctx.getBean("LockService");
            }
            if (this.dictionaryService == null)
            {
                this.dictionaryService = (DictionaryService)ctx.getBean("DictionaryService");
            }
        }
        
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
                ForumModel.TYPE_POST,
                new JavaBehaviour(this, "onUpdateProperties"));
        this.policyComponent.bindClassBehaviour(
                NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
                ForumModel.TYPE_POST,
                new JavaBehaviour(this, "beforeDeleteNode"));
    }

    @Override
    protected void onShutdown(ApplicationEvent event)
    {
    }

    @Override
    public NodeRef getDiscussableAncestor(NodeRef descendantNodeRef)
    {
        // For "Share comments" i.e. fm:post nodes created via the Share commenting UI, the containment structure is as follows:
        // fm:discussable
        //    - fm:forum
        //        - fm:topic
        //            - fm:post
        // For other fm:post nodes the ancestor structure may be slightly different. (cf. Share discussions, which don't have 'forum')
        //
        // In order to ensure that we only return the discussable ancestor relevant to Share comments, we'll climb the
        // containment tree in a controlled manner.
        // We could navigate up looking for the first fm:discussable ancestor, but that might not find the correct node - it could,
        // for example, find a parent folder which was discussable.
        
        NodeRef result = null;
        if (nodeService.getType(descendantNodeRef).equals(ForumModel.TYPE_POST))
        {
            NodeRef topicNode = nodeService.getPrimaryParent(descendantNodeRef).getParentRef();
            
            if (nodeService.getType(topicNode).equals(ForumModel.TYPE_TOPIC))
            {
                ChildAssociationRef forumToTopicChildAssocRef = nodeService.getPrimaryParent(topicNode);

                if (forumToTopicChildAssocRef.getQName().equals(FORUM_TO_TOPIC_ASSOC_QNAME))
                {
                    NodeRef forumNode = forumToTopicChildAssocRef.getParentRef();
                    
                    if (nodeService.getType(forumNode).equals(ForumModel.TYPE_FORUM))
                    {
                        NodeRef discussableNode = nodeService.getPrimaryParent(forumNode).getParentRef();
    
                        if (nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
                        {
                            result = discussableNode;
                        }
                    }
                }
            }
        }
        
        return result;
    }

    @Override
    public PagingResults<NodeRef> listComments(NodeRef discussableNode, PagingRequest paging)
    {
    	NodeRef commentsFolder = getShareCommentsTopic(discussableNode);
    	if(commentsFolder != null)
    	{
    		List<Pair<QName,Boolean>> sort = new ArrayList<Pair<QName,Boolean>>();
    		sort.add(new Pair<QName, Boolean>(ContentModel.PROP_CREATED, false));

	        // Run the canned query
	        GetChildrenCannedQueryFactory getChildrenCannedQueryFactory = (GetChildrenCannedQueryFactory)cannedQueryRegistry.getNamedObject(CANNED_QUERY_GET_CHILDREN);
	        GetChildrenCannedQuery cq = (GetChildrenCannedQuery)getChildrenCannedQueryFactory.getCannedQuery(commentsFolder, null, null, null, null, sort, paging);
            
            // Execute the canned query
            final CannedQueryResults<NodeRef> results = cq.execute();

            // Now convert the CannedQueryResults<NodeRef> into a more useful PagingResults<NodeRef>
            List<NodeRef> comments = Collections.emptyList();

            if (results.getPageCount() > 0)
            {
                comments = results.getPages().get(0);
            }

            // set total count
            final Pair<Integer, Integer> totalCount;
            if (paging.getRequestTotalCountMax() > 0)
            {
                totalCount = results.getTotalResultCount();
            }
            else
            {
                totalCount = null;
            }

            final List<NodeRef> page = new ArrayList<NodeRef>(comments.size());
            for (NodeRef comment : comments)
            {
                page.add(comment);
            }

            return new PagingResults<NodeRef>()
            {
                @Override
                public String getQueryExecutionId()
                {
                    return results.getQueryExecutionId();
                }

                @Override
                public List<NodeRef> getPage()
                {
                    return page;
                }

                @Override
                public boolean hasMoreItems()
                {
                    return results.hasMoreItems();
                }

                @Override
                public Pair<Integer, Integer> getTotalResultCount()
                {
                    return totalCount;
                }
            };
        }
        else
        {
            return new EmptyPagingResults<NodeRef>();
        }
    }
    
    @Override
    public NodeRef getShareCommentsTopic(NodeRef discussableNode)
    {
        NodeRef result = null;
        
        if (nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
        {
            // We navigate down the "Share comments" containment model, which is based on the more general forum model,
            // but with certain naming conventions.
            List<ChildAssociationRef> fora = nodeService.getChildAssocs(discussableNode, ForumModel.ASSOC_DISCUSSION, ForumModel.ASSOC_DISCUSSION, true);
            
            // There should only be one such assoc.
            if ( !fora.isEmpty())
            {
                final NodeRef firstForumNode = fora.get(0).getChildRef();
                List<ChildAssociationRef> topics = nodeService.getChildAssocs(firstForumNode, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME, true);
                
                // Likewise, only one.
                if ( !topics.isEmpty())
                {
                    final NodeRef firstTopicNode = topics.get(0).getChildRef();
                    result = firstTopicNode;
                }
            }
        }
        
        return result;
    }

//    private ScriptNode createCommentsFolder(ScriptNode node)
//    {
//        final NodeRef nodeRef = node.getNodeRef();
//        
//        NodeRef commentsFolder = AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<NodeRef>()
//        {
//            public NodeRef doWork() throws Exception
//            {
//                NodeRef commentsFolder = null;
//                
//                // ALF-5240: turn off auditing round the discussion node creation to prevent
//                // the source document from being modified by the first user leaving a comment
//                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
//                
//                try
//                {
//                    nodeService.addAspect(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussable"), null);
//                    List<ChildAssociationRef> assocs = nodeService.getChildAssocs(nodeRef, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion"), RegexQNamePattern.MATCH_ALL);
//                    if (assocs.size() != 0)
//                    {
//                        NodeRef forumFolder = assocs.get(0).getChildRef();
//                        
//                        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
//                        props.put(ContentModel.PROP_NAME, COMMENTS_TOPIC_NAME);
//                        commentsFolder = nodeService.createNode(
//                                forumFolder,
//                                ContentModel.ASSOC_CONTAINS, 
//                                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, COMMENTS_TOPIC_NAME), 
//                                QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "topic"),
//                                props).getChildRef();
//                    }
//                }
//                finally
//                {
//                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
//                }
//                
//                return commentsFolder;
//            }
//    
//        }, AuthenticationUtil.getSystemUserName()); 
//        
//        return new ScriptNode(commentsFolder, serviceRegistry, getScope());
//    }
    
    private String getSiteId(final NodeRef nodeRef)
    {
        String siteId = AuthenticationUtil.runAsSystem(new RunAsWork<String>()
        {
			@Override
			public String doWork() throws Exception
			{
				return siteService.getSiteShortName(nodeRef);
			}
        });

        return siteId;
    }

    @SuppressWarnings("unchecked")
	private JSONObject getActivityData(String siteId, final NodeRef nodeRef)
    {
        if(siteId != null)
        {
	        // create an activity (with some Share-specific parts)
	        JSONObject json = new JSONObject();
	        json.put("title", nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
			try
			{
				StringBuilder sb = new StringBuilder("document-details?nodeRef=");
				sb.append(URLEncoder.encode(nodeRef.toString(), "UTF-8"));
				json.put("page", sb.toString());
				// MNT-11667 "createComment" method creates activity for users who are not supposed to see the file
				json.put("nodeRef", nodeRef.toString());
			}
			catch (UnsupportedEncodingException e)
			{
				logger.warn("Unable to urlencode page for create comment activity");
			}

			return json;
        }
        else
        {
        	logger.warn("Unable to determine site in which node " + nodeRef + " resides.");
        	return null;
        }
    }
    
	private void postActivity(String siteId, String activityType, JSONObject activityData)
    {
		if(activityData != null)
		{
			activityService.postActivity(activityType, siteId, "comments", activityData.toString());
		}
    }

	@Override
    public NodeRef createComment(final NodeRef discussableNode, String title, String comment, boolean suppressRollups)
    {
    	if(comment == null)
    	{
    		throw new IllegalArgumentException("Must provide a non-null comment");
    	}

        // There is no CommentService, so we have to create the node structure by hand.
        // This is what happens within e.g. comment.put.json.js when comments are submitted via the REST API.
        if (!nodeService.hasAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE))
        {
            nodeService.addAspect(discussableNode, ForumModel.ASPECT_DISCUSSABLE, null);
        }
        if (!nodeService.hasAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP) && !suppressRollups)
        {
            nodeService.addAspect(discussableNode, ForumModel.ASPECT_COMMENTS_ROLLUP, null);
        }
        // Forum node is created automatically by DiscussableAspect behaviour.
        NodeRef forumNode = nodeService.getChildAssocs(discussableNode, ForumModel.ASSOC_DISCUSSION, QName.createQName(NamespaceService.FORUMS_MODEL_1_0_URI, "discussion")).get(0).getChildRef();
        
        final List<ChildAssociationRef> existingTopics = nodeService.getChildAssocs(forumNode, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME);
        NodeRef topicNode = null;
        if (existingTopics.isEmpty())
        {
            Map<QName, Serializable> props = new HashMap<QName, Serializable>(1, 1.0f);
            props.put(ContentModel.PROP_NAME, COMMENTS_TOPIC_NAME);
            topicNode = nodeService.createNode(forumNode, ContentModel.ASSOC_CONTAINS, FORUM_TO_TOPIC_ASSOC_QNAME, ForumModel.TYPE_TOPIC, props).getChildRef();
        }
        else
        {
            topicNode = existingTopics.get(0).getChildRef();
        }

        NodeRef postNode = nodeService.createNode(topicNode, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS, ForumModel.TYPE_POST).getChildRef();
        nodeService.setProperty(postNode, ContentModel.PROP_CONTENT, new ContentData(null, MimetypeMap.MIMETYPE_TEXT_PLAIN, 0L, null));
        nodeService.setProperty(postNode, ContentModel.PROP_TITLE, title);
        ContentWriter writer = contentService.getWriter(postNode, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_HTML);
        writer.setEncoding("UTF-8");
        writer.putContent(comment);

    	// determine the siteId and activity data of the comment NodeRef
        String siteId = getSiteId(discussableNode);
        JSONObject activityData = getActivityData(siteId, discussableNode);

        postActivity(siteId, ActivityType.COMMENT_CREATED, activityData);

        return postNode;
    }

    public void updateComment(NodeRef commentNodeRef, String title, String comment)
    {
    	QName nodeType = nodeService.getType(commentNodeRef);
    	if(!nodeType.equals(ForumModel.TYPE_POST))
    	{
    		throw new IllegalArgumentException("Node to update is not a comment node.");
    	}
        
        try
        {
            ContentWriter writer = contentService.getWriter(commentNodeRef, ContentModel.PROP_CONTENT, true);
            writer.setMimetype(MimetypeMap.MIMETYPE_HTML); // TODO should this be set by the caller?
            writer.putContent(comment);
        }
        catch (ContentIOException cioe)
        {
            Throwable cause = cioe.getCause();
            if (cause instanceof AccessDeniedException)
            {
                throw (AccessDeniedException)cause;
            }
            else if (cause instanceof NodeLockedException)
            {
                throw (NodeLockedException)cause;
            }
            else
            {
                throw cioe;
            }
        }

    	if(title != null)
    	{
    		nodeService.setProperty(commentNodeRef, ContentModel.PROP_TITLE, title);
    	}

    	// determine the siteId and activity data of the comment NodeRef
        String siteId = getSiteId(commentNodeRef);
        NodeRef discussableNodeRef = getDiscussableAncestor(commentNodeRef);
        if(discussableNodeRef != null)
        {
        	JSONObject activityData = getActivityData(siteId, discussableNodeRef);

        	postActivity(siteId, "org.alfresco.comments.comment-updated", activityData);
        }
        else
        {
        	logger.warn("Unable to determine discussable node for the comment with nodeRef " + commentNodeRef + ", not posting an activity");
        }
    }

    public void deleteComment(NodeRef commentNodeRef)
    {
    	QName nodeType = nodeService.getType(commentNodeRef);
    	if(!nodeType.equals(ForumModel.TYPE_POST))
    	{
    		throw new IllegalArgumentException("Node to delete is not a comment node.");
    	}

    	// determine the siteId and activity data of the comment NodeRef (do this before removing the comment NodeRef)
        String siteId = getSiteId(commentNodeRef);
        NodeRef discussableNodeRef = getDiscussableAncestor(commentNodeRef);
        JSONObject activityData = null;
        if(discussableNodeRef != null)
        {
        	activityData = getActivityData(siteId, discussableNodeRef);
        }

    	nodeService.deleteNode(commentNodeRef);

    	if(activityData != null)
    	{
    		postActivity(siteId, "org.alfresco.comments.comment-deleted", activityData);
    	}
        else
        {
        	logger.warn("Unable to determine discussable node for the comment with nodeRef " + commentNodeRef + ", not posting an activity");
        }
    }

    // TODO review against ACE-5437 (eg. introduce CommentInfo as part of the CommentService)
    public Map<String, Boolean> getCommentPermissions(NodeRef discussableNode, NodeRef commentNodeRef)
    {
        boolean canEdit = false;
        boolean canDelete = false;
        
        // belts-and-braces
        NodeRef discussableNodeRef = getDiscussableAncestor(commentNodeRef);
        if (discussableNodeRef.equals(discussableNode))
        {
            if (!isWorkingCopyOrLocked(discussableNode)
                    || isLockOwner(discussableNode))
            {
                canEdit = canEditPermission(commentNodeRef);
                canDelete = canDeletePermission(commentNodeRef);
            }
        }

        Map<String, Boolean> map = new HashMap<>(2);
        map.put(CommentService.CAN_EDIT, canEdit);
        map.put(CommentService.CAN_DELETE, canDelete);

        return map;
    }

    private boolean canEditPermission(NodeRef commentNodeRef)
    {
        String creator = (String)nodeService.getProperty(commentNodeRef, ContentModel.PROP_CREATOR);
        Serializable owner = nodeService.getProperty(commentNodeRef, ContentModel.PROP_OWNER);
        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();

        boolean isSiteManager = permissionService.hasPermission(commentNodeRef, SiteModel.SITE_MANAGER) == (AccessStatus.ALLOWED);
        boolean isCoordinator = permissionService.hasPermission(commentNodeRef, PermissionService.COORDINATOR) == (AccessStatus.ALLOWED);
        return (isSiteManager || isCoordinator || currentUser.equals(creator) || currentUser.equals(owner));
    }

    private boolean canDeletePermission(NodeRef commentNodeRef)
    {
        return permissionService.hasPermission(commentNodeRef, PermissionService.DELETE) == AccessStatus.ALLOWED;
    }

    private boolean isLockOwner(NodeRef nodeRef)
    {
        return lockService.getLockStatus(nodeRef) == LockStatus.LOCK_OWNER;
    }

    private boolean isWorkingCopyOrLocked(NodeRef nodeRef)
    {
        boolean isWorkingCopy = false;
        boolean isLocked = false;

        if (nodeRef != null)
        {
            Set<QName> aspects = nodeService.getAspects(nodeRef);

            isWorkingCopy = aspects.contains(ContentModel.ASPECT_WORKING_COPY);
            if (!isWorkingCopy)
            {
                if (aspects.contains(ContentModel.ASPECT_LOCKABLE))
                {
                    LockStatus lockStatus = lockService.getLockStatus(nodeRef);
                    if (lockStatus == LockStatus.LOCKED || lockStatus == LockStatus.LOCK_OWNER)
                    {
                        isLocked = true;
                    }
                }
            }
        }
        return (isWorkingCopy || isLocked);
    }

    @Override
    public void onUpdateProperties(NodeRef commentNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        NodeRef discussableNodeRef = getDiscussableAncestor(commentNodeRef);
        if (discussableNodeRef != null)
        {
            if (behaviourFilter.isEnabled(ContentModel.ASPECT_LOCKABLE)
                    && isWorkingCopyOrLocked(discussableNodeRef)
                    && !isLockOwner(discussableNodeRef))
            {
                List<QName> changedProperties = getChangedProperties(before, after);

                // check if comment updated (rather than some other change, eg. addition of lockable aspect only)
                boolean commentUpdated = false;
                for (QName changedProperty : changedProperties)
                {
                    PropertyDefinition propertyDef = dictionaryService.getProperty(changedProperty);
                    if (propertyDef != null)
                    {
                        if (propertyDef.getContainerClass().getName().equals(ContentModel.TYPE_CONTENT))
                        {
                            commentUpdated = true;
                            break;
                        }
                    }
                }

                if (commentUpdated)
                {
                    throw new NodeLockedException(discussableNodeRef);
                }
            }

            boolean canEdit = canEditPermission(commentNodeRef);
            if (! canEdit)
            {
                throw new AccessDeniedException("Cannot edit comment");
            }
        }
    }

    // see also RenditionedAspect
    private List<QName> getChangedProperties(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        List<QName> results = new ArrayList<QName>();
        for (QName propQName : before.keySet())
        {
            if (after.keySet().contains(propQName) == false)
            {
                // property was deleted
                results.add(propQName);
            }
            else
            {
                Serializable beforeValue = before.get(propQName);
                Serializable afterValue = after.get(propQName);
                if (EqualsHelper.nullSafeEquals(beforeValue, afterValue) == false)
                {
                    // Property was changed
                    results.add(propQName);
                }
            }
        }
        for (QName propQName : after.keySet())
        {
            if (before.containsKey(propQName) == false)
            {
                // property was added
                results.add(propQName);
            }
        }

        return results;
    }

    @Override
    public void beforeDeleteNode(final NodeRef commentNodeRef)
    {
        NodeRef discussableNodeRef = getDiscussableAncestor(commentNodeRef);
        if (discussableNodeRef != null)
        {
            boolean canDelete = canDeletePermission(commentNodeRef);
            if (behaviourFilter.isEnabled(ContentModel.ASPECT_LOCKABLE) // eg. delete site (MNT-14671)
                    && isWorkingCopyOrLocked(discussableNodeRef)
                    && !isLockOwner(discussableNodeRef)
                    && canDelete)
            {
                throw new NodeLockedException(discussableNodeRef);
            }

            if (! canDelete)
            {
                throw new AccessDeniedException("Cannot delete comment");
            }
        }
    }
}
