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
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Indicates whether the given disposition action 'may' be scheduled in the future
 *
 * @author Roy Wetherall
 */
public class MayBeScheduledCapabilityCondition extends AbstractCapabilityCondition
{
    /** Disposition action */
    private String dispositionAction;

    /** Disposition service */
    private DispositionService dispositionService;

    /**
     * @param dispositionAction     disposition action
     */
    public void setDispositionAction(String dispositionAction)
    {
        this.dispositionAction = dispositionAction;
    }

    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.declarative.CapabilityCondition#evaluate(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        boolean result = false;

        DispositionSchedule dispositionSchedule = dispositionService.getDispositionSchedule(nodeRef);
        if (dispositionSchedule != null && checkDispositionLevel(nodeRef, dispositionSchedule))
        {
            for (DispositionActionDefinition dispositionActionDefinition : dispositionSchedule.getDispositionActionDefinitions())
            {
                if (dispositionActionDefinition.getName().equals(dispositionAction))
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Checks the disposition level
     *
     * @param nodeRef
     * @param dispositionSchedule
     * @return
     */
    private boolean checkDispositionLevel(NodeRef nodeRef, DispositionSchedule dispositionSchedule)
    {
        boolean result = false;
        boolean isRecordLevelDisposition = dispositionSchedule.isRecordLevelDisposition();
        if (recordService.isRecord(nodeRef) && isRecordLevelDisposition)
        {
            result = true;
        }
        else if (recordFolderService.isRecordFolder(nodeRef) && !isRecordLevelDisposition)

        {
            result = true;
        }
        return result;
    }
}
