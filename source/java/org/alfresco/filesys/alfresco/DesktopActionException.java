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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.filesys.alfresco;

/**
 * Desktop Action Exception Class
 *
 * @author gkspencer
 */
public class DesktopActionException extends Exception {

	private static final long serialVersionUID = 1006648817889605047L;
 
	// Status code
	
	private int m_stsCode;
	
	/**
	 * Class constructor
	 * 
	 * @param sts numeric status code.
	 * @param msg readable error message
	 */
	public DesktopActionException(int sts, String msg)
	{
		super(msg);
		m_stsCode = sts;
	}
	
	/**
     * Class constructor
     * 
     * @param s String
     */
    public DesktopActionException(String s)
    {
        super(s);
    }

    /**
     * Class constructor
     * 
     * @param s String
     * @param ex Exception
     */
    public DesktopActionException(String s, Throwable ex)
    {
        super(s, ex);
    }
    
    /**
     * Return the status code
     * 
     * @return int
     */
    public final int getStatusCode()
    {
    	return m_stsCode;
    }
}
