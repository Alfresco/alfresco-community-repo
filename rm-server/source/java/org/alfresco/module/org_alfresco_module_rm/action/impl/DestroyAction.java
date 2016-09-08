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
package org.alfresco.module.org_alfresco_module_rm.action.impl;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.model.RenditionModel;
import org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase;
import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionSchedule;
import org.alfresco.repo.content.cleanup.EagerContentStoreCleaner;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;

/**
 * Destroy action.
 *
 * @author Roy Wetherall
 */
public class DestroyAction extends RMDispositionActionExecuterAbstractBase
{
    /** Action name */
    public static final String NAME = "destroy";

    /** Eager content store cleaner */
    private EagerContentStoreCleaner eagerContentStoreCleaner;

    /** Capability service */
    private CapabilityService capabilityService;

    /** Indicates if ghosting is enabled or not */
    private boolean ghostingEnabled = true;

    /**
     * @param eagerContentStoreCleaner eager content store cleaner
     */
    public void setEagerContentStoreCleaner(EagerContentStoreCleaner eagerContentStoreCleaner)
    {
        this.eagerContentStoreCleaner = eagerContentStoreCleaner;
    }

    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param ghostingEnabled   true if ghosting is enabled, false otherwise
     */
    public void setGhostingEnabled(boolean ghostingEnabled)
    {
        this.ghostingEnabled = ghostingEnabled;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#checkNextDispositionAction()
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
        List<NodeRef> records = recordService.getRecords(recordFolder);
        for (NodeRef record : records)
        {
            executeRecordLevelDisposition(action, record);
        }
        if (isGhostOnDestroySetForAction(action, recordFolder))
        {
            nodeService.addAspect(recordFolder, ASPECT_GHOSTED, Collections.<QName, Serializable> emptyMap());
        }
        else
        {
            nodeService.deleteNode(recordFolder);
        }
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.action.RMDispositionActionExecuterAbstractBase#executeRecordLevelDisposition(org.alfresco.service.cmr.action.Action, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    protected void executeRecordLevelDisposition(Action action, NodeRef record)
    {
        // Clear the content
        clearAllContent(record);

        // Clear thumbnail content
        clearThumbnails(record);

        if (isGhostOnDestroySetForAction(action, record))
        {
            // Add the ghosted aspect
            nodeService.addAspect(record, ASPECT_GHOSTED, null);
        }
        else
        {
            // If ghosting is not enabled, delete the node
            nodeService.deleteNode(record);
        }
    }

    /**
     * Clear all the content properties
     *
     * @param nodeRef
     */
    private void clearAllContent(NodeRef nodeRef)
    {
        Set<QName> props = this.nodeService.getProperties(nodeRef).keySet();
        props.retainAll(this.dictionaryService.getAllProperties(DataTypeDefinition.CONTENT));
        for (QName prop : props)
        {
            // Clear the content
            clearContent(nodeRef, prop);

            // Remove the property
            this.nodeService.removeProperty(nodeRef, prop);
        }
    }

    /**
     * Clear all the thumbnail information
     *
     * @param nodeRef
     */
    @SuppressWarnings("deprecation")
    private void clearThumbnails(NodeRef nodeRef)
    {
      // Remove the renditioned aspect (and its properties and associations) if it is present.
      //
      // From Alfresco 3.3 it is the rn:renditioned aspect which defines the
      // child-association being considered in this method.
      // Note also that the cm:thumbnailed aspect extends the rn:renditioned aspect.
      //
      // We want to remove the rn:renditioned aspect, but due to the possibility
      // that there is Alfresco 3.2-era data with the cm:thumbnailed aspect
      // applied, we must consider removing it too.
      if (nodeService.hasAspect(nodeRef, RenditionModel.ASPECT_RENDITIONED) ||
          nodeService.hasAspect(nodeRef, ContentModel.ASPECT_THUMBNAILED))
      {
          // Add the ghosted aspect to all the renditioned children, so that they will not be archived when the
          // renditioned aspect is removed
          Set<QName> childAssocTypes = dictionaryService.getAspect(RenditionModel.ASPECT_RENDITIONED).getChildAssociations().keySet();
          for (ChildAssociationRef child : nodeService.getChildAssocs(nodeRef))
          {
              if (childAssocTypes.contains(child.getTypeQName()))
              {
                  // Clear the content and delete the rendition
                  clearAllContent(child.getChildRef());
                  nodeService.deleteNode(child.getChildRef());
              }
          }
       }
    }

    /**
     * Clear a content property
     *
     * @param nodeRef
     * @param contentProperty
     */
    private void clearContent(NodeRef nodeRef, QName contentProperty)
    {
        // Ensure the content is cleaned at the end of the transaction
        ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, contentProperty);
        if (contentData != null && contentData.getContentUrl() != null)
        {
            eagerContentStoreCleaner.registerOrphanedContentUrl(contentData.getContentUrl(), true);
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
            DispositionSchedule dispositionSchedule = this.dispositionService.getDispositionSchedule(nodeRef);
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
