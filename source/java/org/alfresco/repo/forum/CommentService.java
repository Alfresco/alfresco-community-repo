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
package org.alfresco.repo.forum;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ForumModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This is a starting point for a future service for handling Share comments.
 * <p/>
 * This class may change in the future as requirements become clearer.
 * 
 * @author Neil Mc Erlean
 * @since 4.0
 */
public interface CommentService
{
    /**
     * Thi method retrieves the ancestor in the repository containment hierarchy having the
     * {@link ForumModel#ASPECT_DISCUSSABLE fm:discussable} aspect.
     * 
     * @param descendantNodeRef The nodeRef which descends from the f:discussable node.
     * @param expectedNodeType if not <tt>null</tt>, this is an assertion by calling code that the descendantNodeRef
     *                           is of the specified type.
     * @return the fm:discussable ancestor if there is one, else <tt>null</tt>
     * @throws AlfrescoRuntimeException if the specified expectedNodeType is not correct.
     */
    NodeRef getDiscussableAncestor(NodeRef descendantNodeRef, QName expectedNodeType);
    
    /**
     * This method retrieves the {@link ForumModel#TYPE_TOPIC fm:topic} NodeRef which holds the Share comments for
     * the specified {@link ForumModel#ASPECT_DISCUSSABLE fm:discussable} node.
     * 
     * @param discussableNode the node whose Share comments are sought.
     * @return the fm:topic NodeRef, if one exists, else <tt>null</tt>.
     */
    NodeRef getShareCommentsTopic(NodeRef discussableNode);
}
