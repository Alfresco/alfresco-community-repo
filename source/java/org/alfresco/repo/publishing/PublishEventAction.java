/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

package org.alfresco.repo.publishing;

import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This ActionExecuter adds a publish event to the publish event queue.
 * @author Nick Smith
 * @since 4.0
 *
 */
public class PublishEventAction extends ActionExecuterAbstractBase 
{

    /**
    * {@inheritDoc}
    */
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        // TODO Implement execute method.
    }

    /**
    * {@inheritDoc}
    */
    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        // TODO Implement parameter definitions.
        
    }

}
