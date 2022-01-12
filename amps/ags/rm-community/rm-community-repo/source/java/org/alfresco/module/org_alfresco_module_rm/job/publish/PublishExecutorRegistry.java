/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.job.publish;

import java.util.HashMap;
import java.util.Map;

/**
 * Publish executor register.
 * 
 * @author Roy Wetherall
 */
public class PublishExecutorRegistry
{
    /** Map of publish executors */
    private Map<String, PublishExecutor> publishExectors = new HashMap<>(3);
    
    /**
     * Register a publish executor
     * 
     * @param publishExecutor   publish executor
     */
    public void register(PublishExecutor publishExecutor)
    {
        publishExectors.put(publishExecutor.getName(), publishExecutor);
    }
    
    /**
     * Get registered publish executor by name.
     * 
     * @param name  name
     * @return {@link PublishExecutor}]
     */
    public PublishExecutor get(String name)
    {
        return publishExectors.get(name);
    }
}
