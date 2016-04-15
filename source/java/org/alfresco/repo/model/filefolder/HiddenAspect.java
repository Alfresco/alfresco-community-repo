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

import org.alfresco.api.AlfrescoPublicApi;   
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderServiceType;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.FileFilterMode;
import org.alfresco.util.FileFilterMode.Client;
import org.alfresco.util.SearchLanguageConversion;
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
@AlfrescoPublicApi
public class HiddenAspect
{
    private static Log logger = LogFactory.getLog(HiddenAspect.class);
    
    public static Set<QName> HIDDEN_PROPERTIES = new HashSet<QName>();
    
    static
    {
    	HIDDEN_PROPERTIES.add(ContentModel.PROP_CLIENT_CONTROLLED);
    	HIDDEN_PROPERTIES.add(ContentModel.PROP_CASCADE_HIDDEN);
    	HIDDEN_PROPERTIES.add(ContentModel.PROP_CASCADE_INDEX_CONTROL);
    	HIDDEN_PROPERTIES.add(ContentModel.PROP_VISIBILITY_MASK);
    }

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
    private PolicyComponent policyComponent;
    private BehaviourFilter behaviourFilter;

    public HiddenAspect()
    {
    }
    
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setPolicyComponent(PolicyComponent policyComponent)
    {
		this.policyComponent = policyComponent;
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
            this.filters.add(new HiddenFileInfoImpl(filter.getFilter(), filter.getVisibility(), filter.getHiddenAttribute(), filter.cascadeHiddenAspect(), filter.cascadeIndexControlAspect(), filter.isCmisDisableHideConfig()));
        }
    }
    
    public void init()
    {
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
        sp.setQuery("@" + SearchLanguageConversion.escapeLuceneQuery(ContentModel.PROP_NAME.toString()) + ":\"" + name + "\"");
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
     * @param nodeRef NodeRef

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
     * @param nodeRef NodeRef
     */
    public void unhideExplicit(NodeRef nodeRef)
    {
        nodeService.setProperty(nodeRef, ContentModel.PROP_HIDDEN_FLAG, false);
        checkHidden(nodeRef, true, false);
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
    
    private void addHiddenAspect(NodeRef nodeRef, int visibilityMask, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect, boolean clientControlled)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_VISIBILITY_MASK, visibilityMask);
        props.put(ContentModel.PROP_CASCADE_HIDDEN, cascadeHiddenAspect);
        props.put(ContentModel.PROP_CASCADE_INDEX_CONTROL, cascadeIndexControlAspect);
        props.put(ContentModel.PROP_CLIENT_CONTROLLED, clientControlled);
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_HIDDEN, props);

        if (logger.isDebugEnabled())
        {
            logger.debug("Applied hidden marker: " + nodeRef);
        }
    }
    
    private void addHiddenAspect(NodeRef nodeRef, HiddenFileInfo filter)
    {
        Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
        props.put(ContentModel.PROP_VISIBILITY_MASK, filter.getVisibilityMask());
        props.put(ContentModel.PROP_CASCADE_HIDDEN, filter.cascadeHiddenAspect());
        props.put(ContentModel.PROP_CASCADE_INDEX_CONTROL, filter.cascadeIndexControlAspect());
        props.put(ContentModel.PROP_CLIENT_CONTROLLED, filter.isClientControlled());
        nodeService.addAspect(nodeRef, ContentModel.ASPECT_HIDDEN, props);

        if (logger.isDebugEnabled())
        {
            logger.debug("Applied hidden marker: " + nodeRef);
        }
    }
    
    public void removeHiddenAspect(NodeRef nodeRef)
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

    public boolean hasHiddenAspect(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN);
    }
    
    private boolean hasIndexControlAspect(NodeRef nodeRef)
    {
        return nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL);
    }
    
