/*
 * #%L
 * Alfresco Repository
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
package org.alfresco.service.cmr.repository;

import java.io.InputStream;
import java.io.Reader;

/**
 * Interface encapsulating the location of a script and providing access to it.
 * 
 * @author Roy Wetherall
 */
public interface ScriptLocation 
{
    /**
     * Returns an input stream to the contents of the script
     * 
     * @return  the input stream
     */
    InputStream getInputStream();
    
	/**
	 * Returns a reader to the contents of the script
	 * 
	 * @return	the reader
	 */
	Reader getReader();
	
	/**
	 * @return unique path of this script location
	 */
	String getPath();
	
	/**
	 * Returns true if the script content is considered cachedable - i.e. classpath located or similar.
	 * Else the content will be compiled/interpreted on every execution i.e. repo content.
	 * 
	 * @return true if the script content is considered cachedable, false otherwise
	 */
	boolean isCachable();
    
    /**
     * Returns true if the script location is considered secure - i.e. on the repository classpath.
     * Secure scripts may access java.* libraries and instantiate pure Java objects directly. Unsecure
     * scripts only have access to pre-configure host objects and cannot access java.* libs.
     * 
     * @return true if the script location is considered secure
     */
    boolean isSecure();
}