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


import java.io.File;

import org.alfresco.util.ApplicationContextHelper;
import org.alfresco.util.TempFileProvider;
import org.junit.Test;
import org.springframework.context.ApplicationContext;

/**
 * Tests for the DbToXML class.
 * 
 * @author Matt Ward
 */
public class DbToXMLTest
{
    @Test
    public void execute()
    {
        ApplicationContext ctx = ApplicationContextHelper.getApplicationContext();
        File outFile = new File(TempFileProvider.getTempDir(), getClass().getSimpleName() + ".xml");
        System.out.println("Writing to temp file: " + outFile);
        DbToXML dbToXML = new DbToXML(ctx, outFile);
        dbToXML.execute();
    }
}
