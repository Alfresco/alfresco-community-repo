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

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.PropertyMap;

/**
 * Auto Recordable Versions Integration Test
 *  
 * @author Roy Wetherall
 * @since 2.3
 */
public class AutoRecordableVersions extends RecordableVersionsBaseTest 
{
    public final static String MY_NEW_CONTENT = "this is some new content that I have changed to trigger auto version";
    
    public void testAutoVersionRecordAllRevisions()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            public void given() throws Exception
            {     
                // make the node versionable
                PropertyMap versionableProperties = new PropertyMap(1);
                versionableProperties.put(ContentModel.PROP_INITIAL_VERSION, false);
                nodeService.addAspect(dmDocument, ContentModel.ASPECT_VERSIONABLE, versionableProperties);
                
                // set the recordable version policy
                PropertyMap recordableVersionProperties = new PropertyMap(1);
                recordableVersionProperties.put(PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                recordableVersionProperties.put(PROP_FILE_PLAN, filePlan);
                nodeService.addAspect(dmDocument, RecordableVersionModel.ASPECT_VERSIONABLE, recordableVersionProperties);               
            }
            
            public void when()
            {   
                // generate new version by updating content
                ContentWriter writer = contentService.getWriter(dmDocument, ContentModel.PROP_CONTENT, true);
                writer.putContent(MY_NEW_CONTENT);
            }            
            
            public void then()
            {
                // check that the record has been recorded
                checkRecordedVersion(dmDocument, null, "0.2");
            }
        });        
    }  
    
}
