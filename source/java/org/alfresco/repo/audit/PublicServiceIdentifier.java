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
package org.alfresco.repo.audit;

import org.aopalliance.intercept.MethodInvocation;

/**
 * This defines the API to identify the public service upon which a method invocation has been made.
 *  
 * @author Andy Hind
 */
public interface PublicServiceIdentifier
{
    /**
     * Get the name of the public service for the method invocation.
     * 
     * @param mi
     * @return
     */
    public String getPublicServiceName(MethodInvocation mi);
}
