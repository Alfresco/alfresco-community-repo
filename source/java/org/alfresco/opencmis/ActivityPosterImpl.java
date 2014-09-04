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
package org.alfresco.opencmis;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.Client;
import org.alfresco.repo.Client.ClientType;
import org.alfresco.repo.activities.ActivityType;
import org.alfresco.repo.model.filefolder.HiddenAspect;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.activities.ActivityService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.InitializingBean;

/**
 * OpenCMIS methods may use an instance of this class to post activity data.
 * 
 * @see ActivityPoster
 * @author sglover
 */
// TODO consolidate with ActivityPost for WebDAV
public class ActivityPosterImpl implements ActivityPoster, InitializingBean
{
    private static final String APP_TOOL = "CMIS";
    public static final char PathSeperatorChar = '/';

    // Logging
    private static Log logger = LogFactory.getLog(ActivityPoster.class);

    private ActivityService activityService;
    private SiteService siteService;
    private TenantService tenantService;
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private HiddenAspect hiddenAspect;

    private boolean activitiesEnabled = true;

	/**
     * Constructor
     */
    public ActivityPosterImpl()
    {
    }

	public void setHiddenAspect(HiddenAspect hiddenAspect)
	{
		this.hiddenAspect = hiddenAspect;
	}

	public void setFileFolderService(FileFolderService fileFolderService)
    {
		this.fileFolderService = fileFolderService;
	}

	public void setTenantService(TenantService tenantService)
	{
		this.tenantService = tenantService;
	}

	public void setSiteService(SiteService siteService)
    {
		this.siteService = siteService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}

	public void setActivityService(ActivityService activityService)
    {
		this.activityService = activityService;
	}

	public void setActivitiesEnabled(boolean activitiesEnabled)
	{
		this.activitiesEnabled = activitiesEnabled;
	}

