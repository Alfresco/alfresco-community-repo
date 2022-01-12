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
package org.alfresco.module.org_alfresco_module_rm.util;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.query.RecordsManagementQueryDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;

import java.util.Set;

/**
 * Utility class to duplicate the content of a node without triggering the audit or versioning behaviours
 * @author Ross Gale
 * @since 2.7.2
 */
public class ContentBinDuplicationUtility extends ServiceBaseImpl
{
    /**
     * Behaviour filter
     */
    private BehaviourFilter behaviourFilter;

    /**
     * Provides methods for accessing and transforming content.
     */
    private ContentService contentService;

    /** Records Management Query DAO */
    private RecordsManagementQueryDAO recordsManagementQueryDAO;

    /**
     * Setter for behaviour filter
     * @param behaviourFilter BehaviourFilter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Setter for content service
     * @param contentService ContentService
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Setter for the Records Management QueryDAO
     *
     * @param recordsManagementQueryDAO The RM query DAO to set
     */
    public void setRecordsManagementQueryDAO(RecordsManagementQueryDAO recordsManagementQueryDAO)
    {
        this.recordsManagementQueryDAO = recordsManagementQueryDAO;
    }

    /**
     * Determines whether the bin file for a given node has at least one other reference to it
     * Will return true if the binary exists and is referenced by at least one other node
     * @param nodeRef Node with the binary in question
     * @return boolean for if the bin has at least one other reference to it
     */
    public boolean hasAtLeastOneOtherReference(NodeRef nodeRef)
    {
        boolean hasAtLeastOneOtherReference = false;
        String contentUrl = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentUrl();

        Set<NodeRef> referencesToContentNode = recordsManagementQueryDAO.getNodeRefsWhichReferenceContentUrl(contentUrl);
        if (referencesToContentNode.size() > 1)
        {
            hasAtLeastOneOtherReference = true;
        }
        return hasAtLeastOneOtherReference;
    }

    /**
     * Duplicate the content of a node without triggering the audit or versioning behaviours
     *
     * @param nodeRef The node with the content to duplicate
     */
    public void duplicate(NodeRef nodeRef)
    {
        //Adding fix for RM-6788 where too many duplicates are being made this is a workaround waiting on a full
        // solution
        if (!nodeService.hasAspect(nodeRef, ASPECT_ARCHIVED))
        {
            //disabling versioning and auditing
            behaviourFilter.disableBehaviour();
            try
            {
                //create a new content URL for the copy/original node
                updateContentProperty(nodeRef);
            }
            finally
            {
                //enable versioning and auditing
                behaviourFilter.enableBehaviour();
            }
        }
    }

    /**
     * Helper to update the content property for the node
     *
     * @param nodeRef         the node
     */
    private void updateContentProperty(NodeRef nodeRef)
    {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader != null)
        {
            ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
            writer.putContent(reader);
        }
    }
}
