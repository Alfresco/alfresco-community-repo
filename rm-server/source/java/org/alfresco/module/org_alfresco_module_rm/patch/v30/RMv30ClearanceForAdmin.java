/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.patch.v30;

import org.alfresco.module.org_alfresco_module_rm.bootstrap.ClearanceForAdminBootstrapComponent;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;

/**
 * Patch to provide the highest clearance to the admin user.
 *
 * @author tpage
 */
public class RMv30ClearanceForAdmin extends AbstractModulePatch
{
    private ClearanceForAdminBootstrapComponent bootstrapComponent;

    public void setBootstrapComponent(ClearanceForAdminBootstrapComponent bootstrapComponent)
    {
        this.bootstrapComponent = bootstrapComponent;
    }

    /**
     * Give the admin user the maximum clearance.
     *
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        bootstrapComponent.createClearanceForAdmin();
    }
}
