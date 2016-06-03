
package org.alfresco.opencmis.tck.tests.query;

import org.apache.chemistry.opencmis.tck.tests.query.QueryInFolderTest;

/**
 * Fix for MNT-14432 - skip deletion of test data
 * 
 * @author Andreea Dragoi
 * @since 4.2.5
 */

public class QueryInFolderTestCustom extends QueryInFolderTest
{
    protected void deleteTestFolder()
    {
        // do nothing - skip deletion of test folder
    }
}
