
package org.alfresco.repo.jscript.app;

import java.io.Serializable;

/**
 * Interface for returning custom properties
 *
 * @author mikeh
 */
public interface CustomResponse
{
    /**
     * Populates the DocLib webscript response with custom metadata 
     */
    Serializable populate();
}
