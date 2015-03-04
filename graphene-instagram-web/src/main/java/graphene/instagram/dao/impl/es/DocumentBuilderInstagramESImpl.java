package graphene.instagram.dao.impl.es;

import graphene.dao.DocumentBuilder;
import graphene.dao.DocumentGraphParser;
import graphene.instagram.model.media.Media;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_Property;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_SearchResult;
import graphene.model.idl.G_SingletonRange;
import graphene.services.HyperGraphBuilder;
import graphene.util.validator.ValidationUtils;

import java.io.IOException;
import java.util.ArrayList;
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
		final G_SearchResult sr = null;
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
				additionalProperties.put(DocumentGraphParser.SCORE, d.asDouble());
				additionalProperties.put(DocumentGraphParser.CARDINAL_ORDER, index + 1);
				if (hit.has(DocumentGraphParser.HIGHLIGHT)) {
					additionalProperties.put(DocumentGraphParser.HIGHLIGHT,
							hit.findValue(DocumentGraphParser.HIGHLIGHT));
				}
			}
			final DocumentGraphParser parserForObject = phgb.getParserForObject(o);
			if (parserForObject != null) {
				final ArrayList<G_Property> result = new ArrayList<G_Property>();

				result.add(G_Property
						.newBuilder()
						.setFriendlyText(DocumentGraphParser.SCORE)
						.setRange(
								G_SingletonRange.newBuilder().setType(G_PropertyType.DOUBLE).setValue(d.asDouble(0.0d))
										.build()).build());
				result.add(G_Property
						.newBuilder()
						.setFriendlyText(DocumentGraphParser.CARDINAL_ORDER)
						.setRange(
								G_SingletonRange.newBuilder().setType(G_PropertyType.LONG).setValue(index + 1).build())
						.build());
				if (hit.has(DocumentGraphParser.HIGHLIGHT)) {
					result.add(G_Property
							.newBuilder()
							.setFriendlyText(DocumentGraphParser.HIGHLIGHT)
							.setRange(
									G_SingletonRange.newBuilder().setType(G_PropertyType.STRING).setValue(index + 1)
											.build()).build());
				}
				sr.setScore(d.asDouble(0.0d));
				sr.setResult(result);
				final Map<String, List<G_Property>> map = new HashMap<String, List<G_Property>>();
				map.put(DocumentGraphParser.SUMMARY, (List<G_Property>) parserForObject.populateSearchResult(sr, sq));
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
