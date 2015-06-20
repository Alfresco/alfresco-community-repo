package org.alfresco.repo.transfer;

import java.io.Serializable;
import java.io.Writer;
import java.nio.channels.Channels;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.transfer.reportd.XMLTransferDestinationReportWriter;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferProgress.Status;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TransferSummaryReportImpl implements TransferSummaryReport
{
    public static final String _SIMPLE_REPORT = "_simple_report";
    private static final String _SIMPLE_REPORT_XML = _SIMPLE_REPORT + ".xml";
    private static final String MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND = "receiver.record_folder_not_found_for_summary_report";

    private static final Log log = LogFactory.getLog(TransferSummaryReportImpl.class);

    private NodeService nodeService;
    private ContentService contentService;
    private SearchService searchService;
    private FileFolderService fileFolderService;
    private NodeRef reportFile;
    private TransferDestinationReportWriter destinationWriter;

    // where the summary report will be stored
    private String transferSummaryReportLocation;

    private String transferId;

    public TransferSummaryReportImpl(String transferId)
    {
        this.transferId = transferId;
    }

    @Override
    public void logSummaryCreated(NodeRef sourceNode, NodeRef destNode, NodeRef newParent, String newPath, boolean orphan)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeCreated(sourceNode, destNode, newParent, newPath);
    }

    @Override
    public void logSummaryUpdated(NodeRef sourceNode, NodeRef destNode, String path)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeUpdated(sourceNode, destNode, path);
    }

    @Override
    public void logSummaryDeleted(NodeRef sourceNode, NodeRef destNode, String oldPath)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeDeleted(sourceNode, destNode, oldPath);
    }

    @Override
    public void logSummaryMoved(NodeRef sourceNodeRef, NodeRef destNodeRef, String oldPath, NodeRef newParent, String newPath)
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeMoved(sourceNodeRef, destNodeRef, oldPath, newParent, newPath);
    }

    @Override
    public void logSummaryComment(Object obj) throws TransferException
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeComment(obj.toString());
    }

    @Override
    public void logSummaryException(final Object obj, final Throwable ex) throws TransferException
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeComment(obj.toString());
        if (ex != null)
        {

            writer.writeException(ex);
        }
    }

    @Override
    public void logSummaryUpdateStatus(final Status status) throws TransferException
    {
        TransferDestinationReportWriter writer = getLogWriter(transferId);
        writer.writeChangeState(status.toString());
        writer.endTransferReport();
    }

    @Override
    public void finishSummaryReport()
    {
        // try not to throw any exceptions
        try
        {
            TransferDestinationReportWriter writer = getLogWriter(transferId);
            writer.writeChangeState("finished");

            log.debug("closing destination transfer summary report");
            writer.endTransferReport();
        }
        catch (Exception e)
        {
            log.warn(e.getMessage(), e);
        }

    }

    private TransferDestinationReportWriter getLogWriter(String transferId)
    {
        if (destinationWriter == null)
        {
            destinationWriter = new XMLTransferDestinationReportWriter();
            Writer createUnderlyingLogWriter = createUnderlyingLogWriter(transferId);
            destinationWriter.startTransferReport("UTF-8", createUnderlyingLogWriter);
        }
        return destinationWriter;
    }

    protected Writer createUnderlyingLogWriter(String transferId)
    {
        if (reportFile == null)
        {
            reportFile = createTransferRecord(transferId);
        }
        ContentWriter contentWriter = contentService.getWriter(reportFile, ContentModel.PROP_CONTENT, true);
        contentWriter.setMimetype(MimetypeMap.MIMETYPE_XML);
        contentWriter.setEncoding("UTF-8");
        return Channels.newWriter(contentWriter.getWritableChannel(), "UTF-8");
    }

    private NodeRef createTransferRecord(String transferId)
    {
        log.debug("TransferSummaryReport createTransferRecord");

        NodeRef reportParentFolder = getParentFolder();

        String name = getReportFileName(reportParentFolder);

        QName recordName = QName.createQName(NamespaceService.APP_MODEL_1_0_URI, name);

        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        props.put(ContentModel.PROP_NAME, name);
        props.put(TransferModel.PROP_PROGRESS_POSITION, 0);
        props.put(TransferModel.PROP_PROGRESS_ENDPOINT, 1);
        props.put(TransferModel.PROP_TRANSFER_STATUS, TransferProgress.Status.PRE_COMMIT.toString());

        log.debug("Creating transfer summary report with name: " + name);
        ChildAssociationRef assoc = nodeService.createNode(reportParentFolder, ContentModel.ASSOC_CONTAINS, recordName,
                TransferModel.TYPE_TRANSFER_RECORD, props);
        log.debug("<-createTransferSummartReportRecord: " + assoc.getChildRef());

        return assoc.getChildRef();
    }

    private String getReportBaseName(NodeRef reportParentFolder)
    {
        String prefixName = null;
        List<FileInfo> list = fileFolderService.list(reportParentFolder);
        for (FileInfo file : list)
        {
            if (file.getNodeRef().toString().contains(transferId))
            {
                // found the existing "destination" remote file
                Serializable name = nodeService.getProperty(file.getNodeRef(), ContentModel.PROP_NAME);
                if (name instanceof String)
                {
                    String fileName = (String) name;
                    if (fileName.lastIndexOf(".") > 0)
                    {
                        prefixName = fileName.substring(0, fileName.lastIndexOf("."));
                    }
                }
                break;
            }
        }
        return prefixName;
    }

    private String getReportFileName(NodeRef reportParentFolder)
    {
        String prefixName = getReportBaseName(reportParentFolder);

        if (prefixName == null || prefixName.isEmpty())
        {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSSZ");
            String timeNow = format.format(new Date());
            prefixName = timeNow;
        }
        String name = prefixName + _SIMPLE_REPORT_XML;
        return name;
    }

    private NodeRef getParentFolder()
    {
        NodeRef reportParentFolder = null;
        log.debug("Trying to find transfer summary report records folder: " + transferSummaryReportLocation);
        ResultSet rs = null;

        try
        {
            rs = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_XPATH, transferSummaryReportLocation);
            if (rs.length() > 0)
            {
                reportParentFolder = rs.getNodeRef(0);

                log.debug("Found transfer summary report records folder: " + reportParentFolder);
            }
            else
            {
                throw new TransferException(MSG_INBOUND_TRANSFER_FOLDER_NOT_FOUND, new Object[] { transferSummaryReportLocation });
            }
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
        }
        return reportParentFolder;
    }

    public void setTransferSummaryReportLocation(String transferSummaryReportLocation)
    {
        this.transferSummaryReportLocation = transferSummaryReportLocation;
    }

    public void setFileFolderService(FileFolderService fileFolderService)
    {
        this.fileFolderService = fileFolderService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

}
