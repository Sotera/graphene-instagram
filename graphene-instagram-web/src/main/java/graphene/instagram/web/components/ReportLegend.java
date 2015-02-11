package graphene.instagram.web.components;

import graphene.dao.StyleService;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.view.LegendItem;

import java.util.List;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

public class ReportLegend {
	@Inject
	private StyleService style;

	@Property
	private LegendItem currentLegendItem;
	@Property
	private List<LegendItem> legendItems;

	public String getStyleFor(final String nodeType, final String searchValue) {
		return style.getStyle(nodeType, false);
	}

	public String getStyleForAddress() {
		return style.getStyle(G_CanonicalPropertyType.ADDRESS.name(), false);
	}

	public String getStyleForEmail() {
		return style.getStyle(G_CanonicalPropertyType.EMAIL_ADDRESS.name(), false);
	}

	public String getStyleForEVENT() {
		return style.getStyle(G_CanonicalPropertyType.EVENT.name(), false);
	}

	public String getStyleForGovId() {
		return style.getStyle(G_CanonicalPropertyType.GOVERNMENTID.name(), false);
	}

	public String getStyleForHighlight() {
		return style.getHighlightStyle();
	}

	public String getStyleForOccupation() {
		return style.getStyle(G_CanonicalPropertyType.OCCUPATION.name(), false);
	}

	public String getStyleForOther() {
		return style.getStyle(G_CanonicalPropertyType.OTHER.name(), false);
	}

	public String getStyleForSubject() {
		return style.getStyle(G_CanonicalPropertyType.ENTITY.name(), false);
	}

	public String getStyleForVisa() {
		return style.getStyle(G_CanonicalPropertyType.OTHER.name(), false);
	}

	@SetupRender
	private void setupRender() {
		legendItems = style.getLegendForReports();
	}
}
