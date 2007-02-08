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
	  xmlns:c="http://java.sun.com/jsp/jstl/core">

  <jsp:output doctype-root-element="html"
	      doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
	      doctype-system="http://www.w3c.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>

  <jsp:directive.page language="java" contentType="text/html; charset=UTF-8"/>
  <jsp:directive.page isELIgnored="false"/>

  <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
      <jsp:directive.include file="/assets/include_in_head.html"/>
      <title>Alfresco - Open Source Enterprise Content Management (CMS) including Web Content Management</title>
      <meta name="description" lang="en" content="Alfresco offers open source enterprise content management including open source web content management software and document management software (Documentum, Jackrabbit, Sharepoint)" />
      <meta name="GOOGLEBOT" content="index, follow" />
      <meta name="robots" content="index, follow" />
      <meta name="author" content="" />
      <link rel="alternate" type="application/rss+xml" title="RSS" href="/rss.php?feed=all" />
      <script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
      </script>
      <script type="text/javascript">
	_uacct = "UA-70509-2";
	urchinTracker();
      </script>
      <style type="text/css">
	.frontpanel {
	margin: 10px 0 0 0;
	padding: 10px;
	width: 740px;
	background-color: #f1f1f1;
	clear: both;
	}
	.frontpanel-icon {
	padding: 0 3px;
	margin: 0;
	float: left;
	}
	.frontpanel-icon img {
	padding: 0;
	margin: 0;
	border: none;
	}
	.frontpanel-icon ul {
	margin-top: 10px;
	}
	.frontpanel-icon ul li {
	list-style-type: square;
	list-style-image: url(assets/images/frontpanel/bullet_b.gif);
	font-size: 1.2em;
	margin-bottom: 5px;
	}
	#tagline {
	padding: 5px 1% 20px 1%;
	margin: 0;
	}
	#tagline p {
	font-size: 1.5em;
	font-weight: 400;
	color: #666666;
	padding: 0;
	margin: 0px;
	}
	.front_column {
	font-size: 1.2em;
	width: 238px;
	float: left;
	margin: 0 20px 20px 0;
	border: 1px solid #cccccc;
	padding-bottom: 1em;
	}
	.front_list {
	}
	.front_list h2 {
	color: #333333;
	font-size: 1.25em;
	font-weight: 400;
	margin-top: 0;
	margin-bottom: 0;
	padding: 5px 10px;
	background-color: #f1f1f1;
	}
	.front_list h2 a, .front_list h2 a:visited, .front_list h2 a:active {
	color: #333333;
	text-decoration: none;
	}
	.front_list h3 {
	font-size: 1em;
	font-weight: 400;
	margin-top: 1em;
	margin-bottom: 0
	}
	.front_list p {
	margin-bottom: 0;
	}
	.front_list p.date {
	font-size: 0.9em;
	margin-top: 0.25em;
	}
	.front_list h3, .front_list p {
	padding: 0 10px;
	}
	.front_list ul {
	padding-left: 0;
	margin-top: 1em;
	margin-left: 30px;
	margin-bottom: 0;
	list-style-type: none;
	}
	.front_list ul.right {
	padding-left: 0;
	margin-left: 30px;
	}
	.front_list li {
	margin-bottom: 0.5em;
	}
	ul.front-links-top {
	margin-top: 0;
	margin-left: 0;
	padding-left: 0;
	list-style-type: none;
	list-style-image: none;
	margin-bottom: 30px;
	}
	.front-links-top li {
	margin-top: 1em;
	text-align: center;
	list-style-type: none;
	}
	.front-links-top img {
	border: 0;
	margin-bottom: 5px;
	}
	.front-links-bot-left {
	float: left; width: 46%; margin-top: 1em; padding-left: 10px;
	list-style-image: url("assets/images/icons/arrow_b.gif");
	}
	.front-links-bot-right {
	float: left; width: 46%; margin-top: 1em;
	list-style-image: url("assets/images/icons/arrow_b.gif");
	}
	#banner1 {
	display: inline;
	}
	#banner2 {
	display: inline;
	margin-left: 14px;
	}
      </style>
    </head>
    <body>
      <!-- Container -->
      <div id="container">
	<jsp:directive.include file="/assets/include_main_navigation.html"/>
	<div id="outer">
	  <div id="inner">
	    <div id="shome">&#160;</div>
	  </div>
	</div>
	<div id="content"><img id="banner1" src="assets/images/banners/partner_podcast.gif" border="0" width="372" height="146" alt="Alfresco Partner Podcast" usemap="#bannermap1" /><img id="banner2" src="assets/images/banners/1_4_preview_banner.gif" border="0" width="373" height="146" alt="Alfresco 1.4 Community Preview now available" usemap="#bannermap2" /></div>
	<map name="bannermap1" id="bannermap1">
	  <area href="redirect.php?source=30&amp;url=http://blogs.alfresco.com/opentalk/" alt="Listen Now" shape="rect" coords="263,112,363,137" />
	</map>
	<map name="bannermap2" id="bannermap2">
	  <area href="redirect.php?source=27&amp;url=http://www.alfresco.com/products/ecm/releases/1.4_preview/" alt="Learn More" shape="rect" coords="273,112,363,137" />
	</map>
	<div class="frontpanel">
	  <div id="tagline">
	    <p>Alfresco offers true Open Source Enterprise Content Management (ECM) - Document Management, Collaboration, Records Management, Knowledge Management, Web Content Management and Imaging. <a href="/products/ecm/fivesteps/discover/index.html">More...</a></p>
	  </div>
	  <div class="frontpanel-icons">
	    <div class="frontpanel-icon">
	      <a href="/products/ecm/fivesteps/discover/index.html"><img src="/assets/images/frontpanel/icon_discover.gif" border="0" alt="Discover" width="142" height="142" /></a>
	      <ul>
		<li><a href="/products/ecm/presentations/index.html">Presentations</a></li>
		<li><a href="products/ecm/demonstrations/index.html">Demonstrations</a></li>
		<li><a href="products/ecm/tour/index.html">Feature Tour</a></li>
	      </ul>
	    </div>
	    <div class="frontpanel-icon">
	      <a href="/products/ecm/fivesteps/learnmore/index.html"><img src="assets/images/frontpanel/icon_learnmore.gif" border="0" alt="Learn More" width="142" height="142" /></a>
	      <ul>
		<li><a href="products/index.html">Products</a></li>
		<li><a href="media/whitepapers/index.html">White Papers</a></li>
		<li><a href="http://blogs.alfresco.com/">Blogs</a></li>
	      </ul>
	    </div>
	    <div class="frontpanel-icon">
	      <a href="products/ecm/fivesteps/try/index.html"><img src="assets/images/frontpanel/icon_try.gif" border="0" alt="Try" width="142" height="142" /></a>
	      <ul>
		<li><a href="products/ecm/hostedtrials/index.html">Hosted Trials</a></li>
		<li><a href="products/ecm/enttrial/index.html">Enterprise Trial</a></li>
		<li><a href="http://dev.alfresco.com/downloads/">Open Source</a></li>
	      </ul>
	    </div>
	    <div class="frontpanel-icon">
	      <a href="products/ecm/fivesteps/buy/index.html"><img src="assets/images/frontpanel/icon_buy.gif" border="0" alt="Buy" width="142" height="142" /></a>
	      <ul>
		<li><a href="store/index.html">Store</a></li>
	      </ul>
	    </div>
	    <div class="frontpanel-icon">
	      <a href="products/ecm/fivesteps/use/index.html"><img src="assets/images/frontpanel/icon_use.gif" border="0" alt="Use" width="142" height="142" /></a>
	      <ul>
		<li><a href="services/training/index.html">Training</a></li>
		<li><a href="services/consulting/index.html">Consulting</a></li>
		<li><a href="services/support/index.html">Support</a></li>
	      </ul>
	    </div>
	  </div>
	  <div style="clear:both; padding-bottom: 0px;">&#160;</div>
	</div>
	<div style="clear:both; padding-bottom: 20px;">&#160;</div>
	<div class="front_column">
	  <div class="front_list">
	    <h2>Alfresco In the News</h2>

	    <h3><a href="http://www.ecmconnection.com/content/news/article.asp?DocID=%7BBDE8CAFB-4273-4DEF-BF1A-6DBD95E0190E%7D">Alfresco Extends Leadership With Production Ready Open Source Business Process Management</a></h3>
	    <p class="date">Sep 14, 2006 (ECM Connection)</p>

	    <h3><a href="http://www.marketwire.com/mw/release_html_b1?release_id=162734">Alfresco and Kofax Announce Integration of Ascent Capture</a></h3>
	    <p class="date">Sep 13, 2006 (Market Wire)</p>

	    <h3><a href="http://itmanagement.earthweb.com/netsys/article.php/3631536">Open Source ECM in a Windows World</a></h3>
	    <p class="date">Sep 12, 2006 (JupiterWeb)</p>

	    <h3><a href="http://www.marketwire.com/mw/release_html_b1?release_id=161753">Alfresco Launches First Open Source Records Management Solution</a></h3>
	    <p class="date">Sep 11, 2006 (Market Wire)</p>

	    <h3><a href="http://opensource.sys-con.com/read/269540.htm">Alfresco Unveils New Version Of Open Source Enterprise CMS</a></h3>
	    <p class="date">Sep 07, 2006 (Enterprise Open Source)</p>

	    <p><a href="/rss/?feed=coverage">More Coverage</a> | <a href="/media/releases/index.jsp">Press Releases</a></p>
	  </div>
	</div>
	<div class="front_column">
	  <div class="front_list">
	    <h2>Events &amp; Training</h2>

	    <h3><a href="https://optaros.webex.com/optaros/mywebex/epmainframe.php?rlink=https%3A%2F%2Foptaros.webex.com%2Foptaros%2Fonstage%2Fmainframe.php%3Fmainurl%3D%2Foptaros%2Fonstage%2Ftool%2Fevent%2Fevent_detail.php%3FEventID%3D299966243%26FirstEnter%3D1%26GuestTimeZone%3D%26SourceId%3D">Optaros Open Source Content Management Solutions Webinar</a></h3>
	    <p class="date">Sep 26, 2006 (Online WebEx)</p>
	    <h3><a href="/services/training/index.html">US Training - Alfresco for Developers</a></h3>
	    <p class="date">Sep 26-28, 2006 (Washington, DC)</p>
	    <h3><a href="/services/training/index.html">Australia Training - Alfresco for Users and Administrators</a></h3>
	    <p class="date">Sep 26-28, 2006 (Melbourne)</p>
	    <h3><a href="/services/training/index.html">Alfresco at Gartner Open Source</a></h3>
	    <p class="date">Sep 27-29, 2006 (Phoenix)</p>
	    <h3><a href="http://www.forum-geide.com/">Alfresco at Forum de la Geide</a></h3>
	    <p class="date">Oct 03-05, 2006 (Paris)</p>
	    <p><a href="about/events/index.html">More Events</a> | <a href="services/training/index.html">More Training</a></p>
	  </div>
	</div>
	<div class="front_column" style="margin-right: 0;">
	  <div class="front_list" style="margin-right: 0;">
	    <h2>Links</h2>

	    <div>&#160;</div>
	    <div style="clear: both;">&#160;</div>
	    <div class="front-links-bot-left">
	      <ul class="front-links-top">
		<li><a href="http://forge.alfresco.com/" title="Alfresco Community Forge"><img src="http://dev.alfresco.com/community/newsletters/2006/05/forge.gif" alt="Alfresco Forge" align="top" /><br />Forge</a></li>
		<li><a href="http://podcasts.alfresco.com/" title="Open Source Talk podcast series"><img src="http://dev.alfresco.com/community/newsletters/2006/05/podcast.gif" alt="Alfresco Open Source Talk" align="top" /><br />Podcasts</a></li>
	      </ul>
	      <ul class="front-links-bot">
		<li><a href="http://forums.alfresco.com/" title="Alfresco Forums">Forums</a></li>
		<li><a href="http://wiki.alfresco.com/" title="Alfresco Wiki">Wiki</a></li>
		<li><a href="http://www.alfresco.org/jira/" title="Alfresco Bug Tracking (JIRA)">Bug Tracking</a></li>
		<li><a href="http://dev.alfresco.com/" title="Developer site">Developers</a></li>
	      </ul>
	    </div>
	    <div class="front-links-bot-right">
	      <ul class="front-links-top">
		<li><a href="/products/ecm/docs/1.3_draft/index.html" title="Documentation"><img src="document.gif" alt="Documentation" align="top" /><br />Documentation</a></li>
		<li><a href="http://blogs.alfresco.com/" title="Alfresco Blogs"><img src="http://dev.alfresco.com/community/newsletters/2006/05/blogs.gif" alt="Alfresco Blogs" align="top" /><br />Blogs</a></li>
	      </ul>
	      <ul class="front-links-bot">
		<li><a href="http://dev.alfresco.com/downloads/" title="Alfresco Downloads">Downloads</a></li>
		<li><a href="/media/whitepapers/index.html" title="Alfresco White Papers">White Papers</a></li>
		<li><a href="/about/careers/index.html" title="Alfresco Careers">Careers</a></li>
	      </ul>
	    </div>
	    <div style="clear: both;">&#160;</div>
	  </div>
	</div>

	<div style="clear:both; padding-bottom: 10px;">&#160;</div>
      </div> <!-- end container -->
      <jsp:directive.include file="/assets/footer.html"/>
      <!-- START OF VERTICAL LEAP TRACKING -->
      <script type="text/javascript" language="JavaScript">
	<!--
	    var VLCampaignId = "1417";
	    //-->
      </script>
      <script type="text/javascript" language="JavaScript"
	      src="http://roi.vertical-leap.co.uk/scripts/tracker.js"></script>
      <!-- END OF VERTICAL LEAP TRACKING -->
    </body>
  </html>
</jsp:root>
