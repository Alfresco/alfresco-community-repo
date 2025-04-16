/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util.schemacomp.model;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import org.alfresco.test_category.BaseSpringTestsCategory;

/**
 * Tests for the Sequence class.
 * 
 * @author Matt Ward
 */
@Category(BaseSpringTestsCategory.class)
public class SequenceTest extends DbObjectTestBase<Sequence>
{
    private Sequence thisSequence;
    private Sequence thatSequence;

    @Before
    public void setUp()
    {
        thisSequence = new Sequence(null, "this_sequence");
        thatSequence = new Sequence(null, "that_sequence");
    }

    @Test
    public void acceptVisitor()
    {
        thisSequence.accept(visitor);

        verify(visitor).visit(thisSequence);
    }

    @Override
    protected Sequence getThisObject()
    {
        return thisSequence;
    }

    @Override
    protected Sequence getThatObject()
    {
        return thatSequence;
    }

    @Override
    protected void doDiffTests()
    {
        // Nothing extra to diff.
    }
}
