package org.alfresco.util.schemacomp.validator;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.domain.hibernate.dialect.AlfrescoSQLServerDialect;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.model.Column;
import org.alfresco.util.schemacomp.model.DbObject;
import org.alfresco.util.schemacomp.model.Index;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for the TypeNameOnlyValidator class.
 * 
 * @author sergei.shcherbovich
 */
public class TypeNameOnlyValidatorTest 
{
    private TypeNameOnlyValidator validator;
    private DiffContext ctx;
    private Results validationResults;

    @Before
    public void setUp() throws Exception
    {
        validator = new TypeNameOnlyValidator();
        validationResults = new Results();
        ctx = new DiffContext(new AlfrescoSQLServerDialect(), validationResults, null, null);
    }
    
    @Test
    public void validateOnlyColumnsTest()
    {
        try
        {
            validator.validate(null, new Index(null, null, new ArrayList<String>()), ctx);
            fail("TypeNameOnlyValidator should validate only Column");
        }
        catch(AlfrescoRuntimeException e)
        {
            // should validate only Column
        }
    }
    
    @Test
    public void validateColumnNamesTest()
    {
        // shouldn't fail
        assertValidation(column("nvarchar(1)"), column("nvarchar(2)"), false);
        // shouldn't fail
        assertValidation(column("numeric"), column("numeric"), false);
        // should fail 
        assertValidation(column("nvarchar(1)"), column("varchar(1)"), true);
        // shouldn't fail
        assertValidation(column("numeric() identity"), column("numeric() identity"), false);
    }
    
    private void assertValidation(DbObject reference, DbObject target, boolean shouldFail)
    {
        int shouldFailInt = shouldFail ? 1 : 0;
        int beforeValidationResultsSize = validationResults.size();
        
        validator.validate(reference, target, ctx);
        assertEquals(validationResults.size() - beforeValidationResultsSize, shouldFailInt);
    }
    
    private Column column(String typeName)
    {
        return new Column(null, null, typeName, true);
    }
}
