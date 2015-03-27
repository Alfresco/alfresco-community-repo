/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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