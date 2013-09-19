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

/**
 * A Behaviour Binding represents the way in which a Behaviour is bound
 * to a Policy i.e. the key.
 * 
 * @author David Caruana
 *
 */
/*package*/ @AlfrescoPublicApi interface BehaviourBinding
{
    /**
     * Gets a generalised form of the Binding.
     * 
     * For example, if the binding key is hierarchical, return the parent
     * key.
     * 
     * @return  the generalised form (or null, if there isn't one)
     */
    BehaviourBinding generaliseBinding();
}
