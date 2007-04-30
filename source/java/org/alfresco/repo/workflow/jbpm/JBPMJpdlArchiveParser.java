/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
