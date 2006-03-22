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

import java.security.Principal;
import java.util.Date;


/**
 * Provides access to License information.
 * 
 * @author davidc
 */
public interface LicenseDescriptor
{

    /**
     * Gets the date license was issued
     * 
     * @return  issue date
     */
    public Date getIssued();
    
    /**
     * Gets the date license is valid till
     * 
     * @return  valid until date (or null, if no time limit)
     */
    public Date getValidUntil();

    /**
     * Gets the length (in days) of license validity
     *  
     * @return  length in days of license validity (or null, if no time limit)
     */
    public Integer getDays();
    
    /**
     * Ges the number of remaining days left on license
     * 
     * @return  remaining days (or null, if no time limit)
     */
    public Integer getRemainingDays();

    /**
     * Gets the subject of the license
     * 
     * @return  the subject
     */
    public String getSubject();
    
    /**
     * Gets the holder of the license
     * 
     * @return  the holder
     */
    public Principal getHolder();
    
    /**
     * Gets the issuer of the license
     * 
     * @return  the issuer
     */
    public Principal getIssuer();
    
}
