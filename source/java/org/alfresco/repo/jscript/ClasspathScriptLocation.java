/**
 * 
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
