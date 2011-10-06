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
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
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
    private volatile boolean busy;
    
    public static final String JSON_NODEREF_LOOKUP = "nodeRefL"; // requires additional lookup
    
    public static final String JSON_NODEREF = "nodeRef";
    public static final String JSON_NODEREF_PARENT = "parentNodeRef";
    
    public static final String JSON_FIRSTNAME = "firstName";
    public static final String JSON_LASTNAME = "lastName";
    
    public static final String JSON_NAME = "name";
    public static final String JSON_TYPEQNAME = "typeQName";
    public static final String JSON_PARENT_NODEREF = "parentNodeRef";
    public static final String JSON_DISPLAY_PATH = "displayPath";
    
    public static final String JSON_TENANT_DOMAIN = "tenantDomain";
    
    
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
        if (busy)
        {
            logger.warn("Still busy ...");
            return;
        }
        
        busy = true;
        try
        {
            ActivityPostEntity params = new ActivityPostEntity();
            params.setStatus(ActivityPostEntity.STATUS.PENDING.toString());
            
            List<ActivityPostEntity> activityPosts = postDAO.selectPosts(params);
            
            if (activityPosts.size() > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Update: " + activityPosts.size() + " activity post"+(activityPosts.size() == 1 ? "s" : ""));
                }
            }
            
            for (final ActivityPostEntity activityPost : activityPosts)
            {
                final String postUserId = activityPost.getUserId();
                
                try
                {
                    // MT share
                    String tenantDomain = TenantService.DEFAULT_DOMAIN;
                    
                    final JSONObject jo = new JSONObject(new JSONTokener(activityPost.getActivityData()));
                    if (! jo.isNull(JSON_TENANT_DOMAIN))
                    {
                        tenantDomain = jo.getString(JSON_TENANT_DOMAIN);
                    }
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        public Object doWork() throws Exception
                        {
                            try
                            {
                                postDAO.startTransaction();
                                
                                String activityDataStr = null;
                                
                                if (! jo.isNull(JSON_NODEREF_LOOKUP))
                                {
                                    String nodeRefStr = jo.getString(JSON_NODEREF_LOOKUP);
                                    NodeRef nodeRef = new NodeRef(nodeRefStr);
                                    
                                    // lookup additional node data
                                    JSONObject activityData = lookupNode(nodeRef, postUserId, jo);
                                    activityDataStr = activityData.toString();
                                }
                                else
                                {
                                    // lookup additional person data
                                    Pair<String, String> firstLastName = lookupPerson(postUserId);
                                    if (firstLastName != null)
                                    {
                                        jo.put(JSON_FIRSTNAME, firstLastName.getFirst());
                                        jo.put(JSON_LASTNAME, firstLastName.getSecond());
                                        
                                        activityDataStr = jo.toString();
                                    }
                                }
                                
                                if (activityDataStr != null)
                                {
                                    activityPost.setActivityData(activityDataStr);
                                }
                                
                                if ((activityPost.getActivityData() != null) && (activityPost.getActivityData().length() > ActivityPostDAO.MAX_LEN_ACTIVITY_DATA))
                                {
                                    throw new IllegalArgumentException("Invalid activity data - exceeds " + ActivityPostDAO.MAX_LEN_ACTIVITY_DATA + " chars: " + activityPost.getActivityData());
                                }
                                
                                if ((activityPost.getSiteNetwork() != null) && (activityPost.getSiteNetwork().length() > ActivityPostDAO.MAX_LEN_SITE_ID))
                                {
                                    // belts-and-braces - should not get here since checked during post (and not modified)
                                    throw new IllegalArgumentException("Invalid siteId - exceeds " + ActivityPostDAO.MAX_LEN_SITE_ID + " chars: " + activityPost.getSiteNetwork());
                                }
                                
                                activityPost.setLastModified(new Date());
                                
                                postDAO.updatePost(activityPost.getId(), activityPost.getSiteNetwork(), activityPost.getActivityData(), ActivityPostEntity.STATUS.POSTED);
                                
                                if (logger.isDebugEnabled())
                                {
                                    activityPost.setStatus(ActivityPostEntity.STATUS.POSTED.toString()); // for debug output
                                    logger.debug("Updated: " + activityPost);
                                }
                                
                                postDAO.commitTransaction();
                            }
                            catch (IllegalArgumentException e)
                            {
                                // log error, but consume exception (skip this post)
                                logger.error("Skipping activity post " + activityPost.getId() + ": " + e);
                                postDAO.updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                                
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
                            
                            return null;
                        }
                    }, tenantService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));
                }
                catch (JSONException e)
                {
                    // log error, but consume exception (skip this post)
                    logger.error("Skipping activity post " + activityPost.getId() + ": " + e);
                    
                    try
                    {
                        postDAO.updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                        postDAO.commitTransaction();
                    }
                    finally
                    {
                        postDAO.endTransaction();
                    }
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
        finally
        {
            busy = false;
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
                        if (! jo.isNull(JSON_NAME))
                        {
                            name = jo.getString(JSON_NAME);
                        }
                        
                        NodeRef parentNodeRef = null;
                        if (! jo.isNull(JSON_PARENT_NODEREF))
                        {
                            parentNodeRef = new NodeRef(jo.getString(JSON_PARENT_NODEREF));
                        }
                        
                        
                        String typeQName = "";
                        if (! jo.isNull(JSON_TYPEQNAME))
                        {
                            typeQName = jo.getString(JSON_TYPEQNAME);
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
                        jo.put(JSON_NAME, name);
                        jo.put(JSON_NODEREF, nodeRef.toString());
                        jo.put(JSON_TYPEQNAME, typeQName);
                        jo.put(JSON_PARENT_NODEREF, (parentNodeRef != null ? parentNodeRef.toString() : null));
                        jo.put(JSON_DISPLAY_PATH, displayPath);
                        jo.put(JSON_FIRSTNAME, firstName);
                        jo.put(JSON_LASTNAME, lastName);
                        
                        return jo;
                    }
                };
                
                // execute in txn
                return txnHelper.doInTransaction(lookup, true);
            }
        }, AuthenticationUtil.getSystemUserName());
    }
}