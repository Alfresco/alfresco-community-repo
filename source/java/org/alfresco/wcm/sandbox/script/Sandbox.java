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
package org.alfresco.wcm.sandbox.script;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.repo.jscript.ScriptableHashMap;
import org.alfresco.service.cmr.avm.AVMNodeDescriptor;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.wcm.sandbox.SandboxInfo;
import org.alfresco.wcm.sandbox.SandboxService;
import org.alfresco.wcm.webproject.script.WebProject;

/**
 * Sandbox object to expose via JavaScript
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
	 * Submit the modified contents of the webapp within this sandbox
	 */
	public void submitAllWebApp(String webApp, String submitLabel, String submitComment)
	{
		getSandboxService().submitAllWebApp(getSandboxRef(), webApp, submitLabel, submitComment);
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
		getSandboxService().revertAllWebApp(getSandboxRef(), webApp);
	}
	
	/**
	 * Get the snapshots
	 * @param includeSystemGenerated
	 */
	public void getSnapshots(boolean includeSystemGenerated)
	{
		getSandboxService().listSnapshots(getSandboxRef(), includeSystemGenerated);
	}

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
	 * Get the modified assets within this sandbox
	 * @return the list of changed assets
	 */
	public List<Asset> getModifiedAssets()
	{
		List<AVMNodeDescriptor> items = getSandboxService().listChangedItems(getSandboxRef(), true);
        ArrayList<Asset> ret = new ArrayList<Asset>(items.size());
		
		for(AVMNodeDescriptor item : items)
		{
			Asset a = new Asset(this, item);
			ret.add(a);
		}
		return ret;	
	}
	
	/**
	 * Get the modified assets within this sandbox
	 * @return the list of changed assets
	 */
	public List<Asset> getModifiedAssetsWebApp(String webApp)
	{
		List<AVMNodeDescriptor> items = getSandboxService().listChangedItemsWebApp(getSandboxRef(), webApp, true);
        ArrayList<Asset> ret = new ArrayList<Asset>(items.size());
		
		for(AVMNodeDescriptor item : items)
		{
			Asset a = new Asset(this, item);
			ret.add(a);
		}
		return ret;
	}
	
	/**
	 * Submit a list of files
	 */
	public void submitList(List<String> toSubmit, String submitLabel, String submitComment) 
	{
		// TODO - Interface will add string list
		//ss.submitList(sbStoreId, items, submitLabel, submitComment)	
	}
	
	public List<Asset> getAssets(String path)
	{
		return null;
	}
	
	public WebProject getWebproject()
	{
		return this.webproject;
	}
	
	private SandboxService getSandboxService()
	{
	    return webproject.getWebProjects().getSandboxService();
	}
}
