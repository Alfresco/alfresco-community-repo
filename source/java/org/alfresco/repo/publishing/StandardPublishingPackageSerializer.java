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
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode;
import org.alfresco.repo.transfer.manifest.TransferManifestHeader;
import org.alfresco.repo.transfer.manifest.TransferManifestNormalNode;
import org.alfresco.repo.transfer.manifest.TransferManifestProcessor;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestReader;
import org.alfresco.repo.transfer.manifest.XMLTransferManifestWriter;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.publishing.PublishingPackageEntry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.xml.sax.SAXException;

/**
 * @author Brian
 * 
 */
public class StandardPublishingPackageSerializer implements PublishingPackageSerializer
{
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.publishing.PublishingPackageSerializer#deserialize(
     * java.io.Reader)
     */
    @Override
    public PublishingPackage deserialize(InputStream input) throws Exception
    {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        PublishingPackageDeserializer processor = new PublishingPackageDeserializer();

        XMLTransferManifestReader xmlReader = new XMLTransferManifestReader(processor);
        parser.parse(input, xmlReader);
        PublishingPackageImpl publishingPackage = new PublishingPackageImpl(processor.getEntries());
        return publishingPackage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.repo.publishing.PublishingPackageSerializer#serialize(org
     * .alfresco.service.cmr.publishing.PublishingPackage, java.io.Writer)
     */
    @Override
    public void serialize(PublishingPackage publishingPackage, OutputStream output) throws Exception
    {
        try
        {
            Collection<PublishingPackageEntry> entries = publishingPackage.getEntries();

            TransferManifestHeader header = new TransferManifestHeader();
            header.setCreatedDate(new Date());
            header.setNodeCount(entries.size());
            header.setReadOnly(false);
            header.setSync(false);
            
            Writer writer = new OutputStreamWriter(output, "UTF-8");
            
            XMLTransferManifestWriter transferManifestWriter = new XMLTransferManifestWriter();
            transferManifestWriter.startTransferManifest(writer);
            transferManifestWriter.writeTransferManifestHeader(header);
            for (PublishingPackageEntry entry : entries)
            {
                if (PublishingPackageEntryImpl.class.isAssignableFrom(entry.getClass()))
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

        /* (non-Javadoc)
         * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#endTransferManifest()
         */
        @Override
        public void endTransferManifest()
        {
            //NOOP
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#processTransferManifestNode(org.alfresco.repo.transfer.manifest.TransferManifestNormalNode)
         */
        @Override
        public void processTransferManifestNode(TransferManifestNormalNode node)
        {
            entries.put(node.getNodeRef(), new PublishingPackageEntryImpl(true, node.getNodeRef(), node, null));
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#processTransferManifestNode(org.alfresco.repo.transfer.manifest.TransferManifestDeletedNode)
         */
        @Override
        public void processTransferManifestNode(TransferManifestDeletedNode node)
        {
            entries.put(node.getNodeRef(), new PublishingPackageEntryImpl(false, node.getNodeRef(), null, null));
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#processTransferManifiestHeader(org.alfresco.repo.transfer.manifest.TransferManifestHeader)
         */
        @Override
        public void processTransferManifiestHeader(TransferManifestHeader header)
        {
            //NOOP
        }

        /* (non-Javadoc)
         * @see org.alfresco.repo.transfer.manifest.TransferManifestProcessor#startTransferManifest()
         */
        @Override
        public void startTransferManifest()
        {
            //NOOP
        }
    }
}
