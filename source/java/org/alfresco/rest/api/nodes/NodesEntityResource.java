/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.rest.api.nodes;

import org.alfresco.rest.api.Nodes;
import org.alfresco.rest.framework.resource.EntityResource;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * An implementation of an Entity Resource for a Node
 *
 * @author sglover
 * @author Gethin James
 */
@EntityResource(name="nodes", title = "Nodes")
public class NodesEntityResource implements InitializingBean
{
    private Nodes nodes;

    public void setNodes(Nodes nodes)
    {
        this.nodes = nodes;
    }

	@Override
    public void afterPropertiesSet()
    {
        ParameterCheck.mandatory("nodes", this.nodes);
    }
}
