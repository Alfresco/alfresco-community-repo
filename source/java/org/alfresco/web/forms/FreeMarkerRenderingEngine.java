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
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>. */
package org.alfresco.web.forms;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import freemarker.cache.TemplateLoader;
import freemarker.ext.dom.NodeModel;
import freemarker.template.Configuration;
import freemarker.template.SimpleDate;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Implementation of a form data renderer for processing xml instance data
 * using a freemarker template.
 *
 * @author Ariel Backenroth
 */
public class FreeMarkerRenderingEngine
   implements RenderingEngine
{

   private static final Log LOGGER = LogFactory.getLog(FreeMarkerRenderingEngine.class);

   public FreeMarkerRenderingEngine() { }

   public String getName() { return "FreeMarker"; }

   public String getDefaultTemplateFileExtension() { return "ftl"; }

   /**
    * Renders the rendition using the configured freemarker template.  This
    * provides a root map to the freemarker template which places the xml document, and 
    * a variable named alfresco at the root.  the alfresco variable contains a hash of 
    * all parameters and all extension functions.
    */
   public void render(final Map<QName, Object> model,
                      final RenderingEngineTemplate ret,
                      final OutputStream out)
      throws IOException,
      RenderingEngine.RenderingException,
      SAXException
   {

      final Configuration cfg = new Configuration();
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      cfg.setTemplateLoader(new TemplateLoader()
      {
         public void closeTemplateSource(final Object templateSource)
            throws IOException
         {
            ((InputStream)templateSource).close();
         }
         
         public Object findTemplateSource(final String name)
            throws IOException
         {
            LOGGER.debug("request to load template " + name);

//            // WCM
//            final RenderingEngine.TemplateResourceResolver trr = (RenderingEngine.TemplateResourceResolver)
//               model.get(RenderingEngineTemplateImpl.PROP_RESOURCE_RESOLVER);
            final RenderingEngine.TemplateResourceResolver trr = null;

            return trr.resolve(name);
         }

         public long getLastModified(final Object templateSource)
         {
            // no caching for now...
            return System.currentTimeMillis();
         }
         
         public Reader getReader(final Object templateSource, final String encoding)
            throws IOException
         {
            return new InputStreamReader((InputStream)templateSource);
         }
      });
      final Template t = new Template("freemarker_template", 
                                      new InputStreamReader(ret.getInputStream()),
                                      cfg);
      final TemplateHashModel rootModel = this.convertModel(model);
      
      // process the form
      try
      {
         t.process(rootModel, new OutputStreamWriter(out));
      }
      catch (final TemplateException te)
      {
         LOGGER.debug(te);
         throw new RenderingEngine.RenderingException(te);
      }
      finally
      {
         out.flush();
         out.close();
      }
   }

   private TemplateHashModel convertModel(final Map<QName, Object> model)
   {
      final List<TemplateHashModel> rootModelObjects = new LinkedList<TemplateHashModel>();
      final SimpleHash result = new SimpleHash()
      {
         public TemplateModel get(final String key)
            throws TemplateModelException
         {
            TemplateModel result = super.get(key);
            if (result == null)
            {
               for (TemplateHashModel m : rootModelObjects)
               {
                  result = m.get(key);
                  if (result != null)
                  {
                     break;
                  }
               }
            }
            return result;
         }
      };
      for (final Map.Entry<QName, Object> entry : model.entrySet())
      {
         final QName qn = entry.getKey();
         if (qn.equals(RenderingEngine.ROOT_NAMESPACE))
         {
            final TemplateModel m = this.convertValue(entry.getValue());
            if (m instanceof TemplateHashModel)
            {
               rootModelObjects.add((TemplateHashModel)m);
            }
            else
            {
               throw new IllegalArgumentException("root namespace values must be convertable to " + TemplateHashModel.class.getName() +
                                                  ". converted " + entry.getValue().getClass().getName() +
                                                  " to " + m.getClass().getName() + ".");
            }
         }
//         // WCM
//         else if (qn.equals(RenderingEngineTemplateImpl.PROP_RESOURCE_RESOLVER))
//         {
//            continue;
//         }
         else
         {
            final String[] splitQName = QName.splitPrefixedQName(qn.toPrefixString());
            final String variableName = splitQName[1];

            //insert
            if (NamespaceService.DEFAULT_PREFIX.equals(splitQName[0]))
            {
               result.put(variableName, this.convertValue(entry.getValue()));
            }
            else
            {
               SimpleHash prefixModel = null;
               final String prefix = splitQName[0];
               try
               {
                  try
                  {
                     prefixModel = (SimpleHash)result.get(prefix);
                  }
                  catch (ClassCastException cce)
                  {
                     throw new ClassCastException("expected value of " + prefix +
                                                  " to be a " + SimpleHash.class.getName() + 
                                                  ".  found a " + result.get(prefix).getClass().getName());
                  }
               }
               catch (TemplateModelException tme)
               {
               }
               if (prefixModel == null)
               {
                  prefixModel = new SimpleHash();
                  result.put(prefix, prefixModel);
               }

               prefixModel.put(variableName, 
                               this.convertValue(entry.getValue()));
            }
         }
      }
      return result;
   }
            
   private TemplateModel convertValue(final Object value)
   {
      LOGGER.debug("converting value " + value);
      if (value == null)
      {
         return null;
      }
      else if (value.getClass().isArray())
      {
         final Object[] array = (Object[])value;
         LOGGER.debug("converting array of " + array.getClass().getComponentType() +
                      " size " + array.length);
         final SimpleSequence result = new SimpleSequence();
         for (int i = 0; i < array.length; i++)
         {
            result.add(this.convertValue(array[i]));
         }
         return result;
      }
      else if (value instanceof String)
      {
         return new SimpleScalar((String)value);
      }
      else if (value instanceof Number)
      {
         return new SimpleNumber((Number)value);
      }
      else if (value instanceof Boolean)
      {
         return (Boolean)value ? TemplateBooleanModel.TRUE : TemplateBooleanModel.FALSE;
      }
      else if (value instanceof Date)
      {
         return new SimpleDate(((Date)value), SimpleDate.DATETIME);
      }
      else if (value instanceof Document)
      {
         return NodeModel.wrap((Document)value);
      }
      else if (value instanceof Node)
      {
         return NodeModel.wrap((Node)value);
      }
      else if (value instanceof RenderingEngine.TemplateProcessorMethod)
      {
         return new TemplateMethodModel()
         {
            public Object exec(final List args)
               throws TemplateModelException
            {
               try
               {
                  LOGGER.debug("invoking template processor method " + value +
                               " with " + args.size() + " arguments");
                  final Object result = ((TemplateProcessorMethod)value).exec(args.toArray(new Object[args.size()]));
                  return FreeMarkerRenderingEngine.this.convertValue(result);
               }
               catch (Exception e)
               {
                  throw new TemplateModelException(e);
               }
            }
         };
      }
      else
      {
         throw new IllegalArgumentException("unable to convert value " + value.getClass().getName());
      }
   }
}
