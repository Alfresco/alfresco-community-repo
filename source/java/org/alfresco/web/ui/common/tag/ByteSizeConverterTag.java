/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
