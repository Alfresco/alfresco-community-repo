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

package org.alfresco.module.org_alfresco_module_rm.action.impl;

import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.record.RecordMissingMetadataException;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.node.integrity.IntegrityException;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.ParameterCheck;

/**
 * Declare record action
 *
 * @author Roy Wetherall
 */
public class DeclareRecordAction extends RMActionExecuterAbstractBase
{
    private static final String MISSING_PROPERTIES = "missingProperties";
    
    /** action name */
    public static final String NAME = "declareRecord";

    /** Record service */
    private RecordService recordService;

    /**
     * Sets the record service
     *
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef)
    {
        ParameterCheck.mandatory("actionedUponNodeRef", actionedUponNodeRef);
        try
        {
            recordService.complete(actionedUponNodeRef);
        }
        catch (RecordMissingMetadataException e)
        {
            action.setParameterValue(ActionExecuterAbstractBase.PARAM_RESULT, MISSING_PROPERTIES);
        }
        catch (IntegrityException e)
        {
            // IntegrityException is deliberately ignored here, there should be no action taken
        }

    }
    
}
