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
package org.alfresco.repo.workflow.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QNameMap;


/**
 * List of jBPM Nodes
 * 
 * @author davidc
 */
public class JBPMNodeList extends ArrayList<JBPMNode>
{
    private static final long serialVersionUID = 1376915749912156471L;

    protected ServiceRegistry services;
    
    
    /**
     * Construct
     * 
     * @param nodeRef  node reference
     * @param services  services
     */
    public JBPMNodeList(ServiceRegistry services)
    {
        super();
        this.services = services;
    }
    
    /**
     * Accessor to retrieve a named property on all nodes in the list
     * 
     * @param propertyName  the name of the property to retrieve
     * @return  a collection of property name values
     */
    public Map<String, Collection<Object>> getValues(String propertyName)
    {
        ValuesMap<String, Collection<Object>> values = new ValuesMap<String, Collection<Object>>(null); 

        for (int i = 0; i < JBPMNodeList.this.size(); i++)
        {
            JBPMNode node = JBPMNodeList.this.get(i);
            Map<String, Object> nodeValues = node.getProperties();
            for (String key : nodeValues.keySet())
            {
                values.put(key, null);
            }
        }
        
        return values;
    }

    
    public class ValuesMap<K, V> extends QNameMap<K, V>
    {
        /**
         * 
         */
        private static final long serialVersionUID = -6463958742416258009L;


        public ValuesMap(NamespacePrefixResolver resolver)
        {
            super(resolver);
            // TODO Auto-generated constructor stub
        }


        @Override
        public Object get(Object key)
        {
            Collection<Object> values = null;
            if (containsKey(key))
            {
                values = new ArrayList<Object>(this.size());

                for (int i = 0; i < JBPMNodeList.this.size(); i++)
                {
                    JBPMNode node = JBPMNodeList.this.get(i);
                    Object value = node.getProperties().get(key);
                    values.add(value);
                }
            }
            
            return values;        
        }

    }
    
}
