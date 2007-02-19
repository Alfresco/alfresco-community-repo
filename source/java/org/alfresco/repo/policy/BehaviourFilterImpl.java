/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.policy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Implementation of Behaviour Filter.
 * 
 * @author David Caruana
 */
public class BehaviourFilterImpl implements BehaviourFilter
{
    // Thread local storage of filters
    ThreadLocal<List<QName>> classFilter = new ThreadLocal<List<QName>>();
    ThreadLocal<Map<NodeRef,List<QName>>> nodeRefFilter = new ThreadLocal<Map<NodeRef,List<QName>>>();
    
    // Dictionary Service
    private DictionaryService dictionaryService;
    
    /**
     * @param dictionaryService  dictionary service
     */    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
        
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#disableBehaviour(org.alfresco.service.namespace.QName)
     */
    public boolean disableBehaviour(QName className)
    {
        List<QName> classNames = classFilter.get();
        if (classNames == null)
        {
            classNames = new ArrayList<QName>();
            classFilter.set(classNames);
        }
        boolean alreadyDisabled = classNames.contains(className);
        if (!alreadyDisabled)
        {
            classNames.add(className);
        }
        return alreadyDisabled;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#disableBehaviour(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public boolean disableBehaviour(NodeRef nodeRef, QName className)
    {
        Map<NodeRef,List<QName>> filters = nodeRefFilter.get();
        if (filters == null)
        {
            filters = new HashMap<NodeRef,List<QName>>();
            nodeRefFilter.set(filters);
        }
        List<QName> classNames = filters.get(nodeRef);
        if (classNames == null)
        {
            classNames = new ArrayList<QName>();
            filters.put(nodeRef, classNames);
        }
        boolean alreadyDisabled = classNames.contains(className);
        if (!alreadyDisabled)
        {
            classNames.add(className);
        }
        return alreadyDisabled;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#enableBehaviour(org.alfresco.service.namespace.QName)
     */
    public void enableBehaviour(QName className)
    {
        List<QName> classNames = classFilter.get();
        if (classNames != null)
        {
            classNames.remove(className);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#enableBehaviour(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public void enableBehaviour(NodeRef nodeRef, QName className)
    {
        Map<NodeRef,List<QName>> filters = nodeRefFilter.get();
        if (filters != null)
        {
            List<QName> classNames = filters.get(nodeRef);
            if (classNames != null)
            {
                classNames.remove(className);
            }
            if (classNames.size() == 0)
            {
                filters.remove(nodeRef);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#enableBehaviours(org.alfresco.service.cmr.repository.NodeRef)
     */
    public void enableBehaviours(NodeRef nodeRef)
    {
        Map<NodeRef,List<QName>> filters = nodeRefFilter.get();
        if (filters != null)
        {
            filters.remove(nodeRef);
        }
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#enableAllBehaviours()
     */
    public void enableAllBehaviours()
    {
        Map<NodeRef,List<QName>> filters = nodeRefFilter.get();
        if (filters != null)
        {
            filters.clear();
        }
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#isEnabled(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
     */
    public boolean isEnabled(NodeRef nodeRef, QName className)
    {
        // check global filters
        if (!isEnabled(className))
        {
            return false;
        }
        
        // check node level filters
        Map<NodeRef,List<QName>> nodeFilters = nodeRefFilter.get();
        if (nodeFilters != null)
        {
            List<QName> nodeClassFilters = nodeFilters.get(nodeRef);
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
        }
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#isEnabled(org.alfresco.service.namespace.QName)
     */
    public boolean isEnabled(QName className)
    {
        // check global class filters
        List<QName> classFilters = classFilter.get();
        if (classFilters != null)
        {
            boolean filtered = classFilters.contains(className);
            if (filtered)
            {
                return false;
            }
            for (QName filterName : classFilters)
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

    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourFilter#isActivated()
     */
    public boolean isActivated()
    {
        List<QName> classFilters = classFilter.get();
        Map<NodeRef,List<QName>> nodeFilters = nodeRefFilter.get();
        return (classFilters != null && !classFilters.isEmpty()) || (nodeFilters != null && !nodeFilters.isEmpty());
    }

}
