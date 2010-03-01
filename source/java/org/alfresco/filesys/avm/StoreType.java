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
package org.alfresco.filesys.avm;

/**
 * Store Types Class
 * 
 * @author gkspencer
 */
public class StoreType {

	// Store types
	
	public static final int Normal			= 0;
	
    // Site data store types
    
    public static final int SiteStore       = 1;
    
	// Web project store types
	
	public static final int WebAuthorMain		= 2;
	public static final int WebAuthorPreview	= 3;
	public static final int WebStagingPreview	= 4;
	public static final int WebStagingMain      = 5;
	
	// Store type strings
	
	private static final String[] _types = { "Normal", "SiteStore", "AuthorMain", "AuthorPreview", "StagingPreview", "StagingMain" };
	
	/**
	 * Return a store type as a string
	 * 
	 * @param typ int
	 * @return String
	 */
	public final static String asString( int typ)
	{
		if ( typ < 0 || typ > _types.length)
			return "Invalid";
		return _types[ typ];
	}
}
