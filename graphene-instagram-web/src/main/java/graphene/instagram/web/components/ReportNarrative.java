package graphene.instagram.web.components;

import java.util.Collection;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.graphene.augment.mitie.model.MitieResponse;

public class ReportNarrative {
	@Property
	@Parameter(required = true, autoconnect = true)
	private Collection<String> sentences;

	@Property
	private String sentence;

	@Property
	@Parameter(required = true, autoconnect = true)
	private MitieResponse r;
}
