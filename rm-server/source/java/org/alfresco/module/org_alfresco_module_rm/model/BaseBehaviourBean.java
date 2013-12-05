/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.model;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.annotation.BehaviourRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Convenient base class for behaviour beans.
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
public abstract class BaseBehaviourBean implements RecordsManagementModel,
                                           BehaviourRegistry
{
    /** Logger */
    protected static Log logger = LogFactory.getLog(BaseBehaviourBean.class);
    
    /** node service */
    protected NodeService nodeService;
    
    /** dictionary service */
    protected DictionaryService dictionaryService;
    
    /** behaviour filter */
    protected BehaviourFilter behaviourFilter;
    
    /** behaviour map */
    protected Map<String, org.alfresco.repo.policy.Behaviour> behaviours = new HashMap<String, org.alfresco.repo.policy.Behaviour>(7);
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param behaviourFilter   behaviour filter
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }
    
    /**
     * Utility method to safely and quickly determine if a node is a type (or sub-type) of the one specified.
     * 
     * @param nodeRef       node reference
     * @param ofClassName   class name to check
     */
    protected boolean instanceOf(NodeRef nodeRef, QName ofClassName)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("ofClassName", ofClassName);
        boolean result = false;
        if (nodeService.exists(nodeRef) == true &&
            (ofClassName.equals(nodeService.getType(nodeRef)) == true ||
             dictionaryService.isSubClass(nodeService.getType(nodeRef), ofClassName) == true))            
        {
            result = true;
        }    
        return result;
    }

    /**
     * @see org.alfresco.repo.policy.annotation.BehaviourRegistry#registerBehaviour(java.lang.String, org.alfresco.repo.policy.Behaviour)
     */
    @Override
    public void registerBehaviour(String name, org.alfresco.repo.policy.Behaviour behaviour)
    {
        if (behaviours.containsKey(name) == true)
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
