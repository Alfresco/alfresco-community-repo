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

package org.alfresco.repo.workflow;

import java.io.Serializable;

import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @since 3.4.e
 * @author Nick Smith
 *
 */
public class DefaultWorkflowPropertyHandler extends AbstractWorkflowPropertyHandler
{
    /**
    * {@inheritDoc}
    */
    public Object handleProperty(QName key, Serializable value, TypeDefinition type, Object object, Class<?> objectType)
    {
        return handleDefaultProperty(object, type, key, value);
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected QName getKey()
    {
        // Does not have a key!
        return null;
    }

}
