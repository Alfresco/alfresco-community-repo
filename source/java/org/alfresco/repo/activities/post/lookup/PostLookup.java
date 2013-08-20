/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.domain.activities.ActivityPostDAO;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.lock.JobLockService;
import org.alfresco.repo.lock.JobLockService.JobLockRefreshCallback;
import org.alfresco.repo.lock.LockAcquisitionException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.PathUtil;
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
 * 
 * @author janv
 * @since 3.0
 */
public class PostLookup
{
    private static Log logger = LogFactory.getLog(PostLookup.class);
    
    private static VmShutdownListener vmShutdownListener = new VmShutdownListener(PostLookup.class.getName());
    
    /** The name of the lock used to ensure that post lookup does not run on more than one node at the same time */
    private static final QName LOCK_QNAME = QName.createQName(NamespaceService.SYSTEM_MODEL_1_0_URI, "ActivityPostLookup");
    
    /** The time this lock will persist in the database (60 sec but refreshed at regular intervals) */
    private static final long LOCK_TTL = 1000 * 60;
    
    private ActivityPostDAO postDAO;
    private NodeService nodeService;
    private PermissionService permissionService;
    private TransactionService transactionService;
    private PersonService personService;
    private TenantService tenantService;
    private SiteService siteService;
    private JobLockService jobLockService;
    
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
    
    // for Share
    public static final String JSON_TITLE = "title";
    public static final String JSON_PAGE = "page";
    
    private static Map<String, String> rollupTypes = new HashMap<String, String>(3);
    
    // note: consistent with Share 'groupActivitiesAt'
    private int rollupCount = 5;
    
