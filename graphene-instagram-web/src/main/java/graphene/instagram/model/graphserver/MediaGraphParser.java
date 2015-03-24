package graphene.instagram.model.graphserver;

import graphene.dao.G_Parser;
import graphene.instagram.model.media.CommentData;
import graphene.instagram.model.media.LikeData;
import graphene.instagram.model.media.Media;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
import graphene.model.idl.G_Entity;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_EntityTag;
import graphene.model.idl.G_Property;
import graphene.model.idl.G_PropertyTag;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_SearchResult;
import graphene.model.idlhelper.EntityHelper;
import graphene.model.idlhelper.PropertyHelper;
import graphene.util.validator.ValidationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.darpa.vande.generic.V_GenericGraph;
import mil.darpa.vande.generic.V_GenericNode;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This is a parser for a particular document type in this application.
 * 
 * @author djue
 * 
 */
public class MediaGraphParser extends InstagramParser<Media> {

	public MediaGraphParser() {
		supported = new ArrayList<String>(1);
		supported.add("media");
		parenting = false;
	}

	@Override
	public G_Entity buildEntityFromDocument(final JsonNode sr, final G_EntityQuery q) {

		final Map<String, G_Property> map = new HashMap<String, G_Property>();
		resetLists();
		final Media p = getClassFromJSON(sr, Media.class);
		if (!ValidationUtils.isValid(p)) {
			logger.error("Error building DTO from Json for document " + sr.toString());
			return null;
		}
		
		map.put(G_Parser.REPORT_TYPE,
				new PropertyHelper(G_Parser.REPORT_TYPE, G_Parser.REPORT_TYPE, "media", Collections
						.singletonList(G_PropertyTag.ENTITY_TYPE)));

		map.put(MEDIA_LABEL,
				new PropertyHelper(MEDIA_LABEL, MEDIA_LABEL, getReportLabel(p), Collections
						.singletonList(G_PropertyTag.LABEL)));
		map.put(MEDIA_ID,
				new PropertyHelper(MEDIA_ID, MEDIA_ID, p.getId(), Collections.singletonList(G_PropertyTag.ID)));
		map.put(MEDIA_LINK,
				new PropertyHelper(MEDIA_LINK, MEDIA_LINK, p.getLink(), Collections
						.singletonList(G_PropertyTag.LINKED_DATA)));
		map.put(MEDIA_OWNER,
				new PropertyHelper(MEDIA_OWNER, MEDIA_OWNER, p.getUsername(), Collections
						.singletonList(G_PropertyTag.ID)));
		map.put(MEDIA_CREATED_TIME, new PropertyHelper(MEDIA_CREATED_TIME, MEDIA_CREATED_TIME, p.getCreatedTime(),
				G_PropertyType.DATE, Collections.singletonList(G_PropertyTag.DATE)));
		map.put(MEDIA_CAPTION_TEXT, new PropertyHelper(MEDIA_CAPTION_TEXT, MEDIA_CAPTION_TEXT, p.getCaptionText(),
				Collections.singletonList(G_PropertyTag.TEXT)));
		map.put(MEDIA_LIKE_COUNT, new PropertyHelper(MEDIA_LIKE_COUNT, MEDIA_LIKE_COUNT, p.getLikes().getCount(),
				G_PropertyType.LONG, Collections.singletonList(G_PropertyTag.STAT)));
		map.put(MEDIA_COMMENT_COUNT, new PropertyHelper(MEDIA_COMMENT_COUNT, MEDIA_COMMENT_COUNT, p.getComments()
				.getCount(), G_PropertyType.LONG, Collections.singletonList(G_PropertyTag.STAT)));
		map.put(MEDIA_THUMBNAIL,
				new PropertyHelper(MEDIA_THUMBNAIL, MEDIA_THUMBNAIL, p.getThumbnail(), Collections
						.singletonList(G_PropertyTag.LINKED_DATA)));
		map.put(G_Parser.DTO,
				new PropertyHelper(G_Parser.DTO, G_Parser.DTO, p, G_PropertyType.OTHER, G_PropertyTag.ENTITY_TYPE));

		if ((p.getLocation().getLatitude() != null) && (p.getLocation().getLongitude() != null)) {
			map.put(MEDIA_LOCATION_LATLON,
					new PropertyHelper(MEDIA_LOCATION_LATLON, MEDIA_LOCATION_LATLON, p.getLocation().getLatitude()
							+ ", " + p.getLocation().getLongitude(), Collections.singletonList(G_PropertyTag.GEO)));
		}

		map.put(MEDIA_LOCATION_NAME, new PropertyHelper(MEDIA_LOCATION_NAME, MEDIA_LOCATION_NAME, p.getLocation()
				.getName(), Collections.singletonList(G_PropertyTag.GEO)));

		map.put(ALL_ATS, new PropertyHelper(ALL_ATS, ALL_ATS, G_PropertyType.OTHER, p.getAllAts(), G_PropertyTag.STAT));
		map.put(ALL_HASHTAGS, new PropertyHelper(ALL_HASHTAGS, ALL_HASHTAGS, G_PropertyType.OTHER, p.getAllHashTags(),
				G_PropertyTag.STAT));

		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_LABEL)
		// .setKey(MEDIA_LABEL)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(getReportLabel(p)).setType(G_PropertyType.STRING)
		// .build()).build());
		// list.add(G_Property.newBuilder().setFriendlyText(MEDIA_ID).setKey(MEDIA_ID)
		// .setRange(G_SingletonRange.newBuilder().setValue(p.getId()).setType(G_PropertyType.STRING).build())
		// .build());
		//
		// list.add(G_Property.newBuilder().setFriendlyText(MEDIA_LINK).setKey(MEDIA_LINK)
		// .setRange(G_SingletonRange.newBuilder().setValue(p.getLink()).setType(G_PropertyType.STRING).build())
		// .build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_OWNER)
		// .setKey(MEDIA_OWNER)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getUsername()).setType(G_PropertyType.STRING).build())
		// .build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_CREATED_TIME)
		// .setKey(MEDIA_CREATED_TIME)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getCreatedTime()).setType(G_PropertyType.DATE).build())
		// .build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_CAPTION_TEXT)
		// .setKey(MEDIA_CAPTION_TEXT)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getCaptionText()).setType(G_PropertyType.STRING)
		// .build()).build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_LIKE_COUNT)
		// .setKey(MEDIA_LIKE_COUNT)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getLikes().getCount()).setType(G_PropertyType.LONG)
		// .build()).build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_COMMENT_COUNT)
		// .setKey(MEDIA_COMMENT_COUNT)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getComments().getCount()).setType(G_PropertyType.LONG)
		// .build()).build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_THUMBNAIL)
		// .setKey(MEDIA_THUMBNAIL)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getThumbnail()).setType(G_PropertyType.STRING).build())
		// .build());
		//
		// if ((p.getLocation().getLatitude() != null) &&
		// (p.getLocation().getLongitude() != null)) {
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_LOCATION_LATLON)
		// .setKey(MEDIA_LOCATION_LATLON)
		// .setRange(
		// G_SingletonRange.newBuilder()
		// .setValue(p.getLocation().getLatitude() + ", " +
		// p.getLocation().getLongitude())
		// .setType(G_PropertyType.GEO).build()).build());
		//
		// }
		//
		// if (ValidationUtils.isValid(p.getLocation().getName())) {
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText(MEDIA_LOCATION_NAME)
		// .setKey(MEDIA_LOCATION_NAME)
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getLocation().getName())
		// .setType(G_PropertyType.STRING).build()).build());
		// }
		//
		// // FIXME: using literal strings instead of strings defined in
		// // AbstractDocumentParser
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText("ATS_IN_CAPTION")
		// .setKey("ATS_IN_CAPTION")
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getAtsInCaption()).setType(G_PropertyType.OTHER)
		// .build()).build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText("ATS_IN_COMMENTS")
		// .setKey("ATS_IN_COMMENTS")
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getAtsInComments()).setType(G_PropertyType.OTHER)
		// .build()).build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText("HASHTAGS_IN_CAPTION")
		// .setKey("HASHTAGS_IN_CAPTION")
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getHashTagsInCaption()).setType(G_PropertyType.OTHER)
		// .build()).build());
		// list.add(G_Property
		// .newBuilder()
		// .setFriendlyText("HASHTAGS_IN_COMMENTS")
		// .setKey("HASHTAGS_IN_COMMENTS")
		// .setRange(
		// G_SingletonRange.newBuilder().setValue(p.getHashTagsInComments()).setType(G_PropertyType.OTHER)
		// .build()).build());
		final List<G_EntityTag> tags = new ArrayList<G_EntityTag>();
		tags.add(G_EntityTag.FILE);
		final EntityHelper entity = new EntityHelper(getIdFromDoc(p), tags, null, null, map);
		return entity;
		// return list;
	}

	@Override
	public Map<String, Object> getAdditionalProperties(final Object obj) {
		if (!(obj instanceof Media)) {
			return null;
		}
		final Media p = (Media) obj;
		return p.getAdditionalProperties();
	}

	@Override
	public String getIdFromDoc(final Media p) {
		return p.getId();
	}

	@Override
	public String getReportType() {
		return "MEDIA";
	}

	@Override
	public V_GenericGraph getSubGraph(final G_SearchResult sr, final G_EntityQuery q) {
		// TODO Auto-generated method stub
		return null;
	}

	// This method creates a sub graph of the nodes inside a report, and a list
	// of new identifiers to search on.
	@Override
	public boolean parse(final G_SearchResult sr, final G_EntityQuery q) {
		sr.getResult();

		final Media p = getDTO(sr, Media.class);
		// final Media p = getClassFromJSON(sr, Media.class);s
		// Make nodes dealing with the report itself.
		if (ValidationUtils.isValid(p)) {

			final String reportId = p.getId();

			// Don't scan the same object twice!
			if (phgb.isPreviouslyScannedResult(reportId)) {
				return false;
			}

			phgb.addScannedResult(reportId);
			// report node does not attach to anything.
			final V_GenericNode reportNode = phgb.createOrUpdateNode(reportId, G_CanonicalPropertyType.MEDIA.name(),
					G_CanonicalPropertyType.MEDIA.name(), null, null, null);
			reportNode.setLabel(getReportLabel(p));
			reportNode.addData(reportLinkTitle,  "<a href=\"" + p.getLink() + "\" class=\"btn btn-primary\" target=\"" + p.getId() + "\" >"
					+ p.getId() + "</a>");

			// final G_Entity entity = buildEntityFromDocument(sr, q);
			// phgb.addReportDetails(reportNode, entity.getProperties(),
			// reportLinkTitle, reportLink);

			phgb.addGraphQueryPath(reportNode, q);

			if (ValidationUtils.isValid(p.getUsername())) {
				final V_GenericNode ownerNode = phgb.createOrUpdateNode(p.getUsername(),
						G_CanonicalPropertyType.USERNAME.name(), G_CanonicalPropertyType.USERNAME.name(), reportNode,
						G_CanonicalRelationshipType.OWNER_OF.name(), G_CanonicalRelationshipType.OWNER_OF.name());
				phgb.buildQueryForNextIteration(ownerNode);
			}

			// createNodesFromFreeText(p.getCaptionText(), reportNode);

			// V_GenericNode ipNode = null;
			// final V_GenericNode marketNode = null;
			// final V_GenericNode commodityNode = null;

			for (int i = 0; i < p.getComments().getCommentsData().size(); i++) {
				final CommentData comment = p.getComments().getCommentsData().get(i);
				final String commentId = reportNode.getId() + "-comment-" + i;

				if (ValidationUtils.isValid(comment)) {
					V_GenericNode commentNode = null, commenterNode = null;
					commentNode = phgb.createOrUpdateNode(commentId, "Comment", "Comment", reportNode,
							G_CanonicalRelationshipType.PART_OF.name(), G_CanonicalRelationshipType.PART_OF.name());

					if (ValidationUtils.isValid(commentNode)) {
						commentNode.addData("Comment Text", comment.getText());
						commentNode.setLabel(comment.getTextSample());
					}

					if (ValidationUtils.isValid(comment.getUsername())) {
						commenterNode = phgb.createOrUpdateNode(comment.getUsername(),
								G_CanonicalPropertyType.USERNAME.name(), G_CanonicalPropertyType.USERNAME.name(),
								commentNode, G_CanonicalRelationshipType.OWNER_OF.name(),
								G_CanonicalRelationshipType.OWNER_OF.name());
						phgb.buildQueryForNextIteration(commenterNode);
					}
				}
			}

			for (int i = 0; i < p.getLikes().getLikesData().size(); i++) {
				final LikeData like = p.getLikes().getLikesData().get(i);

				if (ValidationUtils.isValid(like)) {

					V_GenericNode likerNode = null;
					if (ValidationUtils.isValid(like.getUsername())) {
						likerNode = phgb.createOrUpdateNode(like.getUsername(),
								G_CanonicalPropertyType.USERNAME.name(), G_CanonicalPropertyType.USERNAME.name(),
								reportNode, G_CanonicalRelationshipType.LIKES.name(),
								G_CanonicalRelationshipType.LIKES.name());
						phgb.buildQueryForNextIteration(likerNode);
					}
				}
			}
		}

		return true;
	}
}
