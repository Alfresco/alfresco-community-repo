/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.util.schemacomp.model;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests for the Schema class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class SchemaTest extends DbObjectTestBase<Schema>
{
    private Schema left;
    private Schema right;
    
    @Before
    public void setUp()
    {
        left = new Schema("left_schema");
        right = new Schema("right_schema");
    }
    
    @Override
    protected Schema getThisObject()
    {
        return left;
    }

    @Override
    protected Schema getThatObject()
    {
        return right;
    }

    @Override
    protected void doDiffTests()
    {
        // In addition to the base class functionality, Schema.diff() compares
        // the DbObjects held in the other schema with its own DbObjects.
        inOrder.verify(comparisonUtils).compareCollections(left.objects, right.objects, ctx);
    }
}
