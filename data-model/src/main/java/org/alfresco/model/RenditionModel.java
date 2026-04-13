/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.model;

import org.alfresco.api.AlfrescoPublicApi;     
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Rendition Model Constants
 */
@AlfrescoPublicApi
public interface RenditionModel
{
    /**
     * Aspect added to renditions created by RenditionService2.
     * Initially used to avoid duplicate rendition requests via the original RenditionService and the new one.
     */
    static final QName ASPECT_RENDITION2 = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "rendition2");

    /**
     * The source node's content hash code. Used to work out if a transform should replace the existing rendition,
     * as transforms may be provided out of order.
     */
    static final QName PROP_RENDITION_CONTENT_HASH_CODE = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "contentHashCode");

    /**
     * @deprecated This rendition aspect will no longger be needed once the original RenditionService has been
     * replaced by RenditionService2 which additionally uses a rendition2 aspect to mark its renditions.
     */
    @Deprecated
    static final QName ASPECT_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "rendition");

    /**
     * @deprecated obsolete when RenditionService is removed.
     */
    @Deprecated
    static final QName ASPECT_HIDDEN_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "hiddenRendition");

    /**
     * @deprecated obsolete when RenditionService is removed.
     */
    @Deprecated
    static final QName ASPECT_VISIBLE_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "visibleRendition");

    static final QName ASPECT_RENDITIONED = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "renditioned");
    static final QName ASSOC_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "rendition");
    
    /**
     * @since 4.0.1
     */
    static final QName ASPECT_PREVENT_RENDITIONS = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "preventRenditions");
}
