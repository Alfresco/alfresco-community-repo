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

package org.alfresco.service.cmr.rule;

import org.alfresco.service.cmr.action.CompositeAction;


/**
 * Rule Interface
 * 
 * @author Roy Wetherall
 */
public interface Rule extends CompositeAction
{
    /**
     * Indicates that the rule is applied to the children of the associated
     * node, not just the node itself.
     * <p>
     * By default this will be set to false.
     * 
     * @return  true if the rule is applied to the children of the associated node,
     *          false otherwise
     */
    boolean isAppliedToChildren();
    
    /**
     * Set whether the rule is applied to all children of the associated node
     * rather than just the node itself.
     * 
     * @param isAppliedToChildren   true if the rule should be applied to the children, false 
     *                              otherwise
     */
    void applyToChildren(boolean isAppliedToChildren);

    /**
     * Get the rule type name
     * 
     * @return  the rule type name
     */
    String getRuleTypeName();
 }