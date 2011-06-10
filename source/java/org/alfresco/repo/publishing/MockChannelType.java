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

import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.namespace.QName;

/**
 * For test purposes only.
 * @author Nick Smith
 * @since 4.0
 *
 */
public class MockChannelType extends AbstractChannelType
{
    public final static String ID = "MockChannelType";
    
    /**
    * {@inheritDoc}
    */
    public String getId()
    {
        return ID;
    }

    /**
    * {@inheritDoc}
    */
    public Map<String, String> getCapabilities()
    {
        return null;
    }

    /**
    * {@inheritDoc}
    */
    public QName getChannelNodeType()
    {
        return PublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    /**
    * {@inheritDoc}
    */
    public QName getContentRootNodeType()
    {
        return ContentModel.TYPE_FOLDER;
    }
}
