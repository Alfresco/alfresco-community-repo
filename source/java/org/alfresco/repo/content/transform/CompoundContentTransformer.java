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
package org.alfresco.repo.content.transform;

import java.io.File;
import java.util.LinkedList;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.content.filestore.FileContentWriter;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A chain of transformations that is able to produce non-zero reliability
 * transformation from one mimetype to another.
 * <p>
 * The reliability of the chain is the product of all the individual
 * transformations.
 * 
 * @author Derek Hulley
 */
public class CompoundContentTransformer implements ContentTransformer
{
    private static final Log logger = LogFactory.getLog(CompoundContentTransformer.class);
    
    /** a sequence of transformers to apply */
    private LinkedList<Transformation> chain;
    /** the combined reliability of all the transformations in the chain */
    private double reliability;
    
    public CompoundContentTransformer()
    {
        chain = new LinkedList<Transformation>();
        reliability = 1.0;
    }
    
    /**
     * Adds a transformation to the chain.  The reliability of each transformation
     * added must be greater than 0.0.
     * 
     * @param sourceMimetype
     * @param targetMimetype
     * @param transformer the transformer that will transform from the source to
     *      the target mimetype
     */
    public void addTransformation(String sourceMimetype, String targetMimetype, ContentTransformer transformer)
    {
        // create a transformation that aggregates the transform info
        Transformation transformation = new Transformation(
                transformer,
                sourceMimetype,
                targetMimetype);
        // add to the chain
        chain.add(transformation);
        // recalculate combined reliability
        double transformerReliability = transformer.getReliability(sourceMimetype, targetMimetype);
        if (transformerReliability <= 0.0 || transformerReliability > 1.0)
        {
            throw new AlfrescoRuntimeException(
                    "Reliability of transformer must be between 0.0 and 1.0: \n" +
                    "   transformer: " + transformer + "\n" +
                    "   source: " + sourceMimetype + "\n" +
                    "   target: " + targetMimetype + "\n" +
                    "   reliability: " + transformerReliability);
        }
        this.reliability *= transformerReliability;
    }
    
    /**
     * In order to score anything, the source mimetype must match the source
     * mimetype of the first transformer and the target mimetype must match
     * the target mimetype of the last transformer in the chain.
     * 
     * @return Returns the product of the individual reliability scores of the
     *      transformations in the chain
     */
    public double getReliability(String sourceMimetype, String targetMimetype)
    {
        if (chain.size() == 0)
        {
            // no transformers therefore no transformation possible
            return 0.0; 
        }
        Transformation first = chain.getFirst();
        Transformation last = chain.getLast();
        if (!first.getSourceMimetype().equals(sourceMimetype)
                && last.getTargetMimetype().equals(targetMimetype))
        {
            // the source type of the first transformation must match the source
            // the target type of the last transformation must match the target
            return 0.0;
        }
        return reliability;
    }

    /**
     * @return Returns 0 if there are no transformers in the chain otherwise
     *      returns the sum of all the individual transformation times
     */
    public long getTransformationTime()
    {
        long transformationTime = 0L;
        for (Transformation transformation : chain)
        {
            ContentTransformer transformer = transformation.transformer;
            transformationTime += transformer.getTransformationTime();
        }
        return transformationTime;
    }
    
    /**
     * 
     */
    public void transform(ContentReader reader, ContentWriter writer) throws ContentIOException
    {
        transform(reader, writer, null);
    }

    /**
     * Executes each transformer in the chain, passing the content between them
     */
    public void transform(ContentReader reader, ContentWriter writer, Map<String, Object> options)
            throws ContentIOException
    {
        if (chain.size() == 0)
        {
            throw new AlfrescoRuntimeException("No transformations present in chain");
        }
        
        // check that the mimetypes of the transformation are valid for the chain
        String sourceMimetype = reader.getMimetype();
        String targetMimetype = writer.getMimetype();
        Transformation firstTransformation = chain.getFirst();
        Transformation lastTransformation = chain.getLast();
        if (!firstTransformation.getSourceMimetype().equals(sourceMimetype)
                && lastTransformation.getTargetMimetype().equals(targetMimetype))
        {
            throw new AlfrescoRuntimeException("Attempting to perform unreliable transformation: \n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer);
        }
        
        ContentReader currentReader = reader;
        ContentWriter currentWriter = null;
        int currentIndex = 0;
        for (Transformation transformation : chain)
        {
            boolean last = (currentIndex == chain.size() - 1);
            if (last)
            {
                // we are on the last transformation so use the final output writer
                currentWriter = writer;
            }
            else
            {
                // have to create an intermediate writer - just use a file writer
                File tempFile = TempFileProvider.createTempFile("transform", ".tmp");
                currentWriter = new FileContentWriter(tempFile);
                // set the writer's mimetype to conform to the transformation we are using
                currentWriter.setMimetype(transformation.getTargetMimetype());
            }
            // transform from the current reader to the current writer
            transformation.execute(currentReader, currentWriter, options);
            if (!currentWriter.isClosed())
            {
                throw new AlfrescoRuntimeException("Writer not closed by transformation: \n" +
                        "   transformation: " + transformation + "\n" +
                        "   writer: " + currentWriter);
            }
            // if we have more transformations, then use the written content
            // as the next source
            if (!last)
            {
                currentReader = currentWriter.getReader();
            }
        }
        // done
        if (logger.isDebugEnabled())
        {
            logger.debug("Executed complex transformation: \n" +
                    "   chain: " + chain + "\n" +
                    "   reader: " + reader + "\n" +
                    "   writer: " + writer);
        }
    }

    /**
     * A transformation that contains the transformer as well as the
     * transformation mimetypes to be used 
     */
    public static class Transformation extends ContentTransformerRegistry.TransformationKey
    {
        private ContentTransformer transformer;
        public Transformation(ContentTransformer transformer, String sourceMimetype, String targetMimetype)
        {
            super(sourceMimetype, targetMimetype);
            this.transformer = transformer;
        }
        
        /**
         * Executs the transformation
         * 
         * @param reader the reader from which to read the content
         * @param writer the writer to write content to
         * @param options the options to execute with
         * @throws ContentIOException if the transformation fails
         */
        public void execute(ContentReader reader, ContentWriter writer, Map<String, Object> options)
                throws ContentIOException
        {
            String sourceMimetype = getSourceMimetype();
            String targetMimetype = getTargetMimetype();
            // check that the source and target mimetypes of the reader and writer match
            if (!sourceMimetype.equals(reader.getMimetype()))
            {
                throw new AlfrescoRuntimeException("The source mimetype doesn't match the reader's mimetype: \n" +
                        "   source mimetype: " + sourceMimetype + "\n" +
                        "   reader: " + reader);
            }
            if (!targetMimetype.equals(writer.getMimetype()))
            {
                throw new AlfrescoRuntimeException("The target mimetype doesn't match the writer's mimetype: \n" +
                        "   target mimetype: " + targetMimetype + "\n" +
                        "   writer: " + writer);
            }
            transformer.transform(reader, writer, options);
        }
    }
}