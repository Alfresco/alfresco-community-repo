/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.repo.activities.post.lookup;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.post.ActivityPostDAO;
import org.alfresco.repo.activities.post.ActivityPostDaoService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.JSONtoFmModel;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONStringer;
import org.quartz.JobExecutionException;

/**
 * The post lookup component is responsible for updating posts that require a secondary lookup (to get additional activity data)
 */
public class PostLookup
{
    private static Log logger = LogFactory.getLog(PostLookup.class);
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(PostLookup.class.getName());
    
    private ActivityPostDaoService postDaoService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private PersonService personService;
    
    public void setPostDaoService(ActivityPostDaoService postDaoService)
    {
        this.postDaoService = postDaoService;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }
    
    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "postDaoService", postDaoService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "personService", personService);
    }
    
    public void execute() throws JobExecutionException
    {
        checkProperties();
        try
        {
            ActivityPostDAO params = new ActivityPostDAO();
            params.setStatus(ActivityPostDAO.STATUS.PENDING.toString());
            
            List<ActivityPostDAO> activityPosts = postDaoService.selectPosts(params);
            
            if (activityPosts.size() > 0)
            {
                logger.info("Update: " + activityPosts.size() + " activity posts");
            }
            
            for (ActivityPostDAO activityPost : activityPosts)
            {             
                Map<String, Object> model = JSONtoFmModel.convertJSONObjectToMap(activityPost.getActivityData());
                
                String postUserId = activityPost.getUserId();
                
                String name = (String)model.get("name"); // can be null
                
                String nodeRefStr = (String)model.get("nodeRef"); // required
                NodeRef nodeRef = new NodeRef(nodeRefStr);
                
                String parentNodeRefStr = (String)model.get("parentNodeRef"); // optional
                NodeRef parentNodeRef = null;
                if (parentNodeRefStr != null)
                {
                    parentNodeRef = new NodeRef(parentNodeRefStr);
                }
                
                String typeQName = (String)model.get("typeQName");
                
                try
                {
                    postDaoService.startTransaction();
                    
                    Pair<String, String> siteNetworkActivityData = lookup(activityPost.getSiteNetwork(), nodeRef, name, typeQName, parentNodeRef, postUserId);
                    
                    activityPost.setSiteNetwork(siteNetworkActivityData.getFirst());
                    activityPost.setActivityData(siteNetworkActivityData.getSecond());
                    activityPost.setLastModified(new Date());
                    
                    postDaoService.updatePost(activityPost.getId(), activityPost.getSiteNetwork(), activityPost.getActivityData(), ActivityPostDAO.STATUS.POSTED);
                    if (logger.isDebugEnabled())
                    {
                        activityPost.setStatus(ActivityPostDAO.STATUS.POSTED.toString()); // for debug output
                        logger.debug("Updated: " + activityPost);
                    }
                    
                    postDaoService.commitTransaction();
                } 
                catch (JSONException e)
                {
                    // log error, but consume exception (skip this post)
                    logger.error(e);
                }
                catch (SQLException e)
                {
                    logger.error("Exception during update of post", e);
                    throw new JobExecutionException(e);
                }
                finally 
                { 
                    postDaoService.endTransaction();
                } 
            }
        }
        catch (SQLException e)
        {
            logger.error("Exception during select of posts", e);
            throw new JobExecutionException(e);
        }
        catch (Throwable e)
        {
            // If the VM is shutting down, then ignore
            if (vmShutdownListener.isVmShuttingDown())
            {
                // Ignore
            }
            else
            {
                logger.error("Exception during update of posts", e);
            }
        }
    }
    
    private Pair<String, String> lookup(final String networkIn, final NodeRef nodeRef, final String nameIn, final String typeQNameIn, final NodeRef parentNodeRef, final String postUserId) throws JSONException
    {
        return AuthenticationUtil.runAs(new RunAsWork<Pair<String, String>>()
        {
            public Pair<String, String> doWork() throws Exception
            {
                RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                
                // wrap to make the request in a transaction
                RetryingTransactionCallback<Pair<String, String>> lookup = new RetryingTransactionCallback<Pair<String, String>>()
                {
                    public Pair<String, String> execute() throws Throwable
                    {
                        String jsonString = null;
                        String displayPath = "";
                        String name = nameIn;
                        String network = networkIn;
                        String typeQName = typeQNameIn;
                        Path path = null;
                        String firstName = "";
                        String lastName = "";
                        
                        if (personService.personExists(postUserId))
                        {
                           NodeRef personRef = personService.getPerson(postUserId);
                           
                           firstName = (String)nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                           lastName = (String)nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
                        }
                        
                        if (((name == null) || (name.length() == 0)) && (nodeRef != null) && (nodeService.exists(nodeRef)))
                        {
                            // node exists, lookup node name
                            if ((name == null) || (name.length() == 0))
                            {
                                name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                            }
                            
                            path = nodeService.getPath(nodeRef);
                            
                            // TODO: missing the prefix ?
                            typeQName = nodeService.getType(nodeRef).toPrefixString();
                        }
                        
                        if (((path == null) || (path.size() == 0)) && (parentNodeRef != null) && (nodeService.exists(parentNodeRef)))
                        {
                            // parent node exists, lookup parent node path
                            path = nodeService.getPath(parentNodeRef);
                        }

                        
                        if (path != null)
                        {
                            // lookup display path
                            displayPath = path.toDisplayPath(nodeService, permissionService);
    
                            // note: for now, also tack on the node name
                            displayPath += "/" + name;
                        }
                        
                        if (name == null)
                        {
                            name = "";
                        }
                        
                        if (typeQName == null)
                        {
                            typeQName = "";
                        }
                        
                        // activity data
                        jsonString = new JSONStringer()
                            .object()
                            .key("name")
                            .value(name)
                            .key("nodeRef")
                            .value(nodeRef)
                            .key("typeQName")
                            .value(typeQName)
                            .key("displayPath")
                            .value(displayPath)
                            .key("firstName")
                            .value(firstName)
                            .key("lastName")
                            .value(lastName)
                        .endObject().toString();

                        return new Pair<String, String>(network, jsonString);
                    }
                };
                
                // execute in txn
                return txnHelper.doInTransaction(lookup, true);
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}