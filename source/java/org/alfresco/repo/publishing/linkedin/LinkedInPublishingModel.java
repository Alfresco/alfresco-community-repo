/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing.linkedin;

import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 */
public interface LinkedInPublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/linkedin/1.0";
    public static final String PREFIX = "linkedin";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");

    public static final QName ASPECT_ASSET = QName.createQName(NAMESPACE, "AssetAspect");
    public static final QName PROP_ASSET_ID = QName.createQName(NAMESPACE, "assetId");
    public static final QName PROP_ASSET_URL = QName.createQName(NAMESPACE, "assetUrl");
}
