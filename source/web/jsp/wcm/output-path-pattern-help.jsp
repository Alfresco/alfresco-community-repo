<jsp:root version="1.2" xmlns:jsp="http://java.sun.com/JSP/Page">
<jsp:directive.page language="java" pageEncoding="UTF-8" />

<script type="text/javascript">
   function toggleOutputPathPatternHelp()
   {
      var d = document.getElementById('output_path_pattern_help');
      d.style.display = d.style.display == 'block' ? 'none' : 'block';
   }
</script>

<div id="output_path_pattern_help" class="summary infoText statusInfoText" style="display:none; padding: 5px;">
<div>An output path pattern is a <a style="color:blue;" href="http://freemarker.sourceforge.net">FreeMarker</a> expression which is used to specify the path to use when saving a generated asset using variable substitution when creating web content.</div>
<br/>
<div style="font-weight: bold">Guidelines</div>
<div>Output path patterns beginning with a leading slash (such as <tt><jsp:text><![CDATA[/&#36;&#123;webapp&#125;/content/&#36;&#123;name&#125;.xml]]></jsp:text></tt>) will produce paths rooted at the sandbox.  Those not begining with a leading slash will produce paths relative to the current working directory when the create web content wizard is invoked.</div>
<br/>
<div style="font-weight: bold">Variables</div>
<table border="0" cellspacing="1" cellpadding="1">
   <colgroup><col width="15%"/><col width="85%"/></colgroup>
   <tbody>
      <tr><td><tt style="font-weight:bold;">name</tt></td><td>The name of the form instance data as entered by the user in the create web content wizard.</td></tr>
      <tr><td><tt style="font-weight:bold;">webapp</tt></td><td>The name of the webapp in which the form instance data is being created.  Typically, if specifying an absolute output path pattern, the path will begin with the webapp folder (i.e. <tt><jsp:text><![CDATA[/&#36;&#123;webapp&#125;/...]]></jsp:text></tt>)</td></tr>
      <tr><td><tt style="font-weight:bold;">cwd</tt></td><td>The webapp relative path in which the form is being created.</td></tr>
      <tr><td><tt style="font-weight:bold;">extension</tt></td><td>The default extension associated with the mime-type configured for the rendering engine template.  This variable is only available for rendition ouput path patterns.</td></tr>
      <tr><td><tt style="font-weight:bold;">xml</tt></td><td>The xml instance data collected by the form.</td></tr>
      <tr><td><tt style="font-weight:bold;">node</tt></td><td>The form instance data node.  This variable is only available for rendition ouput path patterns.</td></tr>
      <tr><td><tt style="font-weight:bold;">date</tt></td><td>The current date at which the form instance data is being saved.  Refer to the <a style="color:blue;" href="http://freemarker.sourceforge.net/docs/ref_builtins_date.html">FreeMarker date reference</a> for more information.</td></tr>

   </tbody>
</table>
<br/>
<div style="font-weight: bold">For example</div>
<table border="0" cellspacing="1" cellpadding="1">
   <colgroup><col width="25%"/><col width="75%"/></colgroup>
   <tbody>
      <tr><td><tt style="font-weight:bold;"><jsp:text><![CDATA[&#36;&#123;name&#125;.xml]]></jsp:text></tt></td><td>form_name.xml</td></tr>
      <tr><td><tt style="font-weight:bold;"><jsp:text><![CDATA[&#36;&#123;name&#125;.&#36;&#123;extension&#125;]]></jsp:text></tt></td><td>form_name.html</td></tr>
      <tr><td><tt style="font-weight:bold;"><jsp:text><![CDATA[&#36;&#123;webapp&#125;/content/&#36;&#123;name&#125;.xml]]></jsp:text></tt></td><td>/ROOT/content/form_name.xml</td></tr>
      <tr><td><tt style="font-weight:bold;"><jsp:text><![CDATA[&#36;&#123;date&#63;string("yyyy-MM-dd")&#125;.xml]]></jsp:text></tt></td><td>2007-01-09.xml</td></tr>
   </tbody>
</table>
<br/>
<div>For a more complete reference, please refer to the <a style="color: blue" href="http://wiki.alfresco.com">wiki.</a></div>
</div>
</jsp:root>