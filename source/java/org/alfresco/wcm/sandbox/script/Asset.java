/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.wcm.sandbox.script;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.repo.dictionary.DictionaryNamespaceComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.avm.AVMBadArgumentException;
import org.alfresco.service.cmr.avm.AVMNotFoundException;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.Content;
import org.springframework.extensions.surf.util.ISO8601DateFormat;

/**
 * WCM Asset in a sandbox exposed over Java Script API.
 * @author mrogers
 *
 */
public class Asset implements Serializable 
{
	private static final QName NAMESPACE_SERVICE = QName.createQName("", "namespaceService");
    /**
	 * 
	 */
	private static final long serialVersionUID = -5759260478423750966L;
	private AssetInfo asset;
	private Sandbox sandbox;
	private Map<String, String> props;
	private Set<QName> updatedProperties = new HashSet<QName>();

	public Asset(Sandbox sandbox, AssetInfo asset) 
	{
		this.sandbox = sandbox;
		this.asset = asset;
	}

	/**
	 * The creator of this asset
	 * @return the creator
	 */
	public String getCreator()
	{
		return asset.getCreator();
	}
	
	public Date getCreatedDate()
	{
		return asset.getCreatedDate();
	}
	
	public long getFileSize()
	{
		return asset.getFileSize();
	}
	
	public String getCreatedDateAsISO8601()
	{
		return ISO8601DateFormat.format(getCreatedDate());
	}
	
	public String getModifier()
	{
		return asset.getModifier();	
	}
	
	public Date getModifiedDate()
	{
		return asset.getModifiedDate();
	}

	public String getModifiedDateAsISO8601()
	{
		return ISO8601DateFormat.format(getModifiedDate());
	}
	
	/**
	 * rename this asset
	 * @param newName
	 */
    public Asset rename(String newName)
    {
    	if(!newName.equals(asset.getName()))
    	{
    		AssetInfo newAsset = getAssetService().renameAsset(asset, newName);
    		this.asset = newAsset;
    	}
    	return this;
    }
    
    /**
     * move this asset
     * @param newPath
     */
    public Asset move(String newPath)
    {
    	if(!newPath.equals(asset.getPath()))
    	{
    		AssetInfo newAsset = getAssetService().moveAsset(asset, newPath);
    		this.asset = newAsset;
    	}
    	return this;
    }
	
	public String getName()
	{
		return asset.getName();
	}
	
	/**
	 * Get the full path of this asset eg. /www/avm_webapps/ROOT/myFile.jpg
	 * @return the path of this asset.
	 */
	public String getPath()
	{
		return asset.getPath();
	}
	
	public boolean isFile()
	{
		return asset.isFile();
	}
	
	public boolean isFolder()
	{
		return asset.isFolder();
	}
	
	public boolean isDeleted()
	{
		return asset.isDeleted();
	}
	
	public boolean isLocked()
	{
		return asset.isLocked();
	}
	
	public String lockOwner()
	{
		return asset.getLockOwner();
	}
	
	public int getVersion()
	{
		return asset.getSandboxVersion();
	}
	
	/**
	 * Get the properties as a key value pair.   The key will be either a local qname e.g. "cm:content" or 
	 * a global qname e.g. "{http://www.alfresco.com/content/1.0}content".
	 * 
	 * Some properties will be updatable, protected properties are not.
	 * 
	 * @return the properties in a key, value pair
	 */
	
	public Map<String, String> getProperties()
	{
		if(props == null) {

			// Note there is something very strange going on with scope which is why there's this guff with propsX
			Map<String, String> propsX = new HashMap<String, String>();
			props = propsX;
			NamespaceService ns = getNamespaceService();
			
			if(!asset.isDeleted())
			{
				Map <QName, Serializable> intprops = getAssetService().getAssetProperties(asset);

				for (QName qname : intprops.keySet())
				{   
					QName prefixQname = qname.getPrefixedQName(ns);
					Serializable propValue = intprops.get(qname);  
					try 
					{
						propsX.put(prefixQname.toPrefixString(), (null == propValue) ? (null):(propValue.toString()));
					} 
					catch (NamespaceException ne)
					{   // No local name, only thing I can do is use the full namke
						propsX.put(qname.toString(), propValue.toString());
					}
				}
			}
		}
		
	    return props; 
	}

