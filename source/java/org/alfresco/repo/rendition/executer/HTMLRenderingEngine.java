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

package org.alfresco.repo.rendition.executer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.rendition.RenditionLocation;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.rendition.RenditionServiceException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.ContainerAwareDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class provides a way to turn documents supported by the
 *  {@link ContentService} standard transformers into basic, clean
 *  HTML.
 * <P/>
 * The HTML that is produced probably isn't going to be suitable
 *  for direct web publishing, as it's likely going to be too
 *  basic. Instead, it should be simple and clean HTML, suitable
 *  for being the basis of some web-friendly HTML once edited
 *  / further transformed. 
 * 
 * @author Nick Burch
 * @since 3.4
 */
public class HTMLRenderingEngine extends AbstractRenderingEngine
{
    private static Log logger = LogFactory.getLog(HTMLRenderingEngine.class);

    /*
     * Action constants
     */
    public static final String NAME = "htmlRenderingEngine";
    
    protected static final QName PRIMARY_IMAGE = QName.createQName(
          "http://www.alfresco.org/model/website/1.0", "primaryImage");
    protected static final QName SECONDARY_IMAGE = QName.createQName(
          "http://www.alfresco.org/model/website/1.0", "secondaryImage");

    private DictionaryService dictionaryService;
    
    public void setDictionaryService(DictionaryService dictionaryService) {
       this.dictionaryService = dictionaryService;
    }

    /*
     * (non-Javadoc)
     * @see org.alfresco.repo.rendition.executer.AbstractRenderingEngine#render(org.alfresco.repo.rendition.executer.AbstractRenderingEngine.RenderingContext)
     */
    @Override
    protected void render(RenderingContext context)
    {
        ContentReader contentReader = context.makeContentReader();
        String sourceMimeType = contentReader.getMimetype();
        String targetMimeType = "text/html";
        
        // Check that Tika supports the supplied file
        AutoDetectParser p = new AutoDetectParser();
        MediaType sourceMediaType = MediaType.parse(sourceMimeType);
        if(! p.getParsers().containsKey(sourceMediaType))
        {
           throw new RenditionServiceException(
                 "Source mime type of " + sourceMimeType + 
                 " is not supported by Tika for HTML conversions"
           );
        }
        
        // Make the HTML Version using Tika
        // This will also extract out any images as found
        generateHTML(p, context);
    }
    
    /**
     * What name should be used for the images directory?
     */
    private String getImagesDirectoryName(RenderingContext context)
    {
       // Based on the name of the source node, which will
       //  also largely be the name of the html node
       String folderName = nodeService.getProperty( 
             context.getSourceNode(),
             ContentModel.PROP_NAME
       ).toString();
       if(folderName.lastIndexOf('.') > -1)
       {
          folderName = folderName.substring(0, folderName.lastIndexOf('.'));
       }
       folderName = folderName + "_files";
       return folderName;
    }
    
    /**
     * Creates a directory to store the images in.
     * The directory will be a sibling of the rendered
     *  HTML, and named similar to it.
     */
    private NodeRef createImagesDirectory(RenderingContext context)
    {
       // It should be a sibling of the HTML in it's eventual location
       //  (not it's current temporary one!)
       RenditionLocation location = resolveRenditionLocation(
             context.getSourceNode(), context.getDefinition(), context.getDestinationNode()
       );
       NodeRef parent = location.getParentRef();
       
       // Figure out what to call it, based on the HTML node
       String folderName = getImagesDirectoryName(context);
       
       // It is already there?
       // (eg from when the rendition is being re-run)
       NodeRef imgFolder = nodeService.getChildByName(
             parent, ContentModel.ASSOC_CONTAINS, folderName
       );
       if(imgFolder != null)
          return imgFolder;
       
       // Create the directory
       Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
       properties.put(ContentModel.PROP_NAME, folderName);
       imgFolder = nodeService.createNode(
             parent,
             ContentModel.ASSOC_CONTAINS,
             QName.createQName(folderName),
             ContentModel.TYPE_FOLDER,
             properties
       ).getChildRef();
       
       return imgFolder;
    }
    
