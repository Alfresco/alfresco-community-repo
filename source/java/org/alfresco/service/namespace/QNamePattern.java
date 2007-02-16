/*
 * Copyright (C) 2005 Alfresco, Inc.
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
package org.alfresco.service.namespace;


/**
 * Provides pattern matching against {@link org.alfresco.service.namespace.QName qnames}.
 * <p>
 * Implementations will use different mechanisms to match against the
 * {@link org.alfresco.service.namespace.QName#getNamespaceURI() namespace} and
 * {@link org.alfresco.service.namespace.QName#getLocalName()() localname}.
 * 
 * @see org.alfresco.service.namespace.QName
 * 
 * @author Derek Hulley
 */
public interface QNamePattern
{
    /**
     * Checks if the given qualified name matches the pattern represented
     * by this instance
     * 
     * @param qname the instance to check
     * @return Returns true if the qname matches this pattern
     */
    public boolean isMatch(QName qname);
}
