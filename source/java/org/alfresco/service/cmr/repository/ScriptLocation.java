/**
 * 
 */
package org.alfresco.service.cmr.repository;

import java.io.InputStream;
import java.io.Reader;

/**
 * Interface encapsulating the location of a script and provding access to it.
 * 
 * @author Roy Wetherall
 *
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
}
