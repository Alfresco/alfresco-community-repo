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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.parsing.AliasDefinition;
import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.beans.factory.parsing.DefaultsDefinition;
import org.springframework.beans.factory.parsing.ImportDefinition;
import org.springframework.beans.factory.parsing.ReaderEventListener;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.star.io.IOException;

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
//      "classpath:alfresco/minimal-context.xml" 
//      "classpath:test/alfresco/fake-context/application-context.xml" 
   };
   private String[] configLocations;
   
   public ContextDependencyLister(String[] configLocations) {
      this.configLocations = configLocations;
   }
   public ContextDependencyLister() {
      this(DEFAULT_CONFIG_LOCATIONS);
   }
   
   public static void main(String[] args) throws Exception {
      ContextDependencyLister lister;
      
      boolean beanLevel = false;
      boolean graphViz = false;
      if(args.length > 0 && "-graphviz".equals(args[0])) {
         graphViz = true;
         lister = new ContextDependencyLister();
      } else if(args.length > 0) {
         beanLevel = true;
         lister = new ContextDependencyLister(args);
      } else {
         lister = new ContextDependencyLister();
      }

      if(graphViz) {
         lister.graphVizDependencies("/tmp/context.dot");
      } else {
         lister.printDependencies(beanLevel);
      }
   }
   
   private BeanTrackingApplicationContext instance;
   private Hashtable<String, List<String>> contextBeans;
   private Hashtable<String, List<String>> beanDependencies;
   private Hashtable<String, List<String>> beanDependedOnBy;
   private Hashtable<String, Set<String>> contextDependsOn;
   private Hashtable<String, Set<String>> contextDependedOnBy;
   
   private void calculateDependencies() {
      long startTime = System.currentTimeMillis();
      instance = new BeanTrackingApplicationContext(configLocations);
      long endTime = System.currentTimeMillis();
      NumberFormat df = NumberFormat.getNumberInstance();
      
      System.out.println();
      System.out.println("Loading complete");
      System.out.println("  Took " + df.format( (endTime-startTime) / 1000 ) + " seconds");
      System.out.println();
      
      // We have a list of where all the beans come from
      // Invert that so we have a list of all the beans for a given context
      contextBeans = new Hashtable<String, List<String>>();
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
      beanDependencies = new Hashtable<String, List<String>>();
      beanDependedOnBy = new Hashtable<String, List<String>>();
      for(String parentBean : instance.btl.beanAllDependencies.keySet()) {
         String parentContext = instance.btl.beanSource.get(parentBean);
         if(parentContext == null) {
            System.err.println("Warning - I don't know where defined bean " + parentBean + " came from!");
            continue;
         }
         
         for(String depBean : instance.btl.beanAllDependencies.get(parentBean)) {
            // Handle bean aliases
            // Note that aliases can be nested!
            while(instance.btl.aliases.containsKey(depBean)) {
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
      contextDependsOn = new Hashtable<String, Set<String>>();
      contextDependedOnBy = new Hashtable<String, Set<String>>();
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
   }

   /**
    * Prints out the dependencies in text format
    */
   public void printDependencies(boolean beanLevel) {
      calculateDependencies();
      
      // Print out the details for each context
      for(String context : instance.btl.usedContextFiles) {
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
   
   /**
    * Renders the dependencies as GraphViz DotXML
    */
   public void graphVizDependencies(String outFile) throws IOException, FileNotFoundException {
      calculateDependencies();
      
      // We need to know one bean from each context that we'll output
      // This is because we can't do context<->context links, we have
      //  to do bean<->bean
      HashMap<String, String> linkBean = new HashMap<String, String>();
      for(String context : instance.btl.usedContextFiles) {
         if(contextBeans.containsKey(context)) {
            for(String ourBean : contextBeans.get(context)) {
               if(beanDependencies.containsKey(ourBean) || beanDependedOnBy.containsKey(ourBean)) {
                  if(! linkBean.containsKey(context)) {
                     linkBean.put(context, ourBean);
                  }
               }
            }
         }
      }
      
      // Off we go
      PrintWriter out = new PrintWriter(new OutputStreamWriter(
            new FileOutputStream(outFile)));
      
      out.println("digraph cluster {");
      out.println(" nodesep = \"0.2\";");
      out.println(" ranksep = \"0.3\";");
      out.println(" compound = \"true\";");
      out.println(" rankdir = \"LR\";");
      
      StringBuffer beanLinks = new StringBuffer();
      StringBuffer contextLinks = new StringBuffer();
      
      for(String context : instance.btl.usedContextFiles) {
         String contextId = makeGraphVizContextId(context);
         String contextLabel = makeGraphVizLabel(context);
         out.println(" subgraph "+contextId+" {");
         out.println("  label = \"" + contextLabel + "\";");
         out.println("  style = filled;"); 
         out.println("  color = \"azure3\";"); 
         out.println("  fontcolor = \"coral3\";"); 
         out.println("  fontname = \"Arial\";"); 
         out.println("  fontsize = \"15\";"); 
         //out.println("<cluster id=\""+contextId+"\" label=\""+context+"\" style=\"filled\" " +
         //		"fillcolor=\"#EEEEFF\" fontcolor=\"#900000\" fontname=\"Arial\" fontsize=\"15\">");
         
         // Only ever print our depends on
         // Depended on by gets done on another pass

         // Other context files
         if(contextDependsOn.containsKey(context)) {
            String ourBean1Id = makeGraphVizNodeId(
                  linkBean.get(context)
            );
            
            for(String ctx : contextDependsOn.get(context)) {
               // We have to draw bean->bean links for this, as
               // graphviz won't do cluster->cluster links
               String theirBean1Id = makeGraphVizNodeId(
                     linkBean.get(ctx)
               );
               String theirId = makeGraphVizContextId(ctx);
               
               contextLinks.append(" " + ourBean1Id + " -> " + theirBean1Id + 
                     " [color=\"coral3\", ltail=" + contextId + ", lhead=" + theirId + "];");
               contextLinks.append('\n');
            }
         }
         
         // Beans
         if(contextBeans.containsKey(context)) {
            // Grab the list
            ArrayList<String> displayBeans = new ArrayList<String>();
            for(String ourBean : contextBeans.get(context)) {
               if(beanDependencies.containsKey(ourBean) || beanDependedOnBy.containsKey(ourBean)) {
                  displayBeans.add(ourBean);
               }
            }
            
            if(! displayBeans.isEmpty()) {
               for(String ourBean : displayBeans) {
                  String nodeId = makeGraphVizNodeId(ourBean);
                  
                  out.println("    " + nodeId + " [label=\"" + ourBean + "\",fontsize=9];");
                  
                  // Only outbound links
                  if(beanDependencies.containsKey(ourBean)) {
                     for(String depBean : beanDependencies.get(ourBean)) {
                        String otherId = makeGraphVizNodeId(depBean);
                        beanLinks.append(" " + nodeId + " -> " + otherId + " [color=\"deepskyblue2\"];");
                        beanLinks.append('\n');
                     }
                  }
               }
            }
         }
         
         out.println(" }");
         
         out.println();
      }
      
      out.print(contextLinks.toString());
      out.println();
      out.print(beanLinks.toString());
      
      out.println("}");
      out.close();
   }
   private String makeGraphVizContextId(String thing) {
      return "cluster" + makeGraphVizId(thing);
   }
   private String makeGraphVizNodeId(String thing) {
      return "n" + makeGraphVizId(thing);
   }
   private String makeGraphVizId(String thing) {
      return Integer.toHexString(thing.hashCode());
   }
   /**
    * Need to break over lines or the boxes end up being
    *  much too big to look nice
    */
   private String makeGraphVizLabel(String thing) {
      if(thing.length() < 45) {
         return thing;
      }
      int minSplit = 20;
      int splitAt = thing.indexOf('/');
      if(splitAt > -1 && splitAt < minSplit) {
         int newSplit = thing.substring(minSplit).indexOf('/');
         if(newSplit > -1) {
            splitAt = minSplit + newSplit;
         } else {
            splitAt = -1;
         }
      }
      if(splitAt == -1 || splitAt > 45) {
         splitAt = 44;
      }
      splitAt++;
      String partA = thing.substring(0, splitAt);
      String partB = makeGraphVizLabel( thing.substring(splitAt) );
      return partA + "\\n" + partB;
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
      // Track the files in order of starting and finishing importing
      private ArrayList<String> usedContextFiles = new ArrayList<String>(); // Use order
      private ArrayList<String> importedContextFiles = new ArrayList<String>(); // Finish order
      
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
            processBeanDefinition(name, bd);
         }
      }
      
      private void processBeanDefinition(String beanName, BeanDefinition bd) {
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
            
            beanSource.put(beanName, source);
            
            // Record this context file if not already seen
            if(! usedContextFiles.contains(source)) {
               usedContextFiles.add(source);
            }
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
         
         // Check for a parent - we'll depend on that too!
         if(bd.getParentName() != null) {
            addRefIfNeeded(bd.getParentName(), refs);
         }
         
         // Record the dependencies if we found any
         if(! refs.isEmpty()) {
            beanAllDependencies.put(beanName, refs);
         }
      }
      
      private void addRefIfNeeded(String beanName, List<String> refs) {
         if(beanName == null) return;
         
         if(beanName.length() > 0) {
            if(beanName.charAt(0) == '&') {
               // Crazy factory bean stuff
               beanName = beanName.substring(1);
            }
            refs.add(beanName);
         }
      }
      private void addRefIfNeeded(Object v, List<String> refs) {
         if(v == null) return;
         
         if(v instanceof RuntimeBeanReference) {
            RuntimeBeanReference r = (RuntimeBeanReference)v;
            String name = r.getBeanName();
            
            addRefIfNeeded(name, refs);
            
            if(name == null || name.length() == 0) {
               System.err.println("Warning - empty reference " + r);
            }
         } else if(v instanceof BeanDefinitionHolder) {
            // Nested bean definition
            BeanDefinitionHolder bdh = (BeanDefinitionHolder)v;
            processBeanDefinition(bdh.getBeanName(), bdh.getBeanDefinition());
         } else if(v instanceof ManagedList<?>) {
            ManagedList<?> ml = (ManagedList<?>)v;
            for(Object le : ml) {
               addRefIfNeeded(le, refs);
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
         
         // Store
         if(! usedContextFiles.contains(context)) {
            usedContextFiles.add( context );
         }
         importedContextFiles.add( context );
      }
   }
}