    private final String getPathFromNode(NodeRef rootNodeRef, NodeRef nodeRef) throws FileNotFoundException
    {
        // Check if the nodes are valid, or equal
        if (rootNodeRef == null || nodeRef == null)
            throw new IllegalArgumentException("Invalid node(s) in getPathFromNode call");
        
        // short cut if the path node is the root node
        if (rootNodeRef.equals(nodeRef))
            return "";
        
        // get the path elements
        List<FileInfo> pathInfos = fileFolderService.getNamePath(rootNodeRef, nodeRef);
        
        // build the path string
        StringBuilder sb = new StringBuilder(pathInfos.size() * 20);
        for (FileInfo fileInfo : pathInfos)
        {
            sb.append(PathSeperatorChar);
            sb.append(fileInfo.getName());
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Build name path for node: \n" +
                    "   root: " + rootNodeRef + "\n" +
                    "   target: " + nodeRef + "\n" +
                    "   path: " + sb);
        }
        return sb.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "activityService", activityService);
        PropertyCheck.mandatory(this, "siteService", siteService);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "fileFolderService", fileFolderService);
	}

    private String getCurrentTenantDomain()
    {
        String tenantDomain = tenantService.getCurrentUserDomain();
        if (tenantDomain == null)
        {
            return TenantService.DEFAULT_DOMAIN;
        }
        return tenantDomain;
    }
    
    private boolean isFolder(NodeRef nodeRef)
    {
	    QName typeQName = nodeService.getType(nodeRef);
	    FileFolderServiceType type = fileFolderService.getType(typeQName);
	    boolean isFolder = type.equals(FileFolderServiceType.FOLDER);
	    return isFolder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileFolderAdded(NodeRef nodeRef)
    {
    	if(activitiesEnabled && !hiddenAspect.hasHiddenAspect(nodeRef))
    	{
        	SiteInfo siteInfo = siteService.getSite(nodeRef);
        	String siteId = (siteInfo != null ? siteInfo.getShortName() : null);
    		
    		if(siteId != null && !siteId.equals(""))
    		{
        		// post only for nodes within sites
    			NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();

    			String path = null;
    			boolean isFolder = isFolder(nodeRef);
    			String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

    			if(isFolder)
    			{
    			    NodeRef documentLibrary = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
    	            path = "/";
    	            try
    	            {
    	                path = getPathFromNode(documentLibrary, nodeRef);
    	            }
    	            catch (FileNotFoundException error)
    	            {
    	                if (logger.isDebugEnabled())
    	                {
    	                    logger.debug("No " + SiteService.DOCUMENT_LIBRARY + " container found.");
    	                }
    	            }
    			}
    		    FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef); 
    			postFileFolderActivity((isFolder ? ActivityType.FOLDER_ADDED : ActivityType.FILE_ADDED), path, parentNodeRef, nodeRef, siteId, name, fileInfo);
    		}
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileFolderUpdated(boolean isFolder, NodeRef nodeRef)
    {
    	if(activitiesEnabled && !hiddenAspect.hasHiddenAspect(nodeRef))
    	{
        	SiteInfo siteInfo = siteService.getSite(nodeRef);
        	String siteId = (siteInfo != null ? siteInfo.getShortName() : null);
    		if(siteId != null && !siteId.equals(""))
    		{
        		// post only for nodes within sites
    			String fileName = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
    			
    		    if (!isFolder)
    		    {
    	            FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef); 
    		        postFileFolderActivity(ActivityType.FILE_UPDATED, null, null, nodeRef, siteId, fileName, fileInfo);
    		    }
    		}
    	}
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void postFileFolderDeleted(ActivityInfo activityInfo)
    {
    	if(activitiesEnabled && activityInfo.getSiteId() != null)
		{
    		// post only for nodes within sites
    		postFileFolderActivity((activityInfo.isFolder() ? ActivityType.FOLDER_DELETED : ActivityType.FILE_DELETED), activityInfo.getParentPath(), activityInfo.getParentNodeRef(), activityInfo.getNodeRef(),
    				activityInfo.getSiteId(), activityInfo.getFileName(), null);
    	}
    }

    public ActivityInfo getActivityInfo(NodeRef nodeRef)
    {
        SiteInfo siteInfo = siteService.getSite(nodeRef);
        String siteId = (siteInfo != null ? siteInfo.getShortName() : null);
    	if(siteId != null && !siteId.equals(""))
    	{
    	    NodeRef parentNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
    	    FileInfo fileInfo = fileFolderService.getFileInfo(nodeRef);
    	    String name = fileInfo.getName();
    	    boolean isFolder = fileInfo.isFolder();
    	        
	        NodeRef documentLibrary = siteService.getContainer(siteId, SiteService.DOCUMENT_LIBRARY);
	        String parentPath = "/";
	        try
	        {
	        	parentPath = getPathFromNode(documentLibrary, parentNodeRef);
		    }
		    catch (FileNotFoundException error)
		    {
		        if (logger.isDebugEnabled())
		        {
		            logger.debug("No " + SiteService.DOCUMENT_LIBRARY + " container found.");
		        }
		    }
	        
		    return new ActivityInfo(nodeRef, parentPath, parentNodeRef, siteId, name, isFolder);
    	}
    	else
    	{
    		return null;
    	}
    }

    private void postFileFolderActivity(
            String activityType,
            String path,
            NodeRef parentNodeRef,
            NodeRef nodeRef,
            String siteId,
            String name,
            FileInfo fileInfo)
    {
    	JSONObject json = createActivityJSON(getCurrentTenantDomain(), path, parentNodeRef, nodeRef, name); 	
    	
    	activityService.postActivity(
    			activityType,
    			siteId,
    			APP_TOOL,
    			json.toString(),
    			Client.asType(ClientType.cmis),
    			fileInfo);
    }
    
    /**
     * Create JSON suitable for create, modify or delete activity posts. Returns a new JSONObject
     * containing appropriate key/value pairs.
     * 
     * @param tenantDomain
     * @param nodeRef
     * @param fileName
     * @throws WebDAVServerException
     * @return JSONObject
     */
    private JSONObject createActivityJSON(
                String tenantDomain,
                String path,
                NodeRef parentNodeRef,
                NodeRef nodeRef,
                String fileName)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("nodeRef", nodeRef);
            
            if (parentNodeRef != null)
            {
                // Used for deleted files.
                json.put("parentNodeRef", parentNodeRef);
            }
            
            if (path != null)
            {
                // Used for deleted files and folders (added or deleted)
                json.put("page", "documentlibrary?path=" + path);
            }
            else
            {
                // Used for added or modified files.
                json.put("page", "document-details?nodeRef=" + nodeRef);
            }
            json.put("title", fileName);
            
            if (!tenantDomain.equals(TenantService.DEFAULT_DOMAIN))
            {
                // Only used in multi-tenant setups.
                json.put("tenantDomain", tenantDomain);
            }
        }
        catch (JSONException error)
        {
            throw new AlfrescoRuntimeException("", error);
        }
        
        return json;
    }
    
    public static class ActivityInfo
    {
    	private NodeRef nodeRef;
        private String parentPath;
        private NodeRef parentNodeRef;
        private String siteId;
        private String fileName;
        private boolean isFolder;

		public ActivityInfo(NodeRef nodeRef, String parentPath, NodeRef parentNodeRef,
				String siteId, String fileName, boolean isFolder)
		{
			super();
			this.nodeRef = nodeRef;
			this.parentPath = parentPath;
			this.parentNodeRef = parentNodeRef;
			this.siteId = siteId;
			this.fileName = fileName;
			this.isFolder = isFolder;
		}
		
		public NodeRef getNodeRef()
		{
			return nodeRef;
		}

		public String getParentPath()
		{
			return parentPath;
		}

		public NodeRef getParentNodeRef()
		{
			return parentNodeRef;
		}

		public String getSiteId()
		{
			return siteId;
		}
		
		public String getFileName()
		{
			return fileName;
		}
		
        public boolean isFolder()
        {
            return isFolder;
        }
    }
}