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
package org.alfresco.service.cmr.invitation;

/**
 * The current user has attempted to do something that they do not have 
 * the rights to do.
 */
public class InvitationExceptionForbidden extends InvitationException 
{

	public InvitationExceptionForbidden(String msg, Object[] args) {
		super(msg, args);
	}
	
	public InvitationExceptionForbidden(String msgId) {
		super(msgId);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3083631235637184401L;

}
