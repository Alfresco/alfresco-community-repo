/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.policy;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

/**
 * Implementation of Behaviour Filter.  All methods operate on transactionally-bound
 * resources.  Behaviour will therefore never span transactions; the filter state has
 * the same lifespan as the transaction in which it was created.
 * <p/>
 * <b>Multitenancy and disabling by <tt>NodeRef</tt>:</b><br/>
 * Conversions based on the current tenant context are done automatically.
 * 
 * @author Derek Hulley
 */
public class BehaviourFilterImpl implements BehaviourFilter
{
    private static final String KEY_FILTER_COUNT = "BehaviourFilterImpl.filterCount";
    private static final String KEY_GLOBAL_FILTERS = "BehaviourFilterImpl.globalFilters";
    private static final String KEY_CLASS_FILTERS = "BehaviourFilterImpl.classFilters";
    private static final String KEY_INSTANCE_CLASS_FILTERS = "BehaviourFilterImpl.instanceClassFilters";
    private static final String KEY_INSTANCE_FILTERS = "BehaviourFilterImpl.instanceFilters";
    
    private static final Log logger = LogFactory.getLog(BehaviourFilterImpl.class);
    
    private DictionaryService dictionaryService;
    private TenantService tenantService;
    
    /**
     * @param dictionaryService  dictionary service
     */    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param tenantService  dictionary service
     */    
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    @Deprecated
    @Override
    // TODO
    public void enableBehaviours(NodeRef nodeRef)
    {
        enableBehaviour(nodeRef);
    }

    @Deprecated
    @Override
    // TODO
    public void disableAllBehaviours()
    {
        disableBehaviour();
    }

    @Deprecated
    @Override
    // TODO
    public void enableAllBehaviours()
    {
        enableBehaviour();
    }

