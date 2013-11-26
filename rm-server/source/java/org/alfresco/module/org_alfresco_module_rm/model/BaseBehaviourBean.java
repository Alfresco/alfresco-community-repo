/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.model;

import org.alfresco.repo.policy.BehaviourFilter;
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
public abstract class BaseBehaviourBean implements RecordsManagementModel
{
    /** Logger */
    protected static Log logger = LogFactory.getLog(BaseBehaviourBean.class);
    
    /** node service */
    protected NodeService nodeService;
    
    /** dictionary service */
    protected DictionaryService dictionaryService;
    
    /** behaviour filter */
    protected BehaviourFilter behaviourFilter;
    
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

}