    private int maxItemsPerCycle = 500;
    
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
    
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }
    
    public void setRollupCount(int rollupCount)
    {
        this.rollupCount = rollupCount;
    }
    
    public void setJobLockService(JobLockService jobLockService)
    {
        this.jobLockService = jobLockService;
    }
    
    public void setMaxItemsPerCycle(int maxItemsPerCycle)
    {
        this.maxItemsPerCycle = maxItemsPerCycle;
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
        
        rollupTypes.put(ActivityType.FILE_ADDED,   ActivityType.FILES_ADDED);
        rollupTypes.put(ActivityType.FILE_UPDATED, ActivityType.FILES_UPDATED);
        rollupTypes.put(ActivityType.FILE_DELETED, ActivityType.FILES_DELETED);
        
        rollupTypes.put(ActivityType.FOLDER_ADDED,   ActivityType.FOLDERS_ADDED);
        rollupTypes.put(ActivityType.FOLDER_DELETED, ActivityType.FOLDERS_DELETED);
    }
    
    public void execute() throws JobExecutionException
    {
        checkProperties();
        
        // Avoid running when in read-only mode
        if (!transactionService.getAllowWrite())
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Post lookup not running due to read-only server");
            }
            return;
        }

        long start = System.currentTimeMillis();
        String lockToken = null;
        LockCallback lockCallback =  new LockCallback();
        try
        {
            if (jobLockService != null)
            {
                lockToken = acquireLock(lockCallback);
            }
            
            ActivityPostEntity params = new ActivityPostEntity();
            params.setStatus(ActivityPostEntity.STATUS.PENDING.toString());
            
            if (logger.isDebugEnabled())
            {
                logger.debug("Selecting activity posts with status: " + ActivityPostEntity.STATUS.PENDING.toString());
            }

            // get all pending post (for this job run) 
            final List<ActivityPostEntity> activityPosts = postDAO.selectPosts(params, maxItemsPerCycle);
            
            if (activityPosts.size() > 0)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Update: " + activityPosts.size() + " activity post"+(activityPosts.size() == 1 ? "s" : ""));
                }
                
                // execute in READ txn
                transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Object>() 
                {
                    public Object execute() throws Throwable
                    {
                        // lookup any additional data
                        lookupPosts(activityPosts);
                        return null;
                    }
                }, true);
                
                // execute in WRITE txn 
                List<ActivityPostEntity> activityPostsToUpdate = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<List<ActivityPostEntity>>() 
                {
                    public List<ActivityPostEntity> execute() throws Throwable
                    {
                        // collapse (ie. rollup) and relevant posts
                        return rollupPosts(activityPosts);
                    }
                }, false);
                
                // update posts + status (note: will also add any new rolled-up posts)
                updatePosts(activityPostsToUpdate);
                
                if (logger.isInfoEnabled())
                {
                    int cnt = activityPostsToUpdate.size();
                    logger.info("Updated: " + cnt + " activity post"+(cnt == 1 ? "" : "s")+" (in "+(System.currentTimeMillis()-start)+" msecs)");
                }
            }
        }
        catch (LockAcquisitionException e)
        {
            // Job being done by another process
            if (logger.isDebugEnabled())
            {
                logger.debug("execute: Can't get lock. Assume post lookup job already underway: "+e);
            }
        }
        catch (SQLException e)
        {
            logger.error("Exception during select of posts: ", e);
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
                logger.error("Exception during update of posts: ", e);
            }
        }
        finally
        {
            releaseLock(lockCallback, lockToken);
        }
    }
    
    private class UserRollupActivity
    {
        private String userId;
        private String activityType;
        private NodeRef parentNodeRef;
        
        public UserRollupActivity(String userId, String activityType, NodeRef parentNodeRef)
        {
            this.userId = userId;
            this.activityType = activityType;
            this.parentNodeRef = parentNodeRef;
        }
        
        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((userId == null) ? 0 : userId.hashCode());
            result = prime * result + ((activityType == null) ? 0 : activityType.hashCode());
            result = prime * result + ((parentNodeRef == null) ? 0 : parentNodeRef.hashCode());
            return result;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj == null) return false;
            if (!(obj instanceof UserRollupActivity)) return false;
            UserRollupActivity that = (UserRollupActivity) obj;
            return this.userId.equals(that.userId) && this.activityType.equals(that.activityType) && this.parentNodeRef.equals(that.parentNodeRef);
        }
    }
    
    private List<ActivityPostEntity> lookupPosts(final List<ActivityPostEntity> activityPosts)
    {
        for (final ActivityPostEntity activityPost : activityPosts)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Selected activity post: " + activityPost);
            }
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
                
                activityPost.setTenantDomain(tenantDomain);
                
                TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        JSONObject joLookup = null;
                        
                        if (! jo.isNull(JSON_NODEREF_LOOKUP))
                        {
                            String nodeRefStr = jo.getString(JSON_NODEREF_LOOKUP);
                            NodeRef nodeRef = new NodeRef(nodeRefStr);
                            
                            // lookup additional node data
                            joLookup = lookupNode(nodeRef, postUserId, jo);
                        }
                        else
                        {
                            // lookup poster's firstname/lastname (if needed)
                            if ((jo.isNull(JSON_FIRSTNAME)) || (jo.isNull(JSON_LASTNAME)))
                            {
                                Pair<String, String> firstLastName = lookupPerson(postUserId);
                                if (firstLastName != null)
                                {
                                    jo.put(JSON_FIRSTNAME, firstLastName.getFirst());
                                    jo.put(JSON_LASTNAME, firstLastName.getSecond());
                                    
                                    joLookup = jo;
                                }
                            }
                            
                            // lookup parent nodeRef (if needed)
                            NodeRef parentNodeRef = activityPost.getParentNodeRef();
                            if (parentNodeRef == null)
                            {
                                String parentNodeRefStr = null;
                                if (jo.isNull(JSON_PARENT_NODEREF))
                                {
                                    if (! jo.isNull(JSON_NODEREF))
                                    {
                                        parentNodeRef = lookupParentNodeRef(new NodeRef(jo.getString(JSON_NODEREF)));
                                        if (parentNodeRef != null)
                                        {
                                            parentNodeRefStr = parentNodeRef.toString();
                                            jo.put(JSON_PARENT_NODEREF, parentNodeRefStr);
                                            
                                            // note: currently only required during lookup/rollup
                                            //joLookup = jo;
                                        }
                                    }
                                }
                                else
                                {
                                    parentNodeRefStr = jo.getString(JSON_PARENT_NODEREF);
                                }
                                
                                if (parentNodeRefStr != null)
                                {
                                    activityPost.setParentNodeRef(new NodeRef(parentNodeRefStr));
                                }
                            }
                            
                            // lookup site (if needed)
                            String siteId = activityPost.getSiteNetwork();
                            if (siteId == null)
                            {
                                if (! jo.isNull(JSON_NODEREF))
                                {
                                    String nodeRefStr = jo.getString(JSON_NODEREF);
                                    if (nodeRefStr != null)
                                    {
                                        siteId = lookupSite(new NodeRef(nodeRefStr));
                                        activityPost.setSiteNetwork(siteId);
                                    }
                                }
                            }
                        }
                        
                        if (joLookup != null)
                        {
                            // extra data was looked-up
                            activityPost.setActivityData(joLookup.toString());
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
                        
                        return null;
                    }
                }, tenantDomain);
            }
            catch (Exception e)
            {
                // log error, but consume exception (skip this post)
                logger.error("Skipping activity post " + activityPost.getId() + ": " + e);
                
                activityPost.setStatus(ActivityPostEntity.STATUS.ERROR.toString());
            }
        }
        
        return activityPosts;
    }
    
    private List<ActivityPostEntity> rollupPosts(List<ActivityPostEntity> activityPosts) throws SQLException
    {
        Map<UserRollupActivity, List<ActivityPostEntity>> rollupPosts = new HashMap<UserRollupActivity, List<ActivityPostEntity>>();
        
        List<ActivityPostEntity> result = new ArrayList<ActivityPostEntity>(activityPosts.size());
        
        for (final ActivityPostEntity post : activityPosts)
        {
            if (rollupTypes.containsKey(post.getActivityType()) && (post.getParentNodeRef() != null))
            {
                UserRollupActivity key = new UserRollupActivity(post.getUserId(), post.getActivityType(), post.getParentNodeRef());
                List<ActivityPostEntity> posts = rollupPosts.get(key);
                if (posts == null)
                {
                    posts = new ArrayList<ActivityPostEntity>();
                    rollupPosts.put(key, posts);
                }
                posts.add(post);
            }
            else
            {
                result.add(post);
            }
        }
        
        for (final Map.Entry<UserRollupActivity, List<ActivityPostEntity>> entry : rollupPosts.entrySet())
        {
            final int count = entry.getValue().size();
            if (count >= rollupCount)
            {
                final ActivityPostEntity oldPost = entry.getValue().get(0);
                final String tenantDomain = oldPost.getTenantDomain();
                
                TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Void>()
                {
                    public Void doWork() throws Exception
                    {
                        String postUserId = oldPost.getUserId();
                        
                        // rollup - create a new 'posted' event that represents the rolled-up activity (and set others to 'processed')
                        ActivityPostEntity newPost = new ActivityPostEntity();
                        
                        newPost.setActivityType(rollupTypes.get(oldPost.getActivityType()));
                        newPost.setPostDate(oldPost.getPostDate());
                        newPost.setUserId(postUserId);
                        newPost.setSiteNetwork(oldPost.getSiteNetwork());
                        newPost.setAppTool(oldPost.getAppTool());
                        newPost.setLastModified(oldPost.getLastModified());
                        newPost.setTenantDomain(tenantDomain);
                        newPost.setJobTaskNode(1);
                        
                        try
                        {
                            JSONObject jo = new JSONObject();
                            jo.put(JSON_NODEREF_PARENT, oldPost.getParentNodeRef().toString());
                            jo.put(JSON_TENANT_DOMAIN, tenantDomain);
                            jo.put(JSON_TITLE, ""+count);
                            
                            Pair<String, String> firstLastName = lookupPerson(postUserId);
                            if (firstLastName != null)
                            {
                                jo.put(JSON_FIRSTNAME, firstLastName.getFirst());
                                jo.put(JSON_LASTNAME, firstLastName.getSecond());
                            }
                            
                            Path path = lookupPath(oldPost.getParentNodeRef());
                            if (path != null)
                            {
                                String displayPath = PathUtil.getDisplayPath(path, true);
                                if (displayPath != null)
                                {
                                    // note: PathUtil.getDisplayPath returns prefix path as: '/company_home/sites/' rather than /Company Home/Sites'
                                    String prefix = "/company_home/sites/"+tenantService.getBaseName(oldPost.getSiteNetwork())+"/documentLibrary";
                                    int idx = displayPath.indexOf(prefix);
                                    if (idx == 0)
                                    {
                                        displayPath = displayPath.substring(prefix.length());
                                    }
                                    
                                    // Share-specific
                                    jo.put(JSON_PAGE, "documentlibrary?path="+displayPath);
                                }
                            }
                            
                            newPost.setActivityData(jo.toString());
                            newPost.setStatus(ActivityPostEntity.STATUS.POSTED.toString());
                        }
                        catch (JSONException e)
                        {
                            logger.warn("Unable to create activity data: "+e);
                            newPost.setStatus(ActivityPostEntity.STATUS.ERROR.toString());
                        }
                        
                        for (ActivityPostEntity post : entry.getValue())
                        {
                            post.setStatus(ActivityPostEntity.STATUS.PROCESSED.toString());
                        }
                        
                        // add the new POSTED
                        entry.getValue().add(newPost);
                        
                        return null;
                    }
                }, tenantDomain);
            }
            
            result.addAll(entry.getValue());
        }
        
        return result;
    }
    
    
    private void updatePosts(List<ActivityPostEntity> activityPosts) throws SQLException
    {
        for (final ActivityPostEntity activityPost : activityPosts)
        {
            // MT share
            final String tenantDomain = activityPost.getTenantDomain();
            TenantUtil.runAsSystemTenant(new TenantUtil.TenantRunAsWork<Object>()
            {
                public Object doWork() throws Exception
                {
                    try
                    {
                        postDAO.startTransaction();
                        
                        ActivityPostEntity.STATUS status = ActivityPostEntity.STATUS.valueOf(activityPost.getStatus());
                        
                        switch (status)
                        {
                            case ERROR:
                            case PROCESSED:
                                postDAO.updatePostStatus(activityPost.getId(), status);
                                break;
                            case POSTED:
                                if (activityPost.getId() == null)
                                {
                                    // eg. rolled-up post
                                    postDAO.insertPost(activityPost);
                                }
                                break;
                            case PENDING:
                                postDAO.updatePost(activityPost.getId(), activityPost.getSiteNetwork(), activityPost.getActivityData(), ActivityPostEntity.STATUS.POSTED);
                                activityPost.setStatus(ActivityPostEntity.STATUS.POSTED.toString()); // for debug output
                                break;
                            default:
                                throw new Exception("Unexpected status: "+status);
                        }
                        
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Updated: " + activityPost);
                        }
                        
                        postDAO.commitTransaction();
                    }
                    catch (SQLException e)
                    {
                        logger.error("Exception during update of post: ", e);
                        throw new JobExecutionException(e);
                    }
                    catch (Exception e)
                    {
                        // log error, but consume exception (skip this post)
                        logger.error("Skipping activity post " + activityPost.getId() + ": " + e);
                        postDAO.updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                        
                        postDAO.commitTransaction();
                    }
                    finally
                    {
                        postDAO.endTransaction();
                    }
                    
                    return null;
                }
            }, tenantDomain);
        }
    }
    
    private Path lookupPath(final NodeRef nodeRef)
    {
        Path path = null;
        if ((nodeRef != null) && (nodeService.exists(nodeRef)))
        {
            path = nodeService.getPath(nodeRef);
        }
        return path;
    }
    
    private Pair<String, String> lookupPerson(final String postUserId) throws JSONException
    {
        Pair<String, String> result = null;
        if (personService.personExists(postUserId))
        {
           NodeRef personRef = personService.getPerson(postUserId);
           
           String firstName = (String)nodeService.getProperty(personRef, ContentModel.PROP_FIRSTNAME);
           String lastName = (String)nodeService.getProperty(personRef, ContentModel.PROP_LASTNAME);
           
           result = new Pair<String, String>(firstName, lastName);
        }
        return result;
    }
    
    private NodeRef lookupParentNodeRef(final NodeRef nodeRef) throws JSONException
    {
        NodeRef parentNodeRef = null;
        if (nodeService.exists(nodeRef))
        {
            parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        }
        return parentNodeRef;
    }
    
    private String lookupSite(final NodeRef nodeRef) throws JSONException
    {
        String siteId = null;
        if (nodeService.exists(nodeRef))
        {
            siteId = siteService.getSiteShortName(nodeRef);
        }
        return siteId;
    }
    
    /**
     * Generic node lookup - note: not currently used (see ActivityService.postActivity when activityData is not supplied)
     */
    private JSONObject lookupNode(final NodeRef nodeRef, final String postUserId, final JSONObject jo) throws JSONException
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
        
        if (parentNodeRef != null)
        {
            // lookup parent node path (if node exists)
            path = lookupPath(parentNodeRef);
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
    
    private class LockCallback implements JobLockRefreshCallback
    {
        final AtomicBoolean running = new AtomicBoolean(true);
        
        @Override
        public boolean isActive()
        {
            return running.get();
        }
        
        @Override
        public synchronized void lockReleased()
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock release notification: " + LOCK_QNAME);
            }
            running.set(false);
        }
    }
    
    private synchronized String acquireLock(LockCallback lockCallback) throws LockAcquisitionException
    {
        // Try to get lock
        String lockToken = jobLockService.getLock(LOCK_QNAME, LOCK_TTL);
        
        // Got the lock - now register the refresh callback which will keep the lock alive
        jobLockService.refreshLock(lockToken, LOCK_QNAME, LOCK_TTL, lockCallback);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Lock acquired: " + LOCK_QNAME + ": "+ lockToken);
        }
        
        return lockToken;
    }
    
    private synchronized void releaseLock(LockCallback lockCallback, String lockToken)
    {
        try
        {
            if (lockCallback != null)
            {
                lockCallback.running.set(false);
            }
            
            if (lockToken != null )
            {
                jobLockService.releaseLock(lockToken, LOCK_QNAME);
                if (logger.isDebugEnabled())
                {
                    logger.debug("Lock released: " + LOCK_QNAME + ": " + lockToken);
                }
            }
        }
        catch (LockAcquisitionException e)
        {
            // Ignore
            if (logger.isDebugEnabled())
            {
                logger.debug("Lock release failed: " + LOCK_QNAME + ": " + lockToken + "(" + e.getMessage() + ")");
            }
        }
    }
}