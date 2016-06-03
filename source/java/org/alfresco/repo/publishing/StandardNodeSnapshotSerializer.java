
package org.alfresco.repo.publishing;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestWriter;
import org.alfresco.service.cmr.publishing.NodeSnapshot;

/**
 * @author Brian
 * @author Nick Smith
 * @since 4.0
 */
public class StandardNodeSnapshotSerializer implements NodeSnapshotSerializer
{
    /**
    * {@inheritDoc}
    */
    @Override
    public List<NodeSnapshot> deserialize(InputStream input) throws Exception
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        NodeSnapshotDeserializer processor = new NodeSnapshotDeserializer();

        XMLTransferManifestReader xmlReader = new XMLTransferManifestReader(processor);
        parser.parse(input, xmlReader);
        return processor.getSnapshots();
    }

    /**
     * {@inheritDoc}
     */
     public void serialize(Collection<NodeSnapshot> snapshots, OutputStream output) throws Exception
     {
        try
        {   
            TransferManifestHeader header = new TransferManifestHeader();
            header.setCreatedDate(new Date());
            header.setNodeCount(snapshots.size());
            header.setReadOnly(false);
            header.setSync(false);
            
            Writer writer = new OutputStreamWriter(output, "UTF-8");
            
            XMLTransferManifestWriter transferManifestWriter = new XMLTransferManifestWriter();
            transferManifestWriter.startTransferManifest(writer);
            transferManifestWriter.writeTransferManifestHeader(header);

            // Iterate over NodesToPublish and Serialize.
            for (NodeSnapshot snapshot: snapshots)
            {
                if (snapshot instanceof NodeSnapshotTransferImpl)
                {
                    NodeSnapshotTransferImpl snapshotImpl = (NodeSnapshotTransferImpl)snapshot;
                    transferManifestWriter.writeTransferManifestNode(snapshotImpl.getTransferNode());
                }
            }
            transferManifestWriter.endTransferManifest();
            writer.flush();
        }
        catch (Exception e)
        {
            throw new AlfrescoRuntimeException("Failed to serialize node snapshots.", e);
        }
    }

    /**
     * @author Brian
     * @author Nick Smith
     *
     */
    public static class NodeSnapshotDeserializer implements TransferManifestProcessor
    {
        private List<NodeSnapshot> snapshots = new ArrayList<NodeSnapshot>();
        

        /**
         * @return the snapshots
         */
        public List<NodeSnapshot> getSnapshots()
        {
            return snapshots;
        }
        
        /**
        * {@inheritDoc}
         */
        public void endTransferManifest()
        {
            //NOOP
        }

        /**
         * {@inheritDoc}
          */
        public void processTransferManifestNode(TransferManifestNormalNode node)
        {
            NodeSnapshotTransferImpl snapshot = new NodeSnapshotTransferImpl(node);
            snapshots.add(snapshot);
        }

        /**
         * {@inheritDoc}
          */
        public void processTransferManifestNode(TransferManifestDeletedNode node)
        {
            //NOOP
        }

        /**
         * {@inheritDoc}
          */
        public void processTransferManifiestHeader(TransferManifestHeader header)
        {
            //NOOP
        }

        /**
         * {@inheritDoc}
          */
        public void startTransferManifest()
        {
            //NOOP
        }
    }
}