    private NodeRef createEmbeddedImage(NodeRef imgFolder, boolean primary,
          String filename, String contentType, InputStream imageSource,
          RenderingContext context)
    {
       // Create the node if needed
       NodeRef img = nodeService.getChildByName(
             imgFolder, ContentModel.ASSOC_CONTAINS, filename
       );
       if(img == null)
       {
          Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
          properties.put(ContentModel.PROP_NAME, filename);
          img = nodeService.createNode(
                imgFolder,
                ContentModel.ASSOC_CONTAINS,
                QName.createQName(filename),
                ContentModel.TYPE_CONTENT,
                properties
          ).getChildRef();
       }
       
       // If we can, associate it with the rendered HTML, so
       //  that they're properly linked
       QName assocType = SECONDARY_IMAGE;
       if(primary)
       {
          assocType = PRIMARY_IMAGE;
       }
       if(dictionaryService.getAssociation(assocType) != null)
       {
          nodeService.createAssociation(
                context.getDestinationNode(), img, assocType
          );
       }
       
       // Put the image into the node
       ContentWriter writer = contentService.getWriter(
             img, ContentModel.PROP_CONTENT, true
       );
       writer.setMimetype(contentType);
       writer.putContent(imageSource);
       
       // All done
       return img;
    }
    
    /**
     * Builds a Tika-compatible SAX content handler, which will
     *  be used to generate+capture the XHTML
     */
    private ContentHandler buildContentHandler(Writer output) 
    {
       SAXTransformerFactory factory = (SAXTransformerFactory)
                SAXTransformerFactory.newInstance();
       TransformerHandler handler;
       
       try {
          handler = factory.newTransformerHandler();
       } catch (TransformerConfigurationException e) {
          throw new RenditionServiceException("SAX Processing isn't available - " + e);
       }
       
       handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
       handler.setResult(new StreamResult(output));
       handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
       
       return handler;
    }
    
    /**
     * Asks Tika to translate the contents into HTML
     */
    private void generateHTML(Parser p, RenderingContext context)
    {
       ContentReader contentReader = context.makeContentReader();
       
       // Setup things to parse with
       StringWriter sw = new StringWriter();
       ContentHandler handler = new TikaImageRewritingContentHandler( 
                   buildContentHandler(sw),
                   getImagesDirectoryName(context)
       );
       
       // Tell Tika what we're dealing with
       Metadata metadata = new Metadata();
       metadata.set(
             Metadata.CONTENT_TYPE, 
             contentReader.getMimetype()
       );
       metadata.set(
             Metadata.RESOURCE_NAME_KEY, 
             nodeService.getProperty( 
                   context.getSourceNode(),
                   ContentModel.PROP_NAME
             ).toString()
       );
       
       // Our parse context needs to extract images
       ParseContext parseContext = new ParseContext();
       parseContext.set(Parser.class, new TikaImageExtractingParser(context));
       
       // Parse
       try {
          p.parse(
                contentReader.getContentInputStream(),
                handler, metadata, parseContext
          );
       } catch(Exception e) {
          throw new RenditionServiceException("Tika HTML Conversion Failed", e);
       }
       
       // Save it
       ContentWriter contentWriter = context.makeContentWriter();
       contentWriter.putContent( sw.toString() );
    }
    
    
    /**
     * A nested Tika parser which extracts out any
     *  images as they come past.
     */
   @SuppressWarnings("serial")
   private class TikaImageExtractingParser implements Parser {
      private Set<MediaType> types;
      
      private RenderingContext renderingContext;
      private NodeRef imgFolder = null;
      private int count = 0;
      
      private TikaImageExtractingParser(RenderingContext renderingContext) {
         this.renderingContext = renderingContext;
         
         // Our expected types
         types = new HashSet<MediaType>();
         types.add(MediaType.image("bmp"));
         types.add(MediaType.image("gif"));
         types.add(MediaType.image("jpg"));
         types.add(MediaType.image("jpeg"));
         types.add(MediaType.image("png"));
         types.add(MediaType.image("tiff"));
      }
      
      @Override
      public Set<MediaType> getSupportedTypes(ParseContext context) {
         return types;
      }

