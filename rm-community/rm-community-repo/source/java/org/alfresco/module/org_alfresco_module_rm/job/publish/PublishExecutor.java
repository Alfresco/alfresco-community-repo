 
package org.alfresco.module.org_alfresco_module_rm.job.publish;

/*
 * #%L
 * This file is part of Alfresco.
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
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


import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Publish executor interface.
 * 
 * @author Roy Wetherall
 */
public interface PublishExecutor
{
    /**
     * @return  publish exector name
     */
    String getName();
    
    /**
     * Publish changes to node.
     * 
     * @param nodeRef   node reference
     */
    void publish(NodeRef nodeRef);
}
