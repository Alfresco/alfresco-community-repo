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
package org.alfresco.module.org_alfresco_module_rm.identifier;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.namespace.QName;

/**
 * Generates an identifier for a content type from a given context.
 * 
 * @author Roy Wetherall
 */
public interface IdentifierGenerator
{
    /**
     * The content type this generator is applicible to.
     * @return  QName   the type
     */
    QName getType();
    
    /**
     * Generates the next id based on the provided context.
     * @param context   map of context values
     * @return String   the next id
     */
    String generateId(Map<String, Serializable> context);
}
