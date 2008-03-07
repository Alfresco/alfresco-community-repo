<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>

<head>
   <title>Apply AVM WebScript Custom View</title>
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
	</style>
</head>

<body>
	<form action="${url.serviceContext}${url.match}" method="post">
	
   	<div class="field">
   		<span class="label">Store:</span>
   		<span class="data"><input type="text" name="store" size="32" /><br />e.g. website1--admin</span>
   	</div>
   	
   	<div class="field">
   		<span class="label">Folder Path:</span>
   		<span class="data"><input type="text" name="path" size="64" /><br />e.g. /ROOT/images</span>
   	</div>
      
   	<div class="field">
   		<span class="label">WebScript URL:</span>
   		<span class="data"><input type="text" name="view" size="64" value="" /><br />e.g. /utils/avmview<br>A well known token {path} can be used in the url and will be replaced by the current AVM folder path at runtime.</span>
   	</div>
      
   	<div class="field">
   		<span><input type="submit" value="Apply" /></span>
   	</div>
	
	</form>

</body>

</html>