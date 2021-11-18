/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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

import org.alfresco.rest.api.ContentStorageInformation;
import org.alfresco.rest.api.model.ContentStorageInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.DynamicNamespacePrefixResolver;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import java.util.Map;

public class ContentStorageInformationImpl implements ContentStorageInformation
{

    public static final char PREFIX_SPLITTER = '_';

    private final ContentService contentService;

    public ContentStorageInformationImpl(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentStorageInfo getStorageInfo(String nodeId, String contentPropName, Parameters parameters)
    {
        final NodeRef nodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, nodeId);
        //FIXME: figure out how to properly and neatly convert contentPropName param into QName
        final QName propQName = getQName(contentPropName);
        final Map<String, String> storageProperties = contentService.getStorageProperties(nodeRef, propQName);
        final ContentStorageInfo storageInfo = new ContentStorageInfo();
        storageInfo.setId(propQName.toString());
        storageInfo.setStorageProperties(storageProperties);
        return storageInfo;
    }

    private QName getQName(final String contentPropName)
    {
        final DynamicNamespacePrefixResolver namespacePrefixResolver = new DynamicNamespacePrefixResolver();
        //for now there is only single content namespace being registered - this may be extended to more
        namespacePrefixResolver.registerNamespace(NamespaceService.CONTENT_MODEL_PREFIX,
                NamespaceService.CONTENT_MODEL_1_0_URI);

        //following may be removed if it is aligned that endpoint will take contentPropName in a format of prefix:localName
        //instead of prefix_localName which is assumed at the moment
        final String properContentPropName = contentPropName.replace(PREFIX_SPLITTER, QName.NAMESPACE_PREFIX);
        return QName.resolveToQName(namespacePrefixResolver, properContentPropName);
    }

}
