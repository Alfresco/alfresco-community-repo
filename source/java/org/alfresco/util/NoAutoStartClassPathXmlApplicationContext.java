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
package org.alfresco.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParserDelegate;
import org.springframework.beans.factory.xml.DefaultBeanDefinitionDocumentReader;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlReaderContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.w3c.dom.Element;

/**
 * A wrapper around {@link ClassPathXmlApplicationContext} which 
 *  stops Alfresco Subsystem (abstractPropertyBackedBean based)
 *  beans from being AutoStarted by tweaking their property definitions.
 * You shouldn't do this in production, but it can be handy with
 *  unit tests, as it allows a quicker startup by preventing
 *  subsystems from starting up
 *  
 * @author Nick Burch
 */
public class NoAutoStartClassPathXmlApplicationContext extends
      ClassPathXmlApplicationContext {
   
   public NoAutoStartClassPathXmlApplicationContext(String[] configLocations)
         throws BeansException {
      super(configLocations);
   }

   protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
      super.initBeanDefinitionReader(reader);
      
      postInitBeanDefinitionReader(reader);
   }
   
   /**
    * Does the work of disabling the autostart of the
    *  Subsystem (abstractPropertyBackedBean) beans
    *  on the xml bean reader
    */
   protected static void postInitBeanDefinitionReader(XmlBeanDefinitionReader reader) {
      reader.setDocumentReaderClass(NoAutoStartBeanDefinitionDocumentReader.class);
   }

   protected static class NoAutoStartBeanDefinitionDocumentReader extends DefaultBeanDefinitionDocumentReader {
      @Override
      protected BeanDefinitionParserDelegate createHelper(
            XmlReaderContext readerContext, Element root) {
         BeanDefinitionParserDelegate delegate = new NoAutoStartBeanDefinitionParserDelegate(readerContext);
         delegate.initDefaults(root);
         return delegate;
      }
   }
   
   protected static class NoAutoStartBeanDefinitionParserDelegate extends BeanDefinitionParserDelegate {
      protected NoAutoStartBeanDefinitionParserDelegate(XmlReaderContext readerContext) {
         super(readerContext);
      }
      
      @Override  
      public void parsePropertyElement(Element ele, BeanDefinition bd) {
         String propertyName = ele.getAttribute("name");
         if("autoStart".equals(propertyName)) {
            if("abstractPropertyBackedBean".equals(bd.getParentName())) {
               String id = ele.getParentNode().getAttributes().getNamedItem("id").getTextContent();
               System.out.println("Preventing the autostart of Subsystem " + id);
               return;
            }
         }
         
         super.parsePropertyElement(ele, bd);
      }
   }
}
