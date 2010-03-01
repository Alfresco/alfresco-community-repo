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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.extensions.surf.util.ISO8601DateFormat;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.asset.AssetService;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxConstants;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.webproject.script.WebProject;

/**
 * Sandbox object to expose via JavaScript
 * 
 * Provides access to the sandbox metadata and its collection of assets.
 * 
 * @author mrogers
 *
 */
public class Sandbox implements Serializable 
{
	/**
	 * serial version id
	 */
	private static final long serialVersionUID = -9176488061624800911L;

	private SandboxInfo si;	
	private WebProject webproject;

	/*
	 * Constructor from a SandboxInfo
	 */
	public Sandbox(WebProject webproject, SandboxInfo si)
	{
		this.webproject = webproject;
		this.si = si;
	}
	
	public void setName(String name) 
	{
		// read only
	}

	/**
	 * Display name for the sandbox
	 * @return the name of the sandbox
	 */
	public String getName() 
	{
		return si.getName();
	}

	/**
	 * Set the unique reference for this sandbox - no-op, read only
	 * @param sandboxRef
	 */
	public void setSandboxRef(String sandboxRef)
	{
		// read only
	}

	/**
	 * Submit the modified contents of this sandbox
	 */
	public void submitAll(String submitLabel, String submitComment)
	{
		getSandboxService().submitAll(getSandboxRef(), submitLabel, submitComment);
	}

	/**
	 * Revert the specified assets (files and directories) modified contents of this sandbox
	 */
	public void revertAssets(Asset[] files)
	{
		List<String> items = new ArrayList<String>(files.length);
		
		for(int i = 0; i < files.length; i++)
		{
			items.add(i, files[i].getPath());
		}
		
		getSandboxService().revertList(getSandboxRef(), items);
	}
	
	/**
	 * Revert the specified files and directories modified contents of this sandbox
	 */
	public void revert(String[] files)
	{
		List<String> items = new ArrayList<String>(files.length);
		
		for(int i = 0; i < files.length; i++)
		{
			items.add(i, files[i]);
		}
		
		getSandboxService().revertList(getSandboxRef(), items);
	}
	
	/**
	 * Submit the specified assets (files and directories) modified contents of this sandbox
	 */
	public void submitAssets(Asset[] files, String submitLabel, String submitComment)
	{
		List<String> items = new ArrayList<String>(files.length);
		
		for(int i = 0; i < files.length; i++)
		{
			items.add(i, files[i].getPath());
		}
		
		getSandboxService().submitList(getSandboxRef(), items, submitLabel, submitComment);
	}
	
	/**
	 * Submit the specified files and directories modified contents of this sandbox
	 */
	public void submit(String[] files, String submitLabel, String submitComment)
	{
		List<String> items = new ArrayList<String>(files.length);
		
		for(int i = 0; i < files.length; i++)
		{
			items.add(i, files[i]);
		}
		
		getSandboxService().submitList(getSandboxRef(), items, submitLabel, submitComment);
	}
	
	/**
	 * Submit the modified contents of the webapp within this sandbox
	 */
	public void submitAllWebApp(String webApp, String submitLabel, String submitComment)
	{
		getSandboxService().submitWebApp(getSandboxRef(), webApp, submitLabel, submitComment);
	}
	
	/**
	 * Revert all modified contents within this sandbox
	 */
	public void revertAll()
	{
		getSandboxService().revertAll(getSandboxRef());
	}
	
	/**
	 * Revert all modified contents within this sandbox
	 */
	public void revertAllWebApp(String webApp)
	{
		getSandboxService().revertWebApp(getSandboxRef(), webApp);
	}
	
//	/**
//	 * Get the snapshots, Version Descriptors
//	 * @param includeSystemGenerated
//	 */
//	public void getSnapshots(boolean includeSystemGenerated)
//	{
//		//TODO - What is returned?
//		getSandboxService().listSnapshots(getSandboxRef(), includeSystemGenerated);
//	}

