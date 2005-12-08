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
package jsftest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.alfresco.web.ui.common.component.UIActionLink;
import org.alfresco.web.ui.common.component.UIBreadcrumb;
import org.apache.log4j.Logger;

/**
 * @author kevinr
 */
public class TestList
{
   /**
    * Constructor
    */
   public TestList()
   {
      // Create test data rows
      
      Calendar date = new GregorianCalendar(1999, 1, 5);
      rows.add(new TestRow("monkey", 5, true, 0.1f, date.getTime()));
      date = new GregorianCalendar(2000, 12, 5);
      rows.add(new TestRow("biscuit", 15, true, 0.2f, date.getTime()));
      date = new GregorianCalendar(1999, 11, 15);
      rows.add(new TestRow("HORSEY", 23, false, 0.333f, date.getTime()));
      date = new GregorianCalendar(2003, 11, 11);
      rows.add(new TestRow("thing go here", 5123, true, 0.999f, date.getTime()));
      date = new GregorianCalendar(1999, 2, 3);
      rows.add(new TestRow("I like docs", -5, false, 0.333f, date.getTime()));
      date = new GregorianCalendar(2005, 1, 1);
      rows.add(new TestRow("Document", 1235, false, 12.0f, date.getTime()));
      date = new GregorianCalendar(1998, 8, 8);
      rows.add(new TestRow("1234567890", 52, false, 5.0f, date.getTime()));
      date = new GregorianCalendar(1997, 9, 30);
      rows.add(new TestRow("space", 77, true, 17.5f, date.getTime()));
      date = new GregorianCalendar(2001, 7, 15);
      rows.add(new TestRow("House", 12, true, 0.4f, date.getTime()));
      date = new GregorianCalendar(2002, 5, 28);
      rows.add(new TestRow("Baboon", 14, true, -0.888f, date.getTime()));
      date = new GregorianCalendar(2003, 11, 11);
      rows.add(new TestRow("Woof", 0, true, 0.0f, date.getTime()));
   }
   
   public List getRows()
   {
      return this.rows;
   }
   
   public void clickBreadcrumb(ActionEvent event)
   {
      if (event.getComponent() instanceof UIBreadcrumb)
      {
         s_logger.debug("clickBreadcrumb action listener called, path now: " + ((UIBreadcrumb)event.getComponent()).getValue());
      }
   }
   
   public void clickActionLink(ActionEvent event)
   {
      s_logger.debug("clickActionLink");
   }
   
   public void clickNameLink(ActionEvent event)
   {
      UIActionLink link = (UIActionLink)event.getComponent();
      Map<String, String> params = link.getParameterMap();
      String value = params.get("name");
      if (value != null)
      {
         s_logger.debug("clicked item in list: " + value);
      }
   }


   private final static Logger s_logger = Logger.getLogger(TestList.class);
   
   private List<TestRow> rows = new ArrayList<TestRow>();;
}
