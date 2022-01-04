/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.audit;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Deprecated records management audit interface methods.
 *
 * @author Roy Wetherall
 * @since 2.1
 * @deprecated as of 2.1, see {@link RecordsManagementAuditService}.
 */
@AlfrescoPublicApi
public interface RecordsManagementAuditServiceDeprecated
{
    /**
     * @deprecated as of 2.1, see {@link RecordsManagementAuditService#stopAuditLog(NodeRef)}
     */
    @Deprecated
    void stop();

    /**
     * @deprecated as of 2.1, see {@link RecordsManagementAuditService#clearAuditLog(NodeRef)}
     */
    @Deprecated
    void clear();

    /**
     * @deprecated as of 2.1, see {@link RecordsManagementAuditService#isAuditLogEnabled(NodeRef)}
     */
    @Deprecated
    boolean isEnabled();

    /**
     * @deprecated as of 2.1, see {@link RecordsManagementAuditService#getDateAuditLogLastStarted(NodeRef)}
     */
    @Deprecated
    Date getDateLastStarted();

    /**
     * @deprecated as of 2.1, see {@link RecordsManagementAuditService#getDateLastStopped()}
     */
    Date getDateLastStopped();

    /**
     * @deprecated as of 2.1
     */
    @Deprecated
    void auditRMAction(RecordsManagementAction action, NodeRef nodeRef, Map<String, Serializable> parameters);

}
