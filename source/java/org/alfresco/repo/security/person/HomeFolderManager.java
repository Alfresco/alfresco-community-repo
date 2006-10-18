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
package org.alfresco.repo.security.person;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.datatype.DefaultTypeConverter;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.InitializingBean;

/**
 * Manage home folder creation by binding to events from the cm:person type.
 *  
 * @author Andy Hind
 */
public class HomeFolderManager implements InitializingBean, NodeServicePolicies.OnCreateNodePolicy
{
    
    private PolicyComponent policyComponent;

    private NodeService nodeService;

    /**
     * A default provider
     */
    private HomeFolderProvider defaultProvider;

    /**
     * Providers that have registered and are looken up by name (== bean name)
     */
    private Map<String, HomeFolderProvider> providers = new HashMap<String, HomeFolderProvider>();

    /**
     * Bind the calss behaviour to this implementation
     */
    public void afterPropertiesSet() throws Exception
    {
        policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onCreateNode"),
                ContentModel.TYPE_PERSON, new JavaBehaviour(this, "onCreateNode"));
    }

    /**
     * Set the policy component.
     * 
     * @param policyComponent
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the node service.
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Register a home folder provider.
     * 
     * @param provider
     */
    public void addProvider(HomeFolderProvider provider)
    {
        providers.put(provider.getName(), provider);
    }

    /**
     * Set the default home folder provider (user which none is specified or when one is not found)
     * @param defaultProvider
     */
    public void setDefaultProvider(HomeFolderProvider defaultProvider)
    {
        this.defaultProvider = defaultProvider;
    }

    /**
     * Find the provider and call.
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        HomeFolderProvider provider = defaultProvider;
        String providerName = DefaultTypeConverter.INSTANCE.convert(String.class, nodeService.getProperty(childAssocRef
                .getChildRef(), ContentModel.PROP_HOME_FOLDER_PROVIDER));
        if (providerName != null)
        {
            provider = providers.get(providerName);
            if (provider == null)
            {
                provider = defaultProvider;
            }
        }
        if (provider != null)
        {
            provider.onCreateNode(childAssocRef);
        }
    }
}
