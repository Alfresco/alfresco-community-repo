package org.alfresco.util.schemacomp;


import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.alfresco.repo.domain.schema.SchemaBootstrap;
import org.alfresco.test_category.OwnJVMTestsCategory;
import org.alfresco.util.ApplicationContextHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test intended for use in the continuous integration system that checks whether the
 * schema reference file (for whichever database the tests are being run against)
 * is in sync with the actual schema. If the test fails (and the schema comparator is
 * in working order) then the most likely cause is that a new up-to-date schema reference file
 * needs to be created.
 * <p>
 * Schema reference files are created using the {@link DbToXML} tool.
 * <p>
 * Note: if no reference file exists then the test will pass, this is to allow piece meal
 * introduction of schmea reference files.
 * 
 * @see DbToXML
 * @author Matt Ward
 */
@Category(OwnJVMTestsCategory.class)
public class SchemaReferenceFileTest
{
    private ClassPathXmlApplicationContext ctx;
    private SchemaBootstrap schemaBootstrap;
    
    @Before
    public void setUp() throws Exception
    {
        ctx = (ClassPathXmlApplicationContext) ApplicationContextHelper.getApplicationContext();    
        schemaBootstrap = (SchemaBootstrap) ctx.getBean("schemaBootstrap");
    }
    
    @After
    public void tearDown()
    {
        if(ctx != null)
        {
            ctx.close();
        }
    }

    @Test
    public void checkReferenceFile()
    {
        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(buff);
        int numProblems = schemaBootstrap.validateSchema(null, out);
        out.flush();
        
        if (numProblems > 0)
        {
            fail(buff.toString());
        }
    }
}
