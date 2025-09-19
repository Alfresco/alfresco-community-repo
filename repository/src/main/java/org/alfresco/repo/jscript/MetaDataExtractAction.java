/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
/*
 * Copyright (C) 2005 Jesper Steen Møller
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
package org.alfresco.repo.jscript;

import org.apache.commons.lang3.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.forms.FormData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * JavaScript wrapper for the "extract-metadata" action.
 * <p>
 * This class provides a scriptable interface to trigger metadata extraction actions within the Alfresco repository. It is similar to {@link Actions} class but is dedicated to metadata extraction functionality.
 */
public final class MetaDataExtractAction extends Actions
{
    private Log logger = LogFactory.getLog(getClass());

    private final static String ACTION_NAME = "extract-metadata";

    private ContentService contentService;

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * Create a new metadata extraction action instance
     *
     * @param setActionContext
     *            if true, sets the action context to "scriptaction".
     * @return the newly created action
     */

    public ScriptAction create(boolean setActionContext)
    {
        return create(ACTION_NAME, setActionContext);
    }

    /**
     * Check if the metadata (title or description) has changed
     * 
     * @param itemId
     * @param formData
     * @return true if title or description has changed, false otherwise
     */
    public boolean isContentChanged(String itemId, FormData formData)
    {

        try
        {
            NodeRef nodeRef = null;
            if (NodeRef.isNodeRef(itemId))
            {
                nodeRef = new NodeRef(itemId);
            }
            else
            {
                // split the string into the 3 required parts
                String[] parts = itemId.split("/");
                if (parts.length == 3)
                {
                    nodeRef = new NodeRef(parts[0], parts[1], parts[2]);
                }
            }
            if (nodeRef == null)
            {
                return false;
            }

            ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
            String contentString = reader.getContentString();
            FormData.FieldData fieldData = formData.getFieldData("prop_cm_content");

            if (fieldData == null || fieldData.getValue() == null)
            {
                // no content in form data, so content has not changed
                return false;
            }
            else
            {
                String propCmContent = String.valueOf(fieldData.getValue());

                return !Strings.CS.equals(contentString, propCmContent);
            }
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Unable to determine if content has changed for node: " + itemId, e);
            }
            return false;
        }
    }
}
