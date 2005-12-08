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
package org.alfresco.repo.action.evaluator;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionConditionImpl;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.BaseSpringTest;
import org.alfresco.util.GUID;

/**
 * Compare property value evaluator test
 * 
 * @author Roy Wetherall
 */
public class CompareMimeTypeEvaluatorTest extends BaseSpringTest
{
    private NodeService nodeService;
    private ContentService contentService;
    private StoreRef testStoreRef;
    private NodeRef rootNodeRef;
    private NodeRef nodeRef;
    private CompareMimeTypeEvaluator evaluator;
    
    /**
     * @see org.springframework.test.AbstractTransactionalSpringContextTests#onSetUpInTransaction()
     */
    @Override
    protected void onSetUpInTransaction() throws Exception
    {
        
        this.nodeService = (NodeService)this.applicationContext.getBean("nodeService");
        this.contentService = (ContentService)this.applicationContext.getBean("contentService");
        
        // Create the store and get the root node
        this.testStoreRef = this.nodeService.createStore(
                StoreRef.PROTOCOL_WORKSPACE, "Test_"
                        + System.currentTimeMillis());
        this.rootNodeRef = this.nodeService.getRootNode(this.testStoreRef);
        
        // Create the node used for tests
        this.nodeRef = this.nodeService.createNode(
                this.rootNodeRef,
                ContentModel.ASSOC_CHILDREN,
                QName.createQName("{test}testnode"),
                ContentModel.TYPE_CONTENT).getChildRef();
        
        this.evaluator = (CompareMimeTypeEvaluator)this.applicationContext.getBean(CompareMimeTypeEvaluator.NAME);
    }
    
    public void testContentPropertyComparisons()
    {
        ActionConditionImpl condition = new ActionConditionImpl(GUID.generate(), ComparePropertyValueEvaluator.NAME);
        
        // What happens if you do this and the node has no content set yet !!
        
        // Add some content to the node reference
        ContentWriter contentWriter = this.contentService.getWriter(this.nodeRef, ContentModel.PROP_CONTENT, true);
        contentWriter.setEncoding("UTF-8");
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        contentWriter.putContent("This is some test content.");        
        
        // Test matching the mimetype
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_TEXT_PLAIN);
        assertTrue(this.evaluator.evaluate(condition, this.nodeRef));
        condition.setParameterValue(ComparePropertyValueEvaluator.PARAM_VALUE, MimetypeMap.MIMETYPE_HTML);
        assertFalse(this.evaluator.evaluate(condition, this.nodeRef));          
    }
}