//    private void applyHidden(NodeRef nodeRef, HiddenFileInfo filter, int visibilityMask)
//    {
//    	if(!filter.cascadeHiddenAspect() && !filter.cascadeIndexControlAspect())
//    	{
//    		return;
//    	}
//
//        PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE, null);
//        PagingResults<FileInfo> results = fileFolderService.list(nodeRef, true, true, null, null, pagingRequest);
//        List<FileInfo> files = results.getPage();
//
//        // apply the hidden aspect to all folders and folders and then recursively to all sub-folders, unless the sub-folder
//        // already has the hidden aspect applied (it may have been applied for a different pattern).
//        for(FileInfo file : files)
//        {
//        	NodeRef childNodeRef = file.getNodeRef();
//            if(filter.cascadeHiddenAspect() && !hasHiddenAspect(childNodeRef))
//            {
//                addHiddenAspect(childNodeRef, visibilityMask, false);
//            }
//            
//            if(filter.cascadeIndexControlAspect() && !hasIndexControlAspect(childNodeRef))
//            {
//                addIndexControlAspect(childNodeRef);
//            }
//
//            if(file.isFolder())
//            {
//                applyHidden(file.getNodeRef(), filter, visibilityMask);
//            }
//        }
//    }
    
    private void applyHidden(FileInfo fileInfo, HiddenFileInfo filter)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();

        if(!hasHiddenAspect(nodeRef))
        {
            // the file matches a pattern, apply the hidden and aspect control aspects
            addHiddenAspect(nodeRef, filter);
        }
        else
        {
            nodeService.setProperty(nodeRef, ContentModel.PROP_VISIBILITY_MASK, filter.getVisibilityMask());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CASCADE_HIDDEN, filter.cascadeHiddenAspect());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CASCADE_INDEX_CONTROL, filter.cascadeIndexControlAspect());
        }

        if(!hasIndexControlAspect(nodeRef))
        {
            addIndexControlAspect(nodeRef);
        }
        
        if(fileInfo.isFolder() && (filter.cascadeHiddenAspect() || filter.cascadeIndexControlAspect()))
        {
            PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE, null);
            PagingResults<FileInfo> results = fileFolderService.list(nodeRef, true, true, null, null, pagingRequest);
            List<FileInfo> files = results.getPage();
    
            // apply the hidden aspect to all folders and folders and then recursively to all sub-folders, unless the sub-folder
            // already has the hidden aspect applied (it may have been applied for a different pattern).
            for(FileInfo file : files)
            {
                behaviourFilter.disableBehaviour(file.getNodeRef(), ContentModel.ASPECT_LOCKABLE);
                try
                {
                    applyHidden(file, filter);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(file.getNodeRef(), ContentModel.ASPECT_LOCKABLE);
                }
            }
        }
    }

    private void applyHidden(NodeRef nodeRef, HiddenFileInfo filter, boolean checkChildren)
    {
        if(!hasHiddenAspect(nodeRef))
        {
            addHiddenAspect(nodeRef, filter);
        }
        else
        {
            nodeService.setProperty(nodeRef, ContentModel.PROP_VISIBILITY_MASK, filter.getVisibilityMask());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CASCADE_HIDDEN, filter.cascadeHiddenAspect());
            nodeService.setProperty(nodeRef, ContentModel.PROP_CASCADE_INDEX_CONTROL, filter.cascadeIndexControlAspect());
        }

        if(!hasIndexControlAspect(nodeRef))
        {
            addIndexControlAspect(nodeRef);
        }

        QName typeQName = nodeService.getType(nodeRef);
        FileFolderServiceType type = fileFolderService.getType(typeQName);
        boolean isFolder = type.equals(FileFolderServiceType.FOLDER) || type.equals(FileFolderServiceType.SYSTEM_FOLDER);
        if(isFolder && checkChildren && (filter.cascadeHiddenAspect() || filter.cascadeIndexControlAspect()))
        {
            PagingRequest pagingRequest = new PagingRequest(0, Integer.MAX_VALUE, null);
            PagingResults<FileInfo> results = fileFolderService.list(nodeRef, true, true, null, null, pagingRequest);
            List<FileInfo> files = results.getPage();
    
            // apply the hidden aspect to all folders and folders and then recursively to all sub-folders, unless the sub-folder
            // already has the hidden aspect applied (it may have been applied for a different pattern).
            for(FileInfo file : files)
            {
                behaviourFilter.disableBehaviour(file.getNodeRef(), ContentModel.ASPECT_LOCKABLE);
                try
                {
                    applyHidden(file, filter);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(file.getNodeRef(), ContentModel.ASPECT_LOCKABLE);
                }
            }
        }
    }
    
    public void removeHidden(NodeRef nodeRef)
    {
    	Client saveClient = FileFilterMode.getClient();
    	FileFilterMode.setClient(Client.admin);
    	try
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
                        behaviourFilter.disableBehaviour(file.getNodeRef(), ContentModel.ASPECT_LOCKABLE);
                        try
                        {
                            if (hasHiddenAspect(file.getNodeRef()))
                            {
                                removeHiddenAspect(file.getNodeRef());
                            }
                            if (hasIndexControlAspect(file.getNodeRef()))
                            {
                                removeIndexControlAspect(file.getNodeRef());
                            }
                        }
                        finally
                        {
                            behaviourFilter.enableBehaviour(file.getNodeRef(), ContentModel.ASPECT_LOCKABLE);
                        }
	
	                if(file.isFolder())
	                {
	                    removeHidden(file.getNodeRef());
	                }
	            }
	        }
    	}
    	finally
    	{
    		FileFilterMode.setClient(saveClient);
    	}
    }
    
    private HiddenFileInfo findMatch(NodeRef nodeRef)
    {
        HiddenFileInfo ret = null;
        Path path = null;
        String name = null;

        OUTER: for(HiddenFileInfo filter : filters)
        {
            if (Client.cmis.equals(FileFilterMode.getClient()) && filter instanceof ConfigurableHiddenFileInfo)
            {
                if (((ConfigurableHiddenFileInfo) filter).isCmisDisableHideConfig())
                {
                    continue;
                }
            }
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
     * @param client Client
     * @param visibility Visibility
     * @return the client visibilityMask
     */
    public int getClientVisibilityMask(Client client, Visibility visibility)
    {
        return visibility.getMask() << getClientIndex(client)*2;
    }
    
    /**
     * Checks whether the node is on a hidden path
     *
     * @param nodeRef NodeRef
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
     * @param nodeRef nodeRef
     * @param cascadeHiddenAspect boolean
     * @param cascadeIndexControlAspect boolean
     * @param clientControlled boolean
     */
    public void hideNode(NodeRef nodeRef, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect, boolean clientControlled)
    {
        addHiddenAspect(nodeRef, 0, cascadeHiddenAspect, cascadeIndexControlAspect, clientControlled);
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
     * @param clientVisibilityMask int
     * @param cascadeHiddenAspect boolean
     * @param cascadeIndexControlAspect boolean
     * @param clientControlled boolean
     */
    public void hideNode(NodeRef nodeRef, int clientVisibilityMask, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect, boolean clientControlled)
    {
        addHiddenAspect(nodeRef, clientVisibilityMask, cascadeHiddenAspect, cascadeIndexControlAspect, clientControlled);
        addIndexControlAspect(nodeRef);
    }
    
    /**
     * Searches for nodes in the given store that should be hidden (i.e. match the hidden pattern)
     * and hides them if they are not already hidden.
     * 
     * @param storeRef StoreRef
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
                    hideNode(nodeRef, filter.getVisibilityMask(), true, true, false);
                }
            }
        }
    }
    
    /**
     * Checks whether the file should be hidden and applies the hidden and not indexed aspects if so.
     * 
     * @param fileInfo FileInfo
     * @param both     if true, will check if the node should not be hidden and remove hidden and index control
     *                 aspects if they are present
     * @param  checkChildren boolean
     * @return boolean
     */
    public boolean checkHidden(FileInfo fileInfo, boolean both, boolean checkChildren)
    {
        NodeRef nodeRef = fileInfo.getNodeRef();
        boolean ret = checkHidden(nodeRef, both, checkChildren);
        return ret;
    }

    /**
     * Hides the node by applying the hidden and not indexed aspects. The node will be hidden from clients
     * according to the visibility mask.
     * 
     * @param fileInfo file to make hidden
     * @param visibilityMask int
     * @param cascadeHiddenAspect boolean
     * @param cascadeIndexControlAspect boolean
     * @param clientControlled boolean
     */
    public void hideNode(FileInfoImpl fileInfo, int visibilityMask, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect, boolean clientControlled)
    {
        hideNode(fileInfo.getNodeRef(), visibilityMask, cascadeHiddenAspect, cascadeIndexControlAspect, clientControlled);
        fileInfo.setHidden(true);
    }
    
    private HiddenFileInfo isParentHidden(NodeRef nodeRef)
    {
        HiddenFileInfo info = null;

        ChildAssociationRef childAssocRef = nodeService.getPrimaryParent(nodeRef);
        if(childAssocRef != null)
        {
            NodeRef primaryParentNodeRef = childAssocRef.getParentRef();
            if(primaryParentNodeRef != null)
            {
                boolean isParentHidden = hasHiddenAspect(primaryParentNodeRef);
                if(isParentHidden)
                {
                    final Integer visibilityMask = (Integer)nodeService.getProperty(primaryParentNodeRef, ContentModel.PROP_VISIBILITY_MASK);
                    final Boolean cascadeHidden = (Boolean)nodeService.getProperty(primaryParentNodeRef, ContentModel.PROP_CASCADE_HIDDEN);
                    final Boolean cascadeIndexControl = (Boolean)nodeService.getProperty(primaryParentNodeRef, ContentModel.PROP_CASCADE_HIDDEN);
	    			final Boolean clientControlled = (Boolean)nodeService.getProperty(primaryParentNodeRef, ContentModel.PROP_CLIENT_CONTROLLED);


                    info = new HiddenFileInfo()
                    {
                        @Override
                        public boolean isHidden(String path)
                        {
                            // not checking by path but by hidden aspect, not used in this use case anyway
                            return false;
                        }
                        
                        @Override
                        public int getVisibilityMask()
                        {
                            // default is hidden to all clients if not specified
                            return visibilityMask != null ? visibilityMask.intValue() : 0;
                        }

                        @Override
                        public boolean isClientControlled()
                        {
                            return clientControlled != null ? clientControlled.booleanValue() : false;
                        }
                        
                        @Override
                        public String getFilter()
                        {
                            return null;
                        }

                        @Override
                        public boolean cascadeIndexControlAspect()
                        {
                            return cascadeIndexControl != null ? cascadeIndexControl.booleanValue() : false;
                        }

                        @Override
                        public boolean cascadeHiddenAspect()
                        {
                            return cascadeHidden != null ? cascadeHidden.booleanValue() : false;
                        }
                    };
                }
            }
        }

        return info;
    }
    
    public boolean isClientControlled(NodeRef nodeRef)
    {
    	Boolean clientControlled = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_CLIENT_CONTROLLED);
    	return clientControlled != null && clientControlled.booleanValue();
    }

    /**
     * Checks whether the file should be hidden and applies the hidden and not indexed aspects to it
     * and its children (if cascadeHidden == true). The visibility mask property will determine visibility for specific
     * clients.
     * <p>
     * Can optionally remove the hidden and index control aspects if the name of a node no longer matches the filter.
     * 
     * @param nodeRef NodeRef
     * @param both     if true, will check both if the node should not be hidden and remove hidden and index control
     * 				   aspects if they are present, and if the node should be hidden and add hidden and index control
     * 				   aspects if they are not present.
     * @param checkChildren boolean
     * @return true if the node is hidden, irrespective of the clientVisibility property value.
     */
    public boolean checkHidden(NodeRef nodeRef, boolean both, boolean checkChildren)
    {
        if(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_HIDDEN))
        {
            Boolean isHiddenFlag = (Boolean)nodeService.getProperty(nodeRef, ContentModel.PROP_HIDDEN_FLAG);
            if(isHiddenFlag != null && isHiddenFlag)
            {
                logger.debug("node has hidden flag set");
                // node has hidden flag - we are not going to change anything.
                return true;
            }
        }
        
        boolean isHidden = false;

    	if(hasHiddenAspect(nodeRef) && isClientControlled(nodeRef))
    	{
    		// node is already hidden and client controlled -> hidden
    		isHidden = true;
    	}
    	else
    	{
            HiddenFileInfo info = isParentHidden(nodeRef);
            if(info != null && info.cascadeHiddenAspect())
            {
                // Parent has hidden aspect and cascade == true, so apply hidden aspect to children
                isHidden = true;
                if(!hasHiddenAspect(nodeRef))
                {
                    addHiddenAspect(nodeRef, info);
                }

                if(!hasIndexControlAspect(nodeRef))
                {
                    addIndexControlAspect(nodeRef);
                }
                applyHidden(nodeRef, info, checkChildren);
            }
	    	else
	        {
	    		// apply the "old" behaviour: try to match the node path against one of the registered hidden file patterns.
	    		info = findMatch(nodeRef);
	            if(info != null)
	            {
	                isHidden = true;
	                if(!hasHiddenAspect(nodeRef))
	                {
	                    addHiddenAspect(nodeRef, info);
	                }
	                else
	                {
	                    nodeService.setProperty(nodeRef, ContentModel.PROP_VISIBILITY_MASK, info.getVisibilityMask());
	                    nodeService.setProperty(nodeRef, ContentModel.PROP_CASCADE_HIDDEN, info.cascadeHiddenAspect());
	                    nodeService.setProperty(nodeRef, ContentModel.PROP_CASCADE_INDEX_CONTROL, info.cascadeIndexControlAspect());
	                }

	                if(!hasIndexControlAspect(nodeRef))
	                {
	                    addIndexControlAspect(nodeRef);
	                }

	                applyHidden(nodeRef, info, checkChildren);
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
	        }
        }

        return isHidden;
    }

    /**
     * Gets the visibility constraint for the given client on the given node.
     * 
     * @param client Client
     * @param nodeRef NodeRef
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
                	if(client != null && client.equals(Client.admin))
                	{
                        ret = Visibility.Visible;
                	}
                	else if(visibilityMask.intValue() == 0)
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

    private class HiddenFileInfoImpl implements ConfigurableHiddenFileInfo
    {
        private Pattern filter;
        private Set<Client> clientVisibility = new HashSet<Client>(10);
        private Set<Client> hiddenAttribute = new HashSet<Client>(10);
        private int visibilityMask;
        private boolean cascadeHiddenAspect;
        private boolean cascadeIndexControlAspect;
        private boolean cmisDisableHideConfig;

        public HiddenFileInfoImpl(String regexp, String visibility, String hiddenAttribute, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect)
        {
            this.filter = Pattern.compile(regexp);
            this.cascadeHiddenAspect = cascadeHiddenAspect;
            this.cascadeIndexControlAspect = cascadeIndexControlAspect;
            setVisibility(visibility);
            setHiddenAttribute(hiddenAttribute);
            calculateVisibilityMask();
        }
        
        public HiddenFileInfoImpl(String regexp, String visibility, String hiddenAttribute, boolean cascadeHiddenAspect, boolean cascadeIndexControlAspect, boolean cmisDisableHideConfig)
        {
            this(regexp,visibility,hiddenAttribute, cascadeHiddenAspect, cascadeIndexControlAspect);
            this.cmisDisableHideConfig = cmisDisableHideConfig;
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

		@Override
		public boolean isClientControlled()
		{
			return false;
		}
		
        public boolean isCmisDisableHideConfig()
        {
            return cmisDisableHideConfig;
        }

        public void setCmisDisableHideConfig(boolean cmisDisableHideConfig)
        {
            this.cmisDisableHideConfig = cmisDisableHideConfig;
        }
    }
}
