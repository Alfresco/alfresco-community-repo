/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.rest.api;


import java.util.List;

import org.alfresco.rest.api.model.Action;
import org.alfresco.rest.api.model.ActionDefinition;
import org.alfresco.rest.api.model.ActionParameterConstraint;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.service.Experimental;
import org.alfresco.service.cmr.repository.NodeRef;

public interface Actions
{
    CollectionWithPagingInfo<ActionDefinition> getActionDefinitions(NodeRef nodeRef, Parameters params);

    CollectionWithPagingInfo<ActionDefinition> getActionDefinitions(Parameters params);

    ActionDefinition getActionDefinitionById(String actionDefinitionId);
    
    enum SortKey
    {
        NAME,
        TITLE
    }
    
    Action executeAction(Action action, Parameters parameters);

    @Experimental
    ActionParameterConstraint getActionConstraint(String constraintName);
    @Experimental
    ActionDefinition getRuleActionDefinitionById(String actionDefinitionId);
}
