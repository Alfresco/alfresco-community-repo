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

	.article_page_title {
		font-weight: bold;
	}

	.article_related {
		margin: 10px 0 10px 0px;
	}

	.legal_text {
		font-style: italic;
		font-size: 10px;		
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
<div class="article_tags">
	<#if article.tag[0]??>
		<b>Tags: </b>
		<#list article.tag as tag>
				${tag};
		</#list>	
	</#if>
</div>
<div class="article_pages">
	<#list article.page as page>
		<#if page.page_title[0]??>
			<div class="article_page_title">${page.page_title}</div>
		</#if>
		<#if page.page_picture[0]??>
			<#if page.page_picture.image["@xsi:nil"] == "false">
				<#if page_index % 2 == 1>
					<div  class="article_page_picture_left">
						<img src="${page.page_picture.image}" class="article_page_picture"/>
						<#if page.page_picture.caption["@xsi:nil"] == "false">			
							<div>${page.page_picture.caption}</div>
						</#if>
					</div>	
				<#else>
					<div  class="article_page_picture_right">
						<img src="${page.page_picture.image}" class="article_page_picture"/>
						<#if page.page_picture.caption["@xsi:nil"] == "false">			
							<div>${page.page_picture.caption}</div>
						</#if>
					</div>	
				</#if>
			</#if>
		</#if>
		${page.page_body}
	</#list>	
</div>
<div class="article_related">
	<div><b>Related</b></div>	
	<#if article.related[0]??>
		<div>
			<ul>
				<#list article.related as related>
					<li><a href="${related.item_link}">${related.item_label}</a>
				</#list>
			</ul>	
		</div>	
	</#if>
</div>
<div class="legal_text">
	<#if article.include_legal_text == "true">
		<#include "/legal.html" parse=false>	
	</#if>
</div>


