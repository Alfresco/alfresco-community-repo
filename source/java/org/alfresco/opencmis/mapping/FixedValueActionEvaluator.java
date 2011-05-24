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

import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Action Evaluator whose evaluation is fixed
 * 
 * @author davidc
 * 
 */
public class FixedValueActionEvaluator<T> extends AbstractActionEvaluator<T>
{
    private boolean allowed;

    /**
     * Construct
     * 
     * @param serviceRegistry
     * @param action
     */
    protected FixedValueActionEvaluator(ServiceRegistry serviceRegistry, Action action, boolean allowed)
    {
        super(serviceRegistry, action);
        this.allowed = allowed;
    }

    public boolean isAllowed(T object)
    {
        return allowed;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("FixedValueActionEvaluator[action=").append(getAction());
        builder.append(", allowed=").append(allowed).append("]");
        return builder.toString();
    }
}
