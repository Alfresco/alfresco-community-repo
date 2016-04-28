/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.update.tool.dircomp;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * Format results as HTML.
 * 
 * @author Matt Ward
 */
public class HtmlResultFormatter implements ResultFormatter
{
    private int maxPathDisplayLength = 50;
    private boolean differencesOnly;
    
    
    @Override
    public void format(ResultSet resultSet, OutputStream out)
    {
        boolean failed = resultSet.stats.differenceCount > 0;

        try(PrintWriter pw = new PrintWriter(out))
        {
            pw.println("<!DOCTYPE HTML>");
            pw.println("<html>");
            pw.println("<head>");
            pw.println("<title>File tree comparison results</title>");
            pw.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css\" integrity=\"sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7\" crossorigin=\"anonymous\">");
            pw.println("<link rel=\"stylesheet\" href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css\" integrity=\"sha384-fLW2N01lMqjakBkx3l/M9EahuwpSfeNvV63J5ezn3uZzapT0u7EYsXMjQV+0En5r\" crossorigin=\"anonymous\">");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<div class=\"container\">");

            String passOrFail;
            String alertClass;
            if (failed)
            {
                alertClass = "alert-danger";
                passOrFail = "FAILED";
            }
            else
            {
                alertClass = "alert-success";
                passOrFail = "PASSED";
            }

            pw.println("<div class=\"page-header\">\n" +
                    "  <h1>Fresh installation vs updated installation <small>Diff tool results</small></h1>\n" +
                    "</div>");
            pw.println("<div class=\"alert "+alertClass+"\" role=\"alert\">");
            pw.println("   <p>Status: "+passOrFail+"</p>");
            pw.println("   <p>Files examined: <strong>"+resultSet.stats.resultCount+"</strong></p>");
            pw.println("   <p>Files with differences: <strong>"+resultSet.stats.differenceCount+"</strong></p>");
            pw.println("   <p>Files with <em>allowed</em> differences: <strong>"+resultSet.stats.suppressedDifferenceCount+"</strong></p>");
            pw.println("   <p>Ignored files: <strong>"+resultSet.stats.ignoredFileCount+"</strong></p>");
            pw.println("</div>");
            outputResultsTable(resultSet.results, pw, 0);

            pw.println("</div><!-- end container -->");
            pw.println("</body>");
            pw.println("</html>");
        }
    }

    private void outputResultsTable(List<Result> results, PrintWriter pw, int row)
    {
        pw.println("<table class=\"table table-striped table-hover\">");
        pw.println("<thead>");
        pw.println("<tr>");
        pw.println("<th>#</th>");
        pw.println("<th>Updated install</th>");
        pw.println("<th>Fresh install</th>");
        pw.println("</tr>");
        pw.println("</thead>");
        pw.println("<tbody>");

        for (Result r : results)
        {
            ++row;

            outputResult(pw, row, r);

            if (!r.subResults.isEmpty())
            {
                // Only show the subresults if there are
                if (!differencesOnly || !r.equal)
                {
                    pw.println("<tr><td>&nbsp;</td><td colspan=\"2\">");
                    outputResultsTable(r.subResults, pw, row);
                    pw.println("</td></tr>");
                }
            }
        }

        pw.println("</tbody>");
        pw.println("</table>");
    }

    private void outputResult(PrintWriter pw, int row, Result r)
    {
        if (differencesOnly && r.equal)
        {
            return;
        }
        pw.println("<tr>");
        
        pw.println("<td>"+row+"</td>");
        
        String p1 = (r.p1 == null) ? "" : r.p1.toString();
        String p1Abbr = abbreviate(p1, maxPathDisplayLength);
        String p2 = (r.p2 == null) ? "" : r.p2.toString();
        String p2Abbr = abbreviate(p2, maxPathDisplayLength);
        
        // TODO: URL/HTML-encode as appropriate
        String diffClass;
        if (r.equal)
        {
            if (r.subResults.isEmpty())
            {
                // Result refers to a normal file or directory.
                diffClass = "info";
            }
            else
            {
                // File is a special archive, but no differences in the sub-results are considered important.
                diffClass = "warning";
            }
        }
        else
        {
            // The file/directory appears different in each tree. If it is a special archive, then there
            // are differences that we care about.
            diffClass = "danger";
        }

        pw.println(
                String.format("<td class=\"%s\"><a href=\"file://%s\">%s</a></td><td class=\"%s\"><a href=\"file://%s\">%s</a></td>",
                diffClass,
                p1,
                p1Abbr,
                diffClass,
                p2,
                p2Abbr));

        pw.println("</tr>");
    }
    
    private String abbreviate(String str, int maxLength)
    {
        return (str.length() > maxLength) ? "..."+str.substring(str.length()-maxLength) : str;
    }

    public void setMaxPathDisplayLength(int maxPathDisplayLength)
    {
        this.maxPathDisplayLength = maxPathDisplayLength;
    }

    public void setDifferencesOnly(boolean differencesOnly)
    {
        this.differencesOnly = differencesOnly;
    }
}
