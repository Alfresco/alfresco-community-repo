/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
