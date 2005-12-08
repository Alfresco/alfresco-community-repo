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

import org.alfresco.service.namespace.QName;


/**
 * Definition of a Policy
 * 
 * @author David Caruana
 *
 * @param <P>  the policy interface
 */
public interface PolicyDefinition<P extends Policy>
{
    /**
     * Gets the name of the Policy
     * 
     * @return  policy name
     */
    public QName getName();
    
    
    /**
     * Gets the Policy interface class
     * 
     * @return  the class
     */
    public Class<P> getPolicyInterface();

    
    /**
     * Gets the Policy type
     * @return  the policy type
     */
    public PolicyType getType();
}
