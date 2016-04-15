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
package org.alfresco.repo.jscript;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.repository.ScriptLocation;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Classpath script location object.
 * 
 * @author Roy Wetherall
 */
public class ClasspathScriptLocation implements ScriptLocation 
{
	/** Classpath location **/
	private final String location;
	
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
    
	/**
     * @see org.alfresco.service.cmr.repository.ScriptLocation#getPath()
     */
    public String getPath()
    {
        return this.location;
    }
    
    public boolean isCachable()
    {
        return true;
    }

    public boolean isSecure()
    {
        return true;
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
        return this.location.equals(other.location);
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
