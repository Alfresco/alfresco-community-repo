/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.config.source;

import java.io.InputStream;
import java.util.List;

import org.alfresco.config.ConfigException;
import org.alfresco.config.source.UrlConfigSource;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidStoreRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;

/**
 * ConfigSource that looks for a prefix to determine where to look for the config.</br>
 * Valid prefixes are:
 * <ul>
 *   <li><b><storeProtocol>://<storeIdentifier></b> the location provided is a path to a repository file</li>
 * </ul>
 * as well as those defined in the core (UrlConfigSource)
 *
 * Example store URLs
 *   <code>workspace://SpacesStore/${spaces.company_home.childname}/${spaces.dictionary.childname}/${spaces.webclient_extension.childname}/cm:web-client-config-custom.xml</code>
 *   <code>workspace://SpacesStore/app:company_home/app:dictionary/cm:webclient_extension/cm:web-client-config-custom.xml</code> 
 */
public class RepoUrlConfigSource extends UrlConfigSource
{   
    private TenantService tenantService;
    private SearchService searchService;
    private ContentService contentService;
    private NamespaceService namespaceService;
    private NodeService nodeService;
    

    public void setTenantService(TenantService tenantService) 
    {
        this.tenantService = tenantService;
    }
    
    public void setSearchService(SearchService searchService) 
    {
        this.searchService = searchService;
    }
    
    public void setContentService(ContentService contentService) 
    {
        this.contentService = contentService;
    }
    
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    public void setNodeService(NodeService nodeService) 
    {
        this.nodeService = nodeService;
    }
    
    
    public RepoUrlConfigSource(String sourceLocation)
    {
        super(sourceLocation);
    }

    public RepoUrlConfigSource(List<String> sourceLocations)
    {
        super(sourceLocations);
    }
    

    public InputStream getInputStream(String sourceUrl)
    {
        // determine the config source       
        try
        {
            return super.getInputStream(sourceUrl);
        } 
        catch (ConfigException ce)
        {     
            int idx = sourceUrl.indexOf(StoreRef.URI_FILLER); 
            if (idx != -1)
            {
                // assume this is a repository location
                int idx2 = sourceUrl.indexOf("/", idx+3);
                                
                String store = sourceUrl.substring(0, idx2);
                String path = sourceUrl.substring(idx2);
                
                StoreRef storeRef = tenantService.getName(new StoreRef(store));    
                NodeRef rootNode = null;
                
                try 
                {
                    rootNode = nodeService.getRootNode(storeRef);
                } 
                catch (InvalidStoreRefException e)
                {
                    throw ce;
                }
                
                List<NodeRef> nodeRefs = searchService.selectNodes(rootNode, path, null, namespaceService, false);
                
                if (nodeRefs.size() == 0)
                {
                    // if none found, then simply skip
                    return null;
                }
                else if (nodeRefs.size() > 1)
                {
                    // unexpected
                    throw new ConfigException("Found duplicate config sources in the repository " + sourceUrl);
                }

                NodeRef nodeRef = nodeRefs.get(0);
                
                ContentReader cr = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
                                   
                return cr.getContentInputStream();                                                  
            } 
            else
            {
                // not a repository url
                throw ce;
            }
        }
    }
}
