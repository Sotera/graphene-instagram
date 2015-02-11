package graphene.instagram.web.components;

import graphene.dao.StyleService;

import java.util.Collection;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

/**
 * A report section is a list of objects that will be rendered as beandisplays.
 * 
 * @author djue
 * 
 */
public class ReportSection {
	@Inject
	protected StyleService style;
	@Property
	@Parameter(required = true, autoconnect = true)
	private String typePluralName;
	@Property
	private int index;
	@Property
	@Parameter(required = true, autoconnect = true)
	private String typeName;

	@Property
	@Parameter(autoconnect = true)
	private String typeCount;
	@Property
	@Parameter(required = false, autoconnect = true)
	private String color;

	@Property
	@Parameter(required = true, autoconnect = true)
	private Collection<Object> listOfThings;

	@Property
	private Object currentThing;

}
