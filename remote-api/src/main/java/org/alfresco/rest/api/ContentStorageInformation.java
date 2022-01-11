/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api;

import org.alfresco.rest.api.model.ArchiveContentRequest;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.api.model.RestoreArchivedContentRequest;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Storage information for content API.
 * Note: Currently marked as experimental and subject to change.
 *
 * @author mpichura
 */
@Experimental
public interface ContentStorageInformation
{
    /**
     * Note: Currently marked as experimental and subject to change.
     *
     * @param nodeRef         Node reference
     * @param contentPropName Qualified name of content property (e.g. 'cm_content')
     * @param parameters      {@link Parameters} object to get the parameters passed into the request
     * @return {@link ContentStorageInfo} object consisting of qualified name of content property and a map of storage properties
     */
    @Experimental
    ContentStorageInfo getStorageInfo(NodeRef nodeRef, String contentPropName, Parameters parameters);

    /**
     * Note: Currently marked as experimental and subject to change.
     *
     * @param nodeRef               Node reference
     * @param contentPropName       Qualified name of content property (e.g. 'cm_content')
     * @param archiveContentRequest {@link ArchiveContentRequest} object holding parameters for archive content request
     * @return true when request successful, false when unsuccessful
     */
    @Experimental
    boolean requestArchiveContent(NodeRef nodeRef, String contentPropName, ArchiveContentRequest archiveContentRequest);

    /**
     * Note: Currently marked as experimental and subject to change.
     *
     * @param nodeRef                       Node reference
     * @param contentPropName               Qualified name of content property (e.g. 'cm_content')
     * @param restoreArchivedContentRequest {@link RestoreArchivedContentRequest} object holding parameters for restore from archive request
     * @return true when request successful, false when unsuccessful
     */
    @Experimental
    boolean requestRestoreContentFromArchive(NodeRef nodeRef, String contentPropName,
                                             RestoreArchivedContentRequest restoreArchivedContentRequest);
}
