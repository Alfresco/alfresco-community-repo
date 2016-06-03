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
