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

package org.alfresco.repo.publishing;

import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @since 4.0
 *
 */
public interface PublishingModel
{
    public static final String NAMESPACE = "http://www.alfresco.org/model/publishing/1.0";
    public static final String PREFIX = "pub";
    
    public static final QName TYPE_DELIVERY_CHANNEL = QName.createQName(NAMESPACE, "DeliveryChannel");
    public static final QName TYPE_DELIVERY_SERVER = QName.createQName(NAMESPACE, "DeliveryServer");
    public static final QName TYPE_ENVIRONMENT= QName.createQName(NAMESPACE, "Environment");
    public static final QName TYPE_PUBLISHING_QUEUE = QName.createQName(NAMESPACE, "PublishingQueue");
    public static final QName TYPE_CHANNEL_CONTAINER = QName.createQName(NAMESPACE, "SiteChannelContainer");
    
    public static final QName ASPECT_CONTENT_ROOT = QName.createQName(NAMESPACE, "ContentRoot");

    public static final QName PROP_CHANNEL = QName.createQName(NAMESPACE, "channel");
    public static final QName PROP_CHANNEL_TYPE = QName.createQName(NAMESPACE, "channelType");

}
