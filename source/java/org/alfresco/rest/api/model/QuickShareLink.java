/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
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
package org.alfresco.rest.api.model;

import java.util.Date;

/**
 * Representation of quick share link
 *
 * The "sharedId" provides a short link/url that is easy to copy/paste/send (via email or other).
 * As of now, these links are public in that they provide unauthenticated access to the
 * node's content and limited metadata info, such as file name and last modifer/modification.
 *
 * In the future, the QuickShareService *could* be enhanced to provide additional features,
 * such as link expiry &/or "password" protection, etc.
 *
 * @author janv
 *
 */
public class QuickShareLink
{
	// unique "short" link (ie. shorter than a guid, 22 vs 36 chars)
	private String sharedId;

	private String nodeId;

	private String name;
	private ContentInfo content;

	protected Date modifiedAt;
	protected UserInfo modifiedByUser;

	protected UserInfo sharedByUser;

	public QuickShareLink()
	{
	}

	public QuickShareLink(String sharedId, String nodeId)
	{
		this.sharedId = sharedId;
		this.nodeId = nodeId;
	}

    public String getSharedId() {
        return sharedId;
    }

	public void setSharedId(String sharedId) {
		this.sharedId = sharedId;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public ContentInfo getContent()
	{
		return content;
	}

	public void setContent(ContentInfo content)
	{
		this.content = content;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Date getModifiedAt()
	{
		return modifiedAt;
	}

	public void setModifiedAt(Date modifiedAt)
	{
		this.modifiedAt = modifiedAt;
	}

	public UserInfo getModifiedByUser()
	{
		return modifiedByUser;
	}

	public void setModifiedByUser(UserInfo modifiedByUser)
	{
		this.modifiedByUser = modifiedByUser;
	}

	public UserInfo getSharedByUser()
	{
		return sharedByUser;
	}

	public void setSharedByUser(UserInfo sharedByUser)
	{
		this.sharedByUser = sharedByUser;
	}

	// eg. for debug logging etc
    @Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("QuickShareLink [sharedId=").append(getSharedId());
		sb.append(", nodeId=").append(getNodeId());
		sb.append(", name=").append(getName());
		sb.append(", modifiedAt=").append(getModifiedAt());
		sb.append(", modifiedByUser=").append(getModifiedByUser());
		sb.append(", sharedByUser=").append(getSharedByUser());
		sb.append(", content=").append(getContent());
		sb.append("]");
		return sb.toString();
	}
}
