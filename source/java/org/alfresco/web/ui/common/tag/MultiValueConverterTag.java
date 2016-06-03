package org.alfresco.web.ui.common.tag;

import javax.faces.convert.Converter;
import javax.faces.webapp.ConverterTag;
import javax.servlet.jsp.JspException;

import org.alfresco.web.ui.common.converter.MultiValueConverter;

/**
 * Allows the MultiValueConverter component to be used on JSP pages
 * 
 * @author gavinc
 */
public class MultiValueConverterTag extends ConverterTag
{
   /**
    * Default Constructor
    */
   public MultiValueConverterTag()
   {
      setConverterId(MultiValueConverter.CONVERTER_ID);
   }

   /**
    * @see javax.faces.webapp.ConverterTag#createConverter()
    */
   protected Converter createConverter() throws JspException
   {
      return (MultiValueConverter)super.createConverter();
   }
}
