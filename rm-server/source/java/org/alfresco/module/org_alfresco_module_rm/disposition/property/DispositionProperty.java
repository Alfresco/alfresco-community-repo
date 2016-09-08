/**
 * 
 */
package org.alfresco.module.org_alfresco_module_rm.disposition.property;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionAction;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionActionDefinition;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * @author Roy Wetherall
 */
public class DispositionProperty implements NodeServicePolicies.OnUpdatePropertiesPolicy,
                                            RecordsManagementModel
{
    /** Property QName */
    private QName propertyName;
    
    /** Behaviour */
    private JavaBehaviour behaviour;
    
    /** Namespace service */
    private NamespaceService namespaceService;
    
    /** Disposition service */
    private DispositionService dispositionService;
    
    /** Policy component */
    private PolicyComponent policyComponent;
    
    /** Dictionary service */
    private DictionaryService dictionaryService;
    
    /** Node service */
    private NodeService nodeService;
    
    /** Indicates whether this disposition property applies to a folder level disposition */
    private boolean appliesToFolderLevel = true;

    /** Indicates whether this disposition property applies to a record level disposition */
    private boolean appliesToRecordLevel = true;
    
    /** Set of disposition actions this property does not apply to */
    private Set<String> excludedDispositionActions;
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }
    
    /**
     * @param dispositionService    disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    /**
     * @param policyComponent   policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }
    
    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }
    
    /**
     * @param propertyName property name (as string)
     */
    public void setName(String propertyName)
    {
        this.propertyName = QName.createQName(propertyName, namespaceService);
    }
    
    /**
     * @return  property QName
     */
    public QName getQName()
    {
        return this.propertyName;
    }
    
    /**
     * @return  property definition
     */
    public PropertyDefinition getPropertyDefinition()
    {
        return dictionaryService.getProperty(propertyName);
    }
    
    /**
     * @param excludedDispositionActions    list of excluded disposition actions
     */
    public void setExcludedDispositionActions(Set<String> excludedDispositionActions)
    {
        this.excludedDispositionActions = excludedDispositionActions;
    }
    
    /**
     * @param appliesToFolderLevel
     */
    public void setAppliesToFolderLevel(boolean appliesToFolderLevel)
    {
        this.appliesToFolderLevel = appliesToFolderLevel;
    }
    
    /**
     * @param appliesToRecordLevel
     */
    public void setAppliesToRecordLevel(boolean appliesToRecordLevel)
    {
        this.appliesToRecordLevel = appliesToRecordLevel;
    }
    
    /**
     * Bean initialisation method
     */
    public void init()
    {
        // register with disposition service
        dispositionService.registerDispositionProperty(this);
        
        // register interest in the update properties policy for disposition 
        behaviour = new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT);
        policyComponent.bindClassBehaviour(
                NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
                ASPECT_DISPOSITION_LIFECYCLE, 
                behaviour);        
    }
    
    /**
     * Indicates whether the disposition property applies given the context.
     * 
     * @param isRecordLevel      true if record level disposition schedule, false otherwise
     * @param dispositionAction  disposition action name
     * @return boolean           true if applies, false otherwise
     */
    public boolean applies(boolean isRecordLevel, String dispositionAction)
    {
        boolean result = false;
        
        if ((isRecordLevel == true && appliesToRecordLevel == true) ||
            (isRecordLevel == false && appliesToFolderLevel == true))
        {
            if (excludedDispositionActions != null && excludedDispositionActions.size() != 0)
            {
                if (excludedDispositionActions.contains(dispositionAction) == false)
                {
                    result = true;
                }
            }
            else
            {
                result = true;
            }
        }
        
        return result;
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    public void onUpdateProperties(
            final NodeRef nodeRef, 
            final Map<QName, Serializable> before, 
            final Map<QName, Serializable> after)
    {
        if (nodeService.exists(nodeRef) == true)
        {
            // has the property we care about changed?
            if (isPropertyUpdated(before, after) == true)
            {
                behaviour.disable();
                try
                {   
                    AuthenticationUtil.runAs(new RunAsWork<Void>()
                    {
                        @Override
                        public Void doWork() throws Exception
                        {
                            Date updatedDateValue = (Date)after.get(propertyName);
                            if (updatedDateValue != null)
                            {                    
                                DispositionAction dispositionAction = dispositionService.getNextDispositionAction(nodeRef);
                                if (dispositionAction != null)
                                {
                                    DispositionActionDefinition daDefinition = dispositionAction.getDispositionActionDefinition();
                                    if (daDefinition != null)
                                    {
                                        // check whether the next disposition action matches this disposition property
                                        if (propertyName.equals(daDefinition.getPeriodProperty()) == true)
                                        {
                                            Period period = daDefinition.getPeriod();
                                            Date updatedAsOf = period.getNextDate(updatedDateValue);
                                            
                                            // update asOf date on the disposition action based on the new property value
                                            NodeRef daNodeRef = dispositionAction.getNodeRef();
                                            nodeService.setProperty(daNodeRef, PROP_DISPOSITION_AS_OF, updatedAsOf);
                                        }
                                    }
                                }
                            }
                            else
                            {
                                // throw an exception if the property is being 'cleared'
                                if (before.get(propertyName) != null)
                                {
                                    throw new AlfrescoRuntimeException(
                                            "Error updating property " + propertyName.toPrefixString(namespaceService) + 
                                            " to null, because property is being used to determine a disposition date.");
                                }
                            }
                            
                            return null;
                        }
                        
                    }, AuthenticationUtil.getSystemUserName());
                }
                finally
                {
                    behaviour.enable();
                }    
            }
        }
    }
    
    /**
     * Indicates whether the property has been updated or not.
     * 
     * @param before
     * @param after
     * @return
     */
    private boolean isPropertyUpdated(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        boolean result = false;
        
        Serializable beforeValue = before.get(propertyName);
        Serializable afterValue = after.get(propertyName);
        
        if (beforeValue == null && afterValue != null)
        {
            result = true;
        }
        else if (beforeValue != null && afterValue == null)
        {
            result = true;
        }
        else if (beforeValue != null && afterValue != null &&
                 beforeValue.equals(afterValue) == false)
        {
            result = true;
        }
        
        return result;
    }
}
