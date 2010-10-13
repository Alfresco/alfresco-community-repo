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
public class MimetypesGet extends DeclarativeWebScript implements ApplicationContextAware
{
    public static final String MODEL_MIMETYPES = "mimetypes";
    public static final String MODEL_EXTENSIONS = "extensions";
    public static final String MODEL_MIMETYPE_DETAILS = "details";
   
    private MimetypeService mimetypeService;
    private ContentTransformerRegistry contentTransformerRegistry;
    private MetadataExtracterRegistry metadataExtracterRegistry;

    /** So we can spot if it goes via Direct OO */
    ContentTransformerWorker ooDirectWorker;
    protected static final String OODIRECT_WORKER_BEAN = "transformer.worker.OpenOffice";
    
    /** So we can spot if it goes through JODConverter */
    ContentTransformerWorker jodWorker;
    protected static final String JOD_WORKER_BEAN = "transformer.worker.JodConverter";
    
    /**
     * Uses the context to find OpenOffice related beans.
     * Allows us to work more cleanly on Community and Enterprise
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
         throws BeansException {
       if(applicationContext.containsBean(OODIRECT_WORKER_BEAN))
       {
          Object bean = applicationContext.getBean(OODIRECT_WORKER_BEAN);
          if(bean instanceof ContentTransformerWorker)
          {
             ooDirectWorker = (ContentTransformerWorker)bean;
          }
       }
       
       if(applicationContext.containsBean(JOD_WORKER_BEAN))
       {
          Object bean = applicationContext.getBean(JOD_WORKER_BEAN);
          if(bean instanceof ContentTransformerWorker)
          {
             jodWorker = (ContentTransformerWorker)bean;
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
             mtd.put("transformFrom", getTransformersFrom(mimetype, mimetypes));
             mtd.put("transformTo", getTransformersTo(mimetype, mimetypes));
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
    protected List<String> getTransformersFrom(String mimetype, List<String> allMimetypes)
    {
       List<String> transforms = new ArrayList<String>();
       for(String toMT : allMimetypes)
       {
          if(toMT.equals(mimetype))
             continue;
          
          String details = getTransformer(mimetype, toMT);
          if(details != null)
             transforms.add(toMT + " = " + details);
       }
       return transforms;
    }
    protected List<String> getTransformersTo(String mimetype, List<String> allMimetypes)
    {
       List<String> transforms = new ArrayList<String>();
       for(String fromMT : allMimetypes)
       {
          if(fromMT.equals(mimetype))
             continue;
          
          String details = getTransformer(fromMT, mimetype);
          if(details != null)
             transforms.add(fromMT + " = " + details);
       }
       return transforms;
    }
    /** Note - for now, only does the best one, not all */
    protected String getTransformer(String from, String to)
    {
       ContentTransformer ct = contentTransformerRegistry.getTransformer(
             from, to, new TransformationOptions()
       );
       if(ct == null)
          return null;
       
       if(ct instanceof ComplexContentTransformer)
       {
          ComplexContentTransformer cct = (ComplexContentTransformer)ct;
          String text = "Complex via: ";
          for(String imt : cct.getIntermediateMimetypes()) {
             text += imt + " ";
          }
          return text;
       }
       
       if(ct instanceof ProxyContentTransformer)
       {
          ProxyContentTransformer pct = (ProxyContentTransformer)ct;
          ContentTransformerWorker ctw = pct.getWorker();
          
          if(ctw.equals(jodWorker))
             return "Using JOD Converter / Open Office";
          if(ctw.equals(ooDirectWorker))
             return "Using a Direct Open Office Connection";
          
          String text = "Proxy via: " +
             ctw.getClass().getName() + 
             "(" + ctw.getVersionString() + ")";
          return text;
       }
       
       return ct.getClass().getName();
    }
}