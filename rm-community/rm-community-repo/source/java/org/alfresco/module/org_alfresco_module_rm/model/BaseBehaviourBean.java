/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

package org.alfresco.module.org_alfresco_module_rm.model;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.util.ServiceBaseImpl;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.annotation.BehaviourRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient base class for behaviour beans.
 *
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseBehaviourBean extends ServiceBaseImpl
                                        implements RecordsManagementModel,
                                                   BehaviourRegistry
{
    /** Logger */
    protected static final Log LOGGER = LogFactory.getLog(BaseBehaviourBean.class);

    /** behaviour filter */
    protected BehaviourFilter behaviourFilter;

    /** behaviour map */
    protected Map<String, org.alfresco.repo.policy.Behaviour> behaviours = new HashMap<String, org.alfresco.repo.policy.Behaviour>(7);

    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * @see org.alfresco.repo.policy.annotation.BehaviourRegistry#registerBehaviour(java.lang.String, org.alfresco.repo.policy.Behaviour)
     */
    @Override
    public void registerBehaviour(String name, org.alfresco.repo.policy.Behaviour behaviour)
    {
        if (behaviours.containsKey(name))
        {
            throw new AlfrescoRuntimeException("Can not register behaviour, because name " + name + "has already been used.");
        }

        behaviours.put(name, behaviour);
    }

    /**
     * @see org.alfresco.repo.policy.annotation.BehaviourRegistry#getBehaviour(java.lang.String)
     */
    @Override
    public org.alfresco.repo.policy.Behaviour getBehaviour(String name)
    {
        return behaviours.get(name);
    }

}
