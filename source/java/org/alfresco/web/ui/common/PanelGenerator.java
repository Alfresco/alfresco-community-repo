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
package org.alfresco.web.ui.common;

import java.io.IOException;
import java.io.Writer;

/**
 * Helper to generate the rounded panel HTML templates
 * 
 * @author kevinr
 */
public final class PanelGenerator
{
   public static void generatePanel(Writer out, String contextPath, String panel, String inner)
      throws IOException
   {
      generatePanel(out, contextPath, panel, inner, BGCOLOR_WHITE);
   }
   
   public static void generatePanel(Writer out, String contextPath, String panel, String inner, String bgColor)
      throws IOException
   {
      generatePanel(out, contextPath, panel, inner, bgColor, false);
   }
   
   public static void generatePanel(Writer out, String contextPath, String panel, String inner, String bgColor, boolean dialog)
      throws IOException
   {
      generatePanelStart(out, contextPath, panel, bgColor, dialog);
      out.write(inner);
      generatePanelEnd(out, contextPath, panel);
   }
   
   public static void generatePanelStart(Writer out, String contextPath, String panel, String bgColor)
      throws IOException
   {
      generatePanelStart(out, contextPath, panel, bgColor, false);
   }
   
   public static void generatePanelStart(final Writer out, 
                                         final String contextPath, 
                                         final String panel, 
                                         final String bgColor, 
                                         final boolean dialog)
      throws IOException
   {
      out.write("<table cellspacing='0' cellpadding='0' style='border-width: 0px; width: 100%'>");
      out.write("<tr><td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_01.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_02.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_02.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_03");
      if (dialog)
      {
         out.write("_squared");
      }
      out.write(".gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_04.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_04.gif' width='7' height='7' alt=''/></td><td style='background-color:");
      out.write(bgColor);
      out.write(";'>");
   }
   
   public static void generatePanelStartWithBgImg(final Writer out, 
            final String contextPath, final String panel, String bgColor)
            throws IOException
   {
      out.write("<table cellspacing='0' cellpadding='0' style='border-width: 0px; width: 100%'>");
      out.write("<tr><td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_01.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_02.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_02.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_03.gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_04.gif)'></td><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_bg.gif); background-repeat: repeat-x; background-color: ");
      out.write(bgColor);
      out.write(";'>");
   }
   
   public static void generatePanelEnd(final Writer out, 
                                       final String contextPath, 
                                       final String panel)
      throws IOException
   {
      out.write("</td><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_06.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_06.gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_07.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_08.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_08.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_09.gif' width='7' height='7' alt=''/></td></tr></table>");
   }
   
   public static void generatePanelEndWithBgImg(final Writer out, 
                                       final String contextPath, 
                                       final String panel)
      throws IOException
   {
      out.write("</td><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_06.gif)'></td></tr>");
      
      out.write("<tr><td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_07.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_08.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_08.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(panel);
      out.write("_09.gif' width='7' height='7' alt=''/></td></tr></table>");
}
   
   public static void generateTitledPanelMiddle(final Writer out, 
                                                final String contextPath, 
                                                final String titlePanel, 
                                                final String contentPanel, 
                                                final String contentBgColor) 
      throws IOException
   {
      // generate the expanded part, just under the title
      out.write("</td><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_06.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_06.gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_");
      out.write(contentPanel);
      out.write("_07.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_08.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_08.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_");
      out.write(contentPanel);
      out.write("_09.gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(contentPanel);
      out.write("_04.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(contentPanel);
      out.write("_04.gif' width='7' height='7' alt=''/></td><td style='background-color: ");
      out.write(contentBgColor);
      out.write("; padding-top: 6px;'>");
   }
   
   public static void generateExpandedTitledPanelMiddle(final Writer out,
                                                        final String contextPath, 
                                                        final String titlePanel, 
                                                        final String expandedTitlePanel, 
                                                        final String contentPanel, 
                                                        final String contentBgColor)
      throws IOException
   {
      // generate the expanded part, just under the title
      out.write("</td><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_06.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_06.gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='width: 7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_");
      out.write(expandedTitlePanel);
      out.write("_07.gif' width='7' height='7' alt=''/></td>");
      
      out.write("<td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_");
      out.write(expandedTitlePanel);
      out.write("_08.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_");
      out.write(expandedTitlePanel);
      out.write("_08.gif' height='7' alt=''/></td>");
      
      out.write("<td style='width:7px;'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(titlePanel);
      out.write("_");
      out.write(expandedTitlePanel);
      out.write("_09.gif' width='7' height='7' alt=''/></td></tr>");
      
      out.write("<tr><td style='background-image: url(");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(contentPanel);
      out.write("_04.gif)'><img src='");
      out.write(contextPath);
      out.write("/images/parts/");
      out.write(contentPanel);
      out.write("_04.gif' width='7' height='7' alt=''/></td><td style='background-color:");
      out.write(contentBgColor);
      out.write("; padding-top:6px;'>");
   }
   
   public final static String BGCOLOR_WHITE = "#FFFFFF";
}
