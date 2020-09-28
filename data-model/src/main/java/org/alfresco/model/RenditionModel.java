/*
 * #%L
 * Alfresco Data model classes
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
    static final QName ASPECT_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "rendition");
    static final QName ASPECT_HIDDEN_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "hiddenRendition");
    static final QName ASPECT_VISIBLE_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "visibleRendition");
    
    static final QName ASPECT_RENDITIONED = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "renditioned");
    static final QName ASSOC_RENDITION = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "rendition");
    
    /**
     * @since 4.0.1
     */
    static final QName ASPECT_PREVENT_RENDITIONS = QName.createQName(NamespaceService.RENDITION_MODEL_1_0_URI, "preventRenditions");
}
