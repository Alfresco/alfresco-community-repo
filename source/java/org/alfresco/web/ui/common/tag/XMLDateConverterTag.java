package org.alfresco.web.ui.common.tag;

import javax.servlet.jsp.PageContext;

import org.alfresco.web.ui.common.converter.XMLDateConverter;
import org.apache.myfaces.taglib.core.ConvertDateTimeTag;

/**
 * Tag definition to use the XMLDateConverter on a page 
 *  
 * @author gavinc
 */
public class XMLDateConverterTag extends ConvertDateTimeTag
{
   public void setPageContext(PageContext context) 
   {
      super.setPageContext(context);
      setConverterId(XMLDateConverter.CONVERTER_ID);
   }
}
