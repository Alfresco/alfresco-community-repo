/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util.xml;

import java.io.File;
import java.net.URL;

import org.springframework.util.ResourceUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.sun.codemodel.JCodeModel;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;

/**
 * Helper to generate code from XSD files.
 * 
 * @author Derek Hulley
 * @since 3.2
 */
public class SchemaHelper
{
    public static void main(String ... args)
    {
        if (args.length < 2 || !args[0].startsWith("--compile-xsd=") && !args[1].startsWith("--output-dir="))
        {
            System.out.println("Usage: SchemaHelper --compile-xsd=<URL> --output-dir=<directory>");
            System.exit(1);
        }
        final String urlStr = args[0].substring(14);
        if (urlStr.length() == 0)
        {
            System.out.println("Usage: SchemaHelper --compile-xsd=<URL> --output-dir=<directory>");
            System.exit(1);
        }
        final String dirStr = args[1].substring(13);
        if (dirStr.length() == 0)
        {
            System.out.println("Usage: SchemaHelper --compile-xsd=<URL> --output-dir=<directory>");
            System.exit(1);
        }
        try
        {
            URL url = ResourceUtils.getURL(urlStr);
            File dir = new File(dirStr);
            if (!dir.exists() || !dir.isDirectory())
            {
                System.out.println("Output directory not found: " + dirStr);
                System.exit(1);
            }
            
            ErrorListener errorListener = new ErrorListener()
            {
                public void warning(SAXParseException e)
                {
                    System.out.println("WARNING: " + e.getMessage());
                }
                public void info(SAXParseException e)
                {
                    System.out.println("INFO: " + e.getMessage());
                }
                public void fatalError(SAXParseException e)
                {
                    handleException(urlStr, e);
                }
                public void error(SAXParseException e)
                {
                    handleException(urlStr, e);
                }
            };

            SchemaCompiler compiler = XJC.createSchemaCompiler();
            compiler.setErrorListener(errorListener);
            compiler.parseSchema(new InputSource(url.toExternalForm()));
            S2JJAXBModel model = compiler.bind();
            if (model == null)
            {
                System.out.println("Failed to produce binding model for URL " + urlStr);
                System.exit(1);
            }
            JCodeModel codeModel = model.generateCode(null, errorListener);
            codeModel.build(dir);
        }
        catch (Throwable e)
        {
            handleException(urlStr, e);
            System.exit(1);
        }
    }
    private static void handleException(String urlStr, Throwable e)
    {
        System.out.println("Error processing XSD " + urlStr);
        e.printStackTrace();
    }
}
