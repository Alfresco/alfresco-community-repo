//Semicolon is for when this file is integrated with a custom build on one line
//with some other file's contents. Sometimes that makes things not get defined
//properly, particularly with the using the closure below to do all the work.
;(function(){
	//Don't do this work if dojo.js has already done it.
	if(typeof dj_usingBootstrap != "undefined"){
		return;
	}

	var isRhino = false;
	var isSpidermonkey = false;
	var isDashboard = false;
	if((typeof this["load"] == "function")&&((typeof this["Packages"] == "function")||(typeof this["Packages"] == "object"))){
		isRhino = true;
	}else if(typeof this["load"] == "function"){
		isSpidermonkey  = true;
	}else if(window.widget){
		isDashboard = true;
	}

	var tmps = [];
	if((this["djConfig"])&&((djConfig["isDebug"])||(djConfig["debugAtAllCosts"]))){
		tmps.push("debug.js");
	}

	if((this["djConfig"])&&(djConfig["debugAtAllCosts"])&&(!isRhino)&&(!isDashboard)){
		tmps.push("browser_debug.js");
	}

	//Support compatibility packages. Right now this only allows setting one
	//compatibility package. Might need to revisit later down the line to support
	//more than one.
	if((this["djConfig"])&&(djConfig["compat"])){
		tmps.push("compat/" + djConfig["compat"] + ".js");
	}

	var loaderRoot = djConfig["baseScriptUri"];
	if((this["djConfig"])&&(djConfig["baseLoaderUri"])){
		loaderRoot = djConfig["baseLoaderUri"];
	}

	for(var x=0; x < tmps.length; x++){
		var spath = loaderRoot+"src/"+tmps[x];
		if(isRhino||isSpidermonkey){
			load(spath);
		} else {
			try {
				document.write("<scr"+"ipt type='text/javascript' src='"+spath+"'></scr"+"ipt>");
			} catch (e) {
				var script = document.createElement("script");
				script.src = spath;
				document.getElementsByTagName("head")[0].appendChild(script);
			}
		}
	}
})();

// Localization routines

/**
 * Returns canonical form of locale, as used by Dojo.  All variants are case-insensitive and are separated by '-'
 * as specified in RFC 3066
 */
dojo.normalizeLocale = function(locale) {
	return locale ? locale.toLowerCase() : dojo.locale;
};

dojo.searchLocalePath = function(locale, down, searchFunc){
	locale = dojo.normalizeLocale(locale);

	var elements = locale.split('-');
	var searchlist = [];
	for(var i = elements.length; i > 0; i--){
		searchlist.push(elements.slice(0, i).join('-'));
	}
	searchlist.push(false);
	if(down){searchlist.reverse();}

	for(var j = searchlist.length - 1; j >= 0; j--){
		var loc = searchlist[j] || "ROOT";
		var stop = searchFunc(loc);
		if(stop){ break; }
	}
}

/**
 * requireLocalization() is for loading translated bundles provided within a package in the namespace.
 * Contents are typically strings, but may be any name/value pair, represented in JSON format.
 * A bundle is structured in a program as follows, where modulename is mycode.mywidget and
 * bundlename is mybundle:
 *
 * mycode/
 *  mywidget/
 *   nls/
 *    mybundle.js (the fallback translation, English in this example)
 *    de/
 *     mybundle.js
 *    de-at/
 *     mybundle.js
 *    en/
 *     (empty; use the fallback translation)
 *    en-us/
 *     mybundle.js
 *    en-gb/
 *     mybundle.js
 *    es/
 *     mybundle.js
 *   ...etc
 *
 * Each directory is named for a locale as specified by RFC 3066, (http://www.ietf.org/rfc/rfc3066.txt),
 * normalized in lowercase.
 *
 * For a given locale, bundles will be loaded for that locale and all less-specific locales above it, as well
 * as a fallback at the root.  For example, a search for the "de-at" locale will first load nls/de-at/mybundle.js,
 * then nls/de/mybundle.js and finally nls/mybundle.js.  Lookups will traverse the locales in this same order
 * and flatten all the values into a JS object (see dojo.i18n.getLocalization).  A build step can preload the
 * bundles to avoid data redundancy and extra network hits.
 *
 * @param modulename package in which the bundle is found
 * @param bundlename bundle name, typically the filename without the '.js' suffix
 * @param locale the locale to load (optional)  By default, the browser's user locale as defined
 *	in dojo.locale
 */
dojo.requireLocalization = function(modulename, bundlename, locale /*optional*/){
	var bundlepackage = [modulename, "_nls", bundlename].join(".");
	var bundle = dojo.hostenv.startPackage(bundlepackage);
	dojo.hostenv.loaded_modules_[bundlepackage] = bundle; // this seems to be necessary. why?

	if(!dj_undef("dj_localesBuilt", dj_global) && dojo.hostenv.loaded_modules_[bundlepackage]){
		locale = dojo.normalizeLocale(locale);
		for(var i=0; i<dj_localesBuilt.length; i++){
			if(dj_localesBuilt[i] == locale){return;}
		}
	}

	var syms = dojo.hostenv.getModuleSymbols(modulename);
	var modpath = syms.concat("nls").join("/");
	var inherit = false;
	dojo.searchLocalePath(locale, false, function(loc){
		var pkg = bundlepackage + "." + loc;
		var loaded = false;
		if(!dojo.hostenv.findModule(pkg)){
			// Mark loaded whether it's found or not, so that further load attempts will not be made
			dojo.hostenv.loaded_modules_[pkg] = null;
			var module = [modpath];
			if(loc != "ROOT"){module.push(loc);}
			module.push(bundlename);
			var filespec = module.join("/") + '.js';
			loaded = dojo.hostenv.loadPath(filespec, null, function(hash){
				// Use singleton with prototype to point to other bundle, then mix-in result from loadPath
				var clazz = function(){};
				clazz.prototype = inherit;
				bundle[loc] = new clazz();
				for(var j in hash){ bundle[loc][j] = hash[j]; }
			});
		}else{
			loaded = true;
		}
		if(loaded && bundle[loc]){
			inherit = bundle[loc];
		}
	});
};

(function(){
	function preload(locale){
		if(!dj_undef("dj_localesGenerated", dj_global)){
			dojo.setModulePrefix("nls","nls");

			locale = dojo.normalizeLocale(locale);
			dojo.searchLocalePath(locale, true, function(loc){
				for(var i=0; i<dj_localesGenerated.length;i++){
					if(dj_localesGenerated[i] == loc){
						dojo.require("nls.dojo_"+loc);
						return true;
					}
				}
				return false;
			});
		}
	}

	preload(dojo.locale);

	var extra = djConfig.extraLocale;
	if(extra){
		if(!extra instanceof Array){
			extra = [extra];
		}

		for(var i=0; i<extra.length; i++){
			preload(extra[i]);
		}

		var req = dojo.requireLocalization;
		dojo.requireLocalization = function(m, b, locale){
			req(m,b,locale);
			if(locale){return;}
			for(var i=0; i<extra.length; i++){
				req(m,b,extra[i]);
			}
		};
	}
})();
