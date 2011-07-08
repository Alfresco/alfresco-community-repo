/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.subscriptions;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.domain.subscriptions.SubscriptionsDAO;
import org.alfresco.repo.search.SearcherException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.subscriptions.PagingFollowingResults;
import org.alfresco.service.cmr.subscriptions.PagingSubscriptionResults;
import org.alfresco.service.cmr.subscriptions.PrivateSubscriptionListException;
import org.alfresco.service.cmr.subscriptions.SubscriptionItemTypeEnum;
import org.alfresco.service.cmr.subscriptions.SubscriptionService;
import org.alfresco.service.cmr.subscriptions.SubscriptionsDisabledException;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;

public class SubscriptionServiceImpl implements SubscriptionService
{
    /** Logger */
    private static Log logger = LogFactory.getLog(SubscriptionServiceImpl.class);

    /** Activity tool */
    private static final String ACTIVITY_TOOL = "subscriptionService";

    /** Activity values */
    private static final String FOLLOWER_FIRSTNAME = "followerFirstName";
    private static final String FOLLOWER_LASTNAME = "followerLastName";
    private static final String FOLLOWER_USERNAME = "followerUserName";
    private static final String FOLLOWER_JOBTITLE = "followerJobTitle";
    private static final String USER_FIRSTNAME = "userFirstName";
    private static final String USER_LASTNAME = "userLastName";
    private static final String USER_USERNAME = "userUserName";
    private static final String FOLLOWING_COUNT = "followingCount";
    private static final String FOLLOWER_COUNT = "followerCount";

    private static final String SUBSCRIBER_FIRSTNAME = "subscriberFirstName";
    private static final String SUBSCRIBER_LASTNAME = "subscriberLastName";
    private static final String SUBSCRIBER_USERNAME = "subscriberUserName";
    private static final String NODE = "node";

    protected SubscriptionsDAO subscriptionsDAO;
    protected NodeService nodeService;
    protected PersonService personService;
    protected ActivityService activityService;
    protected AuthorityService authorityService;
    protected ActionService actionService;
    protected SearchService searchService;
    protected NamespaceService namespaceService;
    protected FileFolderService fileFolderService;

    /**
     * Sets the subscriptions DAO.
     */
    public void setSubscriptionsDAO(SubscriptionsDAO subscriptionsDAO)
    {
        this.subscriptionsDAO = subscriptionsDAO;
    }

    /**
     * Sets the node service.
     */
    public final void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the person service.
     */
    public final void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    /**
     * Sets the activity service.
     */
    public final void setActivityService(ActivityService activictyService)
    {
        this.activityService = activictyService;
    }

    /**
     * Sets the authority service.
     */
    public final void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Sets the action service.
     */
    public final void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Set the search service.
     */
    public final void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Set the namespace service.
     */
    public final void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Set the fileFolder service.
     */
    public final void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    @Override
    public PagingSubscriptionResults getSubscriptions(String userId, SubscriptionItemTypeEnum type,
            PagingRequest pagingRequest)
    {
        checkEnabled();
        checkRead(userId, true);
        return subscriptionsDAO.selectSubscriptions(userId, type, pagingRequest);
    }

    @Override
    public int getSubscriptionCount(String userId, SubscriptionItemTypeEnum type)
    {
        checkEnabled();
        checkRead(userId, true);
        return subscriptionsDAO.countSubscriptions(userId, type);
    }

    @Override
    public void subscribe(String userId, NodeRef node)
    {
        checkEnabled();
        checkWrite(userId);
        checkUserNode(node);
        subscriptionsDAO.insertSubscription(userId, node);

        if (userId.equalsIgnoreCase(AuthenticationUtil.getRunAsUser()))
        {
            String activityDataJSON = null;
            try
            {
                NodeRef subscriberNode = personService.getPerson(userId, false);
                JSONObject activityData = new JSONObject();
                activityData.put(SUBSCRIBER_USERNAME, userId);
                activityData.put(SUBSCRIBER_FIRSTNAME,
                        nodeService.getProperty(subscriberNode, ContentModel.PROP_FIRSTNAME));
                activityData.put(SUBSCRIBER_LASTNAME,
                        nodeService.getProperty(subscriberNode, ContentModel.PROP_LASTNAME));
                activityData.put(NODE, node.toString());
                activityDataJSON = activityData.toString();
            } catch (JSONException je)
            {
                // log error, subsume exception
                logger.error("Failed to get activity data: " + je);
            }

            activityService.postActivity(ActivityType.SUBSCRIPTIONS_SUBSCRIBE, null, ACTIVITY_TOOL, activityDataJSON);
        }
    }

    @Override
    public void unsubscribe(String userId, NodeRef node)
    {
        checkEnabled();
        checkWrite(userId);
        subscriptionsDAO.deleteSubscription(userId, node);
    }

