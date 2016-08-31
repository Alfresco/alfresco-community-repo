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

import java.net.URL;

/**
 * Class containing debugging utility methods
 * 
 * @author gavinc
 */
public class Debug
{
   /**
    * Returns the location of the file that will be loaded for the given class name 
    * 
    * @param className The class to load
    * @return The location of the file that will be loaded
    * @throws ClassNotFoundException
    */
   public static String whichClass(String className) throws ClassNotFoundException
   {
      String path = className;
      
      // prepare the resource path
      if (path.startsWith("/") == false)
      {
         path = "/" + path;
      }
      path = path.replace('.', '/');
      path = path + ".class";
      
      // get the location
      URL url = Debug.class.getResource(path);
      if (url == null)
      {
         throw new ClassNotFoundException(className);
      }
      
      // format the result
      String location = url.toExternalForm();
      if (location.startsWith("jar"))
      {
         location = location.substring(10, location.lastIndexOf("!"));
      }
      else if (location.startsWith("file:"))
      {
         location = location.substring(6);
      }
      
      return location;
   }
   
   /**
    * Returns the class loader that will load the given class name
    * 
    * @param className The class to load
    * @return The class loader the class will be loaded in
    * @throws ClassNotFoundException
    */
   public static String whichClassLoader(String className) throws ClassNotFoundException
   {
      String result = "Could not determine class loader for " + className;
      
      Class clazz = Class.forName(className);
      ClassLoader loader = clazz.getClassLoader();
      
      if (loader != null)
      {
         result = clazz.getClassLoader().toString();
      }
      
      return result;
   }
   
   /**
    * Returns the class loader hierarchy that will load the given class name
    * 
    * @param className The class to load
    * @return The hierarchy of class loaders used to load the class
    * @throws ClassNotFoundException
    */
   public static String whichClassLoaderHierarchy(String className) throws ClassNotFoundException
   {
      StringBuffer buffer = new StringBuffer();
      Class clazz = Class.forName(className);
      ClassLoader loader = clazz.getClassLoader();
      if (loader != null)
      {
         buffer.append(loader.toString());
         
         ClassLoader parent = loader.getParent();
         while (parent != null)
         {
            buffer.append("\n-> ").append(parent.toString());
            parent = parent.getParent();
         }
      }
      else
      {
         buffer.append("Could not determine class loader for " + className);
      }
      
      return buffer.toString();
   }
}
