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
package org.alfresco.repo.node.integrity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * @see org.alfresco.repo.node.integrity.IntegrityEvent
 * 
 * @author Derek Hulley
 */
public class IntegrityEventTest extends TestCase
{
    private static final String NAMESPACE = "http://test";
    
    private NodeRef nodeRef;
    private QName typeQName;
    private QName qname;
    private IntegrityEvent event;
    
    public void setUp() throws Exception
    {
        nodeRef = new NodeRef("workspace://protocol/ID123");
        typeQName = QName.createQName(NAMESPACE, "SomeTypeQName");
        qname = QName.createQName(NAMESPACE, "qname");
        
        event = new TestIntegrityEvent(null, null, nodeRef, typeQName, qname);
    }
    
    public void testSetFunctionality() throws Exception
    {
        Set<IntegrityEvent> set = new HashSet<IntegrityEvent>(5);
        boolean added = set.add(event);
        assertTrue(added);
        added = set.add(new TestIntegrityEvent(null, null, nodeRef, typeQName, qname));
        assertFalse(added);
    }
    
    private static class TestIntegrityEvent extends AbstractIntegrityEvent
    {
        public TestIntegrityEvent(
                NodeService nodeService,
                DictionaryService dictionaryService,
                NodeRef nodeRef,
                QName typeQName,
                QName qname)
        {
            super(nodeService, dictionaryService, nodeRef, typeQName, qname);
        }

        public void checkIntegrity(List<IntegrityRecord> eventResults)
        {
            throw new UnsupportedOperationException();
        }
    }
}
