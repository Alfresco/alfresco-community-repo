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
package org.alfresco.repo.service;


/**
 * Service Meta Data
 * 
 * @author David Caruana
 */
public interface ServiceDescriptorMetaData
{
    /**
     * @return the service name namespace
     */
    public String getNamespace();

    /**
     * @return the service description
     */
    public String getDescription();

    /**
     * @return the service interface class
     */
    public Class getInterface();
    
}
