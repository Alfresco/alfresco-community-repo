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

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Helper class to list the dependencies between different 
 *  spring context files that make up a full or partial
 *  Application Context.
 * Useful when trying to produce paired-down application
 *  contexts for unit testing or embedding. 
 * 
 * @author Nick Burch
 */
public class ContextDependencyLister
{
   public static final String[] DEFAULT_CONFIG_LOCATIONS = new String[] { 
      "classpath:alfresco/application-context.xml" 
//      "classpath:test/alfresco/fake-context/application-context.xml" 
   };
   private String[] configLocations;
   private boolean beanLevel = false;
   
   public ContextDependencyLister(String[] configLocations, boolean beanLevel) {
      this.configLocations = configLocations;
      this.beanLevel = beanLevel;
   }
   public ContextDependencyLister() {
      this(DEFAULT_CONFIG_LOCATIONS, false);
   }
   
   public static void main(String[] args) throws Exception {
      ContextDependencyLister lister;
      if(args.length > 0) {
         lister = new ContextDependencyLister(args, true);
      } else {
         lister = new ContextDependencyLister();
      }
      
      lister.printDependencies();
   }
   
   public void printDependencies() {
      BeanTrackingApplicationContext instance = 
         new BeanTrackingApplicationContext(configLocations);
      
      System.out.println();
      System.out.println("Loading complete");
      System.out.println();
      
      // We have a list of where all the beans come from
      // Invert that so we have a list of all the beans for a given context
      Hashtable<String, List<String>> contextBeans = new Hashtable<String, List<String>>();
      for(String bean : instance.btl.beanSource.keySet()) {
         String context = instance.btl.beanSource.get(bean);
         if(! contextBeans.containsKey(context)) {
            contextBeans.put(context, new ArrayList<String>());
         }
         contextBeans.get(context).add(bean);
      }
      
      // Filter the list of which beans depend on which beans to only
      //  hold the external dependencies
      // At the same time, generate the inverted list too
      Hashtable<String, List<String>> beanDependencies = new Hashtable<String, List<String>>();
      Hashtable<String, List<String>> beanDependedOnBy = new Hashtable<String, List<String>>();
      for(String parentBean : instance.btl.beanAllDependencies.keySet()) {
         String parentContext = instance.btl.beanSource.get(parentBean);
         if(parentContext == null) {
            System.err.println("Warning - I don't know where defined bean " + parentBean + " came from!");
            continue;
         }
         
         for(String depBean : instance.btl.beanAllDependencies.get(parentBean)) {
            // Handle bean aliases
            if(instance.btl.aliases.containsKey(depBean)) {
               depBean = instance.btl.aliases.get(depBean);
            }
            
            // Grab the context for the bean
            String depContext =  instance.btl.beanSource.get(depBean);
            
            if(depContext == null) {
               System.err.println("Warning - I don't know where depdendency " + depBean + " came from!");
            } else if(depContext.equals(parentContext)) {
               // The bean it depends on is in the same file as the bean itself
               // Don't need to worry about it
            } else {
               // It's an external dependency
               
               // Store the forward lookup
               if(! beanDependencies.containsKey(parentBean)) {
                  beanDependencies.put(parentBean, new ArrayList<String>());
               }
               beanDependencies.get(parentBean).add(depBean);
               
               // Store the reverse lookup
               if(! beanDependedOnBy.containsKey(depBean)) {
                  beanDependedOnBy.put(depBean, new ArrayList<String>());
               }
               beanDependedOnBy.get(depBean).add(parentBean);
            }
         }
      }
      
      // Now move up to the context level
      // Does this by looking up bean<->bean dependencies and resolving to contexts
      Hashtable<String, Set<String>> contextDependsOn = new Hashtable<String, Set<String>>();
      Hashtable<String, Set<String>> contextDependedOnBy = new Hashtable<String, Set<String>>();
      for(String parentBean : beanDependencies.keySet()) {
         String parentContext = instance.btl.beanSource.get(parentBean);
         
         for(String depBean : beanDependencies.get(parentBean)) {
            String depContext =  instance.btl.beanSource.get(depBean);
            
            // Record at the context level
            if(! contextDependsOn.containsKey(parentContext)) {
               contextDependsOn.put(parentContext, new HashSet<String>());
            }
            contextDependsOn.get(parentContext).add(depContext);
         }
      }
      for(String depBean : beanDependedOnBy.keySet()) {
         String depContext =  instance.btl.beanSource.get(depBean);
               
         for(String parentBean : beanDependedOnBy.get(depBean)) {
            String parentContext = instance.btl.beanSource.get(parentBean);
            
            // Record at the context level
            if(! contextDependedOnBy.containsKey(depContext)) {
               contextDependedOnBy.put(depContext, new HashSet<String>());
            }
            contextDependedOnBy.get(depContext).add(parentContext);
         }
      }
      
      
      // Print out the details for each context
      for(String context : instance.btl.contextFiles) {
         System.out.println(context);
         
         // Print which contexts this depends on
         if(contextDependsOn.containsKey(context)) {
            System.out.println("  Depends on:");
            for(String ctx : contextDependsOn.get(context)) {
               System.out.println("    " + ctx);
            }
         } else {
            System.out.println("  (Doesn't depend on anything)");
         }
         
         // Print which contexts depend on this one
         if(contextDependedOnBy.containsKey(context)) {
            System.out.println("  Depended on by:");
            for(String ctx : contextDependedOnBy.get(context)) {
               System.out.println("    " + ctx);
            }
         } else {
            System.out.println("  (Nothing depends on this)");
         }
         
         // Beans if requested
         if(beanLevel && contextBeans.containsKey(context)) {
            // Our beans with external dependencies 
            boolean hasExtDeps = false;
            for(String ourBean : contextBeans.get(context)) {
               if(beanDependencies.containsKey(ourBean)) {
                  ArrayList<String> extDep = new ArrayList<String>();
                  for(String depBean : beanDependencies.get(ourBean)) {
                     String depContext =  instance.btl.beanSource.get(depBean);
                     
                     if(! depContext.equals(context)) {
                        extDep.add(depBean + " <= " + depContext);
                     }
                  }
                  
                  if(extDep.size() > 0) {
                     if(! hasExtDeps) {
                        System.out.println("  Beans with extenal dependencies:");
                        hasExtDeps = true;
                     }
                     System.out.println("    " + ourBean);
                     for(String depBean : extDep) {
                        System.out.println("      " + depBean);
                     }
                  }
               }
            }
            
            // Our beans which others depend on
            boolean hasInboundDeps = false;
            for(String ourBean : contextBeans.get(context)) {
               if(beanDependedOnBy.containsKey(ourBean)) {
                  ArrayList<String> inbDep = new ArrayList<String>();
                  for(String parentBean : beanDependedOnBy.get(ourBean)) {
                     String parentContext =  instance.btl.beanSource.get(parentBean);
                     
                     if(! parentContext.equals(context)) {
                        inbDep.add(parentBean + " <= " + parentContext);
                     }
                  }
                  
                  if(inbDep.size() > 0) {
                     if(! hasInboundDeps) {
                        System.out.println("  Beans others dependend on:");
                        hasInboundDeps = true;
                     }
                     System.out.println("    " + ourBean);
                     for(String parentBean : inbDep) {
                        System.out.println("      " + parentBean);
                     }
                  }
               }
            }
         }
         
         System.out.println();
      }
   }
   
   
   public static class BeanTrackingApplicationContext extends ClassPathXmlApplicationContext {
      private BeanTrackingListener btl;
      
