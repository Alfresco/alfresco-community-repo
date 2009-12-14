/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.springframework.extensions.surf.util.Pair;
import org.springframework.extensions.surf.util.PropertyCheck;
import org.alfresco.util.VmShutdownListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.quartz.JobExecutionException;

/**
 * The post lookup component is responsible for updating posts that require a secondary lookup (to get additional activity data)
 */
public class PostLookup
{
    private static Log logger = LogFactory.getLog(PostLookup.class);
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(PostLookup.class.getName());
    
    private ActivityPostDAO postDAO;
    private NodeService nodeService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private PersonService personService;
    private TenantService tenantService;
    
    public void setPostDAO(ActivityPostDAO postDAO)
    {
        this.postDAO = postDAO;
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
    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Perform basic checks to ensure that the necessary dependencies were injected.
     */
    private void checkProperties()
    {
        PropertyCheck.mandatory(this, "postDAO", postDAO);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "transactionService", transactionService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
    }
    
    public void execute() throws JobExecutionException
    {
        checkProperties();
        try
        {
            ActivityPostEntity params = new ActivityPostEntity();
            params.setStatus(ActivityPostEntity.STATUS.PENDING.toString());
            
            List<ActivityPostEntity> activityPosts = postDAO.selectPosts(params);
            
            if (activityPosts.size() > 0)
            {
                logger.info("Update: " + activityPosts.size() + " activity posts");
            }
            
            for (final ActivityPostEntity activityPost : activityPosts)
            {
                try
                {
                    postDAO.startTransaction();
                    
                    final JSONObject jo = new JSONObject(new JSONTokener(activityPost.getActivityData()));
                    final String postUserId = activityPost.getUserId();
                    
                    // MT share
                    String tenantDomain = tenantService.getUserDomain(postUserId);
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            if (! jo.isNull("nodeRef"))
                            {
                                String nodeRefStr = jo.getString("nodeRef");
                                NodeRef nodeRef = new NodeRef(nodeRefStr);
                                
                                // lookup additional node data
                                JSONObject activityData = lookupNode(nodeRef, postUserId, jo);
                                
                                activityPost.setActivityData(activityData.toString());
                            }
                            else
                            {
                                // lookup additional person data
                                Pair<String, String> firstLastName = lookupPerson(postUserId);
                                if (firstLastName != null)
                                {
                                    jo.put("firstName", firstLastName.getFirst());
                                    jo.put("lastName", firstLastName.getSecond());
                                    
                                    activityPost.setActivityData(jo.toString());
                                }
                            }

                            activityPost.setLastModified(new Date());
                            
                            postDAO.updatePost(activityPost.getId(), activityPost.getSiteNetwork(), activityPost.getActivityData(), ActivityPostEntity.STATUS.POSTED);
                            if (logger.isDebugEnabled())
                            {
                                activityPost.setStatus(ActivityPostEntity.STATUS.POSTED.toString()); // for debug output
                                logger.debug("Updated: " + activityPost);
                            }
                            
                            return null;
                        }
                    }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
                    
                    postDAO.commitTransaction();
                } 
                catch (JSONException e)
                {
                    // log error, but consume exception (skip this post)
                    logger.error("Skipping activity post " + activityPost.getId() + ": " + e);
                    postDAO.updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    
                    postDAO.commitTransaction();
                }
                catch (SQLException e)
                {
                    logger.error("Exception during update of post", e);
                    throw new JobExecutionException(e);
                }
                finally 
                { 
                    postDAO.endTransaction();
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
    
    private Pair<String, String> lookupPerson(final String postUserId) throws JSONException
    {
        RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
        
        // wrap to make the request in a transaction
        RetryingTransactionCallback<Pair<String, String>> lookup = new RetryingTransactionCallback<Pair<String, String>>()
        {
            public Pair<String, String> execute() throws Throwable
            {
                String firstName = "";
                String lastName = "";
                
                if (personService.personExists(postUserId))
                {
                   NodeRef personRef = personService.getPerson(postUserId);
                   
                   firstName = (String)nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                   lastName = (String)nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
                   
                   return new Pair<String, String>(firstName, lastName);
                }

                return null;
            }
        };
        
        // execute in txn
        return txnHelper.doInTransaction(lookup, true);
    }
    
    private JSONObject lookupNode(final NodeRef nodeRef, final String postUserId, final JSONObject jo) throws JSONException
    {
        return AuthenticationUtil.runAs(new RunAsWork<JSONObject>()
        {
            public JSONObject doWork() throws Exception
            {
                RetryingTransactionHelper txnHelper = transactionService.getRetryingTransactionHelper();
                
                // wrap to make the request in a transaction
                RetryingTransactionCallback<JSONObject> lookup = new RetryingTransactionCallback<JSONObject>()
                {
                    public JSONObject execute() throws Throwable
                    {
                        String name = "";
                        if (! jo.isNull("name"))
                        {
                            name = jo.getString("name");
                        }
                        
                        NodeRef parentNodeRef = null;
                        if (! jo.isNull("parentNodeRef"))
                        {
                            parentNodeRef = new NodeRef(jo.getString("parentNodeRef"));
                        }
                        
                        
                        String typeQName = "";
                        if (! jo.isNull("typeQName"))
                        {
                            typeQName = jo.getString("typeQName");
                        }
                        
                        String displayPath = "";
                        Path path = null;
                        String firstName = "";
                        String lastName = "";
                        
                        if (personService.personExists(postUserId))
                        {
                           // lookup firstname, lastname
                           NodeRef personRef = personService.getPerson(postUserId);
                           
                           firstName = (String)nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
                           lastName = (String)nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
                        }
                        
                        if ((nodeRef != null) && (nodeService.exists(nodeRef)))
                        {
                            if (name.length() == 0)
                            {
                                // lookup node name
                                name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
                            }
                            
                            if (typeQName.length() == 0)
                            {
                                // lookup type
                                typeQName = nodeService.getType(nodeRef).toPrefixString(); // TODO: missing the prefix ?
                            }
                            
                            if (parentNodeRef == null)
                            {
                                // lookup parent node
                                parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
                            }
                        }
                        
                        if ((parentNodeRef != null) && (nodeService.exists(parentNodeRef)))
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
                        
                        // merge with existing activity data
                        jo.put("name", name);
                        jo.put("nodeRef", nodeRef.toString());
                        jo.put("typeQName", typeQName);
                        jo.put("parentNodeRef", (parentNodeRef != null ? parentNodeRef.toString() : null));
                        jo.put("displayPath", displayPath);
                        jo.put("firstName", firstName);
                        jo.put("lastName", lastName);

                        return jo;
                    }
                };
                
                // execute in txn
                return txnHelper.doInTransaction(lookup, true);
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}