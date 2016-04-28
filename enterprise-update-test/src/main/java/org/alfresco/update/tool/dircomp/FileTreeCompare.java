/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.nio.file.Path;
import java.util.List;


/**
 * File tree comparison tool interface.
 * 
 * @author Matt Ward
 */
public interface FileTreeCompare
{
    ResultSet compare(Path p1, Path p2);
}