    @Override
    public void disableBehaviour()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: DISABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): ALL");
        }

        TransactionalResourceHelper.incrementCount(KEY_FILTER_COUNT);
        
        TransactionalResourceHelper.incrementCount(KEY_GLOBAL_FILTERS);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: " + TransactionalResourceHelper.getCount(KEY_GLOBAL_FILTERS));
        }
    }

    @Override
    public void disableBehaviour(QName className)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: DISABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + className);
        }
        ParameterCheck.mandatory("className",  className);

        TransactionalResourceHelper.incrementCount(KEY_FILTER_COUNT);
        
        Map<QName, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);
        MutableInt filter = classFilters.get(className);
        if (filter == null)
        {
            filter = new MutableInt(1);        // Already incremented
            classFilters.put(className, filter);
        }
        else
        {
            filter.increment();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: " + filter);
        }
    }

    @Override
    public void disableBehaviour(NodeRef nodeRef, QName className)
    {
        ParameterCheck.mandatory("nodeRef",  nodeRef);
        ParameterCheck.mandatory("className",  className);
        nodeRef = tenantService.getName(nodeRef);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: DISABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + nodeRef + "/" + className);
        }
        nodeRef = tenantService.getName(nodeRef);
        
        TransactionalResourceHelper.incrementCount(KEY_FILTER_COUNT);
        
        Map<NodeRef, Map<QName, MutableInt>> instanceClassFilters = TransactionalResourceHelper.getMap(KEY_INSTANCE_CLASS_FILTERS);
        Map<QName, MutableInt> classFilters = instanceClassFilters.get(nodeRef);
        if (classFilters == null)
        {
            classFilters = new HashMap<QName, MutableInt>(3);
            instanceClassFilters.put(nodeRef, classFilters);
        }
        MutableInt filter = classFilters.get(className);
        if (filter == null)
        {
            filter = new MutableInt(0);
            classFilters.put(className, filter);
        }
        filter.increment();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: " + filter);
        }
    }

    @Override
    public void disableBehaviour(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef",  nodeRef);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: DISABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + nodeRef + "/ALL:");
        }
        nodeRef = tenantService.getName(nodeRef);
        
        TransactionalResourceHelper.incrementCount(KEY_FILTER_COUNT);
        
        Map<NodeRef, MutableInt> instanceFilters = TransactionalResourceHelper.getMap(KEY_INSTANCE_FILTERS);
        MutableInt filter = instanceFilters.get(nodeRef);
        if (filter == null)
        {
            filter = new MutableInt(0);
            instanceFilters.put(nodeRef, filter);
        }
        filter.increment();
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now:" + filter);
        }
    }

    @Override
    public void enableBehaviour()
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: ENABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): ALL");
        }

        TransactionalResourceHelper.decrementCount(KEY_FILTER_COUNT, false);
        
        TransactionalResourceHelper.decrementCount(KEY_GLOBAL_FILTERS, false);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: " + TransactionalResourceHelper.getCount(KEY_GLOBAL_FILTERS));
        }
    }

    @Override
    public void enableBehaviour(QName className)
    {
        ParameterCheck.mandatory("className", className);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: ENABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + className);
        }
        
        TransactionalResourceHelper.decrementCount(KEY_FILTER_COUNT, false);
        
        if (!TransactionalResourceHelper.isResourcePresent(KEY_CLASS_FILTERS))
        {
            // Nothing was disabled
            return;
        }
        Map<QName, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);
        MutableInt filter = classFilters.get(className);
        if (filter == null)
        {
            // Class was not disabled
            return;
        }
        else if (filter.intValue() <= 0)
        {
            // Can't go below zero for this
        }
        else
        {
            filter.decrement();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: "+ filter);
        }
    }

    @Override
    public void enableBehaviour(NodeRef nodeRef, QName className)
    {
        ParameterCheck.mandatory("nodeRef",  nodeRef);
        ParameterCheck.mandatory("className",  className);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: ENABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + nodeRef + "/" + className);
        }
        
        TransactionalResourceHelper.decrementCount(KEY_FILTER_COUNT, false);
        
        if (!TransactionalResourceHelper.isResourcePresent(KEY_INSTANCE_CLASS_FILTERS))
        {
            // Nothing was disabled
            return;
        }
        nodeRef = tenantService.getName(nodeRef);

        Map<NodeRef, Map<QName, MutableInt>> instanceClassFilters = TransactionalResourceHelper.getMap(KEY_INSTANCE_CLASS_FILTERS);
        Map<QName, MutableInt> classFilters = instanceClassFilters.get(nodeRef);
        if (classFilters == null)
        {
            // Instance classes were not disabled
            return;
        }
        MutableInt filter = classFilters.get(className);
        if (filter == null)
        {
            // Class was not disabled
            return;
        }
        else if (filter.intValue() <= 0)
        {
            // Can't go below zero for this
        }
        else
        {
            filter.decrement();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: "+ filter);
        }
    }

    @Override
    public void enableBehaviour(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef",  nodeRef);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: ENABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + nodeRef + "/ALL");
        }
        
        TransactionalResourceHelper.decrementCount(KEY_FILTER_COUNT, false);
        
        if (!TransactionalResourceHelper.isResourcePresent(KEY_INSTANCE_FILTERS))
        {
            // Nothing was disabled
            return;
        }
        nodeRef = tenantService.getName(nodeRef);

        Map<NodeRef, MutableInt> instanceFilters = TransactionalResourceHelper.getMap(KEY_INSTANCE_FILTERS);
        MutableInt filter = instanceFilters.get(nodeRef);
        if (filter == null)
        {
            // Instance was not disabled
            return;
        }
        else if (filter.intValue() <= 0)
        {
            // Can't go below zero for this
        }
        else
        {
            filter.decrement();
        }
        
        if (logger.isDebugEnabled())
        {
            logger.debug("   Now:" + filter);
        }
    }

    @Override
    public boolean isEnabled()
    {
        return TransactionalResourceHelper.getCount(KEY_GLOBAL_FILTERS) <= 0;
    }

    @Override
    public boolean isEnabled(QName className)
    {
        ParameterCheck.mandatory("className", className);
        
        // Check the global, first
        if (!isEnabled())
        {
            return false;
        }
        
        if (!TransactionalResourceHelper.isResourcePresent(KEY_CLASS_FILTERS))
        {
            // Nothing was disabled
            return true;
        }
        Map<QName, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);
        MutableInt classFilter = classFilters.get(className);
        return (classFilter == null) || classFilter.intValue() <= 0;
    }

    @Override
    public boolean isEnabled(NodeRef nodeRef, QName className)
    {
        ParameterCheck.mandatory("nodeRef",  nodeRef);
        ParameterCheck.mandatory("className",  className);
        
        // Check the class (includes global) and instance, first
        if (!isEnabled(className) || !isEnabled(nodeRef))
        {
            return false;
        }
        
        if (!TransactionalResourceHelper.isResourcePresent(KEY_INSTANCE_CLASS_FILTERS))
        {
            // Nothing was disabled
            return true;
        }
        nodeRef = tenantService.getName(nodeRef);

        Map<NodeRef, Map<QName, MutableInt>> instanceClassFilters = TransactionalResourceHelper.getMap(KEY_INSTANCE_CLASS_FILTERS);
        Map<QName, MutableInt> classFilters = instanceClassFilters.get(nodeRef);
        if (classFilters == null)
        {
            // Instance classes were not disabled
            return true;
        }
        for (QName classCheck : classFilters.keySet())
        {
            // Ignore if it is not part of the hierarchy we are requesting
            if (!dictionaryService.isSubClass(className, classCheck))
            {
                continue;
            }
            MutableInt filter = classFilters.get(className);
            if (filter != null && filter.intValue() > 0)
            {
                // Class was disabled
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean isEnabled(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef",  nodeRef);
        
        // Check the class (includes global) and instance, first
        if (!isEnabled())
        {
            return false;
        }
        
        if (!TransactionalResourceHelper.isResourcePresent(KEY_INSTANCE_FILTERS))
        {
            // Nothing was disabled
            return true;
        }
        nodeRef = tenantService.getName(nodeRef);

        Map<NodeRef, MutableInt> instanceFilters = TransactionalResourceHelper.getMap(KEY_INSTANCE_FILTERS);
        MutableInt filter = instanceFilters.get(nodeRef);
        if (filter != null && filter.intValue() > 0)
        {
            // Instance was disabled
            return false;
        }
        return true;
    }

    @Override
    public boolean isActivated()
    {
        return TransactionalResourceHelper.getCount(KEY_FILTER_COUNT) > 0;
    }
}
