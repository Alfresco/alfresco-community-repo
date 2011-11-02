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
package org.alfresco.web.data;

import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Sort
 * 
 * Base sorting helper supports locale specific case sensitive, case in-sensitive and
 * numeric data sorting.
 * 
 * @author Kevin Roast
 */
public abstract class Sort
{
   // ------------------------------------------------------------------------------
   // Construction
   
   /**
    * Constructor
    * 
    * @param data             a the List of String[] data to sort
    * @param column           the column getter method to use on the row to sort
    * @param bForward         true for a forward sort, false for a reverse sort
    * @param mode             sort mode to use (see IDataContainer constants)
    */
   public Sort(List data, String column, boolean bForward, String mode)
   {
      this.data = data;
      this.column = column;
      this.bForward = bForward;
      this.sortMode = mode;
      
      if (this.data.size() != 0)
      {
         // setup the Collator for our Locale
         Collator collator = Collator.getInstance(Locale.getDefault());
         
         // set the strength according to the sort mode
         if (mode.equals(IDataContainer.SORT_CASEINSENSITIVE))
         {
            collator.setStrength(Collator.SECONDARY);
         }
         else
         {
            collator.setStrength(Collator.IDENTICAL);
         }
         
         this.keys = buildCollationKeys(collator);
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Abstract Methods
   
   /**
    * Runs the Sort routine on the current dataset
    */
   public abstract void sort();
   
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Build a list of collation keys for comparing locale sensitive strings or build
    * the appropriate objects for comparison for other standard data types.
    * 
    * @param collator      the Collator object to use to build String keys
    */
   @SuppressWarnings("unchecked")
   protected List buildCollationKeys(Collator collator)
   {
      List data = this.data;
      int iSize = data.size();
      List keys = new ArrayList(iSize);
      
      try
      {
         // create the Bean getter method invoker to retrieve the value for a colunm
         String methodName = getGetterMethodName(this.column);
         Class returnType = null;;
         Method getter = null;
         // there will always be at least one item to sort if we get to this method
         Object bean = this.data.get(0);
         try
         {
            getter = bean.getClass().getMethod(methodName, (Class [])null);
            returnType = getter.getReturnType();
         }
         catch (NoSuchMethodException nsmerr)
         {
            // no bean getter method found - try Map implementation
            if (bean instanceof Map)
            {
               Object obj = ((Map)bean).get(this.column);
               if (obj != null)
               {
                  returnType = obj.getClass();
               }
               else
               {
                  if (s_logger.isInfoEnabled())
                  {
                     s_logger.info("Unable to get return type class for RichList column: " + column +
                           ". Suggest set java type directly in sort component tag.");
                  }
                  returnType = Object.class;
               }
            }
            else
            {
               throw new IllegalStateException("Unable to find bean getter or Map impl for column name: " + this.column);
            }
         }
         
         // create appropriate comparator instance based on data type
         // using the strategy pattern so  sub-classes of Sort simply invoke the
         // compare() method on the comparator interface - no type info required
         boolean bknownType = true;
         if (returnType.equals(String.class))
         {
            if (strongStringCompare == true)
            {
               this.comparator = new StringComparator();
            }
            else
            {
               this.comparator = new SimpleStringComparator();
            }
         }
         else if (returnType.equals(Date.class))
         {
            this.comparator = new DateComparator();
         }
         else if (returnType.equals(boolean.class) || returnType.equals(Boolean.class))
         {
            this.comparator = new BooleanComparator();
         }
         else if (returnType.equals(int.class) || returnType.equals(Integer.class))
         {
            this.comparator = new IntegerComparator();
         }
         else if (returnType.equals(long.class) || returnType.equals(Long.class))
         {
            this.comparator = new LongComparator();
         }
         else if (returnType.equals(float.class) || returnType.equals(Float.class))
         {
            this.comparator = new FloatComparator();
         }
         else if (returnType.equals(Timestamp.class))
         {
            this.comparator = new TimestampComparator();
         }
         else if (DynamicResolver.class.isAssignableFrom(returnType))
         {
            this.comparator = new SimpleStringComparator();
         }
         else
         {
            if (s_logger.isDebugEnabled())
                s_logger.debug("Unsupported sort data type: " + returnType + " defaulting to .toString()");
            this.comparator = new SimpleComparator();
            bknownType = false;
         }
         
         // create a collation key for each required column item in the dataset
         for (int iIndex=0; iIndex<iSize; iIndex++)
         {
            Object obj;
            if (getter != null)
            {
               // if we have a bean getter method impl use that
               try
               {
                  getter.setAccessible(true);
               }
               catch (SecurityException se)
               {
               }
               obj = getter.invoke(data.get(iIndex), (Object [])null);
            }
            else
            {
               // else we must have a bean Map impl
               obj = ((Map)data.get(iIndex)).get(column);
            }
            
            if (obj instanceof String)
            {
               String str = (String)obj;
               if (strongStringCompare == true)
               {
                  if (str.indexOf(' ') != -1)
                  {
                     // quote white space characters or they will be ignored by the Collator!
                     int iLength = str.length();
                     StringBuilder s = new StringBuilder(iLength + 4);
                     char c;
                     for (int i=0; i<iLength; i++)
                     {
                        c = str.charAt(i);
                        if (c != ' ')
                        {
                           s.append(c);
                        }
                        else
                        {
                           s.append('\'').append(c).append('\'');
                        }
                     }
                     str = s.toString();
                  }
                  keys.add(collator.getCollationKey(str));
               }
               else
               {
                  keys.add(str);
               }
            }
            else if (bknownType == true)
            {
               // the appropriate wrapper object will be create by the reflection
               // system to wrap primative types e.g. int and boolean.
               // therefore the correct type will be ready for use by the comparator
               keys.add(obj);
            }
            else
            {
               if (obj != null)
               {
                  keys.add(obj.toString());
               }
               else
               {
                  keys.add(null);
               }
            }
         }
      }
      catch (Exception err)
      {
         throw new RuntimeException(err);
      }
      
      return keys;
   }
   
   /**
    * Given the array and two indices, swap the two items in the
    * array.
    */
   @SuppressWarnings("unchecked")
   protected void swap(final List v, final int a, final int b)
   {
      Object temp = v.get(a);
      v.set(a, v.get(b));
      v.set(b, temp);
   }
   
   /**
    * Return the comparator to be used during column value comparison
    * 
    * @return Comparator for the appropriate column data type
    */
   protected Comparator getComparator()
   {
      return this.comparator;
   }
   
   /**
    * Return the name of the Bean getter method for the specified getter name
    * 
    * @param name of the field to build getter method name for e.g. "value"
    * 
    * @return the name of the Bean getter method for the field name e.g. "getValue"
    */
   protected static String getGetterMethodName(String name)
   {
      return "get" + name.substring(0, 1).toUpperCase() +
             name.substring(1, name.length());
   }
   
   
   // ------------------------------------------------------------------------------
   // Inner classes for data type comparison
   
   private static class SimpleComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return (obj1.toString()).compareTo(obj2.toString());
      }
   }
   
