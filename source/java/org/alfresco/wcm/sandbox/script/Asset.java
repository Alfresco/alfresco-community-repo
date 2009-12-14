/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
package org.alfresco.wcm.sandbox.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.namespace.NamespaceException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxService;

/**
 * WCM Asset in a sandbox exposed over Java Script API.
 * @author mrogers
 *
 */
public class Asset implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5759260478423750966L;
	private AssetInfo asset;
	private Sandbox sandbox;
	private Map<String, String> props;
	
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
						propsX.put(prefixQname.toPrefixString(), propValue.toString());
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
	
//	/**
//	 * Save the properties please note some system properties are protected and cannot be updated.  If you attempt to 
//	 * update a protected property your request will be ignored.
//	 * @param properties
//	 */
//	public void save()
//	{
//		if(props != null)
//		{
//			/**
//			 * Need to map the <String, String> to a <Qname, Serializable>
//			 */
//			NamespaceService ns = getNamespaceService();
//			Map<QName, Serializable> newProps = new HashMap<QName, Serializable>(props.size());
//			for (String key : props.keySet())
//			{
//				String value = props.get(key);
//				QName q = QName.resolveToQName(ns, key);
//				newProps.put(q, value);
//			}
//			getAssetService().setAssetProperties(asset, newProps);
//		}
//	}
	
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
}
