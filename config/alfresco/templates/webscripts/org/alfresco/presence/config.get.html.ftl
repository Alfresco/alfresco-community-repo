<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
   <title>Online Presence Space Configurator</title>
	<style>
	body {
		font-family: Verdana, Helvetica, sans-serif;
		font-size: 10pt;
	}
	.label {
		float: left;
		width: 10em;
	}
	.data {
		float: left;
	}
	.field {
		clear: left;
		float: left;
		padding: 8px;
	}
	.action {
		color: green;
		font-weight: bold;
		float: left;
		clear: both;
	}
	</style>
</head>
<body>
<h3>Online Presence Space Configurator</h3>
	<form action="${url.serviceContext}${url.match}" method="get">
	
	<div class="field">
		<span class="label">Space nodeRef:</span>
		<span class="data"><input type="text" name="n" size="64" /><br />e.g. &quot;workspace://SpacesStore/e3741425-35cf-11dc-9762-4b73d0280543&quot;</span>
	</div>

<#if args.advanced?exists>
	<div class="field">
		<span class="label">Webscript URL:</span>
		<span class="data"><input type="text" name="w" size="64" value="/wcs/ui/presence/status?nodeRef={noderef}" /></span>
		<input type="hidden" name="advanced" value="${args.advanced}" />
	</div>
<#else>
	<input type="hidden" name="w" value="/wcs/ui/presence/status?nodeRef={noderef}" />
</#if>

	<div class="field">
		<span><input type="submit" value="Add" name="add" /></span>
		<span><input type="submit" value="Remove" name="remove" /></span>
	</div>
	
	</form>

<#if args.add?exists>
	<div class="action">Aspect added to space.</div>
<#elseif args.remove?exists>
	<div class="action">Aspect removed from space.</div>
</#if>
</body>
</html>