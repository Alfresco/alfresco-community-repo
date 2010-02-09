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
 *  stops abstractPropertyBackedBean based beans from being
 *  AutoStarted by tweaking their property definitions.
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
               System.out.println("Preventing the autostart of " + bd.getBeanClassName());
               return;
            }
         }
         
         super.parsePropertyElement(ele, bd);
      }
   }
}
