/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.util.schemacomp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.xml.transform.stream.StreamResult;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import org.alfresco.util.schemacomp.model.Schema;

/**
 * Tool to export a database schema to an XML file.
 * 
 * @author Matt Ward
 */
public class DbToXML
{
    private ApplicationContext context;
    private File outputFile;
    private String namePrefix = "alf_";
    private String dbSchemaName;

    /**
     * Constructor. Uses a default name prefix of 'alf_' during the export.
     */
    public DbToXML(ApplicationContext context, File outputFile)
    {
        this.context = context;
        this.outputFile = outputFile;
    }

    /**
     * Constructor. Allows specification of a name prefix, e.g. "jpbm_", that will be used during the export.
     */
    public DbToXML(ApplicationContext context, File outputFile, String namePrefix)
    {
        this(context, outputFile);
        this.namePrefix = namePrefix;
    }

    /**
     * Set an optional default schema name
     *
     * @param dbSchemaName
     *            String
     */
    public void setDbSchemaName(String dbSchemaName)
    {
        this.dbSchemaName = dbSchemaName;
    }

    public void execute()
    {
        ExportDb exporter = new ExportDb(context);
        exporter.setNamePrefix(namePrefix);
        exporter.setDbSchemaName(dbSchemaName);
        exporter.execute();
        Schema schema = exporter.getSchema();
        // Write to a string buffer and then write the results to a file
        // since we need to write windows line endings - and even passing in a suitable
        // PrintWriter to StreamResult does not seem to result in the correct line endings.
        StringWriter stringWriter = new StringWriter();
        SchemaToXML schemaToXML = new SchemaToXML(schema, new StreamResult(stringWriter));
        schemaToXML.execute();
        writeToFile(stringWriter.getBuffer().toString());
    }

    private void writeToFile(String content)
    {
        PrintWriter pw = printWriter(outputFile, SchemaComparator.CHAR_SET, SchemaComparator.LINE_SEPARATOR);

        String[] lines = content.split(System.getProperty("line.separator"));
        for (String line : lines)
        {
            pw.println(line);
        }

        pw.close();
    }

    private static PrintWriter printWriter(File file, String charSet, String lineSeparator)
    {
        Properties props = System.getProperties();
        synchronized (props)
        {
            String oldLineSep = null;
            try
            {
                oldLineSep = (String) props.setProperty("line.separator", lineSeparator);
                return new PrintWriter(file, charSet);
            }
            catch (FileNotFoundException error)
            {
                throw new RuntimeException("Unable to write to file " + file, error);
            }
            catch (UnsupportedEncodingException error)
            {
                throw new RuntimeException("Unsupported encoding" + charSet, error);
            }
            finally
            {
                if (oldLineSep != null)
                {
                    props.put("line.separator", oldLineSep);
                }
            }
        }
    }

    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.err.println("Usage:");
            System.err.println("java " + DbToXML.class.getName() + " <context.xml> <output.xml>");
            System.exit(1);
        }
        String contextPath = args[0];
        File outputFile = new File(args[1]);

        // Create the Spring context
        FileSystemXmlApplicationContext context = new FileSystemXmlApplicationContext(contextPath);
        DbToXML dbToXML = new DbToXML(context, outputFile);

        dbToXML.execute();

        // Shutdown the Spring context
        context.close();
    }
}