   private static class SimpleStringComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return (obj1.toString()).compareToIgnoreCase(obj2.toString());
      }
   }
   
   private static class StringComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((CollationKey)obj1).compareTo((CollationKey)obj2);
      }
   }
   
   private static class IntegerComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((Integer)obj1).compareTo((Integer)obj2);
      }
   }
   
   private static class FloatComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((Float)obj1).compareTo((Float)obj2);
      }
   }
   
   private static class LongComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((Long)obj1).compareTo((Long)obj2);
      }
   }
   
   private static class BooleanComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((Boolean)obj1).equals((Boolean)obj2) ? -1 : 1;
      }
   }
   
   private static class DateComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((Date)obj1).compareTo((Date)obj2);
      }
   }
   
   private static class TimestampComparator implements Comparator
   {
      /**
       * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
       */
      public int compare(final Object obj1, final Object obj2)
      {
         if (obj1 == null && obj2 == null) return 0;
         if (obj1 == null) return -1;
         if (obj2 == null) return 1;
         return ((Timestamp)obj1).compareTo((Timestamp)obj2);
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Private Data
   
   /** list of Object[] data to sort */
   protected List data;
   
   /** column name to sort against */
   protected String column;
   
   /** sort direction */
   protected boolean bForward;
   
   /** sort mode (see IDataContainer constants) */
   protected String sortMode;
   
   /** locale sensitive collator */
   protected Collator collator;
   
   /** collation keys for comparisons */
   protected List keys = null;
   
   /** the comparator instance to use for comparing values when sorting */
   private Comparator comparator = null;
   
   // TODO: make this configurable
   /** config value whether to use strong collation Key string comparisons */
   private boolean strongStringCompare = true;
   
   private static Log    s_logger = LogFactory.getLog(IDataContainer.class);
   
} // end class Sort
