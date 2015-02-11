package graphene.instagram.web.pages.experimental;

import graphene.model.idl.G_VisualType;
import graphene.services.HyperGraphBuilder;
import graphene.util.ExceptionUtil;
import graphene.util.validator.ValidationUtils;
import graphene.web.annotations.PluginPage;
import graphene.web.components.ui.CytoscapeGraph;
import mil.darpa.vande.converters.cytoscapejs.V_CSGraph;
import mil.darpa.vande.generic.V_GenericGraph;
import mil.darpa.vande.generic.V_GraphQuery;

import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

@PluginPage(visualType = G_VisualType.EXPERIMENTAL, menuName = "Graph Viewer", icon = "fa fa-lg fa-fw fa-code-fork")
public class GraphViewer {
	@Property
	private String theProperty;

	@Component(parameters = { "csgraph=testData",
			"graphTitle=literal:Cytoscape Graph", "options=options" })
	private CytoscapeGraph mygraph;

	void onActivate(String theProperty) {
		this.theProperty = theProperty;
	}

	String onPassivate() {
		return theProperty;
	}

	@InjectService("HyperProperty")
	private HyperGraphBuilder propertyGraphBuilder;

	@Inject
	private Logger logger;

	@Inject
	private AlertManager alertManager;

	@Property
	@Persist
	private V_CSGraph graph;

	public JSONObject getOptions() {
		JSONObject options = new JSONObject();

		JSONArray styleArray = new JSONArray();

		// node style
		JSONObject nodeCSSStyle = new JSONObject()
				.put("background-color", "red").put("font-size", "12")
				.put("content", "data(label)").put("text-valign", "center")
				.put("background-color", "data(color)").put("color", "#FFF");
		JSONObject nodeStyle = new JSONObject();
		nodeStyle.put("selector", "node");
		nodeStyle.put("css", nodeCSSStyle);

		styleArray.put(nodeStyle);
		// parent node
		// node style
		JSONObject parentnodeCSSStyle = new JSONObject()
				.put("padding-top", "10px").put("padding-bottom", "10px")
				.put("padding-left", "10px").put("padding-right", "10px")
				.put("text-valign", "top").put("text-halign", "center");
		JSONObject parentnodeStyle = new JSONObject();
		parentnodeStyle.put("selector", "$node > node");
		parentnodeStyle.put("css", parentnodeCSSStyle);
		styleArray.put(parentnodeStyle);
		// selected style

		JSONObject selectedCSSStyle = new JSONObject();
		selectedCSSStyle.put("background-color", "#000");
		selectedCSSStyle.put("line-color", "#000");
		selectedCSSStyle.put("target-arrow-color", "#000");
		selectedCSSStyle.put("text-outline-color", "#000");

		JSONObject selectedStyle = new JSONObject();
		selectedStyle.put("selector", ":selected");
		selectedStyle.put("css", selectedCSSStyle);

		styleArray.put(selectedStyle);

		// edge style

		JSONObject edgeCSSStyle = new JSONObject();
		edgeCSSStyle.put("width", "2");
		edgeCSSStyle.put("target-arrow-shape", "triangle");
		JSONObject edgeStyle = new JSONObject();
		edgeStyle.put("selector", "edge");
		edgeStyle.put("css", edgeCSSStyle);

		styleArray.put(edgeStyle);

		options.put("minZoom", "0.25");
		options.put("maxZoom", "3");
		options.put("style", styleArray);

		JSONObject layout = new JSONObject();
		layout.put("name", "cose");
		layout.put("padding", "0");

		options.put("layout", layout);
		options.put("title", "Graph for " + theProperty);
		return options;
	}

	/**
	 * 
	 * @return
	 */

	public V_CSGraph getTestData() {
		if (ValidationUtils.isValid(theProperty)) {
			try {
				V_GenericGraph g = null;
				graph = null;
				try {
					V_GraphQuery q = new V_GraphQuery();
					q.addSearchIds(theProperty);
					q.setDirected(false);
					q.setMaxNodes(400);
					q.setMaxEdgesPerNode(50);
					q.setMaxHops(3);
					g = propertyGraphBuilder.makeGraphResponse(q);
					graph = new V_CSGraph(g, true);
				} catch (Exception e) {
					logger.error(ExceptionUtil.getRootCauseMessage(e));
					e.printStackTrace();
				}
			} catch (Exception e1) {
				alertManager.alert(Duration.SINGLE, Severity.ERROR,
						"ERROR: " + e1.getMessage());
				e1.printStackTrace();
			}
		} else {
			graph = null;
		}
		return graph;
	}

	void setupRender() {

	}
}
