<div class="blogTitle">Blog Status</div>

<div id="blogContainer">
   <div class="blogStatusTitle">Pending Articles</div>
   <div class="blogPending">
<#list blogSpace.pending as p>
   	<div class="blogPost">
   	   <div class="blogPostName">
      	   ${p.name}, ${p.properties["cm:title"]!""}
   	   </div>
   	   <div class="blogPostActions">
   	      <span class="blogPostAction"><img src="${url.context}/images/icons/blog_post.png"><input type="button" href="${scripturl("?nodeRef=" + p.parent.nodeRef + "&n=" + p.nodeRef + "&a=p")}" value="Post" /></span>
   	   </div>
   	</div>
</#list>
   </div>

   <div class="blogStatusTitle">Published Articles</div>
   <div class="blogPublished">
<#list blogSpace.published as p>
   	<div class="blogPost">
   	   <div class="blogPostName">
   	      <a href="${p.properties["blg:link"]}" target="_blank">${p.properties["cm:title"]!""} (${p.name}) Published=${p.properties["blg:published"]?string}</a>
   	   </div>
   	   <div class="blogPostActions">
   	      <span class="blogPostAction"><img src="${url.context}/images/icons/blog_update.png"><input type="button" href="${scripturl("?nodeRef=" + p.parent.nodeRef + "&n=" + p.nodeRef + "&a=u")}" value="Update" /></span>
   	      <span class="blogPostAction"><img src="${url.context}/images/icons/blog_remove.png"><input type="button" href="${scripturl("?nodeRef=" + p.parent.nodeRef + "&n=" + p.nodeRef + "&a=r")}" value="Remove" /></span>
   	   </div>
   	</div>
</#list>
   </div>
</div>

<style>
.blogTitle
{
	font-family: "Trebuchet MS", Verdana, Helvetica, sans-serif;
	font-size: medium;
	font-weight: bold;
	margin: -8px 0px 4px;
	float: left;
}

.blogStatusTitle
{
	padding: 4px 0px 0px 0px;
	font-weight: bold;
   clear: left;
	float: left;
}

.blogPending
{
   clear: left;
   float: left;
}

.blogPublished
{
   clear: left;
   float: left;
}

.blogPost
{
   clear: left;
   float: left;
}

#blogContainer
{
	clear: left;
}
</style>