    @Override
    public boolean hasSubscribed(String userId, NodeRef node)
    {
        checkEnabled();
        checkRead(userId, true);
        return subscriptionsDAO.hasSubscribed(userId, node);
    }

    @Override
    public PagingFollowingResults getFollowing(String userId, PagingRequest pagingRequest)
    {
        checkEnabled();
        checkRead(userId, true);
        return subscriptionsDAO.selectFollowing(userId, pagingRequest);
    }

    @Override
    public int getFollowingCount(String userId)
    {
        checkEnabled();
        checkRead(userId, true);
        return getSubscriptionCount(userId, SubscriptionItemTypeEnum.USER);
    }

    @Override
    public PagingFollowingResults getFollowers(String userId, PagingRequest pagingRequest)
    {
        checkEnabled();
        checkRead(userId, false);
        return subscriptionsDAO.selectFollowers(userId, pagingRequest);
    }

    @Override
    public int getFollowersCount(String userId)
    {
        checkEnabled();
        checkRead(userId, false);
        return subscriptionsDAO.countFollowers(userId);
    }

    @Override
    public void follow(String userId, String userToFollow)
    {
        checkEnabled();
        checkWrite(userId);
        subscriptionsDAO.insertSubscription(userId, getUserNodeRef(userToFollow));

        if (userId.equalsIgnoreCase(AuthenticationUtil.getRunAsUser()))
        {
            try
            {
                String activityDataJSON = null;

                NodeRef followerNode = personService.getPerson(userId, false);
                NodeRef userNode = personService.getPerson(userToFollow, false);
                JSONObject activityData = new JSONObject();
                activityData.put(FOLLOWER_USERNAME, userId);
                activityData
                        .put(FOLLOWER_FIRSTNAME, nodeService.getProperty(followerNode, ContentModel.PROP_FIRSTNAME));
                activityData.put(FOLLOWER_LASTNAME, nodeService.getProperty(followerNode, ContentModel.PROP_LASTNAME));
                activityData.put(USER_USERNAME, userToFollow);
                activityData.put(USER_FIRSTNAME, nodeService.getProperty(userNode, ContentModel.PROP_FIRSTNAME));
                activityData.put(USER_LASTNAME, nodeService.getProperty(userNode, ContentModel.PROP_LASTNAME));
                activityDataJSON = activityData.toString();

                activityService.postActivity(ActivityType.SUBSCRIPTIONS_FOLLOW, null, ACTIVITY_TOOL, activityDataJSON);

            } catch (JSONException je)
            {
                // log error, subsume exception
                logger.error("Failed to get activity data: " + je);
            }

            try
            {
                sendFollowingMail(userId, userToFollow);
            } catch (Exception e)
            {
                // log error, subsume exception
                logger.error("Failed to send following email: " + e);
            }
        }
    }

    @Override
    public void unfollow(String userId, String userToUnfollow)
    {
        checkEnabled();
        checkWrite(userId);
        subscriptionsDAO.deleteSubscription(userId, getUserNodeRef(userToUnfollow));
    }

    @Override
    public boolean follows(String userId, String userToFollow)
    {
        checkEnabled();
        checkRead(userId, true);
        return subscriptionsDAO.hasSubscribed(userId, getUserNodeRef(userToFollow));
    }

    @Override
    public void setSubscriptionListPrivate(String userId, boolean isPrivate)
    {
        checkEnabled();
        checkWrite(userId);
        nodeService.setProperty(getUserNodeRef(userId), ContentModel.PROP_SUBSCRIPTIONS_PRIVATE, isPrivate);
    }

    @Override
    public boolean isSubscriptionListPrivate(String userId)
    {
        checkEnabled();

        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        Serializable privateList = nodeService.getProperty(getUserNodeRef(userId),
                ContentModel.PROP_SUBSCRIPTIONS_PRIVATE);
        if (privateList == null)
        {
            return false;
        }

        if (privateList instanceof Boolean && !((Boolean) privateList).booleanValue())
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean subscriptionsEnabled()
    {
        return true;
    }

    /**
     * Checks if the subscription service is enabled.
     */
    protected void checkEnabled()
    {
        if (!subscriptionsEnabled())
        {
            throw new SubscriptionsDisabledException("subscription_service.err.disabled");
        }
    }

    /**
     * Checks if the current user is allowed to get subscription data.
     */
    protected void checkRead(String userId, boolean checkPrivate)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        if (!checkPrivate)
        {
            return;
        }

        String currentUser = AuthenticationUtil.getRunAsUser();
        if (currentUser == null)
        {
            throw new IllegalArgumentException("No current user!");
        }

        if (currentUser.equalsIgnoreCase(userId) || authorityService.isAdminAuthority(currentUser)
                || AuthenticationUtil.isRunAsUserTheSystemUser() || !isSubscriptionListPrivate(userId))
        {
            return;
        }

        throw new PrivateSubscriptionListException("subscription_service.err.private-list");
    }

