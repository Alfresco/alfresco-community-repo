/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.rm.rest.api.retentionschedule;

import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.rest.framework.resource.actions.interfaces.EntityResourceAction;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rm.rest.api.model.RetentionScheduleActionDefinition;
import org.springframework.beans.factory.InitializingBean;

/**
 * Retention schedule entity resource
 * @author sathishkumar.t
 */
@EntityResource(name="retention-schedules", title = "Retention Schedule")
public class RetentionScheduleEntityResource implements EntityResourceAction.ReadById<RetentionScheduleActionDefinition>,
        EntityResourceAction.Update<RetentionScheduleActionDefinition>,
        EntityResourceAction.Delete,
        InitializingBean
{
    @Override
    public RetentionScheduleActionDefinition readById(String retentionScheduleActionId, Parameters parameters)
    {
        // TODO add the implementation to get the retention schedule action definition by id
        return new RetentionScheduleActionDefinition();
    }

    @Override
    public RetentionScheduleActionDefinition update(String retentionScheduleActionId, RetentionScheduleActionDefinition entity, Parameters parameters)
    {
        // TODO add the implementation to update the retention schedule action definition
        return new RetentionScheduleActionDefinition();
    }

    @Override
    public void delete(String retentionScheduleActionId, Parameters parameters)
    {
        // TODO add the implementation to delete the retention schedule action definition
    }

    @Override
    public void afterPropertiesSet()
    {
        //TODO add the properties
    }
}
