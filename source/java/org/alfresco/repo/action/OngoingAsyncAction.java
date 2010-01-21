/*
 * Copyright (C) 2005-2009 Alfresco Software Limited.
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
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.action;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.EqualsHelper;

/**
 * This class represents an ongoing asynchronous action.
 */
public class OngoingAsyncAction
{
	private final NodeRef node;
	private final Action action;
	
	public OngoingAsyncAction(NodeRef node, Action action)
	{
		this.node = node;
		this.action = action;
	}

	public NodeRef getNodeRef()
	{
		return node;
	}

	public Action getAction()
	{
		return action;
	}

	@Override
	public boolean equals(Object otherObj)
	{
		if (otherObj == null || !otherObj.getClass().equals(this.getClass()))
		{
			return false;
		}
		OngoingAsyncAction otherNodeBeingActioned = (OngoingAsyncAction)otherObj;
		
        return EqualsHelper.nullSafeEquals(this.node, otherNodeBeingActioned.node) &&
            EqualsHelper.nullSafeEquals(this.action.getActionDefinitionName(), otherNodeBeingActioned.action.getActionDefinitionName());
	}

	@Override
	public int hashCode() {
		return this.node.hashCode() + 7 * this.action.getActionDefinitionName().hashCode();
	}

	@Override
	public String toString()
	{
		StringBuilder msg = new StringBuilder();
		msg.append(node).append(", ").append(action.getActionDefinitionName());
		return msg.toString();
	}
}
