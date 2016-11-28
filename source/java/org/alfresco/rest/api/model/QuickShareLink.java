/*
 * #%L
 * Alfresco Remote API
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
package org.alfresco.rest.api.model;

import java.util.Date;
import java.util.List;

/**
 * Representation of quick share link
 *
 * The shared link id provides a short id that can be part of a short app url that is easy to
 * copy/paste/send (via email or other).
 *
 * As of now, these shared links are public in that they provide unauthenticated access to the
 * node's content and limited metadata info, such as file name and last modifier/modification.
 *
 * In the future, the QuickShareService *could* be enhanced to provide additional features,
 * such as link expiry &/or "password" protection, etc.
 *
 * @author janv
 *
 */
public class QuickShareLink
{
	// unique short id (ie. shorter than a guid, 22 vs 36 chars)
	private String sharedId;

    private Date expiresAt;

	private String nodeId;

	private String name;
	private ContentInfo content;

	private Date modifiedAt;
	private UserInfo modifiedByUser;

	private UserInfo sharedByUser;

	private List<String> allowableOperations;


	public QuickShareLink()
	{
	}

	public QuickShareLink(String sharedId, String nodeId)
	{
		this.sharedId = sharedId;
		this.nodeId = nodeId;
	}

    public String getId() {
        return sharedId;
    }

	public void setId(String sharedId) {
		this.sharedId = sharedId;
	}

    public Date getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt)
    {
        this.expiresAt = expiresAt;
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

	public List<String> getAllowableOperations()
	{
		return allowableOperations;
	}

	public void setAllowableOperations(List<String> allowableOperations)
	{
		this.allowableOperations = allowableOperations;
	}

	// eg. for debug logging etc
    @Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("QuickShareLink [id=").append(getId());
		sb.append(", nodeId=").append(getNodeId());
		sb.append(", name=").append(getName());
		sb.append(", modifiedAt=").append(getModifiedAt());
		sb.append(", modifiedByUser=").append(getModifiedByUser());
		sb.append(", sharedByUser=").append(getSharedByUser());
		sb.append(", content=").append(getContent());
		sb.append(", allowableOperations=").append(getAllowableOperations());
        sb.append(", expiresAt=").append(getExpiresAt());
		sb.append("]");
		return sb.toString();
	}
}
