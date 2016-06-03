package org.alfresco.web.ui.common.tag;

import javax.faces.convert.Converter;
import javax.faces.webapp.ConverterTag;
import javax.servlet.jsp.JspException;

import org.alfresco.web.ui.common.converter.ByteSizeConverter;

/**
 * @author Kevin Roast
 */
public class ByteSizeConverterTag extends ConverterTag
{
   /**
    * Default Constructor
    */
   public ByteSizeConverterTag()
   {
      setConverterId(ByteSizeConverter.CONVERTER_ID);
   }

   /**
    * @see javax.faces.webapp.ConverterTag#createConverter()
    */
   protected Converter createConverter() throws JspException
   {
      return (ByteSizeConverter)super.createConverter();
   }
}
