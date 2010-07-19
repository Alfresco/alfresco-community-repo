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

package org.alfresco.repo.forms.processor.workflow;

/**
 * Visitor interface used to enable the visitor pattern on {@link DataKeyInfo}
 * instances. Implementations of this interface can call
 * <code>DataKeyInfo.visit(DataKeyInfoVisitor)</code> to have the appropriate
 * visit method called on the visitor, based on the fieldType of the
 * {@link DataKeyInfo} instance.
 * 
 * @author Nick Smith
 */
public interface DataKeyInfoVisitor<T>
{
    /**
     * Called for {@link DataKeyInfo} instances with a field type of ASSOCIATION.
     * @param info
     * @return
     */
    T visitAssociation(DataKeyInfo info);

    /**
     * Called for {@link DataKeyInfo} instances with a field type of PROPERTY.
     * @param info
     * @return
     */
    T visitProperty(DataKeyInfo info);
    
    /**
     * Called for {@link DataKeyInfo} instances with a field type of TRANSIENT_ASSOCIATION.
     * @param info
     * @return
     */
    T visitTransientAssociation(DataKeyInfo info);
    
    /**
     * Called for {@link DataKeyInfo} instances with a field type of TRANSIENT_PROPERTY.
     * @param info
     * @return
     */
    T visitTransientProperty(DataKeyInfo info);
}
