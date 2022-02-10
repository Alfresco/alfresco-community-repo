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

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.content.ContentDestructionComponent;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.record.InplaceRecordService;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionService;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;

/**
 * Destroy action.
 *
 * @author Roy Wetherall
 */
public class DestroyAction extends RMDispositionActionExecuterAbstractBase
{
    /** Action name */
    public static final String NAME = "destroy";

    /** content destruction component */
    private ContentDestructionComponent contentDestructionComponent;

    /** Capability service */
    private CapabilityService capabilityService;

    /** Recordable version service */
    private RecordableVersionService recordableVersionService;

    /** Inplace record service */
    private InplaceRecordService inplaceRecordService;

    /** Indicates if ghosting is enabled or not */
    private boolean ghostingEnabled = true;

    /**
     * @param contentDestructionComponent   content destruction component
     */
    public void setContentDestructionComponent(ContentDestructionComponent contentDestructionComponent)
    {
        this.contentDestructionComponent = contentDestructionComponent;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param recordableVersionService  recordable version service
     */
    public void setRecordableVersionService(RecordableVersionService recordableVersionService)
    {
        this.recordableVersionService = recordableVersionService;
    }

    /**
     * @param inplaceRecordService  inplace record service
     */
    public void setInplaceRecordService(InplaceRecordService inplaceRecordService)
    {
        this.inplaceRecordService = inplaceRecordService;
    }

    /**
     * @param ghostingEnabled   true if ghosting is enabled, false otherwise
     */
    public void setGhostingEnabled(boolean ghostingEnabled)
    {
        this.ghostingEnabled = ghostingEnabled;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#checkNextDispositionAction(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean checkNextDispositionAction(NodeRef actionedUponNodeRef)
    {
        return checkForDestroyRecordsCapability(actionedUponNodeRef);
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#checkEligibility(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected boolean checkEligibility(NodeRef actionedUponNodeRef)
    {
        return checkForDestroyRecordsCapability(actionedUponNodeRef);
    }

    /**
     *
     * @param actionedUponNodeRef
     * @return
     */
    private boolean checkForDestroyRecordsCapability(NodeRef actionedUponNodeRef)
    {
        boolean result = true;
        if (AccessStatus.ALLOWED.equals(capabilityService.getCapability("DestroyRecords").hasPermission(actionedUponNodeRef)))
        {
            result = false;
        }
        return result;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordFolderLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordFolderLevelDisposition(Action action, NodeRef recordFolder)
    {
        List<NodeRef> records = getRecordService().getRecords(recordFolder);
        for (NodeRef record : records)
        {
            executeRecordLevelDisposition(action, record);
        }
        if (isGhostOnDestroySetForAction(action, recordFolder))
        {
            // add aspect
            getNodeService().addAspect(recordFolder, ASPECT_GHOSTED, Collections.<QName, Serializable> emptyMap());
        }
        else
        {
            // just delete the node
            getNodeService().deleteNode(recordFolder);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        if (isGhostOnDestroySetForAction(action, record))
        {
            // mark version as destroyed
            Version version = recordableVersionService.getRecordedVersion(record);
            if (version != null)
            {
                recordableVersionService.destroyRecordedVersion(version);
            }

            // Hide from inplace users to give the impression of destruction
            inplaceRecordService.hideRecord(record);

            // Add the ghosted aspect
            getNodeService().addAspect(record, ASPECT_GHOSTED, null);

            // destroy content
            contentDestructionComponent.destroyContent(record);
        }
        else
        {
            // just delete the node
            getNodeService().deleteNode(record);
        }
    }

    /**
     * Return true if the ghost on destroy property is set against the
     * definition for the passed action on the specified node
     *
     * @param action
     * @param nodeRef
     * @return
     */
    private boolean isGhostOnDestroySetForAction(Action action, NodeRef nodeRef)
    {
        boolean ghostOnDestroy = this.ghostingEnabled;
        String actionDefinitionName = action.getActionDefinitionName();
        if (!StringUtils.isEmpty(actionDefinitionName))
        {
            DispositionSchedule dispositionSchedule = this.getDispositionService().getDispositionSchedule(nodeRef);
            if (dispositionSchedule != null)
            {
                DispositionActionDefinition actionDefinition = dispositionSchedule
                        .getDispositionActionDefinitionByName(actionDefinitionName);
                if (actionDefinition != null)
                {
                    String ghostOnDestroyProperty = actionDefinition.getGhostOnDestroy();
                    if (ghostOnDestroyProperty != null)
                    {
                        ghostOnDestroy = "ghost".equals(actionDefinition.getGhostOnDestroy());
                    }
                }
            }
        }
        return ghostOnDestroy;
    }
}
