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
package org.alfresco.service.license;


/**
 * Contract for managing licenses
 * 
 * @author davidc
 */
public interface LicenseService
{

    /**
     * Verify License
     * 
     * @throws LicenseException
     */
    public void verifyLicense() throws LicenseException;
    
    /**
     * Get description of installed license
     * 
     * @return  license descriptor (or null, if one is not installed)
     * @throws LicenseException
     */
    public LicenseDescriptor getLicense() throws LicenseException;
    
}
