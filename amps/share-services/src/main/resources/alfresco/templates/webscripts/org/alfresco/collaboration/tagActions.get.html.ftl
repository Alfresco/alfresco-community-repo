<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
<head>
   <title>Tagging Test UI</title>
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
<h3>Tagging Test UI</h3>
	<form action="${url.serviceContext}${url.match}" method="post">
	
	<div class="field">
		<span class="label">Space nodeRef:</span>
		<span class="data"><input type="text" name="n" size="64" /><br />e.g. &quot;e3741425-35cf-11dc-9762-4b73d0280543&quot;</span>
	</div>

	<div class="field">
		<span class="label">Tag:</span>
		<span class="data"><input type="text" name="t" size="64" value="" /></span>
	</div>

	<div class="field">
		<span><input type="submit" value="Add" name="add" /></span>
		<span><input type="submit" value="Remove" name="remove" /></span>
	</div>
	
	</form>

</body>
</html>