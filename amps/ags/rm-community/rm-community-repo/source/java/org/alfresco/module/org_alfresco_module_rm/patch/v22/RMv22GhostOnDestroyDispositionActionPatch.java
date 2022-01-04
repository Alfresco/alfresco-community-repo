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

package org.alfresco.module.org_alfresco_module_rm.patch.v22;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Sets the ghost on destroy property for existing destroy disposition actions
 * to the value specified in the global properties file
 *
 * @author Mark Hibbins
 * @since 2.2
 */
public class RMv22GhostOnDestroyDispositionActionPatch extends AbstractModulePatch
{
    /** Disposition service */
    private DispositionService dispositionService;

    /** File plan service */
    private FilePlanService filePlanService;

    /** Node service */
    private NodeService nodeService;

    /** Ghost on destroy setting */
    private boolean ghostingEnabled;

    /**
     * @param dispositionService disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * @param filePlanService file plan service
     */
    public void setFilePlanService(FilePlanService filePlanService)
    {
        this.filePlanService = filePlanService;
    }

    /**
     * @param nodeService file plan service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param ghostingEnabled Ghost on destroy setting from
     *            alfresco-global.properties
     */
    public void setGhostingEnabled(boolean ghostingEnabled)
    {
        this.ghostingEnabled = ghostingEnabled;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch#applyInternal()
     */
    @Override
    public void applyInternal()
    {
        Set<NodeRef> filePlans = filePlanService.getFilePlans();
        for (NodeRef filePlan : filePlans)
        {
            processFilePlan(filePlan);
        }
    }

    /**
     * Apply the patch to each file plan
     *
     * @param filePlan
     */
    private void processFilePlan(NodeRef filePlan)
    {
        Set<DispositionSchedule> dispositionSchedules = new HashSet<>();
        getDispositionSchedules(filePlan, dispositionSchedules);
        for (DispositionSchedule dispositionSchedule : dispositionSchedules)
        {
            processDispositionSchedule(dispositionSchedule);
        }
    }

    /**
     * Add the disposition schedule associated with the node ref to the passed
     * set of disposition schedule then call this method recursively for this
     * node's children
     *
     * @param nodeRef
     * @param dispositionSchedules
     */
    private void getDispositionSchedules(NodeRef nodeRef, Set<DispositionSchedule> dispositionSchedules)
    {
        if (filePlanService.isRecordCategory(nodeRef))
        {
            DispositionSchedule dispositionSchedule = this.dispositionService.getDispositionSchedule(nodeRef);
            if (dispositionSchedule != null)
            {
                dispositionSchedules.add(dispositionSchedule);
            }

            List<ChildAssociationRef> children = nodeService.getChildAssocs(nodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
            for (ChildAssociationRef childAssoc : children)
            {
                getDispositionSchedules(childAssoc.getChildRef(), dispositionSchedules);
            }
        }
    }

    /**
     * Patch the specified disposition schedule. To do this add the host on
     * destroy to any action definition that doesn't already have it defined and
     * set the value to the value set in the global properties file. Leave any
     * action definitions that have this property already defined untouched.
     *
     * @param dispositionSchedule
     */
    private void processDispositionSchedule(DispositionSchedule dispositionSchedule)
    {
        List<DispositionActionDefinition> actionDefinitions = dispositionSchedule.getDispositionActionDefinitions();
        for(DispositionActionDefinition actionDefinition : actionDefinitions)
        {
            String actionName = (String) nodeService.getProperty(actionDefinition.getNodeRef(),
                    RecordsManagementModel.PROP_DISPOSITION_ACTION_NAME);
            if ("destroy".equals(actionName))
            {
                // we only want to add the ghost on destroy property to action
                // definitions that do not already have it defined
                String ghostOnDestroyValue = (String) nodeService.getProperty(actionDefinition.getNodeRef(),
                        RecordsManagementModel.PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY);
                if (ghostOnDestroyValue == null)
                {
                    Map<QName, Serializable> props = new HashMap<>(1);
                    props.put(RecordsManagementModel.PROP_DISPOSITION_ACTION_GHOST_ON_DESTROY,
                            this.ghostingEnabled ? "ghost" : "destroy");
                    this.dispositionService.updateDispositionActionDefinition(actionDefinition, props);
                }
            }
        }
    }
}
