/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * This class is needed to provide some infrastructure, but the actual evaluation of Composite Conditions happens inside the ActionServiceImpl as a special case.
 * 
 * @author Jean Barmash
 */
public class CompositeConditionEvaluator extends ActionConditionEvaluatorAbstractBase
{

    private static Log logger = LogFactory.getLog(CompositeConditionEvaluator.class);

    @Override
    protected boolean evaluateImpl(ActionCondition actionCondition,
            NodeRef actionedUponNodeRef)
    {
        logger.error("Evaluating composite condition.  Should not be called.");
        return false;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {}
}
