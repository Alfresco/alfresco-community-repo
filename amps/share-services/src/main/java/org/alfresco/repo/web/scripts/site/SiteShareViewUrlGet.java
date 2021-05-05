/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
package org.alfresco.repo.web.scripts.site;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.model.DataListModel;
import org.alfresco.model.ForumModel;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.calendar.CalendarModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Returns the Share URL to view a given NodeRef. 
 *
 * The supplied NodeRef must be within a Site, and must be
 *  of a type supported by Share
 * 
 * @author Nick Burch
 * @since 4.0.2
 */
public class SiteShareViewUrlGet extends DeclarativeWebScript
{
    private static Log logger = LogFactory.getLog(SiteShareViewUrlGet.class);
            
    protected NodeService nodeService;
    protected SiteService siteService;
    protected SysAdminParams sysAdminParams;
    protected DictionaryService dictionaryService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // Grab the NodeRef
        String nodeRefS = req.getParameter("nodeRef");
        if (nodeRefS == null)
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "NodeRef must be supplied");
        if (! NodeRef.isNodeRef(nodeRefS))
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid NodeRef");
        
        // Check the node exists
        NodeRef nodeRef = new NodeRef(nodeRefS);
        if (! nodeService.exists(nodeRef))
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Node Does Not Exist");
        
        
        // Work out what site it's in, and what container in the site
        SiteInfo site = null;
        NodeRef siteContainer = null;
        {
            NodeRef current = nodeRef;
            NodeRef prev = null;
            while (current != null)
            {
                // Are we at a site yet?
                QName type = nodeService.getType(current);
                if (dictionaryService.isSubClass(type, SiteModel.TYPE_SITE))
                {
                    // Found it!
                    siteContainer = prev;
                    site = siteService.getSite(current);
                    break;
                }
                
                // Step down
                prev = current;
                current = nodeService.getPrimaryParent(current).getParentRef();
            }
        }
        if (site == null)
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Node isn't part of a site");
        
        // Grab the main URL for the site
        String baseUrl = getShareSiteRootStem(req, site);
        
        // Identify the appropriate Share URL, based on the Node Type
        QName nodeType = nodeService.getType(nodeRef);
        
        
        // Start on the model
        Map<String,Object> model = new HashMap<String, Object>();
        model.put("node", nodeRef);
        model.put("site", site);
        model.put("type", nodeType);
        
        // Get the URL, and we're done
        String page = identifySharePage(nodeRef, site, siteContainer, nodeType);
        model.put("url", baseUrl + page);
        
        return model;
    }
    
    private static QName TYPE_LINK = QName.createQName(NamespaceService.LINKS_MODEL_1_0_URI, "link");
    protected String identifySharePage(NodeRef nodeRef, SiteInfo site, NodeRef siteContainer, QName nodeType)
    {
        // Grab the name of the Node itself - often used for the URL
        String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        
        
        // Wiki and Blog both use cm:content in special containers
        if (siteContainer != null && dictionaryService.isSubClass(nodeType, ContentModel.TYPE_CONTENT))
        {
            QName containerName = nodeService.getPrimaryParent(siteContainer).getQName();
            if (containerName.equals( QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "blog") ))
            {
                // Wiki - cm:content in folder called cm:wiki
                return "wiki-page?title=" + name;
            }
            if (containerName.equals( QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "wiki") ))
            {
                // Blog - cm:content in cm:blog
                return "blog-postview?postId=" + name;
            }
        }
        
        // Is it a Data List?
        if (dictionaryService.isSubClass(nodeType, DataListModel.TYPE_DATALIST))
        {
            return "data-lists?list=" + name;
        }
        
        // Is it a Link?
        if (dictionaryService.isSubClass(nodeType, TYPE_LINK))
        {
            return "links-view?linkId=" + name;
        }
        
        // Is it a Calendar Entry?
        if (dictionaryService.isSubClass(nodeType, CalendarModel.TYPE_EVENT))
        {
            // Find the date
            Date date = (Date)nodeService.getProperty(nodeRef, CalendarModel.PROP_FROM_DATE);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            
            // Build the URL based on it
            return "calendar?date=" + fmt.format(date);
        }
        
        // Is it a discussions topic or post?
        if (dictionaryService.isSubClass(nodeType, ForumModel.TYPE_TOPIC))
        {
            // Topic is easy
            return "discussions-topicview?topicId=" + name;
        }
        if (dictionaryService.isSubClass(nodeType, ForumModel.TYPE_POST))
        {
            // Go from post to the topic, then list from there
            NodeRef postTopic = nodeService.getPrimaryParent(nodeRef).getParentRef();
            String topicName = (String)nodeService.getProperty(postTopic, ContentModel.PROP_NAME);
            return "discussions-topicview?topicId=" + topicName;
        }
        
        // Is it just regular content?  
        if (dictionaryService.isSubClass(nodeType, ContentModel.TYPE_CONTENT))
        {
            // Simple, Document Details with a noderef
            return "document-details?nodeRef=" + nodeRef.toString();
        }
        
        // Is it a normal folder?
        if (dictionaryService.isSubClass(nodeType, ContentModel.TYPE_FOLDER))
        {
            // Need the path within the site
            List<String> paths = new ArrayList<String>();
            NodeRef current = nodeRef;
            while (current != null && !current.equals(siteContainer) && !current.equals(site.getNodeRef()))
            {
                paths.add( (String)nodeService.getProperty(current, ContentModel.PROP_NAME) );
                current = nodeService.getPrimaryParent(current).getParentRef();
            }
            
            // Invert to build the path
            StringBuilder path = new StringBuilder();
            for (int i=paths.size()-1; i>=0; i--)
            {
                path.append('/');
                path.append(paths.get(i));
            }
            
            if (path.length() > 0)
            {
                // Becomes documentlibrary?path=/Docs/Beta
                return "documentlibrary?path=" + path.toString();
            }
            else
            {
                // Just the root of the document library
                return "documentlibrary";
            }
        }
        
        // If we can't work out what it is, log and take them to the site dashboard
        if (logger.isDebugEnabled())
            logger.debug("COuldn't identify specific URL for Node " + nodeRef + " of type " + nodeType);
        return "dashboard";
    }
    
    /**
     * Returns the root of the Share Site pages for a given site, eg
     *   https://test.alfresco.com/share/page/site/test-site/
     */
    protected String getShareSiteRootStem(WebScriptRequest req, SiteInfo site)
    {
        return getShareRootUrl(req) + "page/site/" + site.getShortName() + "/";
    }
    /**
     * Returns the root of the Share WebApp, eg
     *  http://localhost:8081/share/
     */
    protected String getShareRootUrl(WebScriptRequest req)
    {
        return UrlUtil.getShareUrl(sysAdminParams) + "/";
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
}