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
package org.alfresco.module.org_alfresco_module_rm.patch.v33;

import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.repo.domain.propval.PropertyStringValueEntity;

/**
 * Patch to update values for addToHold and removeFromHold event types
 * <p>
 * See: https://issues.alfresco.com/jira/browse/RM-7098
 *
 * @author Ramona Popa
 * @since 3.3
 */
public class RMv33HoldAuditEntryValuesPatch extends AbstractModulePatch
{

    /**
     * Services
     */
    private RecordsManagementQueryDAO recordsManagementQueryDAO;

    public void setRecordsManagementQueryDAO(RecordsManagementQueryDAO recordsManagementQueryDAO)
    {
        this.recordsManagementQueryDAO = recordsManagementQueryDAO;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     *
     * Updates the property string value entities for addToHold, removeFromHold and deleteHold audit event types
     */
    @Override
    public void applyInternal()
    {
        updatePropertyStringValueEntity("addToHold", "Add To Hold");
        updatePropertyStringValueEntity("removeFromHold", "Remove From Hold");
        updatePropertyStringValueEntity("deleteHold", "Delete Hold");
    }

    private void updatePropertyStringValueEntity(String fromStringValue, String toStringValue)
    {
        PropertyStringValueEntity propertyStringValueEntity = recordsManagementQueryDAO.getPropertyStringValueEntity(fromStringValue);
        if (propertyStringValueEntity != null)
        {
            propertyStringValueEntity.setValue(toStringValue);
            recordsManagementQueryDAO.updatePropertyStringValueEntity(propertyStringValueEntity);
        }
    }

}
