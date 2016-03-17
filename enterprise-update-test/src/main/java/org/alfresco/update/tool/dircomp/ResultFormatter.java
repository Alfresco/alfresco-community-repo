/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.io.OutputStream;
import java.util.Collection;

/**
 * Format a set of {@link Result} objects.
 * 
 * @author Matt Ward
 */
public interface ResultFormatter
{
    /**
     * Format the result set to the supplied {@link OutputStream}. The caller
     * must take care of creating and destroying the OutputStream correctly.
     * 
     * @param results   The results to format.
     * @param out       The stream to format the results to.
     */
    void format(ResultSet results, OutputStream out);
}
