package graphene.instagram.web.pages.experimental;

import graphene.model.idl.G_VisualType;
import graphene.web.annotations.PluginPage;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

@Import(stack = { "GlobeStack" })
@PluginPage(visualType = G_VisualType.EXPERIMENTAL, menuName = "Globe 2", icon = "fa fa-lg fa-fw fa-globe")
public class Globe2 {
	@Inject
	@Path("context:globe/country_iso3166.json")
	@Property
	private Asset country_iso3166;
	@Inject
	@Path("context:globe/country_lat_lon.json")
	@Property
	private Asset country_lat_lon;

	@Inject
	@Path("context:globe/js/main.js")
	@Property
	private Asset mainjs;

	@Property
	private String loadingMessage;
	@Property
	private String title;
	@Property
	private String subtitle;

	@SetupRender
	public void setuprender() {
		loadingMessage = "Loading Transactions.  Please wait...";
		title = "Imports and Exports";
		subtitle = "Report Type XXXX from 2008-2014";
	}
}
