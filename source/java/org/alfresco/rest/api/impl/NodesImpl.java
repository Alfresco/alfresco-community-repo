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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.CMISNodeInfoImpl;
import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.Node;
import org.alfresco.rest.framework.core.exceptions.EntityNotFoundException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;

/**
 * Centralises access to node services and maps between representations.
 * 
 * @author steveglover
 * @since publicapi1.0
 */
public class NodesImpl implements Nodes
{
    private static enum Type
    {
    	// Note: ordered
    	DOCUMENT, FOLDER;
    };
    
	private NodeService nodeService;
    private DictionaryService dictionaryService;
    private CMISConnector cmisConnector;

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

        return new Node(nodeRef, nodeService.getProperties(nodeRef));
    }
    
    public Node getNode(NodeRef nodeRef)
    {
        return new Node(nodeRef, nodeService.getProperties(nodeRef));
    }
    
    private Type getType(NodeRef nodeRef)
    {
        QName type = nodeService.getType(nodeRef);
    	boolean isContainer = Boolean.valueOf((dictionaryService.isSubClass(type, ContentModel.TYPE_FOLDER) == true &&
    			!dictionaryService.isSubClass(type, ContentModel.TYPE_SYSTEM_FOLDER)));
    	return isContainer ? Type.FOLDER : Type.DOCUMENT;
    }

	// TODO filter CMIS properties
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
    
    /**
     * Returns the public api representation of a document.
     * 
     * Note: properties are modelled after the OpenCMIS node properties
     */
    public Document getDocument(NodeRef nodeRef)
    {
    	Type type = getType(nodeRef);
    	if(type.equals(Type.DOCUMENT))
    	{
    		Properties properties = getCMISProperties(nodeRef);
    		Document doc = new Document(nodeRef, properties);
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
    	if(type.equals(Type.FOLDER))
    	{
    		Properties properties = getCMISProperties(nodeRef);
    		Folder folder = new Folder(nodeRef, properties);
    		return folder;
    	}
    	else
    	{
    		throw new InvalidArgumentException("Node is not a folder");
    	}
    }
}
