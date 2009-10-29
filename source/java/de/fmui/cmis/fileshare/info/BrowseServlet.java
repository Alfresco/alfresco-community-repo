/*
 * Copyright (c) 2009, Florian Müller <mueller@gotux.de>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */

package de.fmui.cmis.fileshare.info;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

public class BrowseServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String PARAM_URL = "url";
	private static final String INIT_PARAM_AUXROOT = "auxroot";
	private static final String INIT_PARAM_ALLOW = "allow";
	private static final String INIT_PARAM_STYLESHEET = "stylesheet:";

	private static final int BUFFER_SIZE = 64 * 1024;

	private String fAuxRoot = "";
	private String fAllow = ".*";
	private Map<String, Source> fStyleSheets;

	@Override
	public void init(ServletConfig config) throws ServletException {
		fStyleSheets = new HashMap<String, Source>();

		DocumentBuilder builder = null;
		try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			builder = builderFactory.newDocumentBuilder();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Enumeration<String> initParams = config.getInitParameterNames();
		while (initParams.hasMoreElements()) {
			String param = initParams.nextElement();
			if (param.startsWith(INIT_PARAM_STYLESHEET)) {
				String contentType = param.substring(INIT_PARAM_STYLESHEET.length());
				String stylesheetFileName = config.getInitParameter(param);

				InputStream stream = config.getServletContext().getResourceAsStream(stylesheetFileName);
				if (stream != null) {
					try {
						Document xslDoc = builder.parse(stream);
						addStylesheet(contentType, new DOMSource(xslDoc));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		String initAuxRoot = config.getInitParameter(INIT_PARAM_AUXROOT);
		if (initAuxRoot != null) {
			fAuxRoot = initAuxRoot;
		}

		String initAllow = config.getInitParameter(INIT_PARAM_ALLOW);
		if (initAllow != null) {
			fAllow = initAllow;
		}
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter(PARAM_URL) == null) {
			printInput(req, resp);
			return;
		}

		doBrowse(req, resp);
	}

	protected void doBrowse(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String browseUrl = req.getParameter(PARAM_URL);

		// check URL
		if (!browseUrl.matches(fAllow)) {
			printError(req, resp, "Prohibited URL!", null);
			return;
		}

		try {
			// get content
			URL url = new URL(browseUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			conn.setDoOutput(false);
			conn.setRequestMethod("GET");
			String authHeader = req.getHeader("Authorization");
			if (authHeader != null) {
				conn.setRequestProperty("Authorization", authHeader);
			}
			conn.connect();

			// ask for login
			if (conn.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				resp.setHeader("WWW-Authenticate", conn.getHeaderField("WWW-Authenticate"));
				resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required");
				return;
			}

			// find stylesheet
			Source stylesheet = getStylesheet(conn.getContentType());

			OutputStream out = null;
			InputStream in = new BufferedInputStream(conn.getInputStream(), BUFFER_SIZE);

			if (stylesheet == null) {
				// no stylesheet found -> conduct content
				resp.setContentType(conn.getContentType());
				out = new BufferedOutputStream(resp.getOutputStream(), BUFFER_SIZE);

				byte[] buffer = new byte[BUFFER_SIZE];
				int b;
				while ((b = in.read(buffer)) > -1) {
					out.write(buffer, 0, b);
				}
			} else {
				// apply stylesheet
				TransformerFactory f = TransformerFactory.newInstance();
				Transformer t = f.newTransformer(stylesheet);
				t.setParameter("browseUrl", InfoUtil.getServletUrl(req) + "?url=");
				t.setParameter("auxRoot", InfoUtil.getAuxRoot(req, fAuxRoot));

				resp.setContentType("text/html");
				out = new BufferedOutputStream(resp.getOutputStream(), BUFFER_SIZE);

				Source s = new StreamSource(in);
				Result r = new StreamResult(out);
				t.transform(s, r);
			}

			try {
				out.flush();
				out.close();
			} catch (Exception e) {
			}

			try {
				in.close();
			} catch (Exception e) {
			}
		} catch (Exception e) {
			e.printStackTrace();
			printError(req, resp, e.getMessage(), e);
			return;
		}
	}

	protected void printError(HttpServletRequest req, HttpServletResponse resp, String message, Exception e)
			throws ServletException, IOException {
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter pw = resp.getWriter();

		InfoUtil.printHeader(pw, "Error");
		pw.println("<div style=\"background-color:#eeeeee; margin:15px; padding:5px;\">");
		pw.println("<h3>" + message + "</h3>");

		if (e != null) {
			pw.print("<pre>");
			e.printStackTrace(pw);
			pw.println("</pre>");
		}

		pw.println("</div>");
		InfoUtil.printFooter(pw);
	}

	protected void printInput(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/html;charset=utf-8");
		PrintWriter pw = resp.getWriter();

		InfoUtil.printHeader(pw, "CMIS Browse");
		pw.println("<h1>CMIS Browser</h1>");
		pw.println("<div style=\"background-color:#eeeeee; margin:15px; padding:5px;\">");
		pw.println("<form action=\"\" method=\"GET\">");
		pw.println("CMIS AtomPub URL: ");
		pw.println("<input name=\"url\" type=\"text\" size=\"100\" value=\"" + InfoUtil.getContextUrl(req) + "/atom"
				+ "\"/>");
		pw.println("<input type=\"submit\" value=\" GO \">");
		pw.println("</form>");
		pw.println("</div>");
		InfoUtil.printFooter(pw);
	}

	private void addStylesheet(String contentType, Source source) {
		fStyleSheets.put(contentType, source);
	}

	private Source getStylesheet(String contentType) {
		String[] ctp = contentType.split(";");
		Source source = null;

		String match = "";
		int i = 0;
		while (source == null && i < ctp.length) {
			if (i > 0) {
				match += ";";
			}
			match += ctp[i];
			source = fStyleSheets.get(match);
			i++;
		}

		return source;
	}
}
