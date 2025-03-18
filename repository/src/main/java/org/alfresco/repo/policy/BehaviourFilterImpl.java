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
package org.alfresco.repo.policy;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.ParameterCheck;

import org.alfresco.repo.policy.traitextender.BehaviourFilterExtension;
import org.alfresco.repo.policy.traitextender.BehaviourFilterTrait;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.AJProxyTrait;
import org.alfresco.traitextender.Extend;
import org.alfresco.traitextender.ExtendedTrait;
import org.alfresco.traitextender.Extensible;
import org.alfresco.traitextender.Trait;

/**
 * Implementation of Behaviour Filter. All methods operate on transactionally-bound resources. Behaviour will therefore never span transactions; the filter state has the same lifespan as the transaction in which it was created.
 * <p/>
 * <b>Multitenancy and disabling by <tt>NodeRef</tt>:</b><br/>
 * Conversions based on the current tenant context are done automatically.
 * 
 * @author Derek Hulley
 */
public class BehaviourFilterImpl implements BehaviourFilter, Extensible
{
    private static final String KEY_FILTER_COUNT = "BehaviourFilterImpl.filterCount";
    private static final String KEY_GLOBAL_FILTERS = "BehaviourFilterImpl.globalFilters";
    private static final String KEY_CLASS_FILTERS = "BehaviourFilterImpl.classFilters";
    private static final String KEY_INSTANCE_CLASS_FILTERS = "BehaviourFilterImpl.instanceClassFilters";
    private static final String KEY_INSTANCE_FILTERS = "BehaviourFilterImpl.instanceFilters";

    private static final Log logger = LogFactory.getLog(BehaviourFilterImpl.class);

    private DictionaryService dictionaryService;
    private TenantService tenantService;

    private final ExtendedTrait<BehaviourFilterTrait> behaviourFilterTrait;

    public BehaviourFilterImpl()
    {
        super();
        this.behaviourFilterTrait = new ExtendedTrait<>(AJProxyTrait.create(this, BehaviourFilterTrait.class));
    }

    /**
     * @param dictionaryService
     *            dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * @param tenantService
     *            dictionary service
     */
    public void setTenantService(TenantService tenantService)
    {
        this.tenantService = tenantService;
    }

    @Deprecated
    @Override
    // TODO
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void enableBehaviours(NodeRef nodeRef)
    {
        enableBehaviour(nodeRef);
    }

    @Deprecated
    @Override
    // TODO
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void disableAllBehaviours()
    {
        disableBehaviour();
    }

    @Deprecated
    @Override
    // TODO
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void enableAllBehaviours()
    {
        enableBehaviour();
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
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
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void disableBehaviour(QName className)
    {
        disableBehaviour(className, false);
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void disableBehaviour(QName className, boolean includeSubClasses)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Behaviour: DISABLE (" + AlfrescoTransactionSupport.getTransactionId() + "): " + className);
        }
        ParameterCheck.mandatory("className", className);
        ClassFilter classFilter = new ClassFilter(className, includeSubClasses);

        TransactionalResourceHelper.incrementCount(KEY_FILTER_COUNT);

        Map<ClassFilter, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);
        MutableInt filterNumber = classFilters.get(classFilter);
        if (filterNumber == null)
        {
            filterNumber = new MutableInt(0);
        }
        filterNumber.increment();
        classFilters.put(classFilter, filterNumber);

        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: " + filterNumber);
        }
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void disableBehaviour(NodeRef nodeRef, QName className)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("className", className);
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
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void disableBehaviour(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

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
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
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
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
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
        Map<ClassFilter, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);
        MutableInt filterNumber = null;
        for (ClassFilter classFilter : classFilters.keySet())
        {
            if (classFilter.getClassName().equals(className))
            {
                filterNumber = classFilters.get(classFilter);
                break;
            }
        }
        if (filterNumber == null)
        {
            // Class was not disabled
            return;
        }
        else if (filterNumber.intValue() <= 0)
        {
            // Can't go below zero for this
        }
        else
        {
            filterNumber.decrement();
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("   Now: " + filterNumber);
        }
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void enableBehaviour(NodeRef nodeRef, QName className)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("className", className);

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
            logger.debug("   Now: " + filter);
        }
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public void enableBehaviour(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

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
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public boolean isEnabled()
    {
        return TransactionalResourceHelper.getCount(KEY_GLOBAL_FILTERS) <= 0;
    }

    /**
     * @param className
     *            the class name
     * @return the super class or <code>null</code>
     */
    private QName generaliseClass(QName className)
    {
        ClassDefinition classDefinition = dictionaryService.getClass(className);
        if (classDefinition == null)
        {
            // The class definition doesn't exist
            return null;
        }
        QName parentClassName = classDefinition.getParentName();
        return parentClassName;
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
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
        Map<ClassFilter, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);

        // Check this class to be disabled
        ClassFilter classFilter = getClassFilter(className);
        if (classFilter != null)
        {
            MutableInt filterNumber = classFilters.get(classFilter);
            if (filterNumber != null && filterNumber.intValue() > 0)
            {
                // the class is disabled
                return false;
            }
        }

        // Search for the super classes to be disabled with subclasses
        while (className != null)
        {
            classFilter = getClassFilter(className);
            if (classFilter != null && classFilter.isDisableSubClasses())
            {
                MutableInt filterNumber = classFilters.get(classFilter);
                if (filterNumber != null && filterNumber.intValue() > 0)
                {
                    // the class is disabled
                    return false;
                }
            }
            // continue search
            // look up the hierarchy
            className = generaliseClass(className);
        }
        return true;
    }

    private ClassFilter getClassFilter(QName className)
    {
        ParameterCheck.mandatory("className", className);

        // Check the global, first
        if (!isEnabled())
        {
            return null;
        }

        if (!TransactionalResourceHelper.isResourcePresent(KEY_CLASS_FILTERS))
        {
            // Nothing was disabled
            return null;
        }
        Map<ClassFilter, MutableInt> classFilters = TransactionalResourceHelper.getMap(KEY_CLASS_FILTERS);
        for (ClassFilter classFilter : classFilters.keySet())
        {
            if (classFilter.getClassName().equals(className))
            {
                MutableInt filterNumber = classFilters.get(classFilter);
                if (filterNumber != null && filterNumber.intValue() > 0)
                {
                    return classFilter;
                }
                break;
            }
        }
        return null;
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public boolean isEnabled(NodeRef nodeRef, QName className)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("className", className);

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
            MutableInt filter = classFilters.get(classCheck);
            if (filter != null && filter.intValue() > 0)
            {
                // Class was disabled
                return false;
            }
        }
        return true;
    }

    @Override
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public boolean isEnabled(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

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
    @Extend(traitAPI = BehaviourFilterTrait.class, extensionAPI = BehaviourFilterExtension.class)
    public boolean isActivated()
    {
        return TransactionalResourceHelper.getCount(KEY_FILTER_COUNT) > 0;
    }

    @Override
    public <M extends Trait> ExtendedTrait<M> getTrait(Class<? extends M> traitAPI)
    {
        return (ExtendedTrait<M>) behaviourFilterTrait;
    }
}
