package org.alfresco.util.schemacomp.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.schemacomp.DiffContext;
import org.alfresco.util.schemacomp.Results;
import org.alfresco.util.schemacomp.ValidationResult;
import org.alfresco.util.schemacomp.model.Index;
import org.alfresco.util.schemacomp.model.Table;
import org.hibernate.dialect.Oracle10gDialect;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the IndexColumnsValidator class.
 * 
 * @author pavel.yurkevich
 */
public class IndexColumnsValidatorTest
{
    private IndexColumnsValidator validator;
    private DiffContext ctx;
    private Results validationResults;

    @Before
    public void setUp() throws Exception
    {
        validator = new IndexColumnsValidator();
        validationResults = new Results();
        ctx = new DiffContext(new Oracle10gDialect(), validationResults, null, null);
    }

    @Test
    public void testNonIndex()
    {
        validator.setPattern(Pattern.compile("SYS_NC.+"));
        assertEquals("SYS_NC.+", validator.getPattern().toString());
        
        try
        {
            validator.validate(null, new Table("SYS_NC234234"), ctx);
            fail("Validator should faile for non-index object");
        }
        catch (AlfrescoRuntimeException e)
        {
            // expected to fail for table object
        }
    }

    @Test
    public void testAllOk()
    {
        validator.setPattern(Pattern.compile("SYS_NC.+"));
        assertEquals("SYS_NC.+", validator.getPattern().toString());
        
        validator.validate(indexForColumns("SYS_NC234234", "SYS_NC654123"), indexForColumns("SYS_NC123123", "SYS_NC225588"), ctx);
        assertEquals(0, validationResults.size());
    }

    @Test
    public void testNotValid()
    {
        validator.setPattern(Pattern.compile("SYS_NC.+"));
        assertEquals("SYS_NC.+", validator.getPattern().toString());
        
        Index target = indexForColumns("TEST_INDEX1", "SYS_NC892345", "MY_INDEX");

        validator.validate(indexForColumns("SYS_NC234234", "SYS_NC654123"), target, ctx);
        assertEquals(3, validationResults.size());
        
        assertEquals("TEST_INDEX1", ((ValidationResult) validationResults.get(0)).getValue());
        assertEquals("MY_INDEX", ((ValidationResult) validationResults.get(1)).getValue());
        assertEquals(target.getColumnNames(), ((ValidationResult) validationResults.get(2)).getValue());
    }

    private Index indexForColumns(String ... names)
    {
        return new Index(null, null, Arrays.asList(names));
    }
}
