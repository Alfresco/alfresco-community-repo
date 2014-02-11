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
package org.alfresco.repo.web.scripts.content;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.metadata.MetadataExtracter;
import org.alfresco.repo.content.metadata.MetadataExtracterRegistry;
import org.alfresco.repo.content.transform.ComplexContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformer;
import org.alfresco.repo.content.transform.ContentTransformerRegistry;
import org.alfresco.repo.content.transform.ContentTransformerWorker;
import org.alfresco.repo.content.transform.ProxyContentTransformer;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.TransformationOptions;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;


/**
 * Lists mimetypes, and optionally their associated content transformers
 *  and metadata extractors.
 * 
 * @author Nick Burch
 * @since 3.4.b
 */
public class MimetypesGet extends DeclarativeWebScript implements ApplicationContextAware, InitializingBean
{
    public static final String MODEL_MIMETYPES = "mimetypes";
    public static final String MODEL_EXTENSIONS = "extensions";
    public static final String MODEL_MIMETYPE_DETAILS = "details";
   
    private ApplicationContext applicationContext;
    private MimetypeService mimetypeService;
    private ContentTransformerRegistry contentTransformerRegistry;
    private MetadataExtracterRegistry metadataExtracterRegistry;
    
    private Map<String, String> knownWorkerBeanLabels;
    private Map<ContentTransformerWorker, String> knownWorkers;

    protected static final String OODIRECT_WORKER_BEAN = "transformer.worker.OpenOffice";
    protected static final String JOD_WORKER_BEAN = "transformer.worker.JodConverter";
    protected static final String RTS_WORKER_BEAN = "transformer.worker.remoteServer";
    
    protected static final String PROXY_LABEL_DEFAULT_MESSAGE = "Proxy via: {0} ({1})";
    
    /**
     * Uses the context to find OpenOffice related beans.
     * Allows us to work more cleanly on Community and Enterprise
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception
    {
        // If no override has been supplied use the default known list
        if (knownWorkerBeanLabels == null)
        {
            knownWorkerBeanLabels = new HashMap<String, String>();
            knownWorkerBeanLabels.put(OODIRECT_WORKER_BEAN, "Using a Direct Open Office Connection");
            knownWorkerBeanLabels.put(JOD_WORKER_BEAN, "Using JOD Converter / Open Office");
            knownWorkerBeanLabels.put(RTS_WORKER_BEAN, "Using the Remote Transformation Server v{1}");
        }
        
        // Build the map of known worker bean instances to bean names
        knownWorkers = new HashMap<ContentTransformerWorker, String>();
        for (String workerName : knownWorkerBeanLabels.keySet())
        {
            if(applicationContext.containsBean(workerName))
            {
                Object bean = applicationContext.getBean(workerName);
                if(bean instanceof ContentTransformerWorker)
                {
                    knownWorkers.put((ContentTransformerWorker) bean, workerName);
                }
            }
        }
    }

    /**
     * Sets the Mimetype Service to be used to get the
     *  list of mime types
     */
    public void setMimetypeService(MimetypeService mimetypeService) {
       this.mimetypeService = mimetypeService;
    }

    /**
     * Sets the Content Transformer Registry to be used to
     *  decide what transformations exist
     */
    public void setContentTransformerRegistry(
         ContentTransformerRegistry contentTransformerRegistry) {
       this.contentTransformerRegistry = contentTransformerRegistry;
    }

    /**
     * Sets the Metadata Extractor Registry to be used to
     *  decide what extractors exist
     */
    public void setMetadataExtracterRegistry(
         MetadataExtracterRegistry metadataExtracterRegistry) {
       this.metadataExtracterRegistry = metadataExtracterRegistry;
    }
    
