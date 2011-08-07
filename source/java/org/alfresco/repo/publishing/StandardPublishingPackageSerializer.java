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

package org.alfresco.repo.publishing;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestWriter;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.xml.sax.SAXException;

/**
 * @author Brian
 * @author Nick Smith
 */
public class StandardPublishingPackageSerializer implements PublishingPackageSerializer
{
    /**
     * {@inheritDoc}
     * @return 
      */
    public Map<NodeRef, PublishingPackageEntry> deserialize(InputStream input) throws Exception
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        PublishingPackageDeserializer processor = new PublishingPackageDeserializer();

        XMLTransferManifestReader xmlReader = new XMLTransferManifestReader(processor);
        parser.parse(input, xmlReader);
        return processor.getEntries();
    }

    /**
     * {@inheritDoc}
      */
    public void serialize(PublishingPackage publishingPackage, OutputStream output) throws Exception
    {
        try
        {
            Set<NodeRef> nodesToPublish = publishingPackage.getNodesToPublish();
            TransferManifestHeader header = new TransferManifestHeader();
            header.setCreatedDate(new Date());
            header.setNodeCount(nodesToPublish.size());
            header.setReadOnly(false);
            header.setSync(false);
            
            Writer writer = new OutputStreamWriter(output, "UTF-8");
            
            XMLTransferManifestWriter transferManifestWriter = new XMLTransferManifestWriter();
            transferManifestWriter.startTransferManifest(writer);
            transferManifestWriter.writeTransferManifestHeader(header);

            // Iterate over NodesToPublish and Serialize.
            Map<NodeRef, PublishingPackageEntry> entryMap = publishingPackage.getEntryMap();
            for (NodeRef publishNode: nodesToPublish)
            {
                PublishingPackageEntry entry = entryMap.get(publishNode);
                if (entry instanceof PublishingPackageEntryImpl)
                {
                    PublishingPackageEntryImpl entryImpl = (PublishingPackageEntryImpl)entry;
                    transferManifestWriter.writeTransferManifestNode(entryImpl.getPayload());
                }
            }
            transferManifestWriter.endTransferManifest();
            writer.flush();
        }
        catch (SAXException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            //UTF-8 must be supported, so this is not going to happen
        }
    }

    /**
     * @author Brian
     * @author Nick Smith
     *
     */
    public static class PublishingPackageDeserializer implements TransferManifestProcessor
    {
        Map<NodeRef,PublishingPackageEntry> entries = new HashMap<NodeRef,PublishingPackageEntry>();
        
        /**
         * @return the entries
         */
        public Map<NodeRef,PublishingPackageEntry> getEntries()
        {
            return entries;
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
            Map<QName, Serializable> props = node.getProperties();
            String version = (String) props.get(ContentModel.PROP_VERSION_LABEL);
            entries.put(node.getNodeRef(), new PublishingPackageEntryImpl(true, node.getNodeRef(), node, version));
        }

        /**
         * {@inheritDoc}
          */
        public void processTransferManifestNode(TransferManifestDeletedNode node)
        {
            entries.put(node.getNodeRef(), new PublishingPackageEntryImpl(false, node.getNodeRef(), null, null));
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
