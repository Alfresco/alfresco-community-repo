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

package org.alfresco.module.org_alfresco_module_rm.action;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.repo.action.ActionDefinitionImpl;

/**
 * Extended action definition implementation.
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public class RecordsManagementActionDefinitionImpl extends ActionDefinitionImpl implements RecordsManagementActionDefinition
{
    /** generated serial version id */
    private static final long serialVersionUID = -5226538434707253206L;

    /** Applicable kinds */
    private Set<FilePlanComponentKind> applicableKinds;
    
    /**
     * Default constructor.
     * 
     * @param name  action definition name
     */
    public RecordsManagementActionDefinitionImpl(String name)
    {
        super(name);
    }

    /**
     * @param applicableKinds   applicable kinds
     */
    public void setApplicableKinds(Set<FilePlanComponentKind> applicableKinds)
    {
        this.applicableKinds = applicableKinds;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionDefinition#getApplicableKinds()
     */
    @Override
    public Set<FilePlanComponentKind> getApplicableKinds()
    {
        return applicableKinds;
    }    
}
