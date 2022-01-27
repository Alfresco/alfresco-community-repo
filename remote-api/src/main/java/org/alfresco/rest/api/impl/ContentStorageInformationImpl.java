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

package org.alfresco.rest.api.impl;

import org.alfresco.repo.content.ContentRestoreParams;
import org.alfresco.rest.api.ContentStorageInformation;
import org.alfresco.rest.api.model.ArchiveContentRequest;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.api.model.RestoreArchivedContentRequest;
import org.alfresco.rest.framework.core.exceptions.RestoreInProgressException;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * Default implementation for {@link ContentStorageInformation}
 * Note: Currently marked as experimental and subject to change.
 *
 * @author mpichura
 */
@Experimental
public class ContentStorageInformationImpl implements ContentStorageInformation
{

    public static final char PREFIX_SEPARATOR = '_';

    private final ContentService contentService;
    private final NamespaceService namespaceService;

    public ContentStorageInformationImpl(ContentService contentService, NamespaceService namespaceService)
    {
        this.contentService = contentService;
        this.namespaceService = namespaceService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Experimental
    public ContentStorageInfo getStorageInfo(NodeRef nodeRef, String contentPropName, Parameters parameters)
    {
        final QName propQName = getQName(contentPropName);
        final Map<String, String> storageProperties = contentService.getStorageProperties(nodeRef, propQName);
        final ContentStorageInfo storageInfo = new ContentStorageInfo();
        storageInfo.setId(propQName.toPrefixString(namespaceService));
        storageInfo.setStorageProperties(storageProperties);
        return storageInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestArchiveContent(NodeRef nodeRef, String contentPropName,
                                         ArchiveContentRequest archiveContentRequest)
    {
        final QName propQName = getQName(contentPropName);
        final Map<String, Serializable> archiveParams =
                archiveContentRequest == null ? Collections.emptyMap() : archiveContentRequest.getArchiveParams();
        return contentService.requestSendContentToArchive(nodeRef, propQName, archiveParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestRestoreContentFromArchive(NodeRef nodeRef, String contentPropName,
                                                    RestoreArchivedContentRequest restoreArchivedContentRequest)
    {
        final QName propQName = getQName(contentPropName);
        final Map<String, Serializable> restoreParams =
                (restoreArchivedContentRequest == null || restoreArchivedContentRequest.getRestorePriority() == null) ?
                        Collections.emptyMap() :
                        Map.of(ContentRestoreParams.RESTORE_PRIORITY.name(), restoreArchivedContentRequest.getRestorePriority());
        try
        {
            return contentService.requestRestoreContentFromArchive(nodeRef, propQName, restoreParams);
        }
        catch (org.alfresco.service.cmr.repository.RestoreInProgressException e)
        {
            throw new RestoreInProgressException(e.getMsgId(), e);
        }

    }

    private QName getQName(final String contentPropName)
    {
        final String properContentPropName = contentPropName.replace(PREFIX_SEPARATOR, QName.NAMESPACE_PREFIX);
        return QName.resolveToQName(namespaceService, properContentPropName);
    }

}