	/**
	 * Get the unique reference for this sandbox
	 */ 
	public String getSandboxRef() 
	{
		return si.getSandboxId();
	}
	
	public String getCreator()
	{
		return si.getCreator();
	}
	
	public Date getCreatedDate()
	{
		return si.getCreatedDate();
	}
	
	public String getCreatedDateAsISO8601()
	{
		return ISO8601DateFormat.format(si.getCreatedDate());
	}
	
	/**
	 * Delete this sandbox
	 */
	public void deleteSandbox()
	{
		getSandboxService().deleteSandbox(getSandboxRef());
	}
	
	/*
	 * Save the updates to this sandbox
	 */
	public void save()
	{
		// no read-write params yet ...
	}
	
	/**
	 * Get the store names
	 * @return the list of store names with the "main" store first.
	 */
	public String[] getStoreNames()
	{
		return si.getStoreNames();
	}
	
	/**
	 * Is this an author sandbox ?
	 * @return is this an author sandbox
	 */
	public boolean isAuthorSandbox()
	{
		return si.getSandboxType().equals(SandboxConstants.PROP_SANDBOX_AUTHOR_MAIN);
	}
	
	/**
	 * Is this a staging sandbox ?
	 * @return is this an author sandbox
	 */
	public boolean isStagingSandbox()
	{
		return si.getSandboxType().equals(SandboxConstants.PROP_SANDBOX_STAGING_MAIN);
	}
	
	
	/**
	 * Get the modified assets within this sandbox
	 * @return the list of changed assets
	 */
	public Asset[] getModifiedAssets()
	{
		List<AssetInfo> items = getSandboxService().listChangedAll(getSandboxRef(), true);
        Asset[] ret = new Asset[items.size()];
		
        int i = 0;
		for(AssetInfo item : items)
		{
			ret[i++] = new Asset(this, item);
		}
		return ret;	
	}
	
	/**
	 * Get the specified asset (Either folder or file)
	 * @param path the full path e.g. /www/web_apps/ROOT/index.html
	 * @return the asset or null if it does not exist
	 */
	public Asset getAsset(String path)
	{
		AssetService as = getAssetService();
		AssetInfo item = as.getAsset(getSandboxRef(), path);
		if (item != null)
		{
			Asset newAsset = new Asset(this, item);
			return newAsset;
		}
		return null;
	}
	
	/**
	 * Get the specified asset with a path relative to the specified web app.
	 * @param path e.g. index.html
	 * @param webApp e.g. ROOT 
	 * @return the asset or null if it does not exist
	 */
	public Asset getAssetWebApp(String webApp, String path)
	{
		AssetService as = getAssetService();
		AssetInfo item = as.getAssetWebApp(getSandboxRef(), webApp, path);
		if (item != null)
		{
			Asset newAsset = new Asset(this, item);
			return newAsset;
		}
		return null;

	}
	
	/**
	 * Get the modified assets within this sandbox
	 * @return the list of changed assets
	 */
	public Asset[] getModifiedAssetsWebApp(String webApp)
	{
		List<AssetInfo> items = getSandboxService().listChangedWebApp(getSandboxRef(), webApp, true);
        Asset[] ret = new Asset[items.size()];
        
        int i = 0;
		for(AssetInfo item : items)
		{
			ret[i++] = new Asset(this, item);
		}
		return ret;
	}
	
	/**
	 * Get the web project that owns this sandbox
	 * @return the web project
	 */
	public WebProject getWebproject()
	{
		return this.webproject;
	}
	
	/**
	 * Get the sandbox service
	 * @return the sandbox service
	 */
	private SandboxService getSandboxService()
	{
	    return webproject.getWebProjects().getSandboxService();
	}
	
	/**
	 * Get the asset service
	 * @return the asset service
	 */
	private AssetService getAssetService()
	{
	    return webproject.getWebProjects().getAssetService();
	}
}