      public BeanTrackingApplicationContext(String[] locations) {
         super(locations);
      }
      
      protected void initBeanDefinitionReader(XmlBeanDefinitionReader reader) {
         super.initBeanDefinitionReader(reader);
      
         btl = new BeanTrackingListener();
         reader.setEventListener(btl);
         // Work around a spring bug where setting the listener does nothing 
         try {
            Field f = reader.getClass().getDeclaredField("eventListener");
            f.setAccessible(true);
            f.set(reader, btl);
         } catch(Exception e) {
            throw new RuntimeException(e);
         }
      }
   }
   
   
   public static class BeanTrackingListener implements ReaderEventListener {
      // So we can track them in order
      private ArrayList<String> contextFiles = new ArrayList<String>();
      // Which beans come from where
      // In the case of overriden beans, track the last place
      private Hashtable<String, String> beanSource = new Hashtable<String, String>();
      // Which beans are depended on by which others
      private Hashtable<String, List<String>> beanAllDependencies = new Hashtable<String, List<String>>();
      
      // Our aliases
      private Hashtable<String, String> aliases = new Hashtable<String, String>();
      
      // Path+URI to / on the classpath
      private String pathToSlash;
      private String uriToSlash;
      
      public void aliasRegistered(AliasDefinition alias) {
         if(! aliases.containsKey(alias.getAlias())) {
            aliases.put(alias.getAlias(), alias.getBeanName());
         }
      }

