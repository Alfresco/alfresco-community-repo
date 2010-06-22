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
package org.alfresco.repo.admin.patch.impl;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.Savepoint;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.admin.patch.PatchExecuter;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.control.ControlDAO;
import org.alfresco.repo.domain.node.ChildAssocEntity;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Fixes <a href=https://issues.alfresco.com/jira/browse/ETWOTWO-1133>ETWOTWO-1133</a>.
 * Checks all CRC values for <b>alf_child_assoc.child_node_name_crc and alf_child_assoc.qname_crc</b>.
 * 
 * @author Derek Hulley
 * @since V2.2SP4
 */
public class FixNameCrcValuesPatch extends AbstractPatch
{
    private static final String MSG_SUCCESS = "patch.fixNameCrcValues.result";
    private static final String MSG_REWRITTEN = "patch.fixNameCrcValues.fixed";
    private static final String MSG_UNABLE_TO_CHANGE = "patch.fixNameCrcValues.unableToChange";
    
    private PatchDAO patchDAO;
    private QNameDAO qnameDAO;
    private ControlDAO controlDAO;
    private DictionaryService dictionaryService;
    
    private static Log logger = LogFactory.getLog(FixNameCrcValuesPatch.class);
    private static Log progress_logger = LogFactory.getLog(PatchExecuter.class);
    
    public FixNameCrcValuesPatch()
    {
    }
    
    public void setPatchDAO(PatchDAO patchDAO)
    {
        this.patchDAO = patchDAO;
    }

    /**
     * @param qnameDAO          resolved QNames
     */
    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    /**
     * @param controlDAO        used to create Savepoints
     */
    public void setControlDAO(ControlDAO controlDAO)
    {
        this.controlDAO = controlDAO;
    }
    
