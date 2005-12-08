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

import org.alfresco.service.namespace.QName;


/**
 * Description of a bound Behaviour.
 * 
 * @author David Caruana
 *
 * @param <B>  The type of Binding.
 */
public interface BehaviourDefinition<B extends BehaviourBinding>
{
    /**
     * Gets the Policy bound to
     * 
     * @return  the policy name
     */
    public QName getPolicy();
    
    /**
     * Gets the definition of the Policy bound to
     * 
     * @return  the policy definition (or null, if the Policy has not been registered yet)
     */
    public PolicyDefinition getPolicyDefinition();
    
    /**
     * Gets the binding used to bind the Behaviour to the Policy
     * 
     * @return  the binding
     */
    public B getBinding();
    
    /**
     * Gets the Behaviour
     * 
     * @return  the behaviour
     */
    public Behaviour getBehaviour();
}
