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

    /**
     * Constructor
     * 
     * @param message       the error message   
     * @param throwable     the cause of the exeption
     * @param objects       message arguments
     */
    public CheckOutCheckInServiceException(Throwable throwable, String message, Object ...objects)
    {
        super(message, objects, throwable);
    }

}
