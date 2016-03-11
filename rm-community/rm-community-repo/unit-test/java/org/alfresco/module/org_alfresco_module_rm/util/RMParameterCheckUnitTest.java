package org.alfresco.module.org_alfresco_module_rm.util;

import org.alfresco.module.org_alfresco_module_rm.test.util.ExceptionUtils;
import org.junit.Test;

/**
 * Unit tests for the {@link RMParameter} utility class.
 * 
 * @author tpage
 */
public class RMParameterCheckUnitTest
{
    @Test
    public void checkNotBlank()
    {
        // Check that supplying null causes an exception.
        ExceptionUtils.expectedException(IllegalArgumentException.class, () -> {
            RMParameterCheck.checkNotBlank("name", null);
            return null;
        });

        // Check that supplying an empty string causes an exception.
        ExceptionUtils.expectedException(IllegalArgumentException.class, () -> {
            RMParameterCheck.checkNotBlank("name", "");
            return null;
        });

        // Check that supplying a whitespace only string causes an exception.
        ExceptionUtils.expectedException(IllegalArgumentException.class, () -> {
            RMParameterCheck.checkNotBlank("name", "\n\r \t");
            return null;
        });

        // Check that supplying a mainly whitespace string throws no exceptions.
        RMParameterCheck.checkNotBlank("name", "\n\r *\t");
    }
}
