dojo.provide("dojo.html.csshack");

// provide css classes for browser identification/selection

// classnames must be reasonably unique - css namespace is global.

// Need to allow that some servers may choose to pre-process css to remove irrelevant
// rules, in which case these rule names would need to be appended to the GET for the css 
// file so no problems with proxy caches serving wrong css.

(function(){
	var de = document.documentElement;
	if (de && !djConfig.disableCssHack) {
		with (dojo.render.html) {
			var cl = {b_ie: ie,
						b_ie55: ie55,
						b_ie6: ie60,
						b_ie7: ie70,
						b_iequirks: ie && quirks,
						b_opera: opera,
						b_khtml: khtml,
						b_safari: safari,
						b_gecko: mozilla
						}; // no dojo unsupported browsers
		}
		var cla = [de.className];
		for (var key in cl) {
			if (cl[key]) {
				cla.push(key);
			}
		}
		de.className = cla.join(' ');
	}
})();