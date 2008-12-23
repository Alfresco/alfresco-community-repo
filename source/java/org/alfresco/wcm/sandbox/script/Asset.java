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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.wcm.asset.AssetInfo;
import org.alfresco.wcm.sandbox.SandboxService;

/**
 * WCM Asset in a sandbox exposed over Java Script API.
 * @author mrogers
 *
 */
public class Asset 
{
	private AssetInfo asset;
	private Sandbox sandbox;
	
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
	
	/*
	public String getAssetRef()
	{
		return asset.getGuid();
	}
	*/
	
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
	
	public boolean isDirectory()
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
	
	/* TODO review
	public int getVersion()
	{
		return asset.getVersion();
	}
	*/
	
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
	 * revert this asset
	 */
	public void revert()
	{
		List<String> items = new ArrayList<String>(1);
		items.add(this.getPath());
		getSandboxService().revertList(getSandbox().getSandboxRef(), items);
	}
	
	/**
	 * Get the parent sandbox which contains this asset
	 * @return the parent sandbox which contains this asset
	 */
	public Sandbox getSandbox()
	{
		return this.sandbox;
	}
	
	private SandboxService getSandboxService()
	{
	    return getSandbox().getWebproject().getWebProjects().getSandboxService();
	}
}
