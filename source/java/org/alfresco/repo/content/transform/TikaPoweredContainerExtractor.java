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
package org.alfresco.repo.content.transform;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.ContainerAwareDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedResourceHandler;
import org.apache.tika.extractor.ParserContainerExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;

/**
 * Warning - this is a prototype service, and will likely change dramatically
 *  in Alfresco 4.0!
 * 
 * This proto-service provides a way to have Apache Tika extract out
 *  certain kinds of embedded resources from within a container file.
 * 
 * One use might be to extract all the images in a zip file, another might
 *  be to fetch all the Word Documents embedded in an Excel Spreadsheet.  
 *
 * Uses the Apache Tika ContainerExtractor framework, along with the
 *  Apache Tika Auto-Parser.
 *  
 * Not sprung-in by default, you will need to manually list this in
 *  an extension context file.
 * 
 * @author Nick Burch
 */
public class TikaPoweredContainerExtractor
{
    private static final Log logger = LogFactory.getLog(TikaPoweredContainerExtractor.class);
    
    private NodeService nodeService;
    private ContentService contentService;
    
    private TikaConfig config;
    private AutoDetectParser parser;
    private Detector detector;

    /**
     * Injects the nodeService bean.
     * 
     * @param nodeService the nodeService.
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Injects the contentService bean.
     * 
     * @param contentService the contentService.
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }
    
    /**
     * Injects the TikaConfig to use
     * 
     * @param tikaConfig The Tika Config to use 
     */
    public void setTikaConfig(TikaConfig tikaConfig)
    {
        this.config = tikaConfig;
        
        // Setup the detector and parser
        detector = new ContainerAwareDetector(
                config.getMimeRepository()
        );
        parser = new AutoDetectParser(detector);
    }

    /**
     * Extracts out all the entries from the container
     *  that match the supplied list of mime types.
     * If no mime types are specified, extracts all
     *  available embedded resources. 
     */
    public List<NodeRef> extract(NodeRef source, List<String> mimetypes)
    {
       // Grab the directory to put the nodes into
       // Will be the parent folder of the source
       NodeRef folder = nodeService.getPrimaryParent(source).getParentRef();
       
       // Get the contents
       ContentReader reader = contentService.getReader(source, ContentModel.PROP_CONTENT);
       TikaInputStream stream = TikaInputStream.get(reader.getContentInputStream());

       // Build the recursing parser
       Extractor handler = new Extractor(folder, mimetypes);
       
       // Have Tika look for things
       ParserContainerExtractor extractor = new ParserContainerExtractor(
             parser, detector
       );
       try {
          logger.info("Beginning extraction of " + source.toString());
          extractor.extract(stream, null, handler);
          logger.info("Completed extraction of " + source.toString());
       } catch(TikaException te) {
          throw new AlfrescoRuntimeException("Extraction Failed", te);
       } catch(IOException ie) {
          throw new AlfrescoRuntimeException("Extraction Failed", ie);
       }
       
       // Tidy up
       try {
          stream.close();
       } catch(IOException e) {}
       
       // All done
       return handler.extracted;
    }
    
    /**
     * This EmbeddedResourceHandler is called by Tika for each
     *  embedded resource. It decides if the resource is to
     *  be extracted or not, and if it is, saves it into the
     *  specified folder.
     */
    private class Extractor implements EmbeddedResourceHandler
    {
       private List<NodeRef> extracted;
       private Set<MediaType> acceptTypes;
       private NodeRef folder;
       private int anonymousCount = 0;
       
       private Extractor(NodeRef folder, List<String> types)
       {
          this.folder = folder;
          this.extracted = new ArrayList<NodeRef>();
          
          if(types != null && types.size() > 0)
          {
             acceptTypes = new HashSet<MediaType>();
             for(String type : types)
             {
                acceptTypes.add(MediaType.parse(type));
             }
          }
       }
       