      @Override
      public void parse(InputStream stream, ContentHandler handler,
            Metadata metadata, ParseContext context) throws IOException,
            SAXException, TikaException {
         // Is it a supported image?
         String filename = metadata.get(Metadata.RESOURCE_NAME_KEY);
         String type = metadata.get(Metadata.CONTENT_TYPE);
         boolean accept = false;
         
         if(type != null) {
            for(MediaType mt : types) {
               if(mt.toString().equals(type)) {
                  accept = true;
               }
            }
         }
         if(filename != null) {
            for(MediaType mt : types) {
               String ext = "." + mt.getSubtype();
               if(filename.endsWith(ext)) {
                  accept = true;
               }
            }
         }
         
         if(!accept)
            return;

         handleImage(stream, filename, type);
      }

      @Override
      public void parse(InputStream stream, ContentHandler handler,
            Metadata metadata) throws IOException, SAXException, TikaException {
         parse(stream, handler, metadata, new ParseContext());
      }
      
      private void handleImage(InputStream stream, String filename, String type) {
         count++;
         
         // Do we already have the folder? If not, create it
         if(imgFolder == null) {
            imgFolder = createImagesDirectory(renderingContext);
         }
         
         // Give it a sensible name if needed
         if(filename == null) {
            filename = "image-" + count + ".";
            filename += type.substring(type.indexOf('/')+1);
         }

         // Save the image
         createEmbeddedImage(imgFolder, (count==1), filename, type, stream, renderingContext);
      }
    }
   
    /**
     * A content handler that re-writes image src attributes,
     *  and passes everything else on to the real one.
     */
    private class TikaImageRewritingContentHandler implements ContentHandler {
       private ContentHandler handler;
       private String imageFolder;
       
       private TikaImageRewritingContentHandler(ContentHandler handler, String imageFolder) {
          this.handler = handler;
          this.imageFolder = imageFolder;
       }

       @Override
       public void startElement(String uri, String localName, String qName,
             Attributes origAttrs) throws SAXException {
          // If we have an image tag, re-write the src attribute
          //  if required
          if("img".equals(localName)) {
             AttributesImpl attrs;
             if(origAttrs instanceof AttributesImpl) {
                attrs = (AttributesImpl)origAttrs;
             } else {
                attrs = new AttributesImpl(origAttrs);
             }
             
             for(int i=0; i<attrs.getLength(); i++) {
                if("src".equals(attrs.getLocalName(i))) {
                   String src = attrs.getValue(i);
                   if(src.startsWith("embedded:")) {
                      src = imageFolder + "/" +
                               src.substring(src.indexOf(':')+1);
                      attrs.setValue(i, src);
                   }
                }
             }
             handler.startElement(uri, localName, qName, attrs);
          } else {
             // For any other tag, pass through as-is
             handler.startElement(uri, localName, qName, origAttrs);
          }
       }

       @Override
       public void characters(char[] ch, int start, int length)
       throws SAXException {
          handler.characters(ch, start, length);
       }
       @Override
       public void ignorableWhitespace(char[] ch, int start, int length)
       throws SAXException {
          handler.ignorableWhitespace(ch, start, length);
       }
       
       @Override
       public void endDocument() throws SAXException {
          handler.endDocument();
       }
       @Override
       public void endElement(String uri, String localName, String qName)
       throws SAXException {
          handler.endElement(uri, localName, qName);
       }
       @Override
       public void endPrefixMapping(String prefix) throws SAXException {
          handler.endPrefixMapping(prefix);
       }
       @Override
       public void processingInstruction(String target, String data)
       throws SAXException {
          handler.processingInstruction(target, data);
       }
       @Override
       public void setDocumentLocator(Locator locator) {
          handler.setDocumentLocator(locator);
       }
       @Override
       public void skippedEntity(String name) throws SAXException {
          handler.skippedEntity(name);
       }
       @Override
       public void startDocument() throws SAXException {
          handler.startDocument();
       }
       @Override
       public void startPrefixMapping(String prefix, String uri)
       throws SAXException {
          handler.startPrefixMapping(prefix, uri);
       }
    }
}