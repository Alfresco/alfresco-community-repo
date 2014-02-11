/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.activities.feed;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.activities.post.lookup.PostLookup;
import org.alfresco.repo.domain.activities.ActivityFeedDAO;
import org.alfresco.repo.domain.activities.ActivityFeedEntity;
import org.alfresco.repo.domain.activities.ActivityPostEntity;
import org.alfresco.repo.domain.activities.FeedControlEntity;
import org.alfresco.repo.template.ISO8601DateFormatMethod;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.JSONtoFmModel;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Base64;

import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Responsible for processing the individual task
 */
public abstract class FeedTaskProcessor
{
    private static final Log logger = LogFactory.getLog(FeedTaskProcessor.class);
    
    public static final String FEED_FORMAT_JSON = "json";
    public static final String FEED_FORMAT_ATOMENTRY = "atomentry";
    public static final String FEED_FORMAT_HTML = "html";
    public static final String FEED_FORMAT_RSS = "rss";
    public static final String FEED_FORMAT_TEXT = "text";
    public static final String FEED_FORMAT_XML = "xml";
    
    private static final String URL_SERVICE_SITES     = "/api/sites";
    private static final String URL_MEMBERSHIPS       = "/memberships";
    
    private static final String URL_SERVICE_TEMPLATES = "/api/activities/templates";
    private static final String URL_SERVICE_TEMPLATE  = "/api/activities/template";
    
    public void process(int jobTaskNode, long minSeq, long maxSeq, RepoCtx ctx) throws Exception
    {
        long startTime = System.currentTimeMillis();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Process: jobTaskNode '" + jobTaskNode + "' from seq '" + minSeq + "' to seq '" + maxSeq + "' on this node from grid job.");
        }
        
        ActivityPostEntity selector = new ActivityPostEntity();
        selector.setJobTaskNode(jobTaskNode);
        selector.setMinId(minSeq);
        selector.setMaxId(maxSeq);
        selector.setStatus(ActivityPostEntity.STATUS.POSTED.toString());
        
        List<ActivityPostEntity> activityPosts = null;
        int totalGenerated = 0;
        
