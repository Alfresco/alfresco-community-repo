<#ftl ns_prefixes={"D":"http://www.alfresco.org/alfresco/article","xsi":"http://www.w3.org/2001/XMLSchema-instance"}>
<style>
	.article_details {
		background: #fff;
		padding: 10px;
		position: relative;
		font-family: helvetica;
		font-size: 12px;
	}

	.article_title{
		padding: 8px;
		font-size: 20px;
		color: #666;
	}
	
	.article_author {
		background: #fff;
		border: 1px solid #e2e1e1;
		position: relative;
		padding:10px;
	}
	
	.article_author_picture {
	    display: block;
	    float: left;
		width: 80px;
		margin: 2px;
	}

	.article_author_name {
	}

	.article_teaser {
		clear: left;
		margin: 10px 0 10px 0px;
	}

	.article_tags {
		margin: 10px 0 10px 0px;
	}

	.article_pages {
		margin: 10px 0 10px 0px;
	}

	.article_page_picture_left {
	    display: block;
	    float: left;
		width: 200px;
		padding-right: 15px;
		padding-bottom: 15px;
	}

	.article_page_picture_right {
	    display: block;
	    float: right;
		width: 200px;
		padding-left: 15px;
		padding-bottom: 15px;
	}

	.article_page_picture {
		width: 200px;
		padding: 2px;
	}

	.article_related {
		margin: 10px 0 10px 0px;
	}
</style>
<div class="article_details">

<div class="article_title">
	${article.title}
</div>	

<div class="article_author">
	<#if article.author[0]??>
		<#list article.author as author>
			<#include "${author?replace('.xml', '-details.html')}" parse=false>
		</#list>
	</#if>
</div>

<div class="article_teaser">
	<span>${article.location} ${article.date}</span> - ${article.teaser}
</div>

