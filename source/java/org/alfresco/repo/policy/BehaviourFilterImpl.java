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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of Behaviour Filter.  All methods operate on transactionally-bound
 * resources.  Behaviour will therefore never span transactions; the filter state has
 * the same lifespan as the transaction in which it was created.
 * 
 * @author David Caruana
 */
public class BehaviourFilterImpl implements BehaviourFilter
{
    private static final String KEY_GLOBAL_FILTER = "BehaviourFilterImpl.gloalFilter";
    private static final String KEY_CLASS_FILTER = "BehaviourFilterImpl.classFilter";
    private static final String KEY_NODEREF_FILTER = "BehaviourFilterImpl.nodeRefFilter";
    
    // Dictionary Service
    private DictionaryService dictionaryService;
    
    // Tenant Service
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
        
    public boolean disableBehaviour(QName className)
    {
        List<QName> classFilters = TransactionalResourceHelper.getList(KEY_CLASS_FILTER);
        boolean alreadyDisabled = classFilters.contains(className);
        if (!alreadyDisabled)
        {
            classFilters.add(className);
        }
        return alreadyDisabled;
    }

    public boolean disableBehaviour(NodeRef nodeRef, QName className)
    {
        nodeRef = tenantService.getName(nodeRef);
        
        Map<NodeRef,List<QName>> nodeRefFilters = TransactionalResourceHelper.getMap(KEY_NODEREF_FILTER);
        List<QName> classNames = nodeRefFilters.get(nodeRef);
        if (classNames == null)
        {
            classNames = new ArrayList<QName>();
            nodeRefFilters.put(nodeRef, classNames);
        }
        boolean alreadyDisabled = classNames.contains(className);
        if (!alreadyDisabled)
        {
            classNames.add(className);
        }
        return alreadyDisabled;
    }

    public void enableBehaviour(QName className)
    {
        List<QName> classFilters = TransactionalResourceHelper.getList(KEY_CLASS_FILTER);
        classFilters.remove(className);
    }
    
    public void enableBehaviour(NodeRef nodeRef, QName className)
    {
        nodeRef = tenantService.getName(nodeRef);
        
        Map<NodeRef,List<QName>> nodeRefFilters = TransactionalResourceHelper.getMap(KEY_NODEREF_FILTER);
        List<QName> classNames = nodeRefFilters.get(nodeRef);
        if (classNames != null)
        {
            classNames.remove(className);
            if (classNames.size() == 0)
            {
                nodeRefFilters.remove(nodeRef);
            }
        }
    }

    public void enableBehaviours(NodeRef nodeRef)
    {
        nodeRef = tenantService.getName(nodeRef);
        
        Map<NodeRef,List<QName>> nodeRefFilters = TransactionalResourceHelper.getMap(KEY_NODEREF_FILTER);
        nodeRefFilters.remove(nodeRef);
    }
    
    public void disableAllBehaviours()
    {
        TransactionalResourceHelper.setBoolean(KEY_GLOBAL_FILTER);
    }
    
    public void enableAllBehaviours()
    {
        TransactionalResourceHelper.resetBoolean(KEY_GLOBAL_FILTER);
        
        Map<NodeRef,List<QName>> filters = TransactionalResourceHelper.getMap(KEY_NODEREF_FILTER);
        filters.clear();
    }

    public boolean isEnabled(NodeRef nodeRef, QName className)
    {
        if(TransactionalResourceHelper.testBoolean(KEY_GLOBAL_FILTER))
        {
            return false;
        }
            
        // check global filters
        if (!isEnabled(className))
        {
            return false;
        }
        
        nodeRef = tenantService.getName(nodeRef);
        
        // check node level filters
        Map<NodeRef,List<QName>> filters = TransactionalResourceHelper.getMap(KEY_NODEREF_FILTER);
        List<QName> nodeClassFilters = filters.get(nodeRef);
        if (nodeClassFilters != null)
        {
            boolean filtered = nodeClassFilters.contains(className);
            if (filtered)
            {
                return false;
            }
            for (QName filterName : nodeClassFilters)
            {
                filtered = dictionaryService.isSubClass(className, filterName);
                if (filtered)
                {
                    return false;
                }
            }
        }
        
        return true;
    }

    public boolean isEnabled(QName className)
    {
        if(TransactionalResourceHelper.testBoolean(KEY_GLOBAL_FILTER))
        {
            return false;
        }
        
        // check global class filters
        List<QName> classFilters = TransactionalResourceHelper.getList(KEY_CLASS_FILTER);
        boolean filtered = classFilters.contains(className);
        if (filtered)
        {
            return false;
        }
        for (QName classFilter : classFilters)
        {
            filtered = dictionaryService.isSubClass(className, classFilter);
            if (filtered)
            {
                return false;
            }
        }

        return true;
    }

    public boolean isActivated()
    {
        List<QName> classFilters = TransactionalResourceHelper.getList(KEY_CLASS_FILTER);
        Map<NodeRef,List<QName>> nodeRefFilters = TransactionalResourceHelper.getMap(KEY_NODEREF_FILTER);
        boolean globalFlag = TransactionalResourceHelper.testBoolean(KEY_GLOBAL_FILTER);
        return ((!classFilters.isEmpty()) || (!nodeRefFilters.isEmpty()) || globalFlag);
    }

}
