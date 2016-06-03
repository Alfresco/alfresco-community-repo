package org.alfresco.web.ui.common.tag;

import javax.faces.convert.Converter;
import javax.faces.webapp.ConverterTag;
import javax.servlet.jsp.JspException;

import org.alfresco.web.ui.common.converter.BooleanLabelConverter;

/**
 * Allows the BooleanLabelConverter component to be used on JSP pages
 * 
 * @author gavinc
 */
public class BooleanLabelConverterTag extends ConverterTag
{
   /**
    * Default Constructor
    */
   public BooleanLabelConverterTag()
   {
      setConverterId(BooleanLabelConverter.CONVERTER_ID);
   }

   /**
    * @see javax.faces.webapp.ConverterTag#createConverter()
    */
   protected Converter createConverter() throws JspException
   {
      return (BooleanLabelConverter)super.createConverter();
   }
}
