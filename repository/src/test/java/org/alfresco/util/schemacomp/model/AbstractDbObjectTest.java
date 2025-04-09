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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.inOrder;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.util.schemacomp.DbObjectVisitor;
import org.alfresco.util.schemacomp.DbProperty;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Difference.Where;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.validator.AbstractDbValidator;
import org.alfresco.util.schemacomp.validator.DbValidator;

/**
 * Tests for the AbstractDbObject base class.
 * 
 * @author Matt Ward
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractDbObjectTest
{
    private ConcreteDbObject dbObject;
    private @Mock Results differences;
    private DiffContext ctx;
    @Mock
    private Dialect dialect;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        dbObject = new ConcreteDbObject("the_object");
        ctx = new DiffContext(dialect, differences, null, null);
    }

    @Test
    public void sameAs()
    {
        dbObject.setName(null);
        assertFalse("Not the same.", dbObject.sameAs(null));
        assertFalse("Not the same.", dbObject.sameAs(new ConcreteDbObject("other_obj_name")));
        assertTrue("The very same", dbObject.sameAs(dbObject));

        dbObject.setName("the_name");
        assertFalse("Not the same.", dbObject.sameAs(null));
        assertFalse("Not the same.", dbObject.sameAs(new ConcreteDbObject("different_name")));
        assertFalse("Not the same type", dbObject.sameAs(new AnotherConcreteDbObject("the_name")));
        assertTrue("Logically the same object.", dbObject.sameAs(new ConcreteDbObject("the_name")));
        assertTrue("The very same object with non-null name", dbObject.sameAs(dbObject));
    }

    @Test
    public void diff()
    {
        ConcreteDbObject otherObject = new ConcreteDbObject("the_other_object");

        dbObject.diff(otherObject, ctx);

        InOrder inOrder = inOrder(differences);

        // The name of the object should be diffed
        inOrder.verify(differences).add(
                Where.IN_BOTH_BUT_DIFFERENCE,
                new DbProperty(dbObject, "name"),
                new DbProperty(otherObject, "name"));

        // Then the doDiff() method should be processed
        inOrder.verify(differences).add(
                Where.IN_BOTH_BUT_DIFFERENCE,
                new DbProperty(dbObject, "someProp"),
                new DbProperty(otherObject, "someProp"));
    }

    @Test
    public void canGetValidators()
    {
        List<DbValidator> validators = dbObject.getValidators();
        assertEquals(0, validators.size());

        dbObject.setValidators(null);
        validators = dbObject.getValidators();
        assertEquals(0, validators.size());

        dbObject.setValidators(validatorList(new TestValidator1(), new TestValidator2()));
        validators = dbObject.getValidators();
        assertEquals(2, validators.size());
        assertEquals(TestValidator1.class, validators.get(0).getClass());
        assertEquals(TestValidator2.class, validators.get(1).getClass());
    }

    /**
     * Concrete DbObject for testing the AbstractDbObject base class.
     */
    public static class ConcreteDbObject extends AbstractDbObject
    {
        private String someProp = "property value";

        public ConcreteDbObject(String name)
        {
            super(null, name);
        }

        @Override
        protected void doDiff(DbObject right, DiffContext ctx)
        {
            Results differences = ctx.getComparisonResults();
            differences.add(
                    Where.IN_BOTH_BUT_DIFFERENCE,
                    new DbProperty(this, "someProp"),
                    new DbProperty(right, "someProp"));
        }

        @Override
        public void accept(DbObjectVisitor visitor)
        {}

        public String getSomeProp()
        {
            return this.someProp;
        }
    }

    public static class AnotherConcreteDbObject extends AbstractDbObject
    {
        public AnotherConcreteDbObject(String name)
        {
            super(null, name);
        }

        @Override
        public void accept(DbObjectVisitor visitor)
        {}
    }

    private List<DbValidator> validatorList(DbValidator... validators)
    {
        return Arrays.asList(validators);
    }

    private static class TestValidator extends AbstractDbValidator
    {
        @Override
        public void validate(DbObject reference, DbObject target, DiffContext ctx)
        {}
    }

    private static class TestValidator1 extends TestValidator
    {}

    private static class TestValidator2 extends TestValidator
    {}
}
