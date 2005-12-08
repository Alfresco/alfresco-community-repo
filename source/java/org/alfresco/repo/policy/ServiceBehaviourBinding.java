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


/**
 * Behaviour binding to a Service.
 * 
 * @author David Caruana
 *
 */
public class ServiceBehaviourBinding implements BehaviourBinding
{
    // The service
    private Object service;

    /**
     * Construct
     * 
     * @param service  the service
     */
    /*package*/ ServiceBehaviourBinding(Object service)
    {
        this.service = service;
    }
    
    /* (non-Javadoc)
     * @see org.alfresco.repo.policy.BehaviourBinding#generaliseBinding()
     */
    public BehaviourBinding generaliseBinding()
    {
        return null;
    }
    
    /**
     * Gets the Service
     * 
     * @return  the service
     */
    public Object getService()
    {
        return service;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof ServiceBehaviourBinding))
        {
            return false;
        }
        return service.equals(((ServiceBehaviourBinding)obj).service);
    }

    @Override
    public int hashCode()
    {
        return service.hashCode();
    }

    @Override
    public String toString()
    {
        return "ServiceBinding[service=" + this + "]";
    }

}
