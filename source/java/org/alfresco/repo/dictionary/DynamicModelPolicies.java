package org.alfresco.repo.dictionary;

import org.alfresco.repo.policy.ClassPolicy;
import org.alfresco.service.cmr.repository.NodeRef;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class DynamicModelPolicies 
{
	
    public interface OnLoadDynamicModel extends ClassPolicy
    {
        public static final QName QNAME = QName.createQName(NamespaceService.ALFRESCO_URI, "onLoadDynamicModel");
        
        /**
         * Called after a new dynamic model has been loaded.
         * 
         * @param model the model loaded
         * @param nodeRef the node ref of the model
         */
        public void onLoadDynamicModel(M2Model model, NodeRef nodeRef);
    }

}
