 
package org.alfresco.module.org_alfresco_module_rm.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.QName;

/**
 * Helper class to help with the unit testing of RecordableVersionServiceImpl.
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class TestRecordableVersionServiceImpl extends RecordableVersionServiceImpl
{
    @Override
    protected void invokeBeforeCreateVersion(NodeRef nodeRef)
    {
    }        
    
    @Override
    protected void invokeAfterCreateVersion(NodeRef nodeRef, Version version)
    {
    }
    
    @Override
    protected void invokeAfterVersionRevert(NodeRef nodeRef, Version version)
    {
    }
    
    @Override
    protected void invokeOnCreateVersion(NodeRef nodeRef, Map<String, Serializable> versionProperties,PolicyScope nodeDetails)
    {
    }
    
    @Override
    protected String invokeCalculateVersionLabel(QName classRef, Version preceedingVersion, int versionNumber, Map<String, Serializable> versionProperties)
    {
        return "1.1";
    }
}