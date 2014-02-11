/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */

package org.alfresco.repo.avm;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.test_category.LegacyCategory;
import org.alfresco.util.Pair;
import org.junit.experimental.categories.Category;

import junit.framework.TestCase;

/**
 * Tester of the converter from NodeRef, StoreRef space to AVM space.
 * @author britt
 */
@Category(LegacyCategory.class)
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