      public void componentRegistered(
            ComponentDefinition paramComponentDefinition) {
         String name = paramComponentDefinition.getName();
         
         for(BeanDefinition bd : paramComponentDefinition.getBeanDefinitions()) {
            // Where does it come from?
            String resource = bd.getResourceDescription();
            int openSB = resource.indexOf('['); 
            int closeSB = resource.indexOf(']');
            if(openSB > -1 && closeSB > -1 && openSB < closeSB) {
               String source = resource.substring(openSB+1, closeSB);
               
               if(pathToSlash != null) {
                  if(source.startsWith(pathToSlash)) {
                     source = source.replace(pathToSlash, "");
                  }
                  if(source.startsWith(uriToSlash)) {
                     source = source.replace(uriToSlash, "");
                  }
               }
               
               beanSource.put(name, source);
            } else {
               System.err.println("Unknown resource location:\n\t" + resource);
            }
            
            // What (if anything) does it reference?
            List<String> refs = new ArrayList<String>();
            
            if(! bd.getConstructorArgumentValues().isEmpty()) {
               for(ValueHolder vh : bd.getConstructorArgumentValues().getGenericArgumentValues()) {
                  Object v = vh.getValue();
                  addRefIfNeeded(v, refs);
               }
               for(ValueHolder vh : bd.getConstructorArgumentValues().getIndexedArgumentValues().values()) {
                  Object v = vh.getValue();
                  addRefIfNeeded(v, refs);
               }
            }
            if(! bd.getPropertyValues().isEmpty()) {
               for(PropertyValue pv : bd.getPropertyValues().getPropertyValueList()) {
                  Object v = pv.getValue();
                  addRefIfNeeded(v, refs);
               }
            }
            
            if(! refs.isEmpty()) {
               beanAllDependencies.put(name, refs);
            }
         }
      }
      
      private void addRefIfNeeded(Object v, List<String> refs) {
         if(v == null) return;
         
         if(v instanceof RuntimeBeanReference) {
            RuntimeBeanReference r = (RuntimeBeanReference)v;
            String name = r.getBeanName();
            
            if(name.length() > 0) {
               if(name.charAt(0) == '&') {
                  // Crazy factory bean stuff
                  name = name.substring(1);
               }
               refs.add(name);
            } else {
               System.err.println("Warning - empty reference " + r);
            }
//       } else {
//          System.err.println(v.getClass());
         }
      }

      public void defaultsRegistered(DefaultsDefinition paramDefaultsDefinition) {}

      public void importProcessed(ImportDefinition paramImportDefinition) {
         String context = paramImportDefinition.getImportedResource();

         if(context.startsWith("classpath:")) {
            context = context.substring(10);
            
            if(pathToSlash == null) {
               URL c = ContextDependencyLister.class.getClassLoader().getResource(context);
               uriToSlash = c.toString().replace(context, "");
               pathToSlash = c.getPath().replace(context, "");
            }
         }
         if(context.startsWith("classpath*:")) {
            context = context.substring(11);
         }
         
         contextFiles.add( context );
      }
   }
}
