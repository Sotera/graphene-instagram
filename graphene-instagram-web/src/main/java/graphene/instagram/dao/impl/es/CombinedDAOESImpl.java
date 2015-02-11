/**
 * 
 */
package graphene.instagram.dao.impl.es;

import graphene.dao.CombinedDAO;
import graphene.dao.DocumentGraphParser;
import graphene.dao.es.ESRestAPIConnection;
import graphene.dao.es.JestModule;
import graphene.instagram.model.media.Media;
import graphene.model.idl.G_SearchTuple;
import graphene.model.idl.G_SymbolConstants;
import graphene.model.query.EntityQuery;
import graphene.model.view.GrapheneResults;
import graphene.util.G_CallBack;
import graphene.util.validator.ValidationUtils;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;

/**
 * @author djue
 * 
 */
public class CombinedDAOESImpl implements CombinedDAO {

	private Logger logger;
	private final ESRestAPIConnection c;
	private final String auth;

	@Inject
	private JestClient client;

	private final ObjectMapper mapper;

	@Inject
	@Symbol(JestModule.ES_SEARCH_INDEX)
	private String indexName;

	@Inject
	@Symbol(JestModule.ES_SERVER)
	private String host;

	@Inject
	@Symbol(G_SymbolConstants.DEFAULT_MAX_SEARCH_RESULTS)
	private Long maxSearchResults;

	@Inject
	public CombinedDAOESImpl(final ESRestAPIConnection c, final Logger logger) {
		this.logger = logger;
		auth = null;
		this.c = c;
		mapper = new ObjectMapper(); // can reuse, share globally
	}

	public CombinedDAOESImpl(final String authEncoding, final ESRestAPIConnection c) {
		auth = authEncoding;
		this.c = c;
		mapper = new ObjectMapper(); // can reuse, share globally
	}

	/**
	 * TODO: Consider some kind of configurable mapping between the string of
	 * _type and the class to cast to.
	 * 
	 * @param index
	 * 
	 * @param hit
	 * @return
	 */
	protected Object buildEntityFromDocument(final int index, final JsonNode hit) {
		// ////////////////////////////////
		String type = null;
		DoubleNode d = null;
		JsonNode source = null;
		Object o = null;
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
				o = mapper.readValue(source.toString(), Media.class);
				final Media castObj = (Media) o;
				additionalProperties = castObj.getAdditionalProperties();
				o = castObj;
			} 
			else {
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

		return o;
	}

	@Override
	public long count(final EntityQuery pq) throws Exception {
		if (ValidationUtils.isValid(pq) && ValidationUtils.isValid(pq.getAttributeList())) {
			pq.getAttributeList().get(0);
			String schema = pq.getSchema();
			if (!ValidationUtils.isValid(schema)) {
				schema = c.getIndexName();
			}
			final String term = pq.getAttributeList().get(0).getValue();
			final long x = c.performCount(null, ESUrlConstants.defaultHost, schema, ESUrlConstants.typeAll,
					ESUrlConstants.fieldAll, term);
			return x;
		}
		logger.warn("Did not find any values for " + pq);
		return 0;
	}

