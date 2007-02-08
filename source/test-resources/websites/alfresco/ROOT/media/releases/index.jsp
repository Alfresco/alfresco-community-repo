<!--
Copyright (C) 2005 Alfresco, Inc.

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

Produces the index page for the press release page.
-->
<jsp:root version="1.2"
          xmlns:jsp="http://java.sun.com/JSP/Page"
 	  xmlns:c="http://java.sun.com/jsp/jstl/core"
	  xmlns:pr="http://www.alfresco.org/alfresco/pr"
          xmlns:fmt="http://java.sun.com/jsp/jstl/fmt">
  <!-- xmlns:pr is mapped to /WEB-INF/pr.tld by web.xml -->

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>
  
  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
      <!-- include common navigation components using the jsp compile time include directive -->
      <jsp:directive.include file="/assets/include_in_head.html"/>
      <title>Alfresco Press Releases - Open Source Content Management</title>
      <meta name="description" lang="en" content="News and press releases about Alfresco's enterprise content management system and document management software." />
      <style type="text/css">
	#main_content .headline {
	font-size: 1.2em;
	border-bottom: none;
	margin-bottom: 0.25em;
	}
	#main_content .date {
	color: #666666;
	font-size: 0.9em;
	margin-top: 0;
	margin-bottom: 0.25em;
	}
	#main_content .abstract {
	margin-top: 0;
	}
      </style>
    </head>
    <body>
      <div id="container">
	<jsp:directive.include file="/assets/include_main_navigation.html"/>
	<jsp:directive.include file="/about/navigation.html"/>
	<div id="content">&#160;</div>
	<!-- Main Content -->
	<div id="main_content">
	  
	  <!-- BEGIN MAIN CONTENT -->
	  <h1>Alfresco Press Releases</h1>
	  
	  <!-- load all PressReleaseBeans by using the function defined in /WEB-INF/pr.tld -->
	  <c:forEach items="${pr:getPressReleases(pageContext)}" var="pressRelease">
	    <h2 class="headline">
	      <jsp:element name="a">
		<jsp:attribute name="href"><c:out value="${pressRelease.href}"/></jsp:attribute>
		<jsp:body><c:out value="${pressRelease.title}"/></jsp:body>
	      </jsp:element>
	    </h2>
	    <p class="date"><fmt:formatDate value="${pressRelease.launchDate}" dateStyle="long"/></p>
	    <p class="abstract"><c:out value="${pressRelease.abstract}"/></p>
	  </c:forEach>

	  <!-- END MAIN CONTENT -->
	  
	</div>
	<!-- Feature Content -->
	<div id="right_content">
	  <div class="box_blue">
	    <h2>Press Releases By Category</h2>
	    <ul>
	    <!-- load all categories used by the press releases -->
	    <c:forEach items="${pr:getPressReleaseCategories(pageContext)}" var="category">
	      <li>
		<c:out value="${category}"/>
		<ul>
		  <c:forEach items="${pr:getPressReleasesInCategory(pageContext, category)}" var="pressRelease">
		    <li>
		      <jsp:element name="a">
			<jsp:attribute name="href"><c:out value="${pressRelease.href}"/></jsp:attribute>
			<jsp:body><c:out value="${pressRelease.title}"/></jsp:body>
		      </jsp:element>
		    </li>
		  </c:forEach>
		</ul>
	      </li>
	    </c:forEach>
	    </ul>
	    <h2>Press Release Archive</h2>
	    <ul>
	      <li><a href="/media/releases/archives/index.html">View Archived Releases</a></li>
	    </ul>
	  </div>
	</div>
	<div id="clear">&#160;</div>
      </div>
      <!--All Three End -->
      <jsp:directive.include file="/assets/footer.html"/>
    </body>
  </html>
</jsp:root>
