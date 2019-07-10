/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
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

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

/**
 * Utility class to duplicate the content of a node without triggering the audit or versioning behaviours
 * @author Ross Gale
 * @since 2.7.2
 */
public class ContentBinDuplicationUtility
{

    /**
     * Behaviour filter
     */
    private BehaviourFilter behaviourFilter;

    /**
     * Provides methods for accessing and transforming content.
     */
    private ContentService contentService;

    private NodeService nodeService;

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
     * Duplicate the content of a node without triggering the audit or versioning behaviours
     *
     * @param nodeRef The node with the content to duplicate
     */
    public void duplicate(NodeRef nodeRef)
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

    /**
     * Purposely orphans a binary file.
     *
     * This will prevent the removal of a binary file to address MNT-20740 where a binary
     * file may be shared across nodes that try to delete the binary on deletion.
     *
     * Nodes created before 2.7.2 won't have been picked up by the binary duplication code
     * and will cause problems if the file is removed. Orphaning the binary will mean if
     * there is a node sharing this it will still be available and if there isn't the orphaned
     * file will be picked up by the content cleaner on it's next pass.
     *
     * @param nodeRef The node who's binary file we want to orphan
     */
    public void orphanContent(NodeRef nodeRef)
    {
        Set<String> urlsToDelete = TransactionalResourceHelper.getSet("ContentStoreCleaner.PostCommitDeletionUrls");
        urlsToDelete.remove(contentService.getReader(nodeRef, ContentModel.PROP_CONTENT).getContentUrl());
    }
}