	@Override
	public List<Object> findById(final EntityQuery pq) {
		logger.debug("Query " + pq);
		final List<Object> objects = new ArrayList<Object>();
		String schema = pq.getSchema();
		if (!ValidationUtils.isValid(schema)) {
			schema = indexName;
		}
		String _qResp = null;

		// = c.performQuery(/* ESUrlConstants.auth */null, host,
		// schema, ESUrlConstants.typeAll, pq);

		final StringBuffer sb = new StringBuffer();
		// Dead simple, just coalesces the values as one long phrase
		for (final G_SearchTuple<String> qi : pq.getAttributeList()) {
			sb.append(qi.getValue() + " ");
		}
		final String terms = sb.toString().trim();
		if (ValidationUtils.isValid(terms)) {
			logger.debug("Searching for terms: " + terms + " from query " + pq);
			// Let's decide that at least half of the terms listed need to
			// appear.
			Integer halfTerms = pq.getAttributeList().size() / 2;
			if (halfTerms <= 1) {
				halfTerms = 1;
			}
			final MatchQueryBuilder qbc = QueryBuilders.matchPhraseQuery("_all", terms.toString());

			final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
			searchSourceBuilder.query(qbc);
			if (pq.getMaxResult() == 0) {
				logger.warn("NO MAX RESULT SUPPLIED FOR EntityQuery!  Setting to one.");
				pq.setMaxResult(1l);
			}
			logger.debug("SSB: \n" + searchSourceBuilder.toString());
			final Search action = new Search.Builder(searchSourceBuilder.toString()).addIndex(indexName)
					.setParameter("from", pq.getFirstResult()).setParameter("size", pq.getMaxResult()).build();
			logger.debug("Action:\n" + action.toString());
			SearchResult result;
			try {
				result = c.getClient().execute(action);
				final String resultString = result.getJsonString();
				_qResp = resultString;
				logger.debug(_qResp);
			} catch (final Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (_qResp != null) {
			JsonNode rootNode;
			try {
				rootNode = mapper.readValue(_qResp, JsonNode.class);

				int _totalResults = -1;
				if ((_totalResults == -1) && (rootNode != null) && (rootNode.get("hits") != null)
						&& (rootNode.get("hits").get("total") != null)) {
					_totalResults = rootNode.get("hits").get("total").asInt();
					logger.debug("Found " + _totalResults + " hits in hitparent!");
					final List<JsonNode> hits = rootNode.get("hits").findValues("hits");
					final ArrayNode actualListOfHits = (ArrayNode) hits.get(0);

					logger.debug("actualListOfHits was serialized into  " + actualListOfHits.size() + " object(s)");
					for (int i = 0; i < actualListOfHits.size(); i++) {
						final JsonNode currentHit = actualListOfHits.get(i);
						if (ValidationUtils.isValid(currentHit)) {
							final Object entity = buildEntityFromDocument(i, currentHit);
							objects.add(entity);
						}
					}
				} else {
					logger.warn("Response was unexpected: " + _qResp);
				}
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return objects;
	}

	@Override
	public List<Object> findByQuery(final EntityQuery pq) throws Exception {
		//
		// final List<Object> objects = new ArrayList<Object>();
		// String index = pq.getSchema();
		// if (!ValidationUtils.isValid(index)) {
		// index = indexName;
		// }
		// final StringBuffer terms = new StringBuffer();
		// // Dead simple, just coalesces the values as one long phrase
		// for (final G_SearchTuple<String> qi : pq.getAttributeList()) {
		// qi.getSpecificPropertyType();
		// terms.append(qi.getValue() + " ");
		// }
		// final String queryTerms = terms.toString().trim();
		// final CommonTermsQueryBuilder qbc = QueryBuilders.commonTerms("_all",
		// queryTerms).lowFreqOperator(Operator.AND);
		//
		// final SearchSourceBuilder searchSourceBuilder = new
		// SearchSourceBuilder();
		// final HighlightBuilder h = new HighlightBuilder().field("NARR");
		// searchSourceBuilder.query(qbc).highlight(h).minScore((float)
		// pq.getMinimumScore())
		// .sort(SortBuilders.scoreSort());
		// if (pq.getMaxResult() == 0) {
		// logger.warn("NO MAX RESULT SUPPLIED FOR EntityQuery!  Setting to 200.");
		// pq.setMaxResult(maxSearchResults);
		// }
		// logger.debug("SSB: \n" + searchSourceBuilder.toString());
		// final Search action = new
		// Search.Builder(searchSourceBuilder.toString()).addIndex(index)
		// .setParameter("from", pq.getFirstResult()).setParameter("size",
		// pq.getMaxResult()).build();
		// logger.debug("Action:\n" + action.toString());
		// final String _qResp = c.performQuery(null, host, index,
		// ESUrlConstants.typeAll, action);
		//
		// JsonNode rootNode;
		// rootNode = mapper.readValue(_qResp, JsonNode.class);
		// int _totalResults = -1;
		// if ((_totalResults == -1) && (rootNode != null) &&
		// (rootNode.get("hits") != null)
		// && (rootNode.get("hits").get("total") != null)) {
		// _totalResults = rootNode.get("hits").get("total").asInt();
		// logger.debug("Found " + _totalResults + " hits in hitparent!");
		// final List<JsonNode> hits = rootNode.get("hits").findValues("hits");
		// final ArrayNode actualListOfHits = (ArrayNode) hits.get(0);
		//
		// // Go through the results, and keep a map of
		// // id->DocumentGraphParser.SCORE, as well as
		// // ids to fetch
		// logger.debug("actualListOfHits was serialized into  " +
		// actualListOfHits.size() + " object(s)");
		// for (int i = 0; i < actualListOfHits.size(); i++) {
		// final JsonNode currentHit = actualListOfHits.get(i);
		// if (ValidationUtils.isValid(currentHit)) {
		// final Object entity = buildEntityFromDocument(i, currentHit);
		// objects.add(entity);
		// }
		// }
		// }
		//
		// return objects;
		final GrapheneResults<Object> gr = findByQueryWithMeta(pq);
		return gr.getResults();
	}

	@Override
	public GrapheneResults<Object> findByQueryWithMeta(final EntityQuery pq) throws Exception {
		final GrapheneResults<Object> results = new GrapheneResults<Object>();
		final List<Object> objects = new ArrayList<Object>();

		final String _qResp = c.performQuery(null, host, pq);

		JsonNode rootNode;
		rootNode = mapper.readValue(_qResp, JsonNode.class);
		int _totalResults = -1;
		if ((_totalResults == -1) && (rootNode != null) && (rootNode.get("hits") != null)
				&& (rootNode.get("hits").get("total") != null)) {
			_totalResults = rootNode.get("hits").get("total").asInt();
			logger.debug("Found " + _totalResults + " hits in hitparent!");
			results.setNumberOtResultsTotal(_totalResults);
			final List<JsonNode> hits = rootNode.get("hits").findValues("hits");
			final ArrayNode actualListOfHits = (ArrayNode) hits.get(0);

			// Go through the results, and keep a map of
			// id->DocumentGraphParser.SCORE, as well as
			// ids to fetch
			logger.debug("actualListOfHits was serialized into  " + actualListOfHits.size() + " object(s)");
			results.setNumberOfResultsReturned(actualListOfHits.size());
			for (int i = 0; i < actualListOfHits.size(); i++) {
				final JsonNode currentHit = actualListOfHits.get(i);
				if (ValidationUtils.isValid(currentHit)) {
					final Object entity = buildEntityFromDocument(i, currentHit);
					objects.add(entity);
				} else {
					logger.error("Invalid search result at index " + i + " for query " + pq.toString());
				}
			}
		}

		results.setResults(objects);
		return results;
	}

	@Override
	public List<Object> getAll(final long offset, final long maxResults) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getReadiness() {
		return 1.0;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean performCallback(final long offset, final long maxResults, final G_CallBack<Object, EntityQuery> cb,
			final EntityQuery q) {
		// TODO Auto-generated method stub
		try {
			for (final Object obj : findByQuery(q)) {

				cb.callBack(obj, q);
			}
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void setReady(final boolean b) {
		// TODO Auto-generated method stub

	}
}
