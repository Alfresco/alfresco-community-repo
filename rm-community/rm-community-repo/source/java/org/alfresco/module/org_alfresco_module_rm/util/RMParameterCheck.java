package org.alfresco.module.org_alfresco_module_rm.util;

import org.apache.commons.lang.StringUtils;

/**
 * Utility class that contains validation not present in {@link org.alfresco.util.ParameterCheck}.
 * 
 * @author tpage
 */
public class RMParameterCheck
{
    /**
     * Checks that the string parameter with the given name is not blank i.e. it is not null, zero length or entirely
     * composed of whitespace.
     * 
     * @param strParamName Name of parameter to check
     * @param strParamValue Value of the parameter to check
     */
    public static void checkNotBlank(final String strParamName, final String strParamValue)
                throws IllegalArgumentException
    {
        if (StringUtils.isBlank(strParamValue)) { throw new IllegalArgumentException(strParamName
                    + " is a mandatory parameter"); }
    }
}
