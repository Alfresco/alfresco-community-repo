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

package org.alfresco.module.org_alfresco_module_rm.record;

import static org.alfresco.model.ContentModel.ASPECT_PENDING_DELETE;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.security.ExtendedSecurityService;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.util.ParameterCheck;

/**
 * Inplace record service implementation.
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class InplaceRecordServiceImpl extends ServiceBaseImpl implements InplaceRecordService, RecordsManagementModel
{
    /** Site service */
    private SiteService siteService;

    /** Extended security service */
    private ExtendedSecurityService extendedSecurityService;

    /** File folder service */
    private FileFolderService fileFolderService;

    /**
     * @param siteService site service
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * @param extendedSecurityService extended security service
     */
    public void setExtendedSecurityService(
            ExtendedSecurityService extendedSecurityService)
    {
        this.extendedSecurityService = extendedSecurityService;
    }

    /**
     * @param fileFolderService file folder service
     */
    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.InplaceRecordService#hideRecord(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void hideRecord(final NodeRef nodeRef)
    {
        ParameterCheck.mandatory("NodeRef", nodeRef);

        // do the work of hiding the record as the system user
        authenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                // remove the child association
                NodeRef originatingLocation = (NodeRef) nodeService.getProperty(nodeRef, PROP_RECORD_ORIGINATING_LOCATION);
                
                if (originatingLocation != null)
                {
                    List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);
                    for (ChildAssociationRef childAssociationRef : parentAssocs)
                    {
                        if (!childAssociationRef.isPrimary() &&
                                childAssociationRef.getParentRef().equals(originatingLocation) &&
                                !nodeService.hasAspect(childAssociationRef.getChildRef(), ASPECT_PENDING_DELETE))
                        {
                            nodeService.removeChildAssociation(childAssociationRef);
                            break;
                        }
                    }
    
                    // remove the extended security from the node
                    // this prevents the users from continuing to see the record in searchs and other linked locations
                    extendedSecurityService.remove(nodeRef);
                }

                return null;
            }
        });
    }

    /**
     * @see org.alfresco.module.org_alfresco_module_rm.record.InplaceRecordService#moveRecord(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    public void moveRecord(final NodeRef nodeRef, final NodeRef targetNodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("targetNodeRef", targetNodeRef);

        NodeRef sourceParentNodeRef = null;

        NodeRef originatingLocation = (NodeRef) nodeService.getProperty(nodeRef, PROP_RECORD_ORIGINATING_LOCATION);
        for (ChildAssociationRef parentAssoc : nodeService.getParentAssocs(nodeRef))
        {
            if (!parentAssoc.isPrimary() && parentAssoc.getParentRef().equals(originatingLocation))
            {
                sourceParentNodeRef = parentAssoc.getParentRef();
                break;
            }
        }

        if (sourceParentNodeRef == null)
        {
            throw new AlfrescoRuntimeException("Could not find source parent node reference.");
        }

        SiteInfo sourceSite = siteService.getSite(sourceParentNodeRef);
        SiteInfo targetSite = siteService.getSite(targetNodeRef);

        if (!sourceSite.equals(targetSite))
        {
            throw new AlfrescoRuntimeException("The record can only be moved within the same collaboration site.");
        }

        if (!sourceSite.getSitePreset().equals("site-dashboard"))
        {
            throw new AlfrescoRuntimeException("Only records within a collaboration site can be moved.");
        }

        final NodeRef source = sourceParentNodeRef;

        authenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            @Override
            public Void doWork()
            {
                try
                {
                    // Move the record
                    fileFolderService.moveFrom(nodeRef, source, targetNodeRef, null);

                    // Update the originating location property
                    nodeService.setProperty(nodeRef, PROP_RECORD_ORIGINATING_LOCATION, targetNodeRef);
                }
                catch (FileExistsException | FileNotFoundException ex)
                {
                    throw new AlfrescoRuntimeException("Can't move node: " +  ex);
                }

                return null;
            }
        });
    }
}
