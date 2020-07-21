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
package org.alfresco.repo.action.executer;

import org.alfresco.repo.action.ActionDefinitionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionDefinition;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * An action executor that really does nothing.
 *
 * @author Gethin James
 */
public class EmptyActionExecutor extends ActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(EmptyActionExecutor.class);

    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        logger.info("I did nothing of interest.");
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        //No Op
    }

    @Override
    public ActionDefinition getActionDefinition()
    {
        if (this.actionDefinition == null)
        {
            this.actionDefinition = createActionDefinition(this.name);
            ((ActionDefinitionImpl)this.actionDefinition).setTitleKey(getTitleKey());
            ((ActionDefinitionImpl)this.actionDefinition).setDescriptionKey(getDescriptionKey());
            ((ActionDefinitionImpl)this.actionDefinition).setTrackStatus(getTrackStatus());
            ((ActionDefinitionImpl)this.actionDefinition).setAdhocPropertiesAllowed(getAdhocPropertiesAllowed());
            ((ActionDefinitionImpl)this.actionDefinition).setRuleActionExecutor(this.name);
            ((ActionDefinitionImpl)this.actionDefinition).setApplicableTypes(this.applicableTypes);
            ((ActionDefinitionImpl) this.actionDefinition).setParameterDefinitions(null);
        }
        return this.actionDefinition;

    }
}
