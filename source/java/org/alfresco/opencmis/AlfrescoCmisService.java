package org.alfresco.opencmis;

import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * Extended interface for lifecycle management
 * 
 * @author Derek Hulley
 * @since 4.0
 */
public interface AlfrescoCmisService extends CmisService
{
    /**
     * Called directly before any CMIS method is used
     */
    void beforeCall();
    
    /**
     * Called directly after any CMIS method is used
     */
    void afterCall();
    
    /**
     * Call before the work method and forms the opposite of {@link #close()}.
     * 
     * @param context               the context in which the service must operate
     */
    void open(CallContext context);
}
