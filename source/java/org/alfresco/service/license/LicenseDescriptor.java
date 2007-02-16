/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