        try
        {
            activityPosts = selectPosts(selector);
            
            if (logger.isDebugEnabled()) { logger.debug("Process: " + activityPosts.size() + " activity posts"); }
            
            // local caches for this run of activity posts
            Map<String, Set<String>> siteConnectedUsers = new HashMap<String, Set<String>>();                                 // site -> site members
            Map<Pair<String, String>, Set<String>> followerConnectedUsers = new HashMap<Pair<String, String>, Set<String>>(); // user -> followers
            Map<Pair<String, String>, Boolean> canUserReadSite = new HashMap<Pair<String, String>, Boolean>();                // <user, site> -> true/false (note: used when following, implied as true for site members)
            Map<String, List<FeedControlEntity>> userFeedControls = new HashMap<String, List<FeedControlEntity>>();
            
            List<String> fmTemplates = Arrays.asList(new String[]{"activities/org/alfresco/generic.json.ftl"});
            
            // for each activity post ...
            for (ActivityPostEntity activityPost : activityPosts)
            {
                String postingUserId = activityPost.getUserId();
                String activityType = activityPost.getActivityType();
                
                if (fmTemplates.size() == 0)
                {
                    logger.error("Skipping activity post " + activityPost.getId() + " since no specific/generic templates for activityType: " + activityType );
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                Map<String, Object> model = null;
                try
                {
                    model = JSONtoFmModel.convertJSONObjectToMap(activityPost.getActivityData());
                }
                catch(JSONException je)
                {
                    logger.error("Skipping activity post " + activityPost.getId() + " due to invalid activity data: ", je);
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                String nodeRefStr = (String) model.get(PostLookup.JSON_NODEREF);
                try
                {
                    // If a nodeRef is present, then it must be valid.
                    if (nodeRefStr != null)
                    {
                        // Attempt to create a nodeRef, making use of the constructor's validation.
                        new NodeRef(nodeRefStr);
                    }
                }
                catch (Exception e)
                {
                    logger.error("Skipping activity post " + activityPost.getId() +
                                " due to invalid nodeRef: " + nodeRefStr);
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                // note: for MT share, site id should already be mangled - in addition to extra tenant domain info
                
                String thisSite = activityPost.getSiteNetwork();
                String tenantDomain = (String)model.get(PostLookup.JSON_TENANT_DOMAIN);
                
                if (thisSite != null)
                {
                    if (tenantDomain != null)
                    {
                        thisSite = getTenantName(thisSite, tenantDomain);
                    }
                    else
                    {
                        // for backwards compatibility
                        tenantDomain = getTenantDomain(thisSite);
                    }
                }
                if (tenantDomain == null)
                {
                    tenantDomain = TenantService.DEFAULT_DOMAIN;
                }
                
                model.put(ActivityFeedEntity.KEY_ACTIVITY_FEED_TYPE, activityPost.getActivityType());
                model.put(ActivityFeedEntity.KEY_ACTIVITY_FEED_SITE, thisSite);
                model.put("userId", activityPost.getUserId());
                model.put("id", activityPost.getId());
                model.put("date", activityPost.getPostDate()); // post date rather than time that feed is generated
                model.put("xmldate", new ISO8601DateFormatMethod());
                model.put("repoEndPoint", ctx.getRepoEndPoint());
                
                // Get recipients of this post
                Set<String> recipients = null;
                try
                {
                    recipients = getRecipients(ctx, thisSite, activityPost.getUserId(), tenantDomain, siteConnectedUsers, followerConnectedUsers, canUserReadSite);
                }
                catch (Exception e)
                {
                    logger.error("Skipping activity post " + activityPost.getId() + " since failed to get recipients: ", e);
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.ERROR);
                    continue;
                }
                
                try 
                { 
                    startTransaction();
                    
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Process: " + recipients.size() + " candidate connections for activity post " + activityPost.getId());
                    }
                    
                    int excludedConnections = 0;
                    
                    for (String recipient : recipients)
                    {
                        List<FeedControlEntity> feedControls = null;
                        if (! recipient.equals(""))
                        {
                            // Get user's feed controls
                            feedControls = userFeedControls.get(recipient);
                            if (feedControls == null)
                            {
                                feedControls = getFeedControls(recipient);
                                userFeedControls.put(recipient, feedControls);
                            }
                        }
                        
                        // filter based on opt-out feed controls (if any)
                        if (! acceptActivity(activityPost, feedControls))
                        {
                            excludedConnections++;
                        }
                        else
                        {
                            // node read permission check (if nodeRef is present)
                            if (! canRead(ctx, recipient, model))
                            {
                                excludedConnections++;
                                continue;
                            }
                            
                            for (String fmTemplate : fmTemplates)
                            {
                                String formatFound = FeedTaskProcessor.FEED_FORMAT_JSON;
                                
                                ActivityFeedEntity feed = new ActivityFeedEntity();
                                
                                // Generate activity feed summary 
                                feed.setFeedUserId(recipient);
                                feed.setPostUserId(postingUserId);
                                feed.setActivityType(activityType);
                                
                                String activitySummary = null;
                                    // allows JSON to simply pass straight through
                                    activitySummary = activityPost.getActivityData();
                                
                                if (! activitySummary.equals(""))
                                {
                                    if (activitySummary.length() > ActivityFeedDAO.MAX_LEN_ACTIVITY_SUMMARY)
                                    {
                                        logger.warn("Skip feed entry (activity post " + activityPost.getId() + ") since activity summary - exceeds " + ActivityFeedDAO.MAX_LEN_ACTIVITY_SUMMARY + " chars: " + activitySummary);
                                    }
                                    else
                                    {
                                        feed.setActivitySummary(activitySummary);
                                        feed.setSiteNetwork(thisSite);
                                        feed.setAppTool(activityPost.getAppTool());
                                        feed.setPostDate(activityPost.getPostDate());
                                        feed.setPostId(activityPost.getId());
                                        feed.setFeedDate(new Date());
                                        
                                        // Insert activity feed
                                        insertFeedEntry(feed); // ignore returned feedId
                                        
                                        totalGenerated++;
                                    }
                                }
                                else
                                {
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("Empty template result for activityType '" + activityType + "' using format '" + formatFound + "' hence skip feed entry (activity post " + activityPost.getId() + ")");
                                    }
                                }
                            }
                        }
                    }
                    
                    updatePostStatus(activityPost.getId(), ActivityPostEntity.STATUS.PROCESSED);
                    
                    commitTransaction();
                    
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Processed: " + (recipients.size() - excludedConnections) + " connections for activity post " + activityPost.getId() + " (excluded " + excludedConnections + ")");
                    }
                } 
                finally 
                { 
                    endTransaction();
                } 
            }
        }
        catch(SQLException se)
        {
            logger.error(se);
            throw se;
        }
        finally
        {
            int postCnt = activityPosts == null ? 0 : activityPosts.size();
            
            // TODO i18n info message
            StringBuilder sb = new StringBuilder();
            sb.append("Generated ").append(totalGenerated).append(" activity feed entr").append(totalGenerated == 1 ? "y" : "ies");
            sb.append(" for ").append(postCnt).append(" activity post").append(postCnt != 1 ? "s" : "").append(" (in ").append(System.currentTimeMillis() - startTime).append(" msecs)");
            logger.info(sb.toString());
        }
    }
    
    private Set<String> getRecipients(RepoCtx ctx, String siteId, String postUserId, String tenantDomain,
                                      Map<String, Set<String>> siteConnectedUsers, Map<Pair<String, String>, Set<String>> followerConnectedUsers, Map<Pair<String, String>, Boolean> canUserReadSite) throws Exception
    {
        // Recipients of this post
        Set<String> recipients = new HashSet<String>();
        
        // Add site members to recipient list
        if ((null != siteId) && (siteId.length() > 0))
        {
            // Get the members of this site - save hammering the repository by reusing cached site members
            Set<String> connectedUsers = siteConnectedUsers.get(siteId);
            if (connectedUsers == null)
            {
                try
                {
                    // Repository callback to get site members
                    connectedUsers = getSiteMembers(ctx, siteId, tenantDomain);
                    connectedUsers.add(""); // add empty posting userid - to represent site feed !
                }
                catch(Exception e)
                {
                    throw new Exception("Failed to get site members: "+e);
                }
                
                // Cache them for future use (across activity posts handled) by this same invocation
                siteConnectedUsers.put(siteId, connectedUsers);
            }
            
            recipients.addAll(connectedUsers);
        }
        
        // Add followers to recipient list
        
        // MT Share
        Pair<String, String> userTenantKey = new Pair<String, String>(postUserId, tenantDomain);
        Set<String> followerUsers = followerConnectedUsers.get(userTenantKey);
        if (followerUsers == null)
        {
            try
            {
                followerUsers = getFollowers(postUserId, tenantDomain);
            }
            catch(Exception e)
            {
                throw new Exception("Failed to get followers: "+e);
            }
            
            // Cache them for future use (across activity posts handled) by this same invocation
            followerConnectedUsers.put(userTenantKey, followerUsers);
        }
        
        if ((null != siteId) && (siteId.length() > 0))
        {
            for (String followerUser : followerUsers)
            {
                Pair<String, String> userSiteKey = new Pair<String, String>(followerUser, siteId);
                Boolean canRead = canUserReadSite.get(userSiteKey);
                if (canRead == null)
                {
                    // site read permission check (note: only check followers since implied for site members)
                    canRead = canReadSite(ctx, siteId, followerUser, tenantDomain);
                    canUserReadSite.put(userSiteKey, canRead);
                }
                
                if (canRead)
                {
                    recipients.add(followerUser);
                }
            }
        }
        else
        {
            recipients.addAll(followerUsers);
        }
        
        
        // Add the originator to recipients
        recipients.add(postUserId);
        
        return recipients;
    }
    
    public abstract void startTransaction() throws SQLException;

    public abstract void commitTransaction() throws SQLException;

    public abstract void rollbackTransaction() throws SQLException;

    public abstract void endTransaction() throws SQLException;

    public abstract List<ActivityPostEntity> selectPosts(ActivityPostEntity selector) throws SQLException;

    public abstract List<FeedControlEntity> selectUserFeedControls(String userId) throws SQLException;

    public abstract long insertFeedEntry(ActivityFeedEntity feed) throws SQLException;

    public abstract int updatePostStatus(long id, ActivityPostEntity.STATUS status) throws SQLException;

    protected String callWebScript(String urlString, String ticket) throws MalformedURLException, URISyntaxException, IOException
    {
        URL url = new URL(urlString);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Request URI: " + url.toURI());
        }
        
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        
        if (ticket != null)
        {
            // add Base64 encoded authorization header
            // refer to: http://wiki.alfresco.com/wiki/Web_Scripts_Framework#HTTP_Basic_Authentication
            conn.addRequestProperty("Authorization", "Basic " + Base64.encodeBytes(ticket.getBytes()));
        }
        
        String result = null;
        InputStream is = null;
        BufferedReader br = null;
        
        try
        {
            is = conn.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            
            String line = null;
            StringBuffer sb = new StringBuffer();
            while(((line = br.readLine()) !=null))  
            {
                sb.append(line);
            }
            
            result = sb.toString();
            
            if (logger.isDebugEnabled())
            {
                int responseCode = conn.getResponseCode();
                logger.debug("Response code: " + responseCode);
            }
        }
        finally
        {
            if (br != null) { br.close(); };
            if (is != null) { is.close(); };
        }
        
        return result;
    }
    
    protected String getTenantName(String name, String tenantDomain)
    {
        // note: for MT impl, see override in LocalFeedTaskProcessor
        return name;
    }
    
    protected String getTenantDomain(String name)
    {
        // note: for MT impl, see override in LocalFeedTaskProcessor
        return TenantService.DEFAULT_DOMAIN;
    }
    
    protected Set<String> getSiteMembers(RepoCtx ctx, String siteId, String tenantDomain) throws Exception
    {
        // note: tenant domain ignored her - it should already be part of the siteId
        Set<String> members = new HashSet<String>();
        if ((siteId != null) && (siteId.length() != 0))
        {
            StringBuffer sbUrl = new StringBuffer();
            sbUrl.append(ctx.getRepoEndPoint()).
                  append(URL_SERVICE_SITES).append("/").append(siteId).append(URL_MEMBERSHIPS);
            
            String jsonArrayResult = callWebScript(sbUrl.toString(), ctx.getTicket());
            if ((jsonArrayResult != null) && (jsonArrayResult.length() != 0))
            {
                JSONArray ja = new JSONArray(jsonArrayResult);
                for (int i = 0; i < ja.length(); i++)
                {
                    JSONObject member = (JSONObject)ja.get(i);
                    JSONObject person = (JSONObject)member.getJSONObject("person");
                    
                    String userName = person.getString("userName");
                    if (! ctx.isUserNamesAreCaseSensitive())
                    {
                        userName = userName.toLowerCase();
                    }
                    members.add(userName);
                }
            }
        }
        
        return members;
    }
    
    protected abstract Set<String> getFollowers(String userId, String tenantDomain) throws Exception;
    protected abstract boolean canReadSite(final RepoCtx ctx, String siteIdIn, String connectedUser, final String tenantDomain) throws Exception;
    protected abstract boolean canRead(RepoCtx ctx, final String connectedUser, Map<String, Object> model) throws Exception;
    
    protected Map<String, List<String>> getActivityTypeTemplates(String repoEndPoint, String ticket, String subPath) throws Exception
    {
        StringBuffer sbUrl = new StringBuffer();
        sbUrl.append(repoEndPoint).append(URL_SERVICE_TEMPLATES).append(subPath).append("*").append("?format=json");
        
        String jsonArrayResult = null;
        try
        {
            jsonArrayResult = callWebScript(sbUrl.toString(), ticket);
        }
        catch (FileNotFoundException e)
        {
            return null;
        }
        
        List<String> allTemplateNames = new ArrayList<String>(10);
        
        if ((jsonArrayResult != null) && (jsonArrayResult.length() != 0))
        {
            JSONArray ja = new JSONArray(jsonArrayResult);
            for (int i = 0; i < ja.length(); i++)
            {
                String name = ja.getString(i);
                allTemplateNames.add(name);
            }
        }
        
        return getActivityTemplates(allTemplateNames);
    }

    protected Map<String, List<String>> getActivityTemplates(List<String> allTemplateNames)
    {
        Map<String, List<String>> activityTemplates = new HashMap<String, List<String>>(10);
        
        for (String template : allTemplateNames)
        {
            if (! template.contains(" (Working Copy)."))
            {
                // assume template path = <path>/<base-activityType>.<format>.ftl
                // and base-activityType can contain "."
                
                String baseActivityType = template;
                int idx1 = baseActivityType.lastIndexOf("/");
                if (idx1 != -1)
                {
                    baseActivityType = baseActivityType.substring(idx1+1);
                }
                
                int idx2 = baseActivityType.lastIndexOf(".");
                if (idx2 != -1)
                {
                    int idx3 = baseActivityType.substring(0, idx2).lastIndexOf(".");
                    if (idx3 != -1)
                    {
                        baseActivityType = baseActivityType.substring(0, idx3);
                        
                        List<String> activityTypeTemplateList = activityTemplates.get(baseActivityType);
                        if (activityTypeTemplateList == null)
                        {
                            activityTypeTemplateList = new ArrayList<String>(1);
                            activityTemplates.put(baseActivityType, activityTypeTemplateList);
                        }
                        activityTypeTemplateList.add(template);
                    }
                }
            }
        }
        
        return activityTemplates;
    }

    protected Configuration getFreemarkerConfiguration(RepoCtx ctx)
    {
        Configuration cfg = new Configuration();
        cfg.setObjectWrapper(new DefaultObjectWrapper());

        // custom template loader
        cfg.setTemplateLoader(new TemplateWebScriptLoader(ctx.getRepoEndPoint(), ctx.getTicket()));

        // TODO review i18n
        cfg.setLocalizedLookup(false);

        return cfg;
    }

    protected String processFreemarker(Map<String, Template> templateCache, String fmTemplate, Configuration cfg, Map<String, Object> model) throws IOException, TemplateException, Exception
    {
        // Save on lots of modification date checking by caching templates locally
        Template myTemplate = templateCache.get(fmTemplate);
        if (myTemplate == null)
        {
            myTemplate = cfg.getTemplate(fmTemplate);
            templateCache.put(fmTemplate, myTemplate);
        }
        
        StringWriter textWriter = new StringWriter();
        myTemplate.process(model, textWriter);
        
        return textWriter.toString();
    }

    protected List<FeedControlEntity> getFeedControls(String connectedUser) throws SQLException
    {
        return selectUserFeedControls(connectedUser);
    }

    protected boolean acceptActivity(ActivityPostEntity activityPost, List<FeedControlEntity> feedControls)
    {
        if (feedControls == null)
        {
            return true;
        }
        
        for (FeedControlEntity feedControl : feedControls)
        {
            if (((feedControl.getSiteNetwork() == null) || (feedControl.getSiteNetwork().length() == 0)) && (feedControl.getAppTool() != null))
            {
                if (feedControl.getAppTool().equals(activityPost.getAppTool()))
                {
                    // exclude this appTool (across sites)
                    return false;
                }
            }
            else if (((feedControl.getAppTool() == null) || (feedControl.getAppTool().length() == 0)) && (feedControl.getSiteNetwork() != null))
            {
                if (feedControl.getSiteNetwork().equals(activityPost.getSiteNetwork()))
                {
                    // exclude this site (across appTools)
                    return false;
                }
            }
            else if (((feedControl.getSiteNetwork() != null) && (feedControl.getSiteNetwork().length() > 0)) &&
                     ((feedControl.getAppTool() != null) && (feedControl.getAppTool().length() > 0)))
            {
                if ((feedControl.getSiteNetwork().equals(activityPost.getSiteNetwork())) &&
                    (feedControl.getAppTool().equals(activityPost.getAppTool())))
                {
                    // exclude this appTool for this site
                    return false;
                }
            }
        }
        
        return true;
    }

    protected void addMissingFormats(String activityType, List<String> fmTemplates, List<String> templatesToAdd)
    {
        for (String templateToAdd : templatesToAdd)
        {
            int idx1 = templateToAdd.lastIndexOf(".");
            if (idx1 != -1)
            {
                int idx2 = templateToAdd.substring(0, idx1).lastIndexOf(".");
                if (idx2 != -1)
                {
                    String templateFormat = templateToAdd.substring(idx2+1, idx1);
                    
                    boolean found = false;
                    for (String fmTemplate : fmTemplates)
                    {
                        if (fmTemplate.contains("."+templateFormat+"."))
                        {
                            found = true;
                        }
                    }
                    
                    if (! found)
                    {
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Add template '" + templateToAdd + "' for type '" + activityType + "'");
                        }
                        fmTemplates.add(templateToAdd);
                    }
                }
            }
        }
    }

    protected String getTemplateSubPath(String activityType)
    {
        return (! activityType.startsWith("/") ? "/" : "") + activityType.replace(".", "/");
    }
    
    protected String getBaseActivityType(String activityType)
    {
        String[] parts = activityType.split("\\.");
        
        return (parts.length != 0 ? parts[parts.length-1] : "");
    }
    
    protected class TemplateWebScriptLoader extends URLTemplateLoader
    {
        private String repoEndPoint;
        private String ticketId;
        
        public TemplateWebScriptLoader(String repoEndPoint, String ticketId)
        {
            this.repoEndPoint = repoEndPoint;
            this.ticketId = ticketId;
            }
        
        public URL getURL(String templatePath)
        {
            try
            {
                StringBuffer sb = new StringBuffer();
                sb.append(this.repoEndPoint).
                   append(URL_SERVICE_TEMPLATE).append("/").append(templatePath).
                   append("?format=text").
                   append("&alf_ticket=").append(ticketId);
                
                if (logger.isDebugEnabled())
                {
                    logger.debug("getURL: " + sb.toString());
                }
                
                return new URL(sb.toString());
            } 
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
         }
    }
}
