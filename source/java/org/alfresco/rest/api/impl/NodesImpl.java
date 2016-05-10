/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.rest.api.impl;

import java.io.Serializable;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.model.Repository;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Path.Element;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Centralises access to file/folder/node services and maps between representations.
 * 
 * @author steveglover
 * @author janv
 * 
 * @since publicapi1.0
 */
public class NodesImpl implements Nodes
{
    private static enum Type
    {
    	// Note: ordered
    	DOCUMENT, FOLDER;
    };
    
    private final static String PATH_ROOT = "-root-";
    private final static String PATH_MY = "-my-";
    
	private NodeService nodeService;
    private DictionaryService dictionaryService;
    private CMISConnector cmisConnector;
    private FileFolderService fileFolderService;
    private Repository repositoryHelper;
    private NamespaceService namespaceService;
    private PermissionService permissionService;

	public void setDictionaryService(DictionaryService dictionaryService)
	{
		this.dictionaryService = dictionaryService;
	}

	public void setNodeService(NodeService nodeService)
	{
		this.nodeService = nodeService;
	}
	
	public void setCmisConnector(CMISConnector cmisConnector)
	{
		this.cmisConnector = cmisConnector;
	}
	
	public void setFileFolderService(FileFolderService fileFolderService)
	{
		this.fileFolderService = fileFolderService;
	}
	
	public void setRepositoryHelper(Repository repositoryHelper)
	{
		this.repositoryHelper = repositoryHelper;
	}
	
	public void setNamespaceService(NamespaceService namespaceService)
	{
		this.namespaceService = namespaceService;
	}
	
	public void setPermissionService(PermissionService permissionService)
	{
		this.permissionService = permissionService;
	}

	/*
	 * 
	 * Note: assumes workspace://SpacesStore
	 */
	public NodeRef validateNode(String nodeId)
	{
		return validateNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
	}

	public NodeRef validateNode(StoreRef storeRef, String nodeId)
	{
		String versionLabel = null;

		int idx = nodeId.indexOf(";");
		if(idx != -1)
		{
			versionLabel = nodeId.substring(idx + 1);
			nodeId = nodeId.substring(0, idx);
			if(versionLabel.equals("pwc"))
			{
				// TODO correct exception?
				throw new EntityNotFoundException(nodeId);
			}
		}

		NodeRef nodeRef = new NodeRef(storeRef, nodeId);
		return validateNode(nodeRef);
	}

	public NodeRef validateNode(NodeRef nodeRef)
	{
		if(!nodeService.exists(nodeRef))
		{
    		throw new EntityNotFoundException(nodeRef.getId());
		}

		return nodeRef;
	}

	public boolean nodeMatches(NodeRef nodeRef, Set<QName> expectedTypes, Set<QName> excludedTypes)
	{
		if(!nodeService.exists(nodeRef))
		{
    		throw new EntityNotFoundException(nodeRef.getId());
		}

		QName type = nodeService.getType(nodeRef);

		Set<QName> allExpectedTypes = new HashSet<QName>();
		if(expectedTypes != null)
		{
			for(QName expectedType : expectedTypes)
			{
				allExpectedTypes.addAll(dictionaryService.getSubTypes(expectedType, true));
			}
		}

		Set<QName> allExcludedTypes = new HashSet<QName>();
		if(excludedTypes != null)
		{
			for(QName excludedType : excludedTypes)
			{
				allExcludedTypes.addAll(dictionaryService.getSubTypes(excludedType, true));
			}
		}

		boolean inExpected = allExpectedTypes.contains(type);
		boolean excluded = allExcludedTypes.contains(type);
		return(inExpected && !excluded);
	}
	
    public Node getNode(String nodeId)
    {
    	NodeRef nodeRef = validateNode(nodeId);

        return new Node(nodeRef, nodeService.getProperties(nodeRef), namespaceService);
    }
    
    public Node getNode(NodeRef nodeRef)
    {
        return new Node(nodeRef, nodeService.getProperties(nodeRef), namespaceService);
    }
    
    private Type getType(NodeRef nodeRef)
    {
        return getType(nodeService.getType(nodeRef));
    }
    
