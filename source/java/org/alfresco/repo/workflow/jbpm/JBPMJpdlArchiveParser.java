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
package org.alfresco.repo.workflow.jbpm;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.jbpm.graph.def.ProcessDefinition;
import org.jbpm.jpdl.JpdlException;
import org.jbpm.jpdl.par.ProcessArchive;
import org.jbpm.jpdl.par.ProcessArchiveParser;
import org.jbpm.jpdl.xml.JpdlXmlReader;
import org.xml.sax.InputSource;


/**
 * Alfresco specific process archive parser to allow for extensions
 * to jPDL.
 * 
 * @author davidc
 */
public class JBPMJpdlArchiveParser implements ProcessArchiveParser
{

    private static final long serialVersionUID = 1L;

    /* (non-Javadoc)
     * @see org.jbpm.jpdl.par.ProcessArchiveParser#readFromArchive(org.jbpm.jpdl.par.ProcessArchive, org.jbpm.graph.def.ProcessDefinition)
     */
    public ProcessDefinition readFromArchive(ProcessArchive processArchive, ProcessDefinition processDefinition)
        throws JpdlException
    {
        // NOTE: This method implementation is a copy from the JpdlXmlReader class
        //       with the difference of constructing an AlfrescoCreateTimerAction.
        //       It may need to be updated whenever a jbpm library upgrade is performed.

        try
        {
            byte[] processBytes = processArchive.getEntry("processdefinition.xml");
            if (processBytes == null)
            {
                throw new JpdlException("no processdefinition.xml inside process archive");
            }

            // creating the JpdlXmlReader
            InputStream processInputStream = new ByteArrayInputStream(processBytes);
            InputSource processInputSource = new InputSource(processInputStream);
            JpdlXmlReader jpdlXmlReader = new JBPMJpdlXmlReader(processInputSource, processArchive);
            processDefinition = jpdlXmlReader.readProcessDefinition();

            // close all the streams
            jpdlXmlReader.close();
            processInputStream.close();
        }
        catch (IOException e)
        {
            throw new JpdlException("io problem while reading processdefinition.xml: " + e.getMessage(), e);
        }

        return processDefinition;
    }
}
