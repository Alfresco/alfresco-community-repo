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
package org.alfresco.module.org_alfresco_module_rm.test.integration.version;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.test.util.BaseRMTestCase;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionServiceImpl;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 *
 * 
 * @author Roy Wetherall
 * @since 2.3
 */
public class AdHocRecordableVersions extends BaseRMTestCase implements RecordableVersionModel
{
    private static final QName QNAME_PUBLISHER = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "publisher");
    private static final QName QNAME_SUBJECT = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "subject");
    
    private static final String DESCRIPTION = "description";
    private static final String PUBLISHER = "publisher";
    private static final String SUBJECT = "subject";
    
    @Override
    protected boolean isCollaborationSiteTest()
    {
        return true;
    }
    
    public void testRecordAdHocVersionNoPolicy()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest()
        {
            private Version version;
            private Map<String, Serializable> versionProperties;            
            
            public void given() throws Exception
            {
                // add Dublin core aspect
                PropertyMap dublinCoreProperties = new PropertyMap(2);
                dublinCoreProperties.put(QNAME_PUBLISHER, PUBLISHER);
                dublinCoreProperties.put(QNAME_SUBJECT, SUBJECT);
                nodeService.addAspect(documentLibrary, ContentModel.ASPECT_DUBLINCORE, dublinCoreProperties);
                
                // setup version properties
                versionProperties = new HashMap<String, Serializable>(4);
                versionProperties.put(Version.PROP_DESCRIPTION, DESCRIPTION);
                versionProperties.put(VersionModel.PROP_VERSION_TYPE, VersionType.MINOR);
                versionProperties.put(RecordableVersionServiceImpl.KEY_RECORDABLE_VERSION, true);
                versionProperties.put(RecordableVersionServiceImpl.KEY_FILE_PLAN, filePlan);

            }
            
            public void when()
            {                
                // create version
                version = versionService.createVersion(dmDocument, versionProperties);
            }            
            
            public void then()
            {
                // version has been created
                assertNotNull(version);
                
                // check the version properties
                assertEquals(DESCRIPTION, version.getDescription());
                assertEquals("0.1", version.getVersionLabel());
                
                assertEquals(NAME_DM_DOCUMENT, nodeService.getProperty(dmDocument, ContentModel.PROP_NAME));
                
                NodeRef frozen = version.getFrozenStateNodeRef();
                assertEquals(NAME_DM_DOCUMENT, nodeService.getProperty(frozen, ContentModel.PROP_NAME));
                
                // record version node reference is available on version
                NodeRef record = (NodeRef)version.getVersionProperties().get("RecordVersion");
                assertNotNull(record);
                
                // record version is an unfiled record
                assertTrue(recordService.isRecord(record));
                assertFalse(recordService.isFiled(record));
            }
        });
        
    }
    
}