    /**
     * Save the properties please note some system properties are protected and cannot be updated. If you attempt to update a protected property your request will be ignored.
     * 
     * @param properties
     */
    public void save()
    {
        if (!updatedProperties.isEmpty() && (null != props))
        {
            Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(props.size());
            QName type = getAssetType();
            DictionaryService dictionaryService = getDictionaryService();
            TypeDefinition typeDefinition = (null != type) ? (dictionaryService.getType(type)) : (null);
            if (null != typeDefinition)
            {
                if (updatedProperties.contains(ContentModel.PROP_NAME))
                {
                    updatedProperties.remove(ContentModel.PROP_NAME);
                    rename(getPropertyValue(ContentModel.PROP_NAME));
                }
                Map<QName, PropertyDefinition> propertyDefinitions = typeDefinition.getProperties();
                for (QName key : updatedProperties)
                {
                    PropertyDefinition propertyDefinition = (propertyDefinitions.containsKey(key)) ? (propertyDefinitions.get(key)) : (dictionaryService.getProperty(key));
                    Serializable value = convertValueToDataType(key, propertyDefinition.getDataType().getName(), getPropertyValue(key));
                    newProps.put(key, value);
                }
                getAssetService().setAssetProperties(asset, newProps);
                updatedProperties.clear();
            }
            else
            {
                throw new AVMNotFoundException("The type property of the current Asset not found");
            }
        }
    }

    private String getPropertyValue(QName key)
    {
        String prefixedQName = completeContentModelQName(key).toPrefixString();
        return props.containsKey(prefixedQName) ? (props.get(prefixedQName)) : (props.get(key.toString()));
    }

    private Serializable convertValueToDataType(QName propertyName, QName dataType, String textualValue)
    {
        Serializable result = null;
        if (null != textualValue)
        {
            try
            {
                if (DataTypeDefinition.BOOLEAN.equals(dataType))
                {
                    result = Boolean.parseBoolean(textualValue);
                }
                else if (DataTypeDefinition.DOUBLE.equals(dataType))
                {
                    result = Double.parseDouble(textualValue);
                }
                else if (DataTypeDefinition.FLOAT.equals(dataType))
                {
                    result = Float.parseFloat(textualValue);
                }
                else if (DataTypeDefinition.INT.equals(dataType))
                {
                    result = Integer.parseInt(textualValue);
                }
                else if (DataTypeDefinition.LONG.equals(dataType))
                {
                    result = Long.parseLong(textualValue);
                }
                else if (DataTypeDefinition.NODE_REF.equals(dataType))
                {
                    result = (NodeRef.isNodeRef(textualValue)) ? (new NodeRef(textualValue)) : (null);
                }
                else if (DataTypeDefinition.QNAME.equals(dataType))
                {
                    result = QName.resolveToQName(getNamespaceService(), textualValue);
                }
                else if (DataTypeDefinition.CONTENT.equals(dataType))
                {
                    result = ContentData.createContentProperty(textualValue);
                }
                else if (DataTypeDefinition.TEXT.equals(dataType) || DataTypeDefinition.MLTEXT.equals(dataType))
                {
                    result = textualValue;
                }
            }
            catch (NumberFormatException e)
            {
                throw new AVMBadArgumentException("Value for the '" + propertyName + "' property is invalid! Conversion error: " + e.toString());
            }
        }
        // TODO: Conversion for other DataTypes
        return result;
    }

    /**
     * @param properties
     * @throws JSONException
     */
    public void setProperties(Object nativeProperties) throws JSONException
    {
        JSONObject properties = (JSONObject) nativeProperties;
        if ((null != asset) && !asset.isDeleted())
        {
            Map<String, String> currentProperties = getProperties();
            if (null == currentProperties)
            {
                throw new AVMNotFoundException("No a property found for the current Asset");
            }
            QName type = getAssetType();
            DictionaryService dictionaryService = getDictionaryService();
            TypeDefinition typeDefinition = (null != type) ? (dictionaryService.getType(type)) : (null);
            if (null != typeDefinition)
            {
                Map<QName, PropertyDefinition> propertyDefinitions = typeDefinition.getProperties();
                for (String key : JSONObject.getNames(properties))
                {
                    QName qName = QName.resolveToQName(getNamespaceService(), key);
                    if (ContentModel.PROP_CONTENT.equals(qName))
                    {
                        updatedProperties.clear();
                        throw new AVMBadArgumentException("The 'Content' property can't be set with the 'setProperties()' method! Use a 'writeContent()' instead");
                    }
                    PropertyDefinition property = (propertyDefinitions.containsKey(qName)) ? (propertyDefinitions.get(qName)) : (dictionaryService.getProperty(qName));
                    if (null != property)
                    {
                        // TODO: Maybe are multi-valued properties operable?
                        if (property.isProtected() || property.isMultiValued())
                        {
                            updatedProperties.clear();
                            throw new AVMBadArgumentException("The '" + key + "' property is not updatable");
                        }
                        Object associatedValue = properties.get(key);
                        qName = completeContentModelQName(qName);
                        currentProperties.put(qName.toPrefixString(), (null != associatedValue) ? (associatedValue.toString()) : (null));
                        updatedProperties.add(property.getName());
                    }
                    else
                    {
                        updatedProperties.clear();
                        throw new AVMNotFoundException("The '" + key + "' property definition can't be found");
                    }
                }
                props = currentProperties;
            }
        }
    }

    private QName completeContentModelQName(QName qName)
    {
        if (qName.getLocalName().equals(qName.getPrefixString()) && NamespaceService.CONTENT_MODEL_1_0_URI.equals(qName.getNamespaceURI()))
        {
            DictionaryNamespaceComponent service = (DictionaryNamespaceComponent) getSandbox().getWebproject().getWebProjects().getServiceRegistry().getService(NAMESPACE_SERVICE);
            return QName.createQName(NamespaceService.CONTENT_MODEL_PREFIX, qName.getLocalName(), service);
        }
        return qName;
    }

