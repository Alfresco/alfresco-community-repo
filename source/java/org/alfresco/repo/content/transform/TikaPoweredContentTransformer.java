/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.repo.content.transform;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.content.filestore.FileContentReader;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.extractor.DocumentSelector;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.xml.sax.ContentHandler;

/**
 * Provides helpful services for {@link org.alfresco.repo.content.transform.ContentTransformer}
 *  implementations which are powered by Apache Tika.
 * 
 * To use Tika to transform some content into Text, Html or XML, create an 
 *  implementation of this / use the Auto Detect transformer.
 * 
 * For now, all transformers are registered as regular, rather than explicit
 *  transformations. This should allow you to register your own explicit
 *  transformers and have them nicely take priority.
 * 
 * @author Nick Burch
 */
public abstract class TikaPoweredContentTransformer extends AbstractContentTransformer2
{
    private static final Log logger = LogFactory.getLog(TikaPoweredContentTransformer.class);
    private static final List<String> TARGET_MIMETYPES = Arrays.asList(new String[] { 
            MimetypeMap.MIMETYPE_TEXT_PLAIN,
            MimetypeMap.MIMETYPE_HTML,
            MimetypeMap.MIMETYPE_XHTML,
            MimetypeMap.MIMETYPE_XML});

    protected List<String> sourceMimeTypes;
    protected DocumentSelector documentSelector;
    
    /**
     * Windows carriage return line feed pair.
     */
    protected static final String LINE_BREAK = "\r\n";
    public static final String WRONG_FORMAT_MESSAGE_ID = "transform.err.format_or_password";
    
        protected TikaPoweredContentTransformer(List<String> sourceMimeTypes) {
       this.sourceMimeTypes = sourceMimeTypes;
    }
    protected TikaPoweredContentTransformer(String[] sourceMimeTypes) {
       this(Arrays.asList(sourceMimeTypes));
    }
    
    /**
     * Returns the correct Tika Parser to process
     *  the document.
     * If you don't know which you want, use
     *  {@link TikaAutoContentTransformer} which
     *  makes use of the Tika auto-detection.
     */
    protected abstract Parser getParser();
    
    /**
     * Can we do the requested transformation via Tika?
     * We support transforming to HTML, XML or Text
     */
    @Override
    public boolean isTransformableMimetype(String sourceMimetype, String targetMimetype, TransformationOptions options)
    {
       if(! sourceMimeTypes.contains(sourceMimetype)) 
       {
          // The source isn't one of ours
          return false;
       }
       
       if (TARGET_MIMETYPES.contains(targetMimetype))
       {
          // We can output to this
          return true;
       } 
       else 
       {
          // We support the source, but not the target
          return false;
       }
    }
    
    @Override
    public String getComments(boolean available)
    {
        return getCommentsOnlySupports(sourceMimeTypes, TARGET_MIMETYPES, available);
    }
    
    /**
     * Returns an appropriate Tika ContentHandler for the
     *  requested content type. Normally you'll let this
     *  work as default, but if you need fine-grained
     *  control of how the Tika events become text then
     *  override and supply your own.
     */
    protected ContentHandler getContentHandler(String targetMimeType, Writer output) 
                   throws TransformerConfigurationException
    {
       if(MimetypeMap.MIMETYPE_TEXT_PLAIN.equals(targetMimeType)) 
       {
          return new BodyContentHandler(output);
       }
       
       SAXTransformerFactory factory = (SAXTransformerFactory)
             SAXTransformerFactory.newInstance();
       TransformerHandler handler = factory.newTransformerHandler();
       handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
       handler.setResult(new StreamResult(output));
       
       if(MimetypeMap.MIMETYPE_HTML.equals(targetMimeType))
       {
          handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
          return new ExpandedTitleContentHandler(handler);
       }
       else if(MimetypeMap.MIMETYPE_XHTML.equals(targetMimeType) ||
               MimetypeMap.MIMETYPE_XML.equals(targetMimeType))
       {
          handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
       }
       else
       {
          throw new TransformerInfoException(
                WRONG_FORMAT_MESSAGE_ID,
                new IllegalArgumentException("Requested target type " + targetMimeType + " not supported")
          );
       }
       return handler;
    }
    
    /**
     * Sets the document selector, used for determining whether to parse embedded resources.
     * 
     * @param documentSelector
     */
    public void setDocumentSelector(DocumentSelector documentSelector)
    {
        this.documentSelector = documentSelector;
    }
    /**
     * Gets the document selector, used for determining whether to parse embedded resources, 
     * null by default so parse all.
     * 
     * @param metadata
     * @param targetMimeType
     * @param options
     * @return the document selector
     */
    protected DocumentSelector getDocumentSelector(Metadata metadata, String targetMimeType, TransformationOptions options)
    {
        return documentSelector;
    }

    /**
     * By default returns a ParseContent that does not recurse
     */
    protected ParseContext buildParseContext(Metadata metadata, String targetMimeType, TransformationOptions options)
    {
       ParseContext context = new ParseContext();
       DocumentSelector selector = getDocumentSelector(metadata, targetMimeType, options);
       if (selector != null)
       {
           context.set(DocumentSelector.class, selector);
       }
       return context;
    }
    
    public void transformInternal(ContentReader reader, ContentWriter writer,  TransformationOptions options)
    throws Exception
    {
       OutputStream os = writer.getContentOutputStream();
       String encoding = writer.getEncoding();
       String targetMimeType = writer.getMimetype();
       
       Writer ow = new OutputStreamWriter(os, encoding); 
       
       Parser parser = getParser();
       Metadata metadata = new Metadata();
       
       ParseContext context = buildParseContext(metadata, targetMimeType, options);
       
       ContentHandler handler = getContentHandler(targetMimeType, ow);
       if(handler == null)
       {
          throw new TransformerConfigurationException(
                "Unable to create Tika Handler for configured output " + targetMimeType
          );
       }

       // Prefer the File if available - it takes less memory to process
       InputStream is;
       if(reader instanceof FileContentReader) 
       {
          is = TikaInputStream.get( ((FileContentReader)reader).getFile(), metadata );
       }
       else
       {
          is = reader.getContentInputStream();
       }
       
       try {
          parser.parse(is, handler, metadata, context);
       } 
       finally
       {
          if (is != null)
          {
              try { is.close(); } catch (Throwable e) {}
          }
          if (ow != null)
          {
              try { ow.close(); } catch (Throwable e) {}
          }
          if (os != null)
          {
              try { os.close(); } catch (Throwable e) {}
          }
      }
    }
}
