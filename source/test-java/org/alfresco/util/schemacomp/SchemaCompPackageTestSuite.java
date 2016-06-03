package org.alfresco.util.schemacomp;

import org.alfresco.util.schemacomp.model.ModelTestSuite;
import org.alfresco.util.schemacomp.validator.ValidatorTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite running all the tests in the schemacomp package - and subpackages.
 * @author Matt Ward
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
{
            ModelTestSuite.class,
            ValidatorTestSuite.class,
            SchemaCompTestSuite.class
})
public class SchemaCompPackageTestSuite
{
}
