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
package org.alfresco.web.ui.common.renderer;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.model.SelectItem;

import org.alfresco.web.app.Application;

/**
 * @author kevinr
 * 
 * Example of a custom JSF renderer. This demonstrates how to encode/decode a set
 * of input field params that we use to generate a Date object. This object is held
 * in our component and the renderer will output it to the page.
 */
public class DatePickerRenderer extends BaseRenderer
{
   private static final String FIELD_YEAR = "_year";
   private static final String FIELD_MONTH = "_month";
   private static final String FIELD_DAY = "_day";
   private static final String FIELD_HOUR = "_hour";
   private static final String FIELD_MINUTE = "_minute";

   /**
    * @see javax.faces.render.Renderer#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    * 
    * The decode method takes the parameters from the external requests, finds the
    * ones revelant to this component and decodes the results into an object known
    * as the "submitted value".
    */
   public void decode(FacesContext context, UIComponent component)
   {
      try
      {
         // TODO: should check for disabled/readonly here - no need to decode
         String clientId = component.getClientId(context);
         Map params = context.getExternalContext().getRequestParameterMap();
         String year = (String)params.get(clientId + FIELD_YEAR);
         if (year != null)
         {
            // found data for our component
            String month = (String)params.get(clientId + FIELD_MONTH);
            String day = (String)params.get(clientId + FIELD_DAY);
            String hour = (String)params.get(clientId + FIELD_HOUR);
            String minute = (String)params.get(clientId + FIELD_MINUTE);
            
            // we encode the values needed for the component as we see fit
            int[] parts = new int[5];
            parts[0] = Integer.parseInt(year);
            parts[1] = Integer.parseInt(month);
            parts[2] = Integer.parseInt(day);
            parts[3] = Integer.parseInt(hour);
            parts[4] = Integer.parseInt(minute);
            
            // save the data in an object for our component as the "EditableValueHolder"
            // all UI Input Components support this interface for the submitted value
            ((EditableValueHolder)component).setSubmittedValue(parts);
         }
      }
      catch (NumberFormatException nfe)
      {
         // just ignore the error and skip the update of the property
      }
   }
   
   /**
    * @see javax.faces.render.Renderer#getConvertedValue(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
    * 
    * In the Process Validations phase, this method is called to convert the values
    * to the datatype as required by the component.
    * 
    * It is possible at this point that a custom Converter instance will be used - this
    * is why we have not yet converted the values to a data type.
    */
   public Object getConvertedValue(FacesContext context, UIComponent component, Object val) throws ConverterException
   {
      int[] parts = (int[])val;
      Calendar date = new GregorianCalendar(parts[0], parts[1], parts[2], parts[3], parts[4]);
      return date.getTime();
   }
   
   /**
    * @see javax.faces.render.Renderer#encodeBegin(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
    * 
    * All rendering logic for this component is implemented here. A renderer for an
    * input component must render the submitted value if it's set, and use the local
    * value only if there is no submitted value.
    */
   public void encodeBegin(FacesContext context, UIComponent component)
         throws IOException
   {
      // always check for this flag - as per the spec
      if (component.isRendered() == true)
      {
         Date date = null;
         
         // this is part of the spec:
         // first you attempt to build the date from the submitted value
         int[] submittedValue = (int[])((EditableValueHolder)component).getSubmittedValue();
         if (submittedValue != null)
         {
            date = (Date)getConvertedValue(context, component, submittedValue);
         }
         else
         {
            // second if no submitted value is found, default to the current value
            Object value = ((ValueHolder)component).getValue();
            // finally check for null value and create default if needed
            date = value instanceof Date ? (Date)value : new Date();
         }
         
         // get the attributes from the component we need for rendering
         int nStartYear;
         Integer startYear = (Integer)component.getAttributes().get("startYear");
         if (startYear != null)
         {
            nStartYear = startYear.intValue();
         }
         else
         {
            nStartYear = new Date().getYear() + 1900;
         }
         
         int nYearCount = 25;
         Integer yearCount = (Integer)component.getAttributes().get("yearCount");
         if (yearCount != null)
         {
            nYearCount = yearCount.intValue();
         }
         
         // now we render the output for our component
         // we create 3 drop-down menus for day, month and year and 
         // two text fields for the hour and minute 
         String clientId = component.getClientId(context);
         ResponseWriter out = context.getResponseWriter();
         
         // note that we build a client id for our form elements that we are then
         // able to decode() as above.
         Calendar calendar = new GregorianCalendar();
         calendar.setTime(date);
         renderMenu(out, component, getDays(), calendar.get(Calendar.DAY_OF_MONTH), clientId + FIELD_DAY);
         renderMenu(out, component, getMonths(), calendar.get(Calendar.MONTH), clientId + FIELD_MONTH);
         renderMenu(out, component, getYears(nStartYear, nYearCount), calendar.get(Calendar.YEAR), clientId + FIELD_YEAR);
         
         // make sure we have a flag to determine whether to show the time
         Boolean showTime = (Boolean)component.getAttributes().get("showTime");
         if (showTime == null)
         {
            showTime = Boolean.FALSE;
         }
         
         out.write("&nbsp;");
         renderTimeElement(out, component, calendar.get(Calendar.HOUR_OF_DAY), clientId + FIELD_HOUR, showTime.booleanValue());
         if (showTime.booleanValue())
         {
            out.write("&nbsp;:&nbsp;");
         }
         renderTimeElement(out, component, calendar.get(Calendar.MINUTE), clientId + FIELD_MINUTE, showTime.booleanValue());
      }
   }
   
