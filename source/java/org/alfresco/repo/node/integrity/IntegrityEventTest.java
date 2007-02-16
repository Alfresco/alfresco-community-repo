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
