package graphene.instagram.model.graphserver;

import graphene.dao.DocumentGraphParser;
import graphene.instagram.model.media.CommentData;
import graphene.instagram.model.media.LikeData;
import graphene.instagram.model.media.Media;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
import graphene.model.idl.G_EntityQuery;
import graphene.model.idl.G_Property;
import graphene.model.idl.G_PropertyType;
import graphene.model.idl.G_SearchResult;
import graphene.model.idl.G_SingletonRange;
import graphene.util.validator.ValidationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import mil.darpa.vande.generic.V_GenericNode;

public class MediaGraphParser extends AbstractDocumentGraphParser<Media> {

	public MediaGraphParser() {
		supported = new ArrayList<String>(1);
		supported.add(Media.class.getCanonicalName());
		parenting = false;
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
	public String getReportId(final Media p) {
		// return p.getDETAILS().getCurrentBsaIdentifier().toString();
		return p.getId();
	}

	@Override
	public String getReportType() {
		return "MEDIA";
	}

	// This method creates a sub graph of the nodes inside a report, and a list
	// of new identifiers to search on.
	@Override
	public boolean parse(final G_SearchResult sr, final G_EntityQuery q) {
		if (!(sr.getResult() instanceof Media)) {
			return false;
		}
		final Media p = (Media) sr.getResult();

		// Make nodes dealing with the report itself.
		if (ValidationUtils.isValid(p)) {

			final String reportId = p.getId();

			// Don't scan the same object twice!
			if (phgb.isPreviouslyScannedResult(reportId)) {
				return false;
			}
			populateSearchResult(sr, q);
			p.getAdditionalProperties().get(DocumentGraphParser.SCORE);

			phgb.addScannedResult(reportId);
			// report node does not attach to anything.
			final V_GenericNode reportNode = phgb.createOrUpdateNode(reportId, G_CanonicalPropertyType.MEDIA.name(),
					G_CanonicalPropertyType.MEDIA.name(), null, null, null);
			reportNode.setLabel((String) p.getAdditionalProperties().get(MEDIA_LABEL));
			reportNode.addData("Type", (String) p.getAdditionalProperties().get(REPORT_TYPE));

			// reportNode.addData(reportLinkTitle,
			// getReportViewerLink("BSARReport", reportId));
			reportNode.addData(reportLinkTitle, "<a href=\"" + p.getLink() + "\" class=\"btn btn-primary\" target=\""
					+ p.getId() + "\" >" + p.getId() + "</a>");

			phgb.addReportDetails(reportNode, p.getAdditionalProperties());

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

	@Override
	public Collection<? extends G_Property> populateSearchResult(final G_SearchResult sr, final G_EntityQuery sq) {
		final List<G_Property> list = new ArrayList<G_Property>();
		final Media p = (Media) sr.getResult();
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_LABEL)
				.setKey(MEDIA_LABEL)
				.setRange(
						G_SingletonRange.newBuilder().setValue(getReportLabel(p)).setType(G_PropertyType.STRING)
								.build()).build());
		list.add(G_Property.newBuilder().setFriendlyText(MEDIA_ID).setKey(MEDIA_ID)
				.setRange(G_SingletonRange.newBuilder().setValue(p.getId()).setType(G_PropertyType.STRING).build())
				.build());

		list.add(G_Property.newBuilder().setFriendlyText(MEDIA_LINK).setKey(MEDIA_LINK)
				.setRange(G_SingletonRange.newBuilder().setValue(p.getLink()).setType(G_PropertyType.STRING).build())
				.build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_OWNER)
				.setKey(MEDIA_OWNER)
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getUsername()).setType(G_PropertyType.STRING).build())
				.build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_CREATED_TIME)
				.setKey(MEDIA_CREATED_TIME)
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getCreatedTime()).setType(G_PropertyType.DATE).build())
				.build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_CAPTION_TEXT)
				.setKey(MEDIA_CAPTION_TEXT)
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getCaptionText()).setType(G_PropertyType.STRING)
								.build()).build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_LIKE_COUNT)
				.setKey(MEDIA_LIKE_COUNT)
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getLikes().getCount()).setType(G_PropertyType.LONG)
								.build()).build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_COMMENT_COUNT)
				.setKey(MEDIA_COMMENT_COUNT)
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getComments().getCount()).setType(G_PropertyType.LONG)
								.build()).build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText(MEDIA_THUMBNAIL)
				.setKey(MEDIA_THUMBNAIL)
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getThumbnail()).setType(G_PropertyType.STRING).build())
				.build());

		if ((p.getLocation().getLatitude() != null) && (p.getLocation().getLongitude() != null)) {
			list.add(G_Property
					.newBuilder()
					.setFriendlyText(MEDIA_LOCATION_LATLON)
					.setKey(MEDIA_LOCATION_LATLON)
					.setRange(
							G_SingletonRange.newBuilder()
									.setValue(p.getLocation().getLatitude() + ", " + p.getLocation().getLongitude())
									.setType(G_PropertyType.GEO).build()).build());

		}

		if (ValidationUtils.isValid(p.getLocation().getName())) {
			list.add(G_Property
					.newBuilder()
					.setFriendlyText(MEDIA_LOCATION_NAME)
					.setKey(MEDIA_LOCATION_NAME)
					.setRange(
							G_SingletonRange.newBuilder().setValue(p.getLocation().getName())
									.setType(G_PropertyType.STRING).build()).build());
		}

		// FIXME: using literal strings instead of strings defined in
		// AbstractDocumentParser
		list.add(G_Property
				.newBuilder()
				.setFriendlyText("ATS_IN_CAPTION")
				.setKey("ATS_IN_CAPTION")
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getAtsInCaption()).setType(G_PropertyType.OTHER)
								.build()).build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText("ATS_IN_COMMENTS")
				.setKey("ATS_IN_COMMENTS")
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getAtsInComments()).setType(G_PropertyType.OTHER)
								.build()).build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText("HASHTAGS_IN_CAPTION")
				.setKey("HASHTAGS_IN_CAPTION")
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getHashTagsInCaption()).setType(G_PropertyType.OTHER)
								.build()).build());
		list.add(G_Property
				.newBuilder()
				.setFriendlyText("HASHTAGS_IN_COMMENTS")
				.setKey("HASHTAGS_IN_COMMENTS")
				.setRange(
						G_SingletonRange.newBuilder().setValue(p.getHashTagsInComments()).setType(G_PropertyType.OTHER)
								.build()).build());

		return list;
	}
}
