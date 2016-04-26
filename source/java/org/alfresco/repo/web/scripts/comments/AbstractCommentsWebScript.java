package org.alfresco.repo.web.scripts.comments;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class is the abstract controller for the comments web scripts (delete
 * and post)
 * 
 * @author Ramona Popa
 * @since 4.2.6
 */
public abstract class AbstractCommentsWebScript extends DeclarativeWebScript
{

    protected final static String COMMENTS_TOPIC_NAME = "Comments";

    private static Log logger = LogFactory.getLog(CommentsPost.class);

    protected final static String JSON_KEY_SITE = "site";
    protected final static String JSON_KEY_SITE_ID = "siteid";
    protected final static String JSON_KEY_ITEM_TITLE = "itemTitle";
    protected final static String JSON_KEY_PAGE = "page";
    protected final static String JSON_KEY_TITLE = "title";
    protected final static String JSON_KEY_PAGE_PARAMS = "pageParams";
    protected final static String JSON_KEY_NODEREF = "nodeRef";
    protected final static String JSON_KEY_CONTENT = "content";

    protected final static String COMMENT_CREATED_ACTIVITY = "org.alfresco.comments.comment-created";
    protected final static String COMMENT_DELETED_ACTIVITY = "org.alfresco.comments.comment-deleted";

    protected ServiceRegistry serviceRegistry;
    protected NodeService nodeService;
    protected ContentService contentService;
    protected PersonService personService;
    protected SiteService siteService;
    protected PermissionService permissionService;
    protected ActivityService activityService;

    protected BehaviourFilter behaviourFilter;

    protected static final String PARAM_MESSAGE = "message";
    protected static final String PARAM_NODE = "node";
    protected static final String PARAM_ITEM = "item";

    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
        this.nodeService = serviceRegistry.getNodeService();
        this.siteService = serviceRegistry.getSiteService();
        this.contentService = serviceRegistry.getContentService();
        this.personService = serviceRegistry.getPersonService();
        this.permissionService = serviceRegistry.getPermissionService();
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setActivityService(ActivityService activityService)
    {
        this.activityService = activityService;
    }

    /**
     * returns the nodeRef from  web script request
     * @param req
     * @return
     */
    protected NodeRef parseRequestForNodeRef(WebScriptRequest req)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String storeType = templateVars.get("store_type");
        String storeId = templateVars.get("store_id");
        String nodeId = templateVars.get("id");