       @Override
       public void handle(String filename, MediaType mediaType,
             InputStream stream) {
          // Do we want it?
          if(acceptTypes == null || acceptTypes.contains(mediaType)) 
          {
             // Ensure we have a filename
             if(filename == null) 
             {
                anonymousCount++;
                filename = "embedded"+anonymousCount+"."+mediaType.getSubtype();
             }
             
             logger.info("Extracting embedded " + mediaType +  " entry " + filename);
             
             // Save it
             Map<QName,Serializable> properties = new HashMap<QName,Serializable>();
             properties.put(ContentModel.PROP_NAME, filename);
             NodeRef node = nodeService.createNode(
                   folder,
                   ContentModel.ASSOC_CONTAINS,
                   QName.createQName(filename),
                   ContentModel.TYPE_CONTENT,
                   properties
             ).getChildRef();
             
             ContentWriter writer = contentService.getWriter(
                   node, ContentModel.PROP_CONTENT, true
             );
             writer.setMimetype(mediaType.toString());
             writer.putContent(stream);
          }
          else
          {
             logger.info("Skipping embedded " + mediaType +  " entry " + filename);
          }
       }
    }

    /**
     * This action executor allows you to trigger extraction as an
     *  action, perhaps from a rule. 
     * 
     * Not sprung-in by default, you will need to manually list this in
     *  an extension context file. You will also need to add properties
     *  files entries.
     */
    public static class ExtractorActionExecutor extends ActionExecuterAbstractBase
    {
      public static final String NAME = "extractEmbeddedResources";
      public static final String PARAM_MIME_TYPES = "mime-types";

      private TikaPoweredContainerExtractor extractor;
      public void setTikaPoweredContainerExtractor(TikaPoweredContainerExtractor extractor)
      {
         this.extractor = extractor;
      }
      
      @Override
      protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
         paramList.add(new ParameterDefinitionImpl(
               PARAM_MIME_TYPES,
               DataTypeDefinition.TEXT,
               false,
               getParamDisplayLabel(PARAM_MIME_TYPES)
         ));
      }
      
      @Override
      protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
         List<String> mimeTypes = null;
         String rawTypes = (String)action.getParameterValue(PARAM_MIME_TYPES);
         if(rawTypes != null && rawTypes.length() > 0)
         {
            mimeTypes = new ArrayList<String>();
            StringTokenizer st = new StringTokenizer(rawTypes, ",");
            while(st.hasMoreTokens())
            {
               mimeTypes.add( st.nextToken().trim() );
            }
         }
            
         extractor.extract(actionedUponNodeRef, mimeTypes);
      }
    }
/*
<?xml version='1.0' encoding='UTF-8'?>
<!DOCTYPE beans PUBLIC '-//SPRING//DTD BEAN//EN' 'http://www.springframework.org/dtd/spring-beans.dtd'>
<beans>
   <bean id="tikaPoweredContainerExtractor" class="org.alfresco.repo.content.transform.TikaPoweredContainerExtractor">
       <property name="nodeService">
          <ref bean="NodeService" />
       </property>
       <property name="contentService">
          <ref bean="ContentService" />
       </property>
       <property name="tikaConfig">
          <bean class="org.apache.tika.config.TikaConfig" factory-method="getDefaultConfig" />
       </property>
   </bean>
   <bean id="extractEmbeddedResources" class="org.alfresco.repo.content.transform.TikaPoweredContainerExtractor$ExtractorActionExecutor" parent="action-executer">
       <property name="tikaPoweredContainerExtractor">
          <ref bean="tikaPoweredContainerExtractor" />
       </property>
   </bean>
   <bean id="extractEmbeddedResources-action-messages" class="org.alfresco.i18n.ResourceBundleBootstrapComponent">
       <property name="resourceBundles">
          <list>
            <value>alfresco.extension.extractor-action-messages</value>
          </list>
        </property>
   </bean>
</beans> 
 */
/*
extractEmbeddedResources.title=Extract embedded resources
extractEmbeddedResources.description=Extract resources from within container files, such as .zip or .docx
extractEmbeddedResources.param_mime-types.display-label=Mime Types
 */
}