    /**
     * @param dictionaryService used to check the child associations for unique checking
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    protected void checkProperties()
    {
        super.checkProperties();
        checkPropertyNotNull(patchDAO, "patchDAO");
        checkPropertyNotNull(qnameDAO, "qnameDAO");
        checkPropertyNotNull(applicationEventPublisher, "applicationEventPublisher");
    }

    @Override
    protected String applyInternal() throws Exception
    {
        // initialise the helper
        FixNameCrcValuesHelper helper = new FixNameCrcValuesHelper();
        try
        {
            String msg = helper.fixCrcValues();
            // done
            return msg;
        }
        finally
        {
            helper.closeWriter();
        }
    }
    
    private class FixNameCrcValuesHelper
    {
        private File logFile;
        private FileChannel channel;
        private Integer assocCount;
        private Long minAssocId = 0L;
        
        private FixNameCrcValuesHelper() throws IOException
        {
            // put the log file into a long life temp directory
            File tempDir = TempFileProvider.getLongLifeTempDir("patches");
            logFile = new File(tempDir, "FixNameCrcValuesPatch.log");
            
            // open the file for appending
            RandomAccessFile outputFile = new RandomAccessFile(logFile, "rw");
            channel = outputFile.getChannel();
            // move to the end of the file
            channel.position(channel.size());
            // add a newline and it's ready
            writeLine("").writeLine("");
            writeLine("FixNameCrcValuesPatch executing on " + new Date());
        }
        
        private FixNameCrcValuesHelper write(Object obj) throws IOException
        {
            channel.write(ByteBuffer.wrap(obj.toString().getBytes("UTF-8")));
            return this;
        }
        private FixNameCrcValuesHelper writeLine(Object obj) throws IOException
        {
            write(obj);
            write("\n");
            return this;
        }
        private void closeWriter()
        {
            try { channel.close(); } catch (Throwable e) {}
        }

        public String fixCrcValues() throws Exception
        {
            BatchProcessWorkProvider<Map<String, Object>> workProvider = new BatchProcessWorkProvider<Map<String,Object>>()
            {
                public synchronized int getTotalEstimatedWorkSize()
                {
                    if (assocCount == null)
                    {
                        assocCount = patchDAO.getChildAssocCount();
                    }
                    return assocCount.intValue();
                }
                
                public Collection<Map<String, Object>> getNextWork()
                {
                    // Get the next collection
                    List<Map<String, Object>> results = patchDAO.getChildAssocsForCrcFix(minAssocId, 1000);
                    // Find out what the last ID is
                    int resultsSize = results.size();
                    if (resultsSize > 0)
                    {
                        Map<String, Object> lastResult = results.get(resultsSize - 1);
                        Long id = (Long) lastResult.get("id");
                        minAssocId = id + 1L;
                    }
                    // Hand back the results
                    return results;
                }
            };
            
            // get the association types to check
            BatchProcessor<Map<String, Object>> batchProcessor = new BatchProcessor<Map<String, Object>>(
                    "FixNameCrcValuesPatch",
                    transactionService.getRetryingTransactionHelper(),
                    workProvider,
                    2, 20,
                    applicationEventPublisher,
                    progress_logger, 1000);

            BatchProcessWorker<Map<String, Object>> worker = new BatchProcessWorker<Map<String, Object>>()
            {
                public String getIdentifier(Map<String, Object> entry)
                {
                    return entry.toString();
                }
                
                public void beforeProcess() throws Throwable
                {
                }

                public void process(Map<String, Object> row) throws Throwable
                {
                    Long assocId = (Long) row.get("id");
                    Long typeQNameId = (Long) row.get("typeQNameId");
                    Long qnameNamespaceId = (Long) row.get("qnameNamespaceId");
                    String qnameLocalName = (String) row.get("qnameLocalName");
                    Long childNodeNameCrc = (Long) row.get("childNodeNameCrc");
                    Long qnameCrc = (Long) row.get("qnameCrc");
                    String childNodeUuid = (String) row.get("childNodeUuid");
                    String childNodeName = (String) row.get("childNodeName");
                    // Use the UUID if there is no cm:name
                    childNodeName = (childNodeName ==  null) ? childNodeUuid : childNodeName;
                    // Resolve QNames
                    QName typeQName = qnameDAO.getQName(typeQNameId).getSecond();
                    String namespace = qnameDAO.getNamespace(qnameNamespaceId).getSecond();
                    QName qname = QName.createQName(namespace, qnameLocalName);

                    ChildAssocEntity entity = new ChildAssocEntity();
                    entity.setChildNodeNameAll(dictionaryService, typeQName, childNodeName);
                    entity.setQNameAll(qnameDAO, qname, false);
                    // Check the CRC values for cm:name
                    if (entity.getChildNodeNameCrc().equals(childNodeNameCrc))
                    {
                        // Check the CRC for the QName
                        if (entity.getQnameCrc().equals(qnameCrc))
                        {
                            // This child assoc is good
                            return;
                        }
                    }
                    
                    Savepoint savepoint = null;
                    try
                    {
                        AssociationDefinition assocDef = dictionaryService.getAssociation(typeQName);
                        if (assocDef == null)
                        {
                            throw new DictionaryException("Association type not defined: " + typeQName);
                        }
                        
                        // Being here indicates that the association needs to be updated
                        savepoint = controlDAO.createSavepoint("FixNameCrcValuesPatch");
                        patchDAO.updateChildAssocCrc(assocId, childNodeNameCrc, qnameCrc);
                        controlDAO.releaseSavepoint(savepoint);
                        
                        String msg = I18NUtil.getMessage(
                                    MSG_REWRITTEN,
                                    assocId,
                                    childNodeName, childNodeNameCrc, entity.getChildNodeNameCrc(),
                                    qname, qnameCrc, entity.getQnameCrc());
                        writeLine(msg);
                    }
                    catch (Throwable e)
                    {
                        if (savepoint != null)
                        {
                            controlDAO.rollbackToSavepoint(savepoint);
                        }
                        String msg = I18NUtil.getMessage(
                                MSG_UNABLE_TO_CHANGE,
                                assocId,
                                childNodeName, childNodeNameCrc, entity.getChildNodeNameCrc(),
                                qname, qnameCrc, entity.getQnameCrc(),
                                e.getMessage());
                        // We just log this and add details to the message file
                        if (logger.isDebugEnabled())
                        {
                            logger.debug(msg, e);
                        }
                        else
                        {
                            logger.warn(msg);
                        }
                        writeLine(msg);
                    }
                }
                
                public void afterProcess() throws Throwable
                {
                }
            };
            
            int updated = batchProcessor.process(worker, true);
            
            String msg = I18NUtil.getMessage(MSG_SUCCESS, updated, logFile);
            return msg;
        }
    }
}