        // create the NodeRef and ensure it is valid
        StoreRef storeRef = new StoreRef(storeType, storeId);
        return new NodeRef(storeRef, nodeId);
    }

    /**
     * get the value from JSON for given key if exists
     * @param json
     * @param key
     * @return
     */
    protected String getOrNull(JSONObject json, String key)
    {
        if (json != null && json.containsKey(key))
        {
            return (String) json.get(key);
        }
        return null;
    }

    /**
     * parse JSON from request
     * @param req
     * @return
     */
    protected JSONObject parseJSON(WebScriptRequest req)
    {
        JSONObject json = null;
        String contentType = req.getContentType();
        if (contentType != null && contentType.indexOf(';') != -1)
        {
            contentType = contentType.substring(0, contentType.indexOf(';'));
        }
        if (MimetypeMap.MIMETYPE_JSON.equals(contentType))
        {
            JSONParser parser = new JSONParser();
            try
            {
                json = (JSONObject) parser.parse(req.getContent().getContent());
            }
            catch (IOException io)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + io.getMessage());
            }
            catch (ParseException pe)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid JSON: " + pe.getMessage());
            }
        }
        return json;
    }

    /**
     * parse JSON for a given input string
     * @param input
     * @return
     */
    protected JSONObject parseJSONFromString(String input)
    {
        JSONObject json = null;

        JSONParser parser = new JSONParser();
        try
        {
            if (input != null)
            {
                json = (JSONObject) parser.parse(input);
                return json;
            }
        }
        catch (ParseException pe)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Invalid JSON: " + pe.getMessage());
            }
        }

        return null;
    }

    /**
     * Post an activity entry for the comment added or deleted
     * 
     * @param json
     *            - is not sent null with this activity type - only for delete
     * @param req
     * @param nodeRef
     * @param activityType
     */
    protected void postActivity(JSONObject json, WebScriptRequest req, NodeRef nodeRef, String activityType)
    {
        String jsonActivityData = "";
        String siteId = "";
        String page = "";
        String title = "";

        if (nodeRef == null)
        {
            // in case we don't have an parent nodeRef provided we do not need
            // to post activity for parent node
            return;
        }

        String strNodeRef = nodeRef.toString();

        SiteInfo siteInfo = getSiteInfo(req, COMMENT_CREATED_ACTIVITY.equals(activityType));

        // post an activity item, but only if we've got a site
        if (siteInfo != null)
        {
            siteId = siteInfo.getShortName();
            if (siteId == null || siteId.length() == 0)
            {
                return;
            }
        }

        // json is not sent null with this activity type - only for delete
        if (COMMENT_CREATED_ACTIVITY.equals(activityType))
        {
            try
            {
                org.json.JSONObject params = new org.json.JSONObject(getOrNull(json, JSON_KEY_PAGE_PARAMS));
                String strParams = "";

                Iterator<?> itr = params.keys();
                while (itr.hasNext())
                {
                    String strParam = itr.next().toString();
                    strParams += strParam + "=" + params.getString(strParam) + "&";
                }
                page = getOrNull(json, JSON_KEY_PAGE) + "?" + (strParams != "" ? strParams.substring(0, strParams.length() - 1) : "");
                title = getOrNull(json, JSON_KEY_ITEM_TITLE);

            }
            catch (Exception e)
            {
                logger.warn("Error parsing JSON", e);
            }
        }
        else
        {
            // COMMENT_DELETED_ACTIVITY
            title = req.getParameter(JSON_KEY_ITEM_TITLE);
            page = req.getParameter(JSON_KEY_PAGE) + "?" + JSON_KEY_NODEREF + "=" + strNodeRef;
        }

        try
        {
            JSONWriter jsonWriter = new JSONStringer().object();
            jsonWriter.key(JSON_KEY_TITLE).value(title);
            jsonWriter.key(JSON_KEY_PAGE).value(page);
            jsonWriter.key(JSON_KEY_NODEREF).value(strNodeRef);

            jsonActivityData = jsonWriter.endObject().toString();
            activityService.postActivity(activityType, siteId, COMMENTS_TOPIC_NAME, jsonActivityData);
        }
        catch (Exception e)
        {
            logger.warn("Error adding comment to activities feed", e);
        }

    }

    /**
     * returns SiteInfo needed for post activity
     * @param req
     * @return
     */
    protected SiteInfo getSiteInfo(WebScriptRequest req, boolean searchForSiteInJSON)
    {
        String siteName = req.getParameter(JSON_KEY_SITE);

        if (siteName == null && searchForSiteInJSON )
        {
            JSONObject json = parseJSON(req);
            if (json != null){
                if (json.containsKey(JSON_KEY_SITE))
                {
                    siteName = (String) json.get(JSON_KEY_SITE);
                }
                else if (json.containsKey(JSON_KEY_SITE_ID))
                {
                    siteName = (String) json.get(JSON_KEY_SITE_ID);
                }
            }
        }
        if (siteName != null)
        {
            SiteInfo site = siteService.getSite(siteName);
            return site;
        }

        return null;
    }

    /**
     * Overrides DeclarativeWebScript with parse request for nodeRef 
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // get requested node
        NodeRef nodeRef = parseRequestForNodeRef(req);

        // Have the real work done
        return executeImpl(nodeRef, req, status, cache);
    }

    /**
     * 
     * @param nodeRef
     * @param req
     * @param status
     * @param cache
     * @return
     */
    protected abstract Map<String, Object> executeImpl(NodeRef nodeRef, WebScriptRequest req, Status status, Cache cache);

}
