
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
