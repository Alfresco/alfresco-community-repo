/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.repo.forms.processor;

import java.util.Map;

/**
 * Interface definition for a simple DTO containing various objects 
 * needed to generate Forms.
 * 
 * @since 3.4
 * @author Nick Smith
 */
public interface FormCreationData
{
    /**
     * @return the itemData
     */
    Object getItemData();
    
    /**
     * @return If the <code>fieldName</code> given is a forced field then
     *         returns <code>true</code>, otherwise returns <code>false</code>.
     */
    boolean isForcedField(String fieldName);
    
    /**
     * @return the context
     */
    Map<String, Object> getContext();
}
