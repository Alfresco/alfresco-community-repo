/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.test.integration.version;

import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.relationship.Relationship;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionModel;
import org.alfresco.module.org_alfresco_module_rm.version.RecordableVersionPolicy;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.util.PropertyMap;

/**
 * Auto Recordable Versions Integration Test
 *  
 * @author Roy Wetherall
 * @since 2.3
 */
public class AutoRecordableVersionsTest extends RecordableVersionsBaseTest 
{
    /** example content */
    public final static String MY_NEW_CONTENT = "this is some new content that I have changed to trigger auto version";
    
    /**
     * Given that all revisions will be recorded,
     * When I update the content of a document,
     * Then a recorded version will be created
     */
    public void testAutoVersionRecordAllRevisions()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator)
        {
            public void given() throws Exception
            {     
                // set the recordable version policy
                PropertyMap recordableVersionProperties = new PropertyMap(1);
                recordableVersionProperties.put(PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                recordableVersionProperties.put(PROP_FILE_PLAN, filePlan);
                nodeService.addAspect(dmDocument, RecordableVersionModel.ASPECT_VERSIONABLE, recordableVersionProperties);   
                
                // make the node versionable
                PropertyMap versionableProperties = new PropertyMap(1);
                versionableProperties.put(ContentModel.PROP_INITIAL_VERSION, false);
                nodeService.addAspect(dmDocument, ContentModel.ASPECT_VERSIONABLE, versionableProperties);                                            
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
                checkRecordedVersion(dmDocument, null, "0.1");
            }
        });        
    }  
    
    /**
     * Given that all revisions will be automatically recorded,
     * When I update a document 3 times,
     * Then all 3 created records will be related together using the "VersionedBy" relationship
     */
    public void testVersionRecordsRelated()
    {
        doBehaviourDrivenTest(new BehaviourDrivenTest(dmCollaborator, false)
        {
            /** given **/
            public void given() throws Exception
            {   
                doTestInTransaction(new VoidTest()
                {
                    @Override
                    public void runImpl() throws Exception
                    {                 
                        // set the recordable version policy
                        PropertyMap recordableVersionProperties = new PropertyMap(1);
                        recordableVersionProperties.put(PROP_RECORDABLE_VERSION_POLICY, RecordableVersionPolicy.ALL);
                        recordableVersionProperties.put(PROP_FILE_PLAN, filePlan);
                        nodeService.addAspect(dmDocument, RecordableVersionModel.ASPECT_VERSIONABLE, recordableVersionProperties);   
                        
                        // make the node versionable
                        PropertyMap versionableProperties = new PropertyMap(1);
                        versionableProperties.put(ContentModel.PROP_INITIAL_VERSION, false);
                        nodeService.addAspect(dmDocument, ContentModel.ASPECT_VERSIONABLE, versionableProperties);
                    }
                });
            }
            
            /** when **/
            public void when()
            {   
                // update the content 3 times
                updateContent();
                updateContent();
                updateContent();
            }            
            
            /** then */
            public void then()
            {
                doTestInTransaction(new VoidTest()
                {
                    @Override
                    public void runImpl() throws Exception
                    {
                        // check that the record has been recorded
                        checkRecordedVersion(dmDocument, null, "0.3");
                        
                        Version version = versionService.getCurrentVersion(dmDocument);                        
                        NodeRef record = recordableVersionService.getVersionRecord(version);
                        
                        boolean foundPrevious = false;
                        Set<Relationship> relationships = relationshipService.getRelationshipsFrom(record);
                        assertNotNull(relationships);
                        assertEquals(1, relationships.size());
                        for (Relationship relationship : relationships)
                        {
                            if (relationship.getUniqueName().equals(CUSTOM_REF_VERSIONS.getLocalName()))
                            {
                                NodeRef previousVersionRecord = relationship.getTarget();
                                assertNotNull(previousVersionRecord);
                                foundPrevious = true;
                            }
                        }
                        assertTrue(foundPrevious);
                    }
                });
            }
            
            /**
             * Helper method to update content of dmDocument 
             */
            private void updateContent()
            {
                doTestInTransaction(new VoidTest()
                {
                    @Override
                    public void runImpl() throws Exception
                    {
                        ContentWriter writer = contentService.getWriter(dmDocument, ContentModel.PROP_CONTENT, true);
                        writer.putContent(MY_NEW_CONTENT);
                    }
                });
            }            
        });        
    } 
}
