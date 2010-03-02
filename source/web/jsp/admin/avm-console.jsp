<%--
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
--%>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="r" %>
<%@ page import="java.util.*,java.io.*,org.alfresco.repo.avm.*,org.alfresco.repo.avm.util.*,org.alfresco.service.cmr.avm.*,org.alfresco.service.cmr.repository.*,org.alfresco.repo.security.authentication.*,org.alfresco.service.cmr.avmsync.*,org.alfresco.service.cmr.security.*,org.alfresco.service.cmr.avm.locking.*" %>

<%!
    static AVMService fgService;
    static BulkLoader fgLoader;
    static AVMInterpreter fgInterpreter;

    static
    {
        fgService = (AVMService)RawServices.Instance().getContext().getBean("AVMService");
        fgLoader = new BulkLoader();
        fgLoader.setAvmService(fgService);
        fgInterpreter = new AVMInterpreter();
        fgInterpreter.setAvmService(fgService);
        fgInterpreter.setAvmSyncService(
            (AVMSyncService)RawServices.Instance().getContext().getBean("AVMSyncService"));
        fgInterpreter.setAvmLockingService(
            (AVMLockingService)RawServices.Instance().getContext().getBean("AVMLockingService"));
        fgInterpreter.setPermissionService(
            (PermissionService)RawServices.Instance().getContext().getBean("PermissionService"));
        fgInterpreter.setBulkLoader(fgLoader);
    }
        
    static String EscapeForHTML(String data)
    {
        StringBuilder builder = new StringBuilder();
        int count = data.length();
        for (int i = 0; i < count; i++)
        {
            char c = data.charAt(i);
            switch (c)
            {
                case '<' :
                    builder.append("&lt;");
                    break;
                case '>' :
                    builder.append("&gt;");
                    break;
                case '"' :
                    builder.append("&quot;");
                    break;
                case '&' :
                    builder.append("&amp;");
                    break;
                case '\'' :
                    builder.append("&apos;");
                    break;
                case '\\' :
                    builder.append("&#092;");
                    break;
                default :
                    builder.append(c);
                    break;
            }
        }
        return builder.toString();
    }
%>

<r:page title="AVM Console">
   <table>
   	<tr>
        	<td>
            <img src="<%=request.getContextPath()%>/images/logo/AlfrescoLogo32.png" alt="Alfresco" />
         </td>
         <td>
            <span class="mainTitle" style="white-space: nowrap;">AVM Console</span>
         </td>
   	</tr>
	</table>
   
   <%
       String command = request.getParameter("command");
       if (command != null)
       {
          long start = System.currentTimeMillis();
          String data = request.getParameter("data");
          data = data + "\n\n";
          BufferedReader in = new BufferedReader(new StringReader(data));
          String result = fgInterpreter.interpretCommand(command, in);
          out.println("<div style='border: 1px dotted #aaa; padding: 6px;'><pre>Output:<br/>");
          out.println(EscapeForHTML(command));
          out.println();
          out.println(EscapeForHTML(result));
          out.println((System.currentTimeMillis() - start) + "ms");
          out.println("</pre></div>");
       }
       else
       {
          command = "";
       }
   %>
   
   <form action="avm-console.jsp" method="POST">
      <p>Command: <input style="font-family:monospace" type="text" name="command" id="command" size="70" value="<%=command%>"></p>
      <p>Optional Data:</p>
      <textarea name="data" cols="80" rows="10"></textarea>
      <p><input type="submit" name="submit" value="Execute"></p>
      <a href="avm-console-help.txt">Help!</a>
   </form>
   
   <script>
      document.getElementById("command").focus();
   </script>
   
</r:page>