   /**
    * Render a drop-down menu to represent an element for the date picker.
    * 
    * @param out              Response Writer to output too
    * @param component        The compatible component
    * @param items            To display in the drop-down list
    * @param selected         Which item index is selected
    * @param clientId         Client Id to use
    * 
    * @throws IOException
    */
   private void renderMenu(ResponseWriter out, UIComponent component, List items,
         int selected, String clientId)
      throws IOException
   {
      out.write("<select");
      outputAttribute(out, clientId, "name");
      
      if (component.getAttributes().get("styleClass") != null)
      {
         outputAttribute(out, component.getAttributes().get("styleClass"), "class");
      }
      if (component.getAttributes().get("style") != null)
      {
         outputAttribute(out, component.getAttributes().get("style"), "style");
      }
      if (component.getAttributes().get("disabled") != null)
      {
         outputAttribute(out, component.getAttributes().get("disabled"), "disabled");
      }
      out.write(">");
      
      for (Iterator i=items.iterator(); i.hasNext(); /**/)
      {
         SelectItem item = (SelectItem)i.next();
         Integer value = (Integer)item.getValue();
         out.write("<option");
         outputAttribute(out, value, "value");
         
         // show selected value
         if (value.intValue() == selected)
         {
            outputAttribute(out, "selected", "selected");
         }
         out.write(">");
         out.write(item.getLabel());
         out.write("</option>");
      }
      out.write("</select>");
   }
   
   /**
    * Renders either the hour or minute field
    * 
    * @param out The ResponseWriter
    * @param currentValue The value of the hour or minute
    * @param clientId The id to use for the field
    */
   private void renderTimeElement(ResponseWriter out, UIComponent component, 
         int currentValue, String clientId, boolean showTime) throws IOException
   {
      out.write("<input");
      outputAttribute(out, clientId, "name");
      
      if (showTime)
      {
         out.write(" type='text' size='1' maxlength='2'");
         
         if (component.getAttributes().get("disabled") != null)
         {
            outputAttribute(out, component.getAttributes().get("disabled"), "disabled");
         }
      }
      else
      {
         out.write(" type='hidden'");
      }
      
      // make sure there are always 2 digits
      String strValue = Integer.toString(currentValue);
      if (strValue.length() == 1)
      {
         strValue = "0" + strValue;
      }
      
      outputAttribute(out, strValue, "value");
      out.write("/>");
   }
   
   private List getYears(int startYear, int yearCount)
   {
      List<SelectItem> years = new ArrayList<SelectItem>();
      for (int i=startYear; i>startYear - yearCount; i--)
      {
         Integer year = Integer.valueOf(i);
         years.add(new SelectItem(year, year.toString()));
      }
      return years;
   }
   
   private List getMonths()
   {
      // get names of the months for default locale
      Locale locale = Application.getLanguage(FacesContext.getCurrentInstance());
      if (locale == null)
      {
         locale = locale.getDefault();
      }
      DateFormatSymbols dfs = new DateFormatSymbols(locale);
      String[] names = dfs.getMonths();
      List<SelectItem> months = new ArrayList<SelectItem>(12);
      for (int i=0; i<12; i++)
      {
         Integer key = Integer.valueOf(i);
         months.add(new SelectItem(key, names[i]));
      }
      return months;
   }
   
   private List getDays()
   {
      List<SelectItem> days = new ArrayList<SelectItem>(31);
      for (int i=1; i<32; i++)
      {
         Integer day = Integer.valueOf(i);
         days.add(new SelectItem(day, day.toString()));
      }
      return days;
   }
}
