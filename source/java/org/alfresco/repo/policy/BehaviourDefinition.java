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
package org.alfresco.repo.policy;

import org.alfresco.api.AlfrescoPublicApi;
import org.alfresco.service.namespace.QName;


/**
 * Description of a bound Behaviour.
 * 
 * @author David Caruana
 *
 * @param <B>  The type of Binding.
 */
@AlfrescoPublicApi
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
