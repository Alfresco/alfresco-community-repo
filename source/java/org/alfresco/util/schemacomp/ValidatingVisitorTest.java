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
package org.alfresco.util.schemacomp;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Table;
import org.alfresco.util.schemacomp.validator.DbValidator;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the ValidatingVisitor class.
 * 
 * @author Matt Ward
 */
public class ValidatingVisitorTest
{
    private DiffContext ctx;
    private ValidatingVisitor visitor;
    private Table refTable;
    private Table targetTable;
    private Schema refSchema;
    private Schema targetSchema;
    private Index refIndex;
    private Index targetIndex1;
    private Index targetIndex2;    
    private Index targetIndex3;    
    private List<DbValidator> validators;
    private ComparisonUtils comparisonUtils;
    

    @Before
    public void setUp() throws Exception
    {
        refTable = new Table("reference_table");
        refIndex = new Index(refTable, "index_name", Arrays.asList("a", "b", "c"));
        ctx = new DiffContext(new MySQL5InnoDBDialect(), new Results(),
                    new ArrayList<ValidationResult>(), refSchema, targetSchema);
        visitor = new ValidatingVisitor(ctx);
        
        validators = new ArrayList<DbValidator>();
        validators.add(Mockito.mock(DbValidator.class));
        validators.add(Mockito.mock(DbValidator.class));
        refIndex.setValidators(validators);

        targetTable = new Table("target_table");
        targetIndex1 = new Index(targetTable, "index_name", Arrays.asList("a", "b", "c"));
        targetIndex2 = new Index(targetTable, "another_index", Arrays.asList("a", "b", "c"));
        targetIndex3 = new Index(targetTable, "index_name", Arrays.asList("e", "f"));
        
        comparisonUtils = Mockito.mock(ComparisonUtils.class);
        visitor.setComparisonUtils(comparisonUtils);
    }
    
    
    @Test
    public void canValidate()
    {
        Mockito.when(comparisonUtils.findEquivalentObjects(refSchema, refIndex)).
            thenReturn(Arrays.asList((DbObject) targetIndex1, targetIndex2, targetIndex3));
        
        // Validate all instances of the target schema's indexes that are equivalent to this index
        visitor.visit(refIndex);
        
        Mockito.verify(validators.get(0)).validate(refIndex, targetIndex1, ctx);
        Mockito.verify(validators.get(0)).validate(refIndex, targetIndex2, ctx);
        Mockito.verify(validators.get(0)).validate(refIndex, targetIndex3, ctx);

        Mockito.verify(validators.get(1)).validate(refIndex, targetIndex1, ctx);
        Mockito.verify(validators.get(1)).validate(refIndex, targetIndex2, ctx);
        Mockito.verify(validators.get(1)).validate(refIndex, targetIndex3, ctx);
    }
    
    
}
