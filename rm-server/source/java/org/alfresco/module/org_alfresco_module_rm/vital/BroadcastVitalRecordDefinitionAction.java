/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.vital;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.FilePlanAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Action to implement the consequences of a change to the value of the VitalRecordDefinition properties. When the
 * VitalRecordIndicator or the reviewPeriod properties are changed on a record container, then any descendant folders or
 * records must be updated as a consequence. Descendant folders should have their reviewPeriods and/or
 * vitalRecordIndicators updated to match the new value. Descendant records should have their reviewAsOf date updated.
 * 
 * @author Neil McErlean
 */
public class BroadcastVitalRecordDefinitionAction extends RMActionExecuterAbstractBase
{
    private FilePlanAuthenticationService filePlanAuthenticationService;
    
    public void setFilePlanAuthenticationService(FilePlanAuthenticationService filePlanAuthenticationService)
    {
        this.filePlanAuthenticationService = filePlanAuthenticationService;
    }
    
    /**
     * @see org.alfresco.repo.action.executer.ActionExecuterAbstractBase#executeImpl(org.alfresco.service.cmr.action.Action,
     *      org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeImpl(Action action, final NodeRef actionedUponNodeRef)
    {
        filePlanAuthenticationService.runAsRmAdmin(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                propagateChangeToChildrenOf(actionedUponNodeRef);
                return null;
            }
        });        
    }

    /**
     * Propagates the changes to the children of the node specified.
     * 
     * @param actionedUponNodeRef   actioned upon node reference
     */
    private void propagateChangeToChildrenOf(NodeRef actionedUponNodeRef)
    {
        Map<QName, Serializable> parentProps = nodeService.getProperties(actionedUponNodeRef);
        boolean parentVri = (Boolean) parentProps.get(PROP_VITAL_RECORD_INDICATOR);
        Period parentReviewPeriod = (Period) parentProps.get(PROP_REVIEW_PERIOD);

        List<ChildAssociationRef> assocs = this.nodeService.getChildAssocs(actionedUponNodeRef, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL);
        for (ChildAssociationRef nextAssoc : assocs)
        {
            NodeRef nextChild = nextAssoc.getChildRef();

            // If the child is a record, then the VitalRecord aspect needs to be applied or updated
            if (recordService.isRecord(nextChild))
            {
                if (parentVri)
                {
                    VitalRecordDefinition vrDefn = vitalRecordService.getVitalRecordDefinition(nextChild);
                    Map<QName, Serializable> aspectProps = new HashMap<QName, Serializable>();
                    aspectProps.put(PROP_REVIEW_AS_OF, vrDefn.getNextReviewDate());

                    nodeService.addAspect(nextChild, RecordsManagementModel.ASPECT_VITAL_RECORD, aspectProps);
                }
                else
                {
                    nodeService.removeAspect(nextChild, RecordsManagementModel.ASPECT_VITAL_RECORD);
                }
            }
            else
            // copy the vitalRecordDefinition properties from the parent to the child
            {
                Map<QName, Serializable> childProps = nodeService.getProperties(nextChild);
                childProps.put(PROP_REVIEW_PERIOD, parentReviewPeriod);
                childProps.put(PROP_VITAL_RECORD_INDICATOR, parentVri);
                nodeService.setProperties(nextChild, childProps);
            }

            // Recurse down the containment hierarchy to all containers
            if (recordService.isRecord(nextChild) == false)
            {
                this.propagateChangeToChildrenOf(nextChild);
            }
        }
    }
}
