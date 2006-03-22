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
package org.alfresco.service.descriptor;

import org.alfresco.service.license.LicenseDescriptor;


/**
 * Service for retrieving meta-data about Alfresco stack.
 * 
 * @author David Caruana
 *
 */
public interface DescriptorService
{
    /**
     * Get descriptor for the server
     * 
     * @return  server descriptor
     */
    public Descriptor getServerDescriptor();
    
    /**
     * Get descriptor for the repository as it was when first installed.  The current
     * repository descriptor will always be the same as the
     * {@link #getServerDescriptor() server descriptor}.
     * 
     * @return  repository descriptor
     */
    public Descriptor getInstalledRepositoryDescriptor();
    
    /**
     * Gets the License Service
     * 
     * @return  the license service
     */
    public LicenseDescriptor getLicenseDescriptor();
    
}