    private QName getAssetType()
    {
        final NodeRef assetNodeRef = AVMNodeConverter.ToNodeRef(asset.getSandboxVersion(), asset.getAvmPath());
        final NodeService nodeService = getNodeService();
        return AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<QName>()
        {
            public QName doWork() throws Exception
            {
                RetryingTransactionHelper helper = getSandbox().getWebproject().getWebProjects().getServiceRegistry().getTransactionService().getRetryingTransactionHelper();
                return helper.doInTransaction(new RetryingTransactionCallback<QName>()
                {
                    public QName execute() throws Throwable
                    {
                        return nodeService.getType(assetNodeRef);
                    }
                });
            }
        }, AuthenticationUtil.getFullyAuthenticatedUser());
    }

    /**
     * Updates a content of a current Asset
     * 
     * @param content {@link String} value which represents new textual content
     * @return <code>true</code> if a content has been set without errors
     */
    public boolean writeContent(String content)
    {
        NodeRef assetNodeRef = AVMNodeConverter.ToNodeRef(asset.getSandboxVersion(), asset.getAvmPath());
        ContentService contentService = getSandbox().getWebproject().getWebProjects().getServiceRegistry().getContentService();
        ContentWriter writer = contentService.getWriter(assetNodeRef, ContentModel.PROP_CONTENT, true);
        if ((null != writer) && (null != content))
        {
            writer.setMimetype("text/plain");
            writer.setEncoding("UTF-8");
            writer.putContent(content);
            return true;
        }
        return false;
    }

    /**
     * Updates a content of the current Asset
     * 
     * @param content a {@link Content} value which represents new content
     * @return <code>true</code> if a content has been set without errors
     */
    public boolean writeContent(Content content)
    {
        NodeRef assetNodeRef = AVMNodeConverter.ToNodeRef(asset.getSandboxVersion(), asset.getAvmPath());
        ContentService contentService = getSandbox().getWebproject().getWebProjects().getServiceRegistry().getContentService();
        ContentWriter writer = contentService.getWriter(assetNodeRef, ContentModel.PROP_CONTENT, true);
        if ((null != writer) && (null != content))
        {
            writer.setMimetype(content.getMimetype());
            writer.setEncoding(content.getEncoding());
            writer.putContent(content.getInputStream());
            return true;
        }
        return false;
    }

    /**
     * Returns textual representation of the Asset content
     * 
     * @return content as a text
     * @throws ContentIOException
     * @throws IOException
     */
    public String getContent() throws ContentIOException, IOException
    {
        NodeRef assetNodeRef = AVMNodeConverter.ToNodeRef(asset.getSandboxVersion(), asset.getAvmPath());
        ContentService contentService = getSandbox().getWebproject().getWebProjects().getServiceRegistry().getContentService();
        ContentReader reader = contentService.getReader(assetNodeRef, ContentModel.PROP_CONTENT);
        return (null != reader) ? (reader.getContentString()) : (null);
    }

	/**
	 * Submit this asset to staging
	 * @param submitLabel
	 * @param submitComment
	 */
	public void submit(String submitLabel, String submitComment)
	{
		List<String> items = new ArrayList<String>(1);
		items.add(this.getPath());
		getSandboxService().submitList(getSandbox().getSandboxRef(), items, submitLabel, submitComment);
	}
	
	/**
	 * Delete this asset, after it has been deleted do not use this asset.
	 */
	public void deleteAsset()
	{
		getAssetService().deleteAsset(this.asset);
	}
	
	/**
	 * revert this asset
	 */
	public void revert()
	{
		List<String> items = new ArrayList<String>(1);
		items.add(this.getPath());
		getSandboxService().revertList(getSandbox().getSandboxRef(), items);
	}
	
	/**
	 * Get children of this asset, returns an empty array if there are no children.
	 * Only folders have children.
	 */
	public Asset[] getChildren()
	{
		Asset[] ret = new Asset[0];
		if(asset.isFolder())
		{
			int i = 0;
			List<AssetInfo> assets = getAssetService().listAssets(getSandbox().getSandboxRef(), asset.getPath(), true);
			ret = new Asset[assets.size()];
			for(AssetInfo asset : assets)
			{
				ret[i++]=new Asset(sandbox, asset);
			}
		} 
		return ret;
	}
	
	/**
	 * create a new file with the specified properties and content.
	 * @param name the name of the file
	 * @param stringContent the content of the file.   Can be null.
	 */
	public void createFile(String name, String stringContent)
	{
		 ContentWriter writer = getAssetService().createFile(getSandbox().getSandboxRef(), asset.getPath(), name, null);
		 if(stringContent != null)
		 {	 
			 writer.putContent(stringContent);
		 }
	}
	
	/**
	 * create a new folder
	 * @param name the name of the new folder
	 */
	public void createFolder(String name)
	{
		 getAssetService().createFolder(getSandbox().getSandboxRef(), asset.getPath(), name, null);
	}
	
	/**
	 * Get the parent sandbox which contains this asset
	 * @return the parent sandbox which contains this asset
	 */
	public Sandbox getSandbox()
	{
		return this.sandbox;
	}
	
	/**
	 * @return
	 */
	private SandboxService getSandboxService()
	{
	    return getSandbox().getWebproject().getWebProjects().getSandboxService();
	}
	
	/**
	 * Get the asset service
	 * @return the asset service
	 */
	private AssetService getAssetService()
	{
	    return  getSandbox().getWebproject().getWebProjects().getAssetService();
	}
	
	/**
	 * Get the asset service
	 * @return the asset service
	 */
	private NamespaceService getNamespaceService()
	{
	    return  getSandbox().getWebproject().getWebProjects().getNamespaceService();
	}

    private NodeService getNodeService()
    {
        return getSandbox().getWebproject().getWebProjects().getServiceRegistry().getNodeService();
    }

    private DictionaryService getDictionaryService()
    {
        return getSandbox().getWebproject().getWebProjects().getServiceRegistry().getDictionaryService();
    }
}
