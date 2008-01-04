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
 * http://www.alfresco.com/legal/licensing
 */
package org.alfresco.web.bean.ajax;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * Fast and simple JSON stream writer. Wraps a Writer to output a JSON object stream.
 * No intermediate objects are created - writes are immediate to the underlying stream.
 * 
 * @author Kevin Roast
 */
public class JSONWriter
{
   private Writer out;
   private Stack<Boolean> stack = new Stack<Boolean>();
   
   public JSONWriter(Writer out)
   {
      this.out = out;
      stack.push(Boolean.FALSE);
   }
   
   public void startArray() throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write("[");
      stack.pop();
      stack.push(Boolean.TRUE);
      stack.push(Boolean.FALSE);
   }
   
   public void endArray() throws IOException
   {
      out.write("]");
      stack.pop();
   }
   
   public void startObject() throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write("{");
      stack.pop();
      stack.push(Boolean.TRUE);
      stack.push(Boolean.FALSE);
   }
   
   public void endObject() throws IOException
   {
      out.write("}");
      stack.pop();
   }
   
   public void startValue(String name) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(name);
      out.write(": ");
      stack.pop();
      stack.push(Boolean.TRUE);
      stack.push(Boolean.FALSE);
   }
   
   public void endValue()
   {
      stack.pop();
   }
   
   public void writeValue(String name, String value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(name);
      out.write(": \"");
      out.write(value);
      out.write('"');
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeValue(String name, int value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(name);
      out.write(": ");
      out.write(Integer.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeValue(String name, boolean value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(name);
      out.write(": ");
      out.write(Boolean.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeNullValue(String name) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(name);
      out.write(": null");
      stack.pop();
      stack.push(Boolean.TRUE);
   }
}