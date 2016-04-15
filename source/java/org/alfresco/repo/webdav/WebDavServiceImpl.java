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
package org.alfresco.repo.webdav;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.filefolder.FileFolderServiceImpl.InvalidTypeException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.webdav.WebDavService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.springframework.extensions.surf.util.URLEncoder;

/**
 * The WebDav client is used by the repository to generate webdav URLs
 * <p>
 * This is a bog standard spring bean for the repo side of WebDav.
 * 
 * @link org.alfresco.repo.webdav.WebDavServlet the server side of webdav.
 *
 * @author mrogers
 */
public class WebDavServiceImpl implements WebDavService
{
    public static final String WEBDAV_PREFIX = "webdav"; 
    private boolean enabled = false;
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private FileFolderService fileFolderService;  
    // Root nodes
    private MTNodesCache2 rootNode;
    private boolean activitiesEnabled;

    
    
    /**
     * Spring bean init method
     */
    public void init()
    {
        PropertyCheck.mandatory(this, "nodeService", getNodeService());
        PropertyCheck.mandatory(this, "dictionaryService", getDictionaryService());
        PropertyCheck.mandatory(this, "fileFolderService", getFileFolderService());
        PropertyCheck.mandatory(this, "rootNode", getRootNode());    
    }

    /**
     * Get the WebDavUrl for the specified nodeRef
     * 
     * @param nodeRef the node that the webdav URL (or null)
     * @return the URL of the node in webdav or "" if a URL cannot be built.
     */
    public String getWebdavUrl(NodeRef nodeRef)
    {
        String url = "";
        
        if (!enabled)
        {
            return url;
        }
        
        try
        {
            QName typeName = nodeService.getType(nodeRef);
            
            if (getIsContainer(typeName) || getIsDocument(typeName))
            {
                List<String> paths = fileFolderService.getNameOnlyPath(getRootNode().getNodeForCurrentTenant(), nodeRef);
                
                // build up the webdav url
                StringBuilder path = new StringBuilder(128);
                path.append("/" + WEBDAV_PREFIX);
                
                for (int i=0; i<paths.size(); i++)
                {
                    path.append("/")
                        .append(URLEncoder.encode(paths.get(i)));
                }
                url = path.toString();
            }
        }
        catch (InvalidTypeException typeErr)
        {
            // cannot build path if file is a type such as a rendition
        }
        catch (FileNotFoundException nodeErr)
        {
            // cannot build path if file no longer exists, return default
        }
        return url;
    }
    
    /**
     * @return true if this Node is a container (i.e. a folder)
     */
    private boolean getIsContainer(QName type)
    {
        boolean isContainer =          
            dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) && 
                    !dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER);
        return isContainer;
    }
    
    /**
     * @return true if this Node is a Document (i.e. with content)
     */
    private boolean getIsDocument(QName type)
    {
        boolean isDocument = dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT);
        return isDocument;
    }
    
    public boolean getEnabled()
    {
        return enabled;
    }
    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public NodeService getNodeService()
    {
        return nodeService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public FileFolderService getFileFolderService()
    {
        return fileFolderService;
    }
    
    public void setRootNode(MTNodesCache2 rootNode)
    {
        this.rootNode = rootNode;
    }
    
    public MTNodesCache2 getRootNode()
    {
        return rootNode;
    }

    @Override
    public boolean activitiesEnabled()
    {
        return activitiesEnabled;
    }
    
    public void setActivitiesEnabled(boolean enabled)
    {
        activitiesEnabled = enabled;
    }
}
