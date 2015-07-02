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
import java.util.Collection;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Nick Smith
 * @since 3.4.e
 */
public interface WorkflowNodeConverter
{
    /**
     * Converts a {@link NodeRef} into the appropriate Node type.
     * @param node NodeRef
     * @return Object
     */
    Object convertNode(NodeRef node);
    
    /**
     * Converts a {@link NodeRef}. The return type is
     * dependent on the value of <code>isMany</code>. If <code>true</code> then
     * a {@link List} of the appropriate Node type is returned. Otherwise a
     * single instance of the appropriate Node type is returned.
     * 
     * @param value NodeRef
     * @param isMany boolean
     * @return Object
     */
    Object convertNode(NodeRef value, boolean isMany);

    /**
     * Converts a {@link Collection} of {@link NodeRef}s into a {@link List} of the appropriate Node type.
     * @param values Collection<NodeRef>
     * @return List
     */
    List<? extends Object> convertNodes(Collection<NodeRef> values);

    /**
     * Converts a {@link Collection} of {@link NodeRef}s. The return type is
     * dependent on the value of <code>isMany</code>. If <code>true</code> then
     * a {@link List} of the appropriate Node type is returned. Otherwise a
     * single instance of the appropriate Node type is returned.
     * 
     * @param values Collection<NodeRef>
     * @param isMany boolean
     * @return Object
     */
    Object convertNodes(Collection<NodeRef> values, boolean isMany);
    
    /**
     * Converts a {@link Collection} of {@link NodeRef}s or a single {@link NodeRef}. The return type is
     * dependent on the value of <code>isMany</code>. If <code>true</code> then
     * a {@link List} of the appropriate Node type is returned. Otherwise a
     * single instance of the appropriate Node type is returned.
     
     * @param value Object
     * @param isMany boolean
     * @return Object
     */
    Object convertNodes(Object value, boolean isMany);
    
    /**
     * 
     * @param toConvert Object
     * @return NodeRef
     */
    NodeRef convertToNode(Object toConvert);

    List<NodeRef> convertToNodes(Collection<?> toConvert);

    List<NodeRef> convertToNodes(Object value);

    boolean isSupported(Object object);

    /**
     * Converts the object to a {@link NodeRef} or a {@link List} of
     * {@link NodeRef}s. The return type is dependant on the type of the object
     * parameter. If the object parameter is a {@link Collection} then a
     * {@link List} of {@link NodeRef}s is returned. Otherwise a single
     * {@link NodeRef} is returned.
     * 
     * @param object NodeRef
     * @return Serializable
     */
    Serializable convert(Object object);
}