    private Type getType(QName type)
    {
    	boolean isContainer = Boolean.valueOf((dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true &&
    			!dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER)));
    	return isContainer ? Type.FOLDER : Type.DOCUMENT;
    }

    /*
	// TODO filter CMIS properties
    // TODO review & optimise - do we really need to go via CMIS properties !?
    private Properties getCMISProperties(NodeRef nodeRef)
    {
		CMISNodeInfoImpl nodeInfo = cmisConnector.createNodeInfo(nodeRef);
		final Properties properties = cmisConnector.getNodeProperties(nodeInfo, null);

		// fake the title property, which CMIS doesn't give us
		String title = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_TITLE);
		final PropertyStringImpl titleProp = new PropertyStringImpl(ContentModel.PROP_TITLE.toString(), title);
		Properties wrapProperties = new Properties()
		{
			@Override
			public List<CmisExtensionElement> getExtensions()
			{
				return properties.getExtensions();
			}

			@Override
			public void setExtensions(List<CmisExtensionElement> extensions)
			{
				properties.setExtensions(extensions);
			}

			@Override
			public Map<String, PropertyData<?>> getProperties()
			{
				Map<String, PropertyData<?>> updatedProperties = new HashMap<String, PropertyData<?>>(properties.getProperties());
				updatedProperties.put(titleProp.getId(), titleProp);
				return updatedProperties;
			}

			@Override
			public List<PropertyData<?>> getPropertyList()
			{
				List<PropertyData<?>> propertyList = new ArrayList<PropertyData<?>>(properties.getPropertyList());
				propertyList.add(titleProp);
				return propertyList;
			}
		};

		return wrapProperties;
    }
    */
    
    /**
     * Returns the public api representation of a document.
     * 
     * Note: properties are modelled after the OpenCMIS node properties
     */
    public Document getDocument(NodeRef nodeRef)
    {
    	Type type = getType(nodeRef);
    	if (type.equals(Type.DOCUMENT))
    	{
    		//Properties properties = getCMISProperties(nodeRef);
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
    		Document doc = new Document(nodeRef, properties, namespaceService);
    		return doc;
    	}
    	else
    	{
    		throw new InvalidArgumentException("Node is not a file");
    	}
    }
    
    /**
     * Returns the public api representation of a folder.
     * 
     *  Note: properties are modelled after the OpenCMIS node properties
     */
    public Folder getFolder(NodeRef nodeRef)
    {
    	Type type = getType(nodeRef);
    	if (type.equals(Type.FOLDER))
    	{
    		//Properties properties = getCMISProperties(nodeRef);
            Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);
    		Folder folder = new Folder(nodeRef, properties, namespaceService);
    		return folder;
    	}
    	else
    	{
    		throw new InvalidArgumentException("Node is not a folder");
    	}
    }
    
    private NodeRef validateOrLookupNode(String nodeId, String path) {
		final NodeRef parentNodeRef;
		if (nodeId.equals(PATH_ROOT)) 
		{
			parentNodeRef = repositoryHelper.getCompanyHome();
		}
		else if (nodeId.equals(PATH_MY)) 
		{
			NodeRef person = repositoryHelper.getPerson();
	        if (person == null) 
	        {
	        	throw new IllegalArgumentException("Unexpected: cannot use "+PATH_MY);
	        }
	        parentNodeRef = repositoryHelper.getUserHome(person);
		}
		else 
		{
			parentNodeRef = validateNode(nodeId);
		}
		return parentNodeRef;
    }
    
    public Node getFolderOrDocument(String nodeId,  Parameters parameters)
    {
    	String path = parameters.getParameter("path");
    	
    	boolean incPrimaryPath = false;
    	String str = parameters.getParameter("incPrimaryPath");
    	if (str != null) {
    		incPrimaryPath = new Boolean(str);
    	}
    	
    	NodeRef nodeRef = validateOrLookupNode(nodeId, path);
    	QName typeQName = nodeService.getType(nodeRef);
		return getFolderOrDocument(nodeRef, typeQName, incPrimaryPath);
    }
    
    private Node getFolderOrDocument(NodeRef nodeRef, QName typeQName,boolean incPrimaryPath)
    {
    	String primaryPath = null;
        if (incPrimaryPath)
        {
            org.alfresco.service.cmr.repository.Path pp = nodeService.getPath(nodeRef);
            
            // Remove "app:company_home" (2nd element)
            int ppSize = pp.size();
            if (ppSize > 1) {
            	if (ppSize == 2) {
            		pp = pp.subPath(0, 0);
            	}
            	else {
            		Element rootElement = pp.get(0);
            		pp = pp.subPath(2, ppSize-1).prepend(rootElement);
            	}
            }
            
            primaryPath = pp.toDisplayPath(nodeService, permissionService); // note: slower (hence optional when getting node info)
        }
    	
        Node node = null;
        Type type = getType(typeQName);

        Map<QName, Serializable> properties = nodeService.getProperties(nodeRef);

		if (type.equals(Type.DOCUMENT))
    	{
    		//Properties properties = getCMISProperties(nodeRef);
    		node = new Document(nodeRef, properties, namespaceService);
    	}
		else if (type.equals(Type.FOLDER))
    	{
			// container/folder
    		//Properties properties = getCMISProperties(nodeRef);
    		node = new Folder(nodeRef, properties, namespaceService);
    	}
    	else
    	{
    		throw new InvalidArgumentException("Node is not a folder or file");
    	}
		
		node.setType(typeQName.toPrefixString(namespaceService));
		node.setPrimaryPath(primaryPath); // optional - can be null
		return node;
    }
    
    public CollectionWithPagingInfo<Node> getChildren(String parentFolderNodeId, Parameters parameters)
    {
    	// TODO consider using: where=(exists(target/file)) / where=(exists(target/file)) 
    	//       instead of:    includeFiles=true / includeFolders=true
    	
    	boolean includeFolders = true;
    	String str = parameters.getParameter("includeFolders");
        if (str != null) {
        	includeFolders = new Boolean(str);
        }
        
    	boolean includeFiles = true;
    	str = parameters.getParameter("includeFiles");
        if (str != null) {
        	includeFiles = new Boolean(str);
        }
        
        String path = parameters.getParameter("path");
    	
    	Paging paging = parameters.getPaging();
    	final NodeRef parentNodeRef = validateOrLookupNode(parentFolderNodeId, path);
    	
        final Set<QName> folders = new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER));
        if (! nodeMatches(parentNodeRef, folders, null))
        {
            throw new InvalidArgumentException("NodeId of folder is expected");
        }

    	PagingRequest pagingRequest = Util.getPagingRequest(paging);
    	
        final PagingResults<FileInfo> pagingResults = fileFolderService.list(parentNodeRef, includeFiles, includeFolders, null, null, pagingRequest);
        
		final List<FileInfo> page = pagingResults.getPage();
		List<Node> nodes = new AbstractList<Node>()
		{
			@Override
			public Node get(int index)
			{
				FileInfo fInfo = page.get(index);
				return getFolderOrDocument(fInfo.getNodeRef(), fInfo.getType(), false);
			}

			@Override
			public int size()
			{
				return page.size();
			}
		};

        return CollectionWithPagingInfo.asPaged(paging, nodes, pagingResults.hasMoreItems(), pagingResults.getTotalResultCount().getFirst());
    }
    
    public void deleteNode(String nodeId)
    {
    	NodeRef nodeRef = validateNode(nodeId);
        fileFolderService.delete(nodeRef);
    }

	public Folder createFolder(String parentFolderNodeId, Folder folderInfo, Parameters parameters)
	{
		final NodeRef parentNodeRef = validateNode(parentFolderNodeId);

		final Set<QName> folders = new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER));
		if (! nodeMatches(parentNodeRef, folders, null))
		{
			throw new InvalidArgumentException("NodeId of folder is expected");
		}

		String folderName = folderInfo.getName();
		String folderType = folderInfo.getType();
		if (folderType == null) {
			folderType = "cm:folder";
		}

		QName folderTypeQName = QName.resolveToQName(namespaceService, folderType);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>(10);

        props.put(ContentModel.PROP_NAME, folderName);

        String title = folderInfo.getTitle();
        if ((title != null) && (! title.isEmpty())) {
            props.put(ContentModel.PROP_TITLE, title);
        }

        String description = folderInfo.getDescription();
        if ((description != null) && (! description.isEmpty())) {
            props.put(ContentModel.PROP_DESCRIPTION, description);
        }

        // TODO other custom properties !!

        QName assocQName = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(folderName));

        NodeRef nodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName, folderTypeQName, props).getChildRef();

        return (Folder) getFolderOrDocument(nodeRef.getId(), parameters);
	}

    public Node updateNode(String nodeId, Node nodeInfo, Parameters parameters) {

        final NodeRef nodeRef = validateNode(nodeId);

        final Set<QName> fileOrFolder = new HashSet<>(Arrays.asList(ContentModel.TYPE_FOLDER, ContentModel.TYPE_CONTENT));
        if (! nodeMatches(nodeRef, fileOrFolder, null))
        {
            throw new InvalidArgumentException("NodeId of file or folder is expected");
        }

        Map<QName, Serializable> props = new HashMap<>(10);

        String name = nodeInfo.getName();
        if ((name != null) && (! name.isEmpty())) {
            // note: this is equivalent of a rename within target folder
            props.put(ContentModel.PROP_NAME, name);
        }

        String title = nodeInfo.getTitle();
        if ((title != null) && (! title.isEmpty())) {
            props.put(ContentModel.PROP_TITLE, title);
        }

        String description = nodeInfo.getDescription();
        if ((description != null) && (! description.isEmpty())) {
            props.put(ContentModel.PROP_DESCRIPTION, description);
        }

        if (props.size() > 0) {
            nodeService.addProperties(nodeRef, props);
        }

        return getFolderOrDocument(nodeRef.getId(), parameters);
    }
}