    /**
     * Sets the map of content transformer worker bean names to
     * message formatting labels
     * 
     * @param knownWorkerBeanLabels
     */
    public void setKnownWorkerBeanLabels(Map<String, String> knownWorkerBeanLabels)
    {
        this.knownWorkerBeanLabels = knownWorkerBeanLabels;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
       // First up, get the list of mimetypes
       // We want it to be sorted
       String[] mimetypesA = mimetypeService.getMimetypes().toArray(new String[0]);
       Arrays.sort(mimetypesA);
       List<String> mimetypes = new ArrayList<String>(
             Arrays.asList(mimetypesA)
       );
       
       // Now their extensions
       Map<String,String> extensions = new HashMap<String, String>();
       for(String mimetype : mimetypes)
       {
          String ext = mimetypeService.getExtension(mimetype);
          extensions.put(mimetype, ext);
       }
       
       // Now details on those it was requested for
       Map<String,Map<String,List<String>>> details = new HashMap<String, Map<String,List<String>>>();
       String reqMimetype = req.getParameter("mimetype");
       for(String mimetype : mimetypes)
       {
          if(mimetype.equals(reqMimetype) || "*".equals(reqMimetype))
          {
             Map<String,List<String>> mtd = new HashMap<String, List<String>>();
             mtd.put("extractors", getExtractors(mimetype));
             mtd.put("transformFrom", getTransformersFrom(mimetype, -1, mimetypes));
             mtd.put("transformTo", getTransformersTo(mimetype, -1, mimetypes));
             details.put(mimetype, mtd);
          }
       }
       
       // Return the model
       Map<String, Object> model = new HashMap<String, Object>();
       model.put(MODEL_MIMETYPES, mimetypes);
       model.put(MODEL_EXTENSIONS, extensions);
       model.put(MODEL_MIMETYPE_DETAILS, details);
       return model;
    }
    
    protected List<String> getExtractors(String mimetype)
    {
       List<String> exts = new ArrayList<String>();
       MetadataExtracter extractor = metadataExtracterRegistry.getExtracter(mimetype);
       if(extractor != null) {
          exts.add( extractor.getClass().getName() );
       }
       return exts;
    }
    protected List<String> getTransformersFrom(String mimetype, long sourceSize, List<String> allMimetypes)
    {
       List<String> transforms = new ArrayList<String>();
       for(String toMT : allMimetypes)
       {
          if(toMT.equals(mimetype))
             continue;
          
          String details = getTransformer(mimetype, sourceSize, toMT);
          if(details != null)
             transforms.add(toMT + " = " + details);
       }
       return transforms;
    }
    protected List<String> getTransformersTo(String mimetype, long sourceSize, List<String> allMimetypes)
    {
       List<String> transforms = new ArrayList<String>();
       for(String fromMT : allMimetypes)
       {
          if(fromMT.equals(mimetype))
             continue;
          
          String details = getTransformer(fromMT, sourceSize, mimetype);
          if(details != null)
             transforms.add(fromMT + " = " + details);
       }
       return transforms;
    }
    /** Note - for now, only does the best one, not all */
    protected String getTransformer(String from, long sourceSize, String to)
    {
       ContentTransformer ct = contentTransformerRegistry.getTransformer(
             from, sourceSize, to, new TransformationOptions()
       );
       if(ct == null)
          return null;
       
       if(ct instanceof ComplexContentTransformer)
       {
          return getComplexTransformerLabel((ComplexContentTransformer)ct);
       }
       
       if(ct instanceof ProxyContentTransformer)
       {
          String proxyLabel = getProxyTransformerLabel((ProxyContentTransformer)ct);
          if (proxyLabel != null)
          {
              return proxyLabel;
          }
       }
       
       return ct.getClass().getName();
    }
    
    /**
     * Gets the display label for complex transformers
     * 
     * @param cct
     * @return the transformer display label
     */
    protected String getComplexTransformerLabel(ComplexContentTransformer cct)
    {
        String text = "Complex via: ";
        for(String imt : cct.getIntermediateMimetypes()) {
           text += imt + " ";
        }
        return text;
    }
    
    /**
     * Gets the display label for proxy content transformers
     * 
     * @param pct
     * @return the transformer display label
     */
    protected String getProxyTransformerLabel(ProxyContentTransformer pct)
    {
        ContentTransformerWorker ctw = pct.getWorker();
        
        String message = PROXY_LABEL_DEFAULT_MESSAGE;
        
        String beanName = getWorkerBeanName(ctw);
        if (beanName != null)
        {
            message = knownWorkerBeanLabels.get(beanName);
        }
        return MessageFormat.format(message, ctw.getClass().getName(), ctw.getVersionString());
    }
    
    /**
     * Gets the given ContentTransformerWorker's bean name from the cache of known workers
     * <p>
     * In the future ContentTransformerWorker may be made bean name aware.
     * 
     * @param ctw
     * @return the bean name
     */
    protected String getWorkerBeanName(ContentTransformerWorker ctw)
    {
        return knownWorkers.get(ctw);
    }

}