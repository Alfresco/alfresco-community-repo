<script type="text/javascript">
function toggleOutputPathPatternHelpVisible()
{
 var d = document.getElementById('output_path_pattern_help');
 d.style.display = d.style.display == 'block' ? 'none' : 'block';
}
</script>
<div id="output_path_pattern_help" class="summary infoText statusInfoText" style="display:none; padding: 5px;">
<div>An output path pattern is a <a style="color:blue;" href="http://freemarker.sourceforge.net">FreeMarker</a> expression which is used to specify the path to use when saving a generated asset using variable substitution when creating web content.</div>
<br/>
<div style="font-weight: bold">Guidelines</div>
<div>Output path patterns beginning with a leading slash (such as <tt>/${webapp}/content/${name}.xml</tt>) will produce paths rooted at the sandbox.  Those not begining with a leading slash will produce paths relative to the current working directory when the create web content wizard is invoked.</div>
<br/>
<div style="font-weight: bold">Variables</div>
<table border="0" cellspacing="1" cellpadding="1">
<colgroup><col width="15%"/><col width="85%"/></colgroup>
<tbody>
<tr><td><tt style="font-weight:bold;">name</td></tt><td>The name of the form instance data as entered by the user in the create web content wizard.</td></tr>
<tr><td><tt style="font-weight:bold;">webapp</td></tt><td>The name of the webapp in which the form instance data is being created.  Typically, if specifying an absolute output path pattern, the path will begin with the webapp folder (i.e. <tt>/&#36;{webapp}/...</tt>)</td></tr>
<tr><td><tt style="font-weight:bold;">extension</td></tt><td>The default extension associated with the mime-type configured for the rendering engine template.  This variable is only available for rendition ouput path patterns.</td></tr>
<tr><td><tt style="font-weight:bold;">xml</td></tt><td>The xml instance data collected by the form.</td></tr>
<tr><td><tt style="font-weight:bold;">node</td></tt><td>The form instance data node.</td></tr>
<tr><td><tt style="font-weight:bold;">date</td></tt><td>The current date at which the form instance data is being saved.  Refer to the <a style="color:blue;" href="http://freemarker.sourceforge.net/docs/ref_builtins_date.htm">FreeMarker date reference</a> for more information.</td></tr>
</tbody>
</table>
<br/>
<div style="font-weight: bold">For example</div>
<table border="0" cellspacing="1" cellpadding="1">
<colgroup><col width="25%"/><col width="75%"/></colgroup>
<tbody>
<tr><td><tt style="font-weight:bold;">&#36;{name}.xml</td></tt><td>form_name.xml</td></tr>
<tr><td><tt style="font-weight:bold;">&#36;{name}.&#36;{extension}</td></tt><td>form_name.html</td></tr>
<tr><td><tt style="font-weight:bold;">/&#36;{webapp}/content/&#36;{name}.xml</td></tt><td>/ROOT/content/form_name.xml</td></tr>
<tr><td><tt style="font-weight:bold;">&#36;{date?string("yyyy-MM-dd")}.xml</td></tt><td>2007-01-09.xml</td></tr>
</tbody>
</table>
<br/>
<div>For a more complete reference, please refer to the <a style="color: blue" href="http://wiki.alfresco.com">wiki.</a></div>
</div>
