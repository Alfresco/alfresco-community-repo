/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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
