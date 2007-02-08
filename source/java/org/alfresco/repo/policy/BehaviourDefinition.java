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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
