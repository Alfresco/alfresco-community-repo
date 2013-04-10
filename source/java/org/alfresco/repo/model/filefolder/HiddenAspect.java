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
package org.alfresco.repo.model.filefolder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.search.impl.lucene.LuceneQueryParser;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode.Client;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Functionality relating to hidden files and folders.
 * 
 * Support for nodes marked as hidden but with visibility constraints for specific clients. A node
 * can have the hidden aspect applied, which means that the node is hidden. However, 
 * for specific clients it can be defined whether the node is visible or will have its hidden attribute
 * set in FileInfo.
 *
 */
public class HiddenAspect
{
    private static Log logger = LogFactory.getLog(HiddenAspect.class);

    public static enum Visibility
    {
        NotVisible, Visible, HiddenAttribute;
        
        public int getMask()
        {
            if(this == Visible)
            {
                return 2;
            }
            else if(this == HiddenAttribute)
            {
                return 1;
            }
            else if(this == NotVisible)
            {
                return 0;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        
        public static Visibility getVisibility(int mask)
        {
            if(mask == 2)
            {
                return Visible;
            }
            else if(mask == 1)
            {
                return HiddenAttribute;
            }
            else if(mask == 0)
            {
                return NotVisible;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    };

    private List<HiddenFileInfo> filters = new ArrayList<HiddenFileInfo>(10);
    
    private NodeService nodeService;
    private FileFolderService fileFolderService;
    private SearchService searchService;

    public HiddenAspect()
    {
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }
    
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    public void setPatterns(List<HiddenFileFilter> filters)
    {
        for(HiddenFileFilter filter : filters)
        {
            this.filters.add(new HiddenFileInfoImpl(filter.getFilter(), filter.getVisibility(), filter.getHiddenAttribute(), filter.cascadeHiddenAspect(), filter.cascadeIndexControlAspect()));
        }
    }
    
    public List<HiddenFileInfo> getPatterns()
    {
        return filters;
    }
    
    public Client[] getClients()
    {
        return Client.values();
    }

    private ResultSet searchForName(StoreRef storeRef, String name)
    {
        SearchParameters sp = new SearchParameters();
        sp.addStore(storeRef);
        sp.setLanguage("lucene");
        sp.setQuery("@" + LuceneQueryParser.escape(ContentModel.PROP_NAME.toString()) + ":\"" + name + "\"");
        sp.addLocale(new Locale("en"));
        return searchService.query(sp);
    }

    private Integer getClientIndex(Client client)
    {
        return client.ordinal();
    }

    private void addIndexControlAspect(NodeRef nodeRef)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
        props.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
        props.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL, props);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Applied index control marker: " + nodeRef);
        }
    }

    private void removeIndexControlAspect(NodeRef nodeRef)
    {
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL);

        if (logger.isDebugEnabled())
        {
            logger.debug("Removed index control marker: " + nodeRef);
        }
    }
    
    /**
     * Mark this node as hidden regardless of any name/pattern/matching rules.   Following this call the node 
     * will be hidden.
     * 
     * If the node is already hidden will do nothing.
     * 
     * @param nodeRef

     */
    public void hideNodeExplicit(NodeRef nodeRef)
    {
        int mask = 0;
        mask |= getClientVisibilityMask(Client.cifs, Visibility.HiddenAttribute);
        mask |= getClientVisibilityMask(Client.webdav, Visibility.Visible);
        mask |= getClientVisibilityMask(Client.nfs, Visibility.Visible);
        mask |= getClientVisibilityMask(Client.ftp, Visibility.Visible);

        addHiddenAspect(nodeRef, mask, true);
    }
    
    /**
     * Remove the explicit hiding of a node.  Following this call the node may or may not remain hidden based upon the other 
     * properties of the node.
     * 
     * @param nodeRef
     */
    public void unhideExplicit(NodeRef nodeRef)
    {
        nodeService.setProperty(nodeRef, ContentModel.PROP_HIDDEN_FLAG, false);
        checkHidden(nodeRef, true);
    }
    
