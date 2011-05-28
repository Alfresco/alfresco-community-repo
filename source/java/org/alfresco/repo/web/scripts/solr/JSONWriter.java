package org.alfresco.repo.web.scripts.solr;

import java.io.IOException;
import java.io.Writer;
import java.util.Stack;

/**
 * Fast and simple JSON stream writer. Wraps a Writer to output a JSON object stream.
 * No intermediate objects are created - writes are immediate to the underlying stream.
 * Quoted and correct JSON encoding is performed on string values, - encoding is
 * not performed on key names - it is assumed they are simple strings. The developer must
 * call JSONWriter.encodeJSONString() on the key name if required.
 * 
 * @author Kevin Roast
 * @author Steve Glover
 *
 * Adapted from org.springframework.extensions.webscripts.json.
 *   - added writeValue methods for class versions of primitives
 */
public final class JSONWriter
{
   private Writer out;
   private Stack<Boolean> stack = new Stack<Boolean>();
   
   /**
    * Constructor
    * 
    * @param out    The Writer to immediately append values to (no internal buffering)
    */
   public JSONWriter(Writer out)
   {
      this.out = out;
      stack.push(Boolean.FALSE);
   }
   
   /**
    * Start an array structure, the endArray() method must be called later.
    * NOTE: Within the array, either output objects or use the single arg writeValue() method.
    */
   public void startArray() throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write("[");
      stack.pop();
      stack.push(Boolean.TRUE);
      stack.push(Boolean.FALSE);
   }
   
   /**
    * End an array structure.
    */
   public void endArray() throws IOException
   {
      out.write("]");
      stack.pop();
   }
   
   /**
    * Start an object structure, the endObject() method must be called later.
    */
   public void startObject() throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write("{");
      stack.pop();
      stack.push(Boolean.TRUE);
      stack.push(Boolean.FALSE);
   }
   
   /**
    * End an object structure.
    */
   public void endObject() throws IOException
   {
      out.write("}");
      stack.pop();
   }
   
   /**
    * Start a value (outputs just a name key), the endValue() method must be called later.
    * NOTE: follow with an array or object only.
    */
   public void startValue(String name) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      stack.pop();
      stack.push(Boolean.TRUE);
      stack.push(Boolean.FALSE);
   }
   
   /**
    * End a value that was started with startValue()
    */
   public void endValue()
   {
      stack.pop();
   }
   
   /**
    * Output a JSON string name and value pair.
    */
   public void writeValue(String name, String value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": \"");
      out.write(encodeJSONString(value));
      out.write('"');
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON number name and value pair.
    */
   public void writeValue(String name, int value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      out.write(Integer.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeValue(String name, Integer value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON number name and value pair.
    */
   public void writeValue(String name, float value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      out.write(Float.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }

   public void writeValue(String name, Float value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }

   
   /**
    * Output a JSON boolean name and value pair.
    */
   public void writeValue(String name, boolean value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      out.write(Boolean.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeValue(String name, Boolean value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON string value.
    * NOTE: no name is written - call from within an array structure.
    */
   public void writeValue(String value) throws IOException
   {
      if (stack.peek() == true) out.write(",");
      out.write('"');
      out.write(encodeJSONString(value));
      out.write('"');
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON number value.
    * NOTE: no name is written - call from within an array structure. 
    */
   public void writeValue(int value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(Integer.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeValue(Integer value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON long value.
    * NOTE: no name is written - call from within an array structure. 
    */
   public void writeValue(long value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(Long.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }

   public void writeValue(Long value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }

   
   /**
    * Output a JSON number value.
    * NOTE: no name is written - call from within an array structure. 
    */
   public void writeValue(float value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(Float.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   public void writeValue(Float value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON boolean value.
    * NOTE: no name is written - call from within an array structure.
    */
   public void writeValue(boolean value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(Boolean.toString(value));
      stack.pop();
      stack.push(Boolean.TRUE);
   }

   public void writeValue(Boolean value) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write(value.toString());
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON null value.
    * NOTE: no name is written - call from within an array structure.
    */
   public void writeNullValue() throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write("null");
      stack.pop();
      stack.push(Boolean.TRUE);
   }
   
   /**
    * Output a JSON null value.
    */
   public void writeNullValue(String name) throws IOException
   {
      if (stack.peek() == true) out.write(", ");
      out.write('"');
      out.write(name);
      out.write("\": null");
      stack.pop();
      stack.push(Boolean.TRUE);
   }

   
   /**
    * Safely encode a JSON string value.
    * @return encoded string, null is handled and returned as "".
    */
   public static String encodeJSONString(final String s)
   {
       if (s == null || s.length() == 0)
       {
           return "";
       }
       
       StringBuilder sb = null;      // create on demand
       String enc;
       char c;
       int len = s.length();
       for (int i = 0; i < len; i++)
       {
           enc = null;
           c = s.charAt(i);
           switch (c)
           {
               case '\\':
                   enc = "\\\\";
                   break;
               case '"':
                   enc = "\\\"";
                   break;
               case '/':
                   enc = "\\/";
                   break;
               case '\b':
                   enc = "\\b";
                   break;
               case '\t':
                   enc = "\\t";
                   break;
               case '\n':
                   enc = "\\n";
                   break;
               case '\f':
                   enc = "\\f";
                   break;
               case '\r':
                   enc = "\\r";
                   break;

               default:
                   if (((int)c) >= 0x80)
                   {
                       //encode all non basic latin characters
                       String u = "000" + Integer.toHexString((int)c);
                       enc = "\\u" + u.substring(u.length() - 4);

                   }
               break;
           }

           if (enc != null)
           {
               if (sb == null)
               {
                   String soFar = s.substring(0, i);
                   sb = new StringBuilder(i + 8);
                   sb.append(soFar);
               }
               sb.append(enc);
           }
           else
           {
               if (sb != null)
               {
                   sb.append(c);
               }
           }
       }

       if (sb == null)
       {
           return s;
       }
       else
       {
           return sb.toString();
       }
   }
}
