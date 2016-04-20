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