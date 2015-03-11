package graphene.instagram.dao.impl.es;

import graphene.dao.DocumentBuilder;
import graphene.dao.G_Parser;
import graphene.instagram.model.media.Media;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_Property;
import graphene.model.idl.G_PropertyTag;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_SearchResult;
import graphene.model.idl.G_SingletonRange;
import graphene.model.idlhelper.PropertyHelper;
import graphene.services.HyperGraphBuilder;
import graphene.util.validator.ValidationUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.DoubleNode;

public class DocumentBuilderInstagramESImpl implements DocumentBuilder {
	@Inject
	private Logger logger;

	protected ObjectMapper mapper;

	@Inject
	private HyperGraphBuilder<Object> phgb;

	public DocumentBuilderInstagramESImpl() {
		mapper = new ObjectMapper();
	}

	@Override
	public G_SearchResult buildSearchResultFromDocument(final int index, final JsonNode hit, final G_EntityQuery sq) {

		String type = null;
		DoubleNode d = null;
		JsonNode source = null;
		Object o = null;
		G_SearchResult sr = null;
		
		try {
			final JsonNode x = hit.get("_type");
			if (x == null) {
				logger.error("Could not find the type of result. There may be something wrong with your ElasticSearch instance");
			}
			type = x.asText();
			d = (DoubleNode) hit.findValue("_score");
			if (d == null) {
				logger.error("Could not find the score of result. There may be something wrong with your ElasticSearch instance");
			}
			Map<String, Object> additionalProperties = null;
			// do something based on the type:

			source = hit.findValue("_source");
			if (ValidationUtils.isValid(source)) {

				if ("media".equalsIgnoreCase(type)) {

					o = mapper.readValue(source.toString(), Media.class);
					final Media castObj = (Media) o;
					additionalProperties = castObj.getAdditionalProperties();
					o = castObj;
				} else {
					logger.error("Could not parse type " + type);
				}

			} else {
				logger.error("Could not find the source of result. There may be something wrong with your ElasticSearch instance");
			}
			if (additionalProperties != null) {
				additionalProperties.put(G_Parser.SCORE, d.asDouble());
				additionalProperties.put(G_Parser.CARDINAL_ORDER, index + 1);
				if (hit.has(G_Parser.HIGHLIGHT)) {
					additionalProperties.put(G_Parser.HIGHLIGHT,
							hit.findValue(G_Parser.HIGHLIGHT));
				}
			}
			final G_Parser parserForObject = phgb.getParserForObject(o);
			if (parserForObject != null) {
				final ArrayList<G_Property> props = new ArrayList<G_Property>();
				
				props.add(new PropertyHelper(G_Parser.SCORE, G_Parser.SCORE, 0.0, Collections.singletonList(G_PropertyTag.STAT)));
				props.add(new PropertyHelper(G_Parser.CARDINAL_ORDER, G_Parser.CARDINAL_ORDER, (index + 1), Collections.singletonList(G_PropertyTag.STAT)));
				props.add(new PropertyHelper(G_Parser.HIGHLIGHT, G_Parser.HIGHLIGHT, (index + 1), Collections.singletonList(G_PropertyTag.STAT)));
				
				sr = new G_SearchResult();
				sr.setScore(d.asDouble(0.0d));
				sr.setResult(o);
				final Map<String, List<G_Property>> map = new HashMap<String, List<G_Property>>();
				map.put(G_Parser.ROWFORTABLE, (List<G_Property>) parserForObject.buildEntityFromDocument(sr, sq));
				sr.setNamedProperties(map);

				// populatedTableResults.add(parserForObject.getAdditionalProperties(o));
				
			} else {
				logger.error("Could not find parser for " + o);
			}

		} catch (final JsonParseException e) {

			logger.error("Parsing exception " + type + " " + e.getMessage());

		} catch (final JsonMappingException e) {

			logger.error("JSON Mapping Exception " + type + " " + e.getMessage());
			if (source != null) {
				logger.error("Source was \n\n\n" + source.toString() + "\n\n\n");
			}
		} catch (final IOException e) {

			logger.error("IO Exception " + type + " " + e.getMessage());
		}

		return sr;
	}
}
