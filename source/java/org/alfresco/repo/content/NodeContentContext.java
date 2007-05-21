/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.content;

import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;

/**
 * Context information for node-related content.
 * 
 * @author Derek Hulley
 */
public class NodeContentContext extends ContentContext
{
    private static final long serialVersionUID = -1836714367516857907L;

    private NodeRef nodeRef;
    private QName propertyQName;
    
    /**
     * Construct the instance with the content URL.
     * 
     * @param   existingContentReader   content with which to seed the new writer - may be <tt>null</tt>
     * @param   contentUrl              the content URL - may be <tt>null</tt>
     * @param   nodeRef                 the node holding the content metadata - may not be <tt>null</tt>
     * @param   propertyQName           the property holding the content metadata  - may not be <tt>null</tt>
     */
    public NodeContentContext(
            ContentReader existingContentReader,
            String contentUrl,
            NodeRef nodeRef,
            QName propertyQName)
    {
        super(existingContentReader, contentUrl);
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("propertyQName", propertyQName);
        this.nodeRef = nodeRef;
        this.propertyQName = propertyQName;
    }

    /**
     * @return  Returns the node holding the content metadata
     */
    public NodeRef getNodeRef()
    {
        return nodeRef;
    }

    /**
     * 
     * @return  Returns the property holding the content metadata
     */
    public QName getPropertyQName()
    {
        return propertyQName;
    }
}
