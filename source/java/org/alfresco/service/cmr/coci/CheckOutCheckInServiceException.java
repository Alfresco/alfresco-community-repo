/*
 * Copyright (C) 2005 Alfresco, Inc.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.service.cmr.coci;

import org.alfresco.error.AlfrescoRuntimeException;

/**
 * Version opertaions service exception class
 * 
 * @author Roy Wetherall
 */
public class CheckOutCheckInServiceException extends AlfrescoRuntimeException 
{
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 3258410621186618417L;

	/**
	 * Constructor
	 * 
	 * @param message  the error message
	 */
	public CheckOutCheckInServiceException(String message) 
	{
		super(message);
	}

	/**
	 * Constructor
	 * 
	 * @param message		the error message	
	 * @param throwable		the cause of the exeption
	 */
	public CheckOutCheckInServiceException(String message, Throwable throwable)
	{
		super(message, throwable);
	}
}
