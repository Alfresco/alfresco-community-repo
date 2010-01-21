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
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.action;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This filter class is used to identify equivalent create-thumbnail actions.
 * Two create-thumbnail actions are considered equivalent if they are on the same NodeRef and
 * if they have the same thumbnail-name parameter.
 * 
 * @author Neil McErlean
 */
public class CreateThumbnailActionFilter extends AbstractAsynchronousActionFilter
{
	private static final String PARAM_THUMBNAIL_NAME = "thumbnail-name";

	public int compare(OngoingAsyncAction nodeAction1, OngoingAsyncAction nodeAction2)
	{
		NodeRef n1 = nodeAction1.getNodeRef();
		NodeRef n2 = nodeAction2.getNodeRef();
		if (n1.equals(n2) == false)
		{
			return -1;
		}
		else
		{
			String thName1 = (String)nodeAction1.getAction().getParameterValue(PARAM_THUMBNAIL_NAME);
			String thName2 = (String)nodeAction2.getAction().getParameterValue(PARAM_THUMBNAIL_NAME);
			
			return thName1.compareTo(thName2);
		}
	}
}
