/*
 * Copyright (C) 2006 Alfresco, Inc.
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

package org.alfresco.repo.avm;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;

import junit.framework.TestCase;

/**
 * Tester of the converter from NodeRef, StoreRef space to AVM space.
 * @author britt
 */
public class AVMNodeConverterTest extends TestCase
{
    /**
     * Test Going betwwen a NodeRef and a version, path pair.
     */
    public void testTranslate()
    {
        String avmPath = "main:/";
        int version = 2;
        NodeRef nodeRef = AVMNodeConverter.ToNodeRef(version, avmPath);
        System.out.println(nodeRef);
        Pair<Integer, String> backOut = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        assertEquals(2, backOut.getFirst().intValue());
        assertEquals(avmPath, backOut.getSecond());
        avmPath = "main:/fista/mista/wisticuff";
        version = -1;
        nodeRef = AVMNodeConverter.ToNodeRef(version, avmPath);
        System.out.println(nodeRef);
        backOut = AVMNodeConverter.ToAVMVersionPath(nodeRef);
        assertEquals(-1, backOut.getFirst().intValue());
        assertEquals(avmPath, backOut.getSecond());
    }
}