    /**
     * Checks if the current user is allowed to get change data.
     */
    protected void checkWrite(String userId)
    {
        if (userId == null)
        {
            throw new IllegalArgumentException("User Id may not be null!");
        }

        String currentUser = AuthenticationUtil.getRunAsUser();
        if (currentUser == null)
        {
            throw new IllegalArgumentException("No current user!");
        }

        if (currentUser.equalsIgnoreCase(userId) || authorityService.isAdminAuthority(currentUser)
                || AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            return;
        }

        throw new AccessDeniedException("subscription_service.err.write-denied");
    }

    /**
     * Gets the user node ref from the user id.
     */
    protected NodeRef getUserNodeRef(String userId)
    {
        return personService.getPerson(userId, false);
    }

    /**
     * Checks if the node is a user node and throws an exception if it id not.
     */
    protected void checkUserNode(NodeRef nodeRef)
    {
        // we only support user-to-user subscriptions in this release
        if (!ContentModel.TYPE_USER.equals(nodeService.getType(nodeRef)))
        {
            throw new IllegalArgumentException("Only user nodes supported!");
        }
    }

    /**
     * Sends an email to the person that is followed.
     */
    protected void sendFollowingMail(String userId, String userToFollow)
    {
        NodeRef followerNode = personService.getPerson(userId, false);
        NodeRef userNode = personService.getPerson(userToFollow, false);

        Serializable emailFeedDisabled = nodeService.getProperty(userNode, ContentModel.PROP_EMAIL_FEED_DISABLED);
        if (emailFeedDisabled instanceof Boolean && ((Boolean) emailFeedDisabled).booleanValue())
        {
            // this user doesn't want to be notified
            return;
        }

        Serializable emailAddress = nodeService.getProperty(userNode, ContentModel.PROP_EMAIL);
        if (emailAddress == null)
        {
            // we can't send an email without email address
            return;
        }

        NodeRef templateNodeRef = getEmailTemplateRef();
        if (templateNodeRef == null)
        {
            // we can't send an email without template
            return;
        }

        // compile the mail subject
        String followerFullName = (nodeService.getProperty(followerNode, ContentModel.PROP_FIRSTNAME) + " " + nodeService
                .getProperty(followerNode, ContentModel.PROP_LASTNAME)).trim();
        String subjectText = I18NUtil.getMessage("subscription.notification.email.subject", followerFullName);

        Map<String, Object> model = new HashMap<String, Object>();

        model.put(FOLLOWER_USERNAME, userId);
        model.put(FOLLOWER_FIRSTNAME, nodeService.getProperty(followerNode, ContentModel.PROP_FIRSTNAME));
        model.put(FOLLOWER_LASTNAME, nodeService.getProperty(followerNode, ContentModel.PROP_LASTNAME));
        model.put(FOLLOWER_JOBTITLE, nodeService.getProperty(followerNode, ContentModel.PROP_JOBTITLE));
        model.put(USER_USERNAME, userToFollow);
        model.put(USER_FIRSTNAME, nodeService.getProperty(userNode, ContentModel.PROP_FIRSTNAME));
        model.put(USER_LASTNAME, nodeService.getProperty(userNode, ContentModel.PROP_LASTNAME));
        model.put(FOLLOWING_COUNT, -1);
        try
        {
            model.put(FOLLOWING_COUNT, getFollowingCount(userId));
        } catch (Exception e)
        {
        }
        model.put(FOLLOWER_COUNT, -1);
        try
        {
            model.put(FOLLOWER_COUNT, getFollowersCount(userId));
        } catch (Exception e)
        {
        }

        Action mail = actionService.createAction(MailActionExecuter.NAME);

        mail.setParameterValue(MailActionExecuter.PARAM_TO, emailAddress);
        mail.setParameterValue(MailActionExecuter.PARAM_SUBJECT, subjectText);
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, templateNodeRef);
        mail.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) model);

        actionService.executeAction(mail, null);
    }

    /**
     * Returns the NodeRef of the email template or <code>null</code> if the
     * template coudln't be found.
     */
    protected NodeRef getEmailTemplateRef()
    {
        // Find the following email template
        String xpath = "app:company_home/app:dictionary/app:email_templates/app:following/cm:following-email.html.ftl";
        try
        {
            NodeRef rootNodeRef = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
            List<NodeRef> nodeRefs = searchService.selectNodes(rootNodeRef, xpath, null, namespaceService, false);
            if (nodeRefs.size() > 1)
            {
                logger.error("Found too many email templates using: " + xpath);
                nodeRefs = Collections.singletonList(nodeRefs.get(0));
            } else if (nodeRefs.size() == 0)
            {
                logger.error("Cannot find the email template using " + xpath);
                return null;
            }
            // Now localise this
            NodeRef base = nodeRefs.get(0);
            NodeRef local = fileFolderService.getLocalizedSibling(base);
            return local;
        } catch (SearcherException e)
        {
            logger.error("Cannot find the email template!", e);
        }

        return null;
    }
}
