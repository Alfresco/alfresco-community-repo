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
package org.alfresco.web.forms;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Encapsulation of form instance data.
 *
 * @author Ariel Backenroth
 */
public interface FormInstanceData
   extends Serializable
{

   /////////////////////////////////////////////////////////////////////////////

   public static class RegenerateResult implements Serializable
   {

      private static final long serialVersionUID = -3827878774655260635L;
      
      private final RenderingEngineTemplate ret;
      private final String path;
      private final Rendition r;
      private final Exception e;
      private final String lockOwner; // existing lock owner otherwise null

      public RegenerateResult(final RenderingEngineTemplate ret, 
              final String path,
              final Rendition r,
              final String lockOwner)
      {
         this.ret = ret;
         this.r = r;
         this.e = null;
         this.path = path;
         this.lockOwner = lockOwner;
      }
      
      public RegenerateResult(final RenderingEngineTemplate ret,
                              final String path,
                              final Exception e,
                              final String lockOwner)
      {
         this.ret = ret;
         this.e = e;
         this.r = null;
         this.path = path;
         this.lockOwner = lockOwner;
      }

      public RenderingEngineTemplate getRenderingEngineTemplate()
      {
         return this.ret;
      }
      
      public String getPath()
      {
         return this.path;
      }

      public Rendition getRendition()
      {
         return this.r;
      }

      public Exception getException()
      {
         return this.e;
      }
      
      public String getLockOwner()
      {
          return this.lockOwner;
      }
   }

   /////////////////////////////////////////////////////////////////////////////

   /** the form generate this form instance data */
   public Form getForm()
      throws FormNotFoundException;

   /** the name of this instance data */
   public String getName();

   /** the path relative to the containing webapp */
   public String getWebappRelativePath();

   /** the path relative to the sandbox */
   public String getSandboxRelativePath();

   /** the path to the contents of this form instance data */
   public String getPath();

   /** the url to the asset */
   public String getUrl();

   /** returns the parsed form instance data */
   public Document getDocument()
      throws IOException, SAXException;

   /** Regenerates all renditions of this form instance data */
   public List<RegenerateResult> regenerateRenditions()
      throws FormNotFoundException;

   /** returns all renditions of this form instance data */
   public List<Rendition> getRenditions();
   
   /** returns all renditions of this form instance data (include deleted AVM nodes) */
   public List<Rendition> getRenditions(boolean includeDeleted);
}
