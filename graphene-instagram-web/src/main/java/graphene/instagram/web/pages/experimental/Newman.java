package graphene.instagram.web.pages.experimental;

import graphene.model.idl.G_VisualType;
import graphene.web.annotations.PluginPage;

import org.apache.tapestry5.annotations.Import;

@PluginPage(visualType = G_VisualType.EXPERIMENTAL, menuName = "Newman", icon = "fa fa-lg fa-fw fa-cogs")
@Import(library = { "context:newman/js/thirdparty/jquery-1.9.1.min.js",
		"context:newman/js/thirdparty/jquery-ui.min.js",
		"context:newman/js/thirdparty/d3.v3.min.js",
		"context:newman/js/thirdparty/bootstrap.min.js",
		"context:newman/js/thirdparty/underscore-min.js",
		"context:newman/js/thirdparty/jquery.bootstrap-growl.js",
		"context:newman/js/thirdparty/jquery.highlight-4.closure.js",
		"context:newman/js/graphtool.js" }, stylesheet = {
		"context:newman/css/thirdparty/jquery-ui.css",
		"context:newman/css/ner.css",
		"context:newman/css/thirdparty/bootstrap.min.css",
		"context:newman/css/thirdparty/bootstrap-glyphicons.css",
		"context:newman/css/default.css" })
public class Newman {

}
