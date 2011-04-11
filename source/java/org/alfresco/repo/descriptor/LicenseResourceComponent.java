/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.descriptor;

/**
 * The licence resource component knows the locations where license files 
 * may be found.
 * 
 * Locations are suitable to be loaded by spring's getResource method.
 * 
 */
public class LicenseResourceComponent
{
    public LicenseResourceComponent()
    {
    }
    
    private String externalLicenseLocation = "*.lic";
    private String embeddedLicenseLocation = "/WEB-INF/alfresco/license/*.lic";
    private String sharedLicenseLocation = "classpath*:/alfresco/extension/license/*.lic";
    
    public void setExternalLicenseLocation(String externalLicenseLocation)
    {
        this.externalLicenseLocation = externalLicenseLocation;
    }
    public String getExternalLicenseLocation()
    {
        return externalLicenseLocation;
    }
    public void setEmbeddedLicenseLocation(String embeddedLicenseLocation)
    {
        this.embeddedLicenseLocation = embeddedLicenseLocation;
    }
    public String getEmbeddedLicenseLocation()
    {
        return embeddedLicenseLocation;
    }
    public void setSharedLicenseLocation(String sharedLicenseLocation)
    {
        this.sharedLicenseLocation = sharedLicenseLocation;
    }
    public String getSharedLicenseLocation()
    {
        return sharedLicenseLocation;
    }
}