    private void addHiddenAspect(NodeRef nodeRef, int visibilityMask, boolean explicit)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_VISIBILITY_MASK, visibilityMask);
        props.put(ContentModel.PROP_HIDDEN_FLAG, explicit);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_HIDDEN, props);

        if (logger.isDebugEnabled())
        {
            logger.debug("Applied hidden marker: " + nodeRef);
        }
    }
    
    private void removeHiddenAspect(NodeRef nodeRef)
    {
        // Remove the aspect
        nodeService.removeAspect(nodeRef, ContentModel.ASPECT_HIDDEN);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Removed hidden marker: " + nodeRef);
        }        
    }

    private Visibility getVisibility(Integer mask, Client client)
    {
        if(mask == null || mask.intValue() == 0)
        {
            return Visibility.NotVisible;
        }
        
        mask = (mask.intValue() >> (getClientIndex(client))*2) & 3;

        return Visibility.getVisibility(mask);
    }

    /*
     * Determines whether the path matches any one of the hidden file patterns and, if so,
     * returns the matching pattern.
     * 
     * @param path
     * @return
     */
    private HiddenFileInfo isHidden(String name)
    {
        // check against all the filters
        HiddenFileInfo matched = null;

        for(HiddenFileInfo filter : filters)
        {
            if(filter.isHidden(name))
            {
                matched = filter;
                break;
            }
        }

        return matched;
    }

    private boolean hasHiddenAspect(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN);
    }
    
    private boolean hasIndexControlAspect(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL);
    }
    
    private void applyHidden(NodeRef nodeRef, HiddenFileInfo filter, int visibilityMask)
    {
    	if(!filter.cascadeHiddenAspect() && !filter.cascadeIndexControlAspect())
    	{
    		return;
    	}

        PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE, null);
        PagingResults<FileInfo> results = fileFolderService.list(nodeRef, true, true, null, null, pagingRequest);
        List<FileInfo> files = results.getPage();

        // apply the hidden aspect to all folders and folders and then recursively to all sub-folders, unless the sub-folder
        // already has the hidden aspect applied (it may have been applied for a different pattern).
        for(FileInfo file : files)
        {
        	NodeRef childNodeRef = file.getNodeRef();
            if(filter.cascadeHiddenAspect() && !hasHiddenAspect(childNodeRef))
            {
                addHiddenAspect(childNodeRef, visibilityMask, false);
            }
            
            if(filter.cascadeIndexControlAspect() && !hasIndexControlAspect(childNodeRef))
            {
                addIndexControlAspect(childNodeRef);
            }

            if(file.isFolder())
            {
                applyHidden(file.getNodeRef(), filter, visibilityMask);
            }
        }
    }
    
    private void removeHidden(NodeRef nodeRef)
    {
        PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE, null);
        PagingResults<FileInfo> results = fileFolderService.list(nodeRef, true, true, null, null, pagingRequest);
        List<FileInfo> files = results.getPage();

        for(FileInfo file : files)
        {
            String name = (String)nodeService.getProperty(file.getNodeRef(), ContentModel.PROP_NAME);
            // remove hidden aspect only if it doesn't match a hidden pattern
            if(isHidden(name) == null)
            {
                removeHiddenAspect(file.getNodeRef());
                removeIndexControlAspect(file.getNodeRef());

                if(file.isFolder())
                {
                    removeHidden(file.getNodeRef());
                }
            }
        }
    }
    
    private HiddenFileInfo findMatch(NodeRef nodeRef)
    {
        HiddenFileInfo ret = null;
        Path path = null;
        String name = null;

        OUTER: for(HiddenFileInfo filter : filters)
        {
        	if(filter.cascadeHiddenAspect() || filter.cascadeIndexControlAspect())
        	{
        		if(path == null)
        		{
        			path = nodeService.getPath(nodeRef);
        		}

                // TODO would be nice to check each part of the path in turn, bailing out if a match is found
                Iterator<Element> it = path.iterator();
                while(it.hasNext())
                {
                    Path.ChildAssocElement elem = (Path.ChildAssocElement)it.next();
                    QName qname = elem.getRef().getQName();
                    if(qname != null)
                    {
            			if(filter.isHidden(qname.getLocalName()))
            			{
            				ret = filter;
                            break OUTER;
                        }
                    }
        		}
        	}
        	else
        	{
        		if(name == null)
        		{
        			name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
        		}

	            if(filter.isHidden(name))
	            {
	            	ret = filter;
	            	break;
	            }
        	}
        }

        return ret;
    }

    /**
     * getClientVisibilityMap
     * 
     * @param client
     * @param visibility
     * @return the client visibilityMask
     */
    public int getClientVisibilityMask(Client client, Visibility visibility)
    {
        return visibility.getMask() << getClientIndex(client)*2;
    }
    
    /**
     * Checks whether the node is on a hidden path
     *
     * @param nodeRef
     * @return the matching filter, or null if no match
     */
    public HiddenFileInfo onHiddenPath(NodeRef nodeRef)
    {
        HiddenFileInfo ret = null;
        // TODO would be nice to check each part of the path in turn, bailing out if a match is found
        Path path = nodeService.getPath(nodeRef);
        nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

        Iterator<Element> it = path.iterator();
        while(it.hasNext())
        {
            Path.ChildAssocElement elem = (Path.ChildAssocElement)it.next();
            QName qname = elem.getRef().getQName();
            if(qname != null)
            {
                ret = isHidden(qname.getLocalName());
                if(ret != null)
                {
                    break;
                }
            }
        }

        return ret;
    }

    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from all clients.
     * 
     * @param client
     * @param fileInfo
     * @return
     */
    public void hideNode(NodeRef nodeRef)
    {
        addHiddenAspect(nodeRef, 0, false);
        addIndexControlAspect(nodeRef);
    }
    
    /**
     * Removes the hidden and index contol aspect.   Reverses the effect of calling hideNode(NodeRef nodeRef)
     * 
     * @param nodeRef the node to show
     * @param cascade true to cascade to all descendents of this node
     */
    public void showNode(NodeRef nodeRef, boolean cascade)
    {
        removeHiddenAspect(nodeRef);
        removeIndexControlAspect(nodeRef);
        
        if(cascade)
        {
	        for(ChildAssociationRef childRef : nodeService.getChildAssocs(nodeRef))
	        {
	        	showNode(childRef.getChildRef(), cascade);
	        }
        }
    }
    
    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from clients
     * according to the visibility mask.
     * 
     * @param nodeRef the node to hide
     * @param clientVisibilityMask
     */
    public void hideNode(NodeRef nodeRef, int clientVisibilityMask)
    {
        addHiddenAspect(nodeRef, clientVisibilityMask, true);
        addIndexControlAspect(nodeRef);
    }
    
    /**
     * Searches for nodes in the given store that should be hidden (i.e. match the hidden pattern)
     * and hides them if they are not already hidden.
     * 
     * @param storeRef
     */
    public void checkHidden(StoreRef storeRef)
    {
        for(HiddenFileInfo filter : filters)
        {
            String pattern = filter.getFilter();

            ResultSet rs = searchForName(storeRef, pattern);
            for(NodeRef nodeRef : rs.getNodeRefs())
            {
                if(!hasHiddenAspect(nodeRef))
                {
                    hideNode(nodeRef, filter.getVisibilityMask());
                }
            }
        }
    }
    
    /**
     * Checks whether the file should be hidden based upon whether its name maches a set of filters and applies the hidden 
     * and not indexed aspects if so.
     * <p>
     * Can optionally remove the hidden and index control aspects if the name of a node no longer matches the filter.
     * 
     * @param fileInfo
     * @param both     if true, will check if the node should not be hidden and remove hidden and index control
     * 				   aspects if they are present
     * @return
     */
    public HiddenFileInfo checkHidden(FileInfoImpl fileInfo, boolean both)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();
        HiddenFileInfo hiddenFileInfo = checkHidden(nodeRef, both);
        if(hiddenFileInfo != null)
        {
            fileInfo.setHidden(true);
        }
        return hiddenFileInfo;
    }

    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from clients
     * according to the visibility mask.
     * 
     * @see getClientVisibilityMask()
     * @param fileInfo, file to make hidden
     * @param visibilityMask
     */
    public void hideNode(FileInfoImpl fileInfo, int visibilityMask)
    {
        hideNode(fileInfo.getNodeRef(), visibilityMask);
        fileInfo.setHidden(true);
    }
    
    /**
     * Checks whether the file should be hidden based upon whether its name maches a set of filters and applies the hidden 
     * and not indexed aspects if so.
     * <p>
     * Can optionally remove the hidden and index control aspects if the name of a node no longer matches the filter.
     * 
     * @param nodeRef
     * @param both     if true, will check both if the node should not be hidden and remove hidden and index control
     * 				   aspects if they are present, and if the node should be hidden and add hidden and index control
     * 				   aspects if they are not present.
     * @return HiddenFileInfo
     */
    public HiddenFileInfo checkHidden(NodeRef nodeRef, boolean both)
    {   
        if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
        {
            Boolean isHiddenFlag = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_HIDDEN_FLAG);
            if(isHiddenFlag != null && isHiddenFlag)
            {
                logger.debug("node has hidden flag set");
                // node has hidden flag - we are not going to change anything.
                return null;
            }
        }
            
        HiddenFileInfo filter = findMatch(nodeRef);
        if(filter != null)
        {
            int visibilityMask = filter.getVisibilityMask();

            if(!hasHiddenAspect(nodeRef))
            {
                // the file matches a pattern, apply the hidden and aspect control aspects
                addHiddenAspect(nodeRef, visibilityMask, false);
            }
                
            if(!hasIndexControlAspect(nodeRef))
            {
                addIndexControlAspect(nodeRef);
            }

            applyHidden(nodeRef, filter, visibilityMask);
        }
        else if(both)
        {
            // the file does not match the pattern, ensure that the hidden and index control aspects are not present
            if(hasHiddenAspect(nodeRef))
            {
                removeHiddenAspect(nodeRef);
            }

            if(hasIndexControlAspect(nodeRef))
            {
                removeIndexControlAspect(nodeRef);
            }

            removeHidden(nodeRef);
        }

        return filter;
    }

    /**
     * Gets the visibility constraint for the given client on the given node.
     * 
     * @param client
     * @param nodeRef
     * 
     * @return the visibility constraint for the given client and node
     */
    public Visibility getVisibility(Client client, NodeRef nodeRef)
    {
        Visibility ret = Visibility.Visible;

        if (! AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
            {
                Integer visibilityMask = (Integer)nodeService.getProperty(nodeRef, ContentModel.PROP_VISIBILITY_MASK);
                if (visibilityMask != null)
                {
                    if(visibilityMask.intValue() == 0)
                    {
                        ret = Visibility.NotVisible;
                    }
                    else if(client == null)
                    {
                        ret = Visibility.NotVisible;
                    }
                    else
                    {
                        ret = getVisibility(visibilityMask.intValue(), client);
                    }
                }
                else
                {
                    // no visibility mask property, so retain backwards compatibility with 3.4 hidden aspect behaviour
                    if(client == Client.cifs)
                    {
                        ret = Visibility.HiddenAttribute;
                    }
                    else if(client == Client.webdav || client == Client.nfs || client == Client.imap)
                    {
                        ret = Visibility.Visible;
                    }
                    else
                    {
                        ret = Visibility.NotVisible;
                    }
                }
            }
        }
        return ret;
    }

    private class HiddenFileInfoImpl implements HiddenFileInfo
    {
        private Pattern filter;
        private Set<Client> clientVisibility = new HashSet<Client>(10);
        private Set<Client> hiddenAttribute = new HashSet<Client>(10);
        private int visibilityMask;
        private boolean cascadeHiddenAspect;
        private boolean cascadeIndexControlAspect;

        public HiddenFileInfoImpl(String regexp, String visibility, String hiddenAttribute, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect)
        {
            this.filter = Pattern.compile(regexp);
            this.cascadeHiddenAspect = cascadeHiddenAspect;
            this.cascadeIndexControlAspect = cascadeIndexControlAspect;
            setVisibility(visibility);
            setHiddenAttribute(hiddenAttribute);
            calculateVisibilityMask();
        }

        private void setVisibility(String visibility)
        {
            if(visibility != null && !visibility.equals(""))
            {
                for(String clientStr : visibility.split(","))
                {
                    Client client = Client.getClient(clientStr);
                    this.clientVisibility.add(client);
                }
            }
        }
        
        private void setHiddenAttribute(String hiddenAttribute)
        {
            if(hiddenAttribute != null && !hiddenAttribute.equals(""))
            {
                for(String clientStr : hiddenAttribute.split(","))
                {
                    Client client = Client.getClient(clientStr);
                    this.hiddenAttribute.add(client);
                }
            }
        }
        
        private void calculateVisibilityMask()
        {
            visibilityMask = 0;
            for(Client client : getClients())
            {
                if(clientVisibility.contains(client))
                {
                    visibilityMask |= getClientVisibilityMask(client, Visibility.Visible);
                }
                else if(hiddenAttribute.contains(client))
                {
                    visibilityMask |= getClientVisibilityMask(client, Visibility.HiddenAttribute);
                }
                else
                {
                    visibilityMask |= getClientVisibilityMask(client, Visibility.NotVisible);
                }
            }
        }

        public String getFilter()
        {
            return filter.pattern();
        }

        public int getVisibilityMask()
        {
            return visibilityMask;
        }

        public boolean isHidden(String path)
        {
            return filter.matcher(path).matches();
        }
        
        public boolean cascadeHiddenAspect()
        {
        	return cascadeHiddenAspect;
        }
        
        public boolean cascadeIndexControlAspect()
        {
        	return cascadeIndexControlAspect;
        }
    }
}
