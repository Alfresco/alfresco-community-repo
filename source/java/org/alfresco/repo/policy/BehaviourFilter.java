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
package org.alfresco.repo.policy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Contract disabling and enabling policy behaviours.
 * 
 * @author David Caruana
 */
public interface BehaviourFilter
{
    /**
     * Disable behaviour for all nodes
     * 
     * @param className  the type/aspect behaviour to disable
     * @return  true => already disabled
     */
    public boolean disableBehaviour(QName className);

    /**
     * Disable behaviour for specific node
     * 
     * @param nodeRef  the node to disable for
     * @param className  the type/aspect behaviour to disable
     * @return  true => already disabled
     */
    public boolean disableBehaviour(NodeRef nodeRef, QName className);

    /**
     * Enable behaviour for all nodes
     * 
     * @param className  the type/aspect behaviour to enable
     */
    public void enableBehaviour(QName className);
    
    /**
     * Enable behaviour for specific node
     * 
     * @param nodeRef  the node to enable for
     * @param className  the type/aspect behaviour to enable
     */
    public void enableBehaviour(NodeRef nodeRef, QName className);

    /**
     * Enable all behaviours for specific node
     * 
     * @param nodeRef  the node to enable for
     */
    public void enableBehaviours(NodeRef nodeRef);
    
    /**
     * Enable all behaviours
     */
    public void enableAllBehaviours();
    
    /**
     * Determine if behaviour is enabled across all nodes.
     * 
     * @param className  the behaviour to test for
     * @return  true => behaviour is enabled
     */
    public boolean isEnabled(QName className);
    
    /**
     * Determine if behaviour is enabled for specific node.
     * 
     * Note: A node behaviour is enabled only when:
     *       a) the behaviour is not disabled across all nodes
     *       b) the behaviour is not disabled specifically for the provided node
     * 
     * @param nodeRef  the node to test for
     * @param className  the behaviour to test for
     * @return  true => behaviour is enabled
     */
    public boolean isEnabled(NodeRef nodeRef, QName className);
    
    /**
     * Determine if any behaviours have been disabled?
     * 
     * @return  true => behaviours have been filtered
     */
    public boolean isActivated();
}
