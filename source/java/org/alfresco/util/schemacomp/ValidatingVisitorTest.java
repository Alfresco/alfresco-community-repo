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
import java.util.Collections;

import javax.faces.validator.Validator;

import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.ForeignKey;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.PrimaryKey;
import org.alfresco.util.schemacomp.model.Schema;
import org.alfresco.util.schemacomp.model.Sequence;
import org.alfresco.util.schemacomp.model.Table;
import org.alfresco.util.schemacomp.validator.DbValidator;
import org.alfresco.util.schemacomp.validator.NameValidator;
import org.alfresco.util.schemacomp.validator.NullValidator;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.*;
import static org.mockito.Mockito.validateMockitoUsage;

/**
 * TODO: comment me!
 * @author Matt Ward
 */
public class ValidatingVisitorTest
{
    private DiffContext ctx;
    private ValidatingVisitor visitor;
    
    @Before
    public void setUp() throws Exception
    {
        ctx = new DiffContext(new MySQL5InnoDBDialect(), new Differences(), new ArrayList<ValidationResult>());
        visitor = new ValidatingVisitor(ctx);
    }

    @Test
    public void canGetCorrectValidator()
    {
       // Get references to the validator instances to test for
       DbValidator nullValidator = visitor.nullValidator;
       DbValidator nameValidator = visitor.indexNameValidator;
       
       assertSame(nullValidator, visitor.getValidatorFor(Column.class)); 
       assertSame(nullValidator, visitor.getValidatorFor(ForeignKey.class));
       assertSame(nameValidator, visitor.getValidatorFor(Index.class)); 
       assertSame(nullValidator, visitor.getValidatorFor(PrimaryKey.class)); 
       assertSame(nullValidator, visitor.getValidatorFor(Schema.class)); 
       assertSame(nullValidator, visitor.getValidatorFor(Sequence.class)); 
       assertSame(nullValidator, visitor.getValidatorFor(Table.class)); 
    }
    
    @Test
    public void canValidate()
    {
        visitor.indexNameValidator = Mockito.mock(NameValidator.class);
        Index index = new Index("index_name", new ArrayList<String>());
        
        visitor.visit(index);
        
        Mockito.verify(visitor.indexNameValidator).validate(index, ctx);
    }
}
