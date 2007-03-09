/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
package org.alfresco.repo.jscript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.repository.ScriptException;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.alfresco.util.ParameterCheck;

/**
 * Classpath script location object.
 * 
 * @author Roy Wetherall
 *
 */
public class ClasspathScriptLocation implements ScriptLocation 
{
	/** Classpath location **/
	private String location;
	
	/**
	 * Constructor
	 * 
	 * @param location	the classpath location
	 */
	public ClasspathScriptLocation(String location)
	{
		ParameterCheck.mandatory("Location", location);
		this.location = location;
	}
    
    /**
     * @see org.alfresco.service.cmr.repository.ScriptLocation#getInputStream()
     */
    public InputStream getInputStream()
    {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(location);
        if (stream == null)
        {
            throw new AlfrescoRuntimeException("Unable to load classpath resource: " + location);
        }
        return stream;
    }

	/**
	 * @see org.alfresco.service.cmr.repository.ScriptLocation#getReader()
	 */
	public Reader getReader() 
	{
		Reader reader = null;
        try
        {
            InputStream stream = getClass().getClassLoader().getResourceAsStream(location);
            if (stream == null)
            {
                throw new AlfrescoRuntimeException("Unable to load classpath resource: " + location);
            }
            reader = new InputStreamReader(stream);
        }
        catch (Throwable err)
        {
            throw new ScriptException("Failed to load classpath resource '" + location + "': " + err.getMessage(), err);
        }
        
        return reader;
	}
	
	@Override
    public boolean equals(Object obj)
    {
        if (obj == this)
        {
            return true;
        }
        else if (obj == null || !(obj instanceof ClasspathScriptLocation))
        {
            return false;
        }
        ClasspathScriptLocation other = (ClasspathScriptLocation)obj;
        return  this.location.equals(other.location);
    }

    @Override
    public int hashCode()
    {
        return 37 * this.location.hashCode();
    }

    @Override
    public String toString()
    {
        return this.location.toString();
    }
}
