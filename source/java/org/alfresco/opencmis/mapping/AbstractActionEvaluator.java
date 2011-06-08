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
package org.alfresco.opencmis.mapping;

import org.alfresco.opencmis.dictionary.CMISActionEvaluator;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Base class for all action evaluators
 * 
 * @author davidc
 * 
 */
public abstract class AbstractActionEvaluator<T> implements CMISActionEvaluator<T>
{
    private ServiceRegistry serviceRegistry;
    private Action action;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param action
     */
    protected AbstractActionEvaluator(ServiceRegistry serviceRegistry, Action action)
    {
        this.serviceRegistry = serviceRegistry;
        this.action = action;
    }

    /**
     * @return service registry
     */
    protected ServiceRegistry getServiceRegistry()
    {
        return serviceRegistry;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.alfresco.opencmis.CMISActionEvaluator#getAction()
     */
    public Action getAction()
    {
        return action;
    }
}
