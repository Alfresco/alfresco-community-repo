package org.alfresco.module.org_alfresco_module_rm.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Deprecated records management audit interface methods.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public interface RecordsManagementAuditServiceDeprecated 
{
    /**
     * @deprecated as of 2.1, see {@link #stop(NodeRef)}
     */
    @Deprecated
    void stop();
    
    /**
     * @deprecated as of 2.1, see {@link #clear(NodeRef)}
     */
    @Deprecated
    void clear();    
    
    /**
     * @deprecated as of 2.1, see {@link #isEnabled(NodeRef)}
     */
    @Deprecated
    boolean isEnabled();

    /**
     * @deprecated as of 2.1, see {@link #getDateLastStarted(NodeRef)}
     */
    @Deprecated
    Date getDateLastStarted();
    
    /**
     * @deprecated as of 2.1, see {@link #getDateLastStopped(NodeRef)}
     */
    Date getDateLastStopped();
    
    /**
     * @deprecated as of 2.1
     */
    @Deprecated
    void auditRMAction(RecordsManagementAction action, NodeRef nodeRef, Map<String, Serializable> parameters);

}
