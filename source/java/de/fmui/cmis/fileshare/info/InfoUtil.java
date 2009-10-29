package de.fmui.cmis.fileshare.info;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;

public class InfoUtil {

	public static final String CONTEXT_PREFIX = "{ctx}";

	private InfoUtil() {
	}

	public static String getContextUrl(HttpServletRequest request) {
		String scheme = request.getScheme();
		int port = request.getServerPort();

		if ("http".equals(scheme) && (port == 80)) {
			port = -1;
		}
		if ("https".equals(scheme) && (port == 443)) {
			port = -1;
		}

		return scheme + "://" + request.getServerName() + (port > 0 ? ":" + port : "") + request.getContextPath();
	}

	public static String getServletUrl(HttpServletRequest request) {
		return getContextUrl(request) + request.getServletPath();
	}

	public static String getAuxRoot(HttpServletRequest request, String auxRoot) {
		if (auxRoot == null) {
			return getContextUrl(request);
		} else if (auxRoot.startsWith(CONTEXT_PREFIX)) {
			return getContextUrl(request) + auxRoot.substring(CONTEXT_PREFIX.length());
		} else {
			return auxRoot;
		}
	}

	public static void printHeader(PrintWriter pw, String title) {
		pw.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"");
		pw.println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		pw.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		pw.println("<head>");
		pw.println("<title>" + title + "</title>");
		pw.println("<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">");
		pw.println("<style type=\"text/css\">");
		pw.println("body { font-family: arial,sans-serif; font-size: 10pt; }");
		pw.println("</style>");
		pw.println("</head>");
		pw.println("<body>");
	}

	public static void printFooter(PrintWriter pw) {
		pw.println("</body>");
		pw.println("</html>");
	}

	public static void printStartSection(PrintWriter pw, String header) {
		pw.println("<div style=\"background-color:#eeeeee; margin:15px; padding:5px;\">");
		pw.println("<h3>" + header + "</h3>");
	}

	public static void printEndSection(PrintWriter pw) {
		pw.println("</div>");
	}

	public static void printStartTable(PrintWriter pw) {
		pw.println("<table>");
	}

	public static void printEndTable(PrintWriter pw) {
		pw.println("</table>");
	}

	public static void printRow(PrintWriter pw, String... cols) {
		pw.print("<tr>");

		for (String col : cols) {
			pw.print("<td>" + col + "</td>");
		}

		pw.println("</tr>");
	}
}
