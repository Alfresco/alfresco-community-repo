<?xml version="1.0" encoding="UTF-8"?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License. 
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:app="http://www.w3.org/2007/app"
	xmlns:atom="http://www.w3.org/2005/Atom" xmlns:cmis="http://docs.oasis-open.org/ns/cmis/core/200908/"
	xmlns:cmisra="http://docs.oasis-open.org/ns/cmis/restatom/200908/">

	<xsl:output method="html" />

	<xsl:param name="browseUrl"/>
	<xsl:param name="auxRoot"/>

	<xsl:template match="/">
		<html>
			<head>
				<title>Allowable Actions</title>
				<link rel="stylesheet" type="text/css" href="{$auxRoot}browser.css" />
			</head>
			<body>
				<h1>Allowable Actions</h1>
				<table class="entrytable">
				<xsl:for-each select="cmis:allowableActions/*[not(*)]">
					<tr>
						<td><xsl:value-of select="local-name()" /></td>
						<td><xsl:value-of select="current()" /></td>
					</tr>
				</xsl:for-each>
				</table>
			</body>
		</html>
	</xsl:template>
	
</xsl:stylesheet>