/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
