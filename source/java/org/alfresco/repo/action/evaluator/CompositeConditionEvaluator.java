/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.action.evaluator;

import java.util.List;

import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**  
 * This class is needed to provide some infrastructure, but the actual evaluation of 
 * Composite Conditions happens inside the ActionServiceImpl as a special case.
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
    {
    }
}
