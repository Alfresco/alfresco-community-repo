/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.policy.annotation;

import org.alfresco.repo.policy.Behaviour;


/**
 * Interface for a behaviour registry.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public interface BehaviourRegistry
{
    /**
     * Register a behaviour against a given name.
     * 
     * @param behaviour behaviour
     */
    void registerBehaviour(String name, org.alfresco.repo.policy.Behaviour behaviour);
    
    /**
     * Gets the behaviour for a given name.
     * 
     * @param name                  behaviour name
     * @return {@link Behaviour}    behaviour, null otherwise
     */
    org.alfresco.repo.policy.Behaviour getBehaviour(String name);
}
