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

package org.alfresco.module.org_alfresco_module_rm.patch.v23;

import org.alfresco.module.org_alfresco_module_rm.bootstrap.RecordContributorsGroupBootstrapComponent;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;

/**
 * RM v2.3 patch that creates the record contributors group.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RMv23RecordContributorsGroupPatch extends AbstractModulePatch
{
    /** record contributors group bootstrap component */
    private RecordContributorsGroupBootstrapComponent recordContributorsGroupBootstrapComponent;
    
    /**
     * @param recordContributorsGroupBootstrapComponent record contributors group bootstrap component
     */
    public void setRecordContributorsGroupBootstrapComponent(RecordContributorsGroupBootstrapComponent recordContributorsGroupBootstrapComponent)
    {
        this.recordContributorsGroupBootstrapComponent = recordContributorsGroupBootstrapComponent;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        // create record contributors group
        recordContributorsGroupBootstrapComponent.createRecordContributorsGroup();
    }
    
}
