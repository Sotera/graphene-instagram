package graphene.instagram.model.graphserver;

import graphene.dao.DocumentGraphParser;
import graphene.instagram.model.media.Media;
import graphene.instagram.model.media.Comments;
import graphene.instagram.model.media.CommentData;
import graphene.instagram.model.media.Likes;
import graphene.instagram.model.media.LikeData;
import graphene.instagram.model.media.Location;
import graphene.instagram.model.media.Media;
import graphene.model.idl.G_CanonicalPropertyType;
import graphene.model.idl.G_CanonicalRelationshipType;
import graphene.model.query.EntityQuery;
import graphene.util.DataFormatConstants;
import graphene.util.StringUtils;
import graphene.util.validator.ValidationUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
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
//		return p.getDETAILS().getCurrentBsaIdentifier().toString();
		return p.getId();
	}

	@Override
	public String getReportType() {
		return "MEDIA";
	}

	// This method creates a sub graph of the nodes inside a report, and a list
	// of new identifiers to search on.
	@Override
	public boolean parse(final Object obj, final EntityQuery q) {
		if (!(obj instanceof Media)) {
			return false;
		}
		Media p = (Media) obj;

		// Make nodes dealing with the report itself.
		if (ValidationUtils.isValid(p)) {

			final String reportId = p.getId();

			// Don't scan the same object twice!
			if (phgb.isPreviouslyScannedResult(reportId)) {
				return false;
			}
			p = populateExtraFields(p, q);
			final double inheritedScore = (double) p.getAdditionalProperties().get(DocumentGraphParser.SCORE);

			phgb.addScannedResult(reportId);
			// report node does not attach to anything.
			final V_GenericNode reportNode = phgb.createOrUpdateNode(reportId,
					G_CanonicalPropertyType.REPORT_ID.name(), G_CanonicalPropertyType.REPORT_ID.name(), null, null,
					null);
			reportNode.setLabel((String) p.getAdditionalProperties().get(REPORT_LABEL));
			reportNode.addData("Type", (String) p.getAdditionalProperties().get(REPORT_TYPE));

//			reportNode.addData(reportLinkTitle, getReportViewerLink("BSARReport", reportId));		
			reportNode.addData(reportLinkTitle,  "<a href=\"" + p.getLink() + "\" class=\"btn btn-primary\" target=\"" + p.getId() + "\" >"
			+ p.getId() + "</a>");
			
			
			phgb.addReportDetails(reportNode, p.getAdditionalProperties());

			phgb.addGraphQueryPath(reportNode, q);
			
			if (ValidationUtils.isValid(p.getUsername())) {
				V_GenericNode ownerNode = phgb.createOrUpdateNode(p.getUsername(), G_CanonicalPropertyType.USERNAME.name(), G_CanonicalPropertyType.USERNAME.name(),
						reportNode, G_CanonicalRelationshipType.OWNER_OF.name(),
						G_CanonicalRelationshipType.OWNER_OF.name());
				phgb.buildQueryForNextIteration(ownerNode);
			}

//			createNodesFromFreeText(p.getCaptionText(), reportNode);

//			V_GenericNode ipNode = null;
//			final V_GenericNode marketNode = null;
//			final V_GenericNode commodityNode = null;
			
			
			
			for (int i=0; i<p.getComments().getCommentsData().size(); i++) {
				final CommentData comment = p.getComments().getCommentsData().get(i);
				final String commentId = reportNode.getId() + "-comment-" + i;
				
				if (ValidationUtils.isValid(comment)) {
					V_GenericNode commentNode = null, commenterNode = null;
					commentNode = phgb.createOrUpdateNode(commentId, "Comment", "Comment",
							reportNode, G_CanonicalRelationshipType.PART_OF.name(),
							G_CanonicalRelationshipType.PART_OF.name());
					
					if (ValidationUtils.isValid(commentNode)) {
						commentNode.addData("Comment Text", comment.getText());
						commentNode.setLabel(comment.getTextSample());
					}
					
					if (ValidationUtils.isValid(comment.getUsername())) {
						commenterNode = phgb.createOrUpdateNode(comment.getUsername(), G_CanonicalPropertyType.USERNAME.name(), G_CanonicalPropertyType.USERNAME.name(),
								commentNode, G_CanonicalRelationshipType.OWNER_OF.name(),
								G_CanonicalRelationshipType.OWNER_OF.name());
						phgb.buildQueryForNextIteration(commenterNode);
					}
				}
			}
			
			
			for (int i=0; i<p.getLikes().getLikesData().size(); i++) {
				final LikeData like = p.getLikes().getLikesData().get(i);
				
				if (ValidationUtils.isValid(like)) {
					
					V_GenericNode likerNode = null;
					if (ValidationUtils.isValid(like.getUsername())) {
						likerNode = phgb.createOrUpdateNode(like.getUsername(), G_CanonicalPropertyType.USERNAME.name(), G_CanonicalPropertyType.USERNAME.name(),
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
	public Media populateExtraFields(final Media p, final EntityQuery sq) {
//		resetLists();
//		if (ValidationUtils.isValid(p.getSUBJECTS())) {
//			Integer number = 1;
//			for (final Subject s : p.getSUBJECTS()) {
//				s.setAdditionalProperty(NUMBER, number.toString());
//				number++;
//				if (ValidationUtils.isValid(s.getSubjectAddress())) {
//					for (final SubjectAddress sa : s.getSubjectAddress()) {
//						addSafeString(G_CanonicalPropertyType.ADDRESS.name(), subjectAddressList,
//								sa.getEnhancedFullAddress());
//						if (s.getAdditionalProperties().get(LABEL2) == null) {
//							s.setAdditionalProperty(LABEL2, sa.getEnhancedFullAddress());
//						}
//					}
//				}
//				if (ValidationUtils.isValid(s.getSubjectEmail())) {
//
//					for (final SubjectEmail se : s.getSubjectEmail()) {
//						addSafeString(G_CanonicalPropertyType.EMAIL_ADDRESS.name(), subjectCIDList,
//								se.getEmailAddress());
//					}
//				}
//				if (ValidationUtils.isValid(s.getSubjectPhone())) {
//
//					for (final SubjectPhone sp : s.getSubjectPhone()) {
//						addSafeStringWithTitle(G_CanonicalPropertyType.PHONE.name(), sp.getPhoneType(), subjectCIDList,
//								sp.getPhoneNumber(), sp.getPhoneExtension());
//					}
//				}
//
//				if (ValidationUtils.isValid(s.getSubjectId())) {
//					for (final SubjectId si : s.getSubjectId()) {
//						addSafeStringWithTitle(G_CanonicalPropertyType.GOVERNMENTID.name(),
//								si.getMethodOfIdentification(), subjectIDList, si.getIdentificationNumber(),
//								si.getOtherIssuerState(), si.getOtherIssuerCountry());
//
//					}
//				}
//				if (ValidationUtils.isValid(s.getSubjectAlternateName())) {
//					for (final SubjectAlternateName san : s.getSubjectAlternateName()) {
//						addSafeStringWithTitle(G_CanonicalPropertyType.NAME.name(), san.getNameType(), subjectNameList,
//								san.getFullName());
//					}
//				}
//				addSafeString(G_CanonicalPropertyType.NAME.name(), subjectNameList, s.getCompleteName());
//				addSafeStringWithTitle(G_CanonicalPropertyType.OCCUPATION.name(), "Occupation", subjectIDList,
//						s.getOccupationOrBusinessType());
//
//				s.setAdditionalProperty(LABEL, s.getCompleteName());
//
//			}
//		}
//		p.setAdditionalProperty(SUBJECTADDRESSLIST, subjectAddressList);
//		p.setAdditionalProperty(SUBJECTCIDLIST, subjectCIDList);
//		p.setAdditionalProperty(SUBJECTIDLIST, subjectIDList);
//		p.setAdditionalProperty(SUBJECTNAMELIST, subjectNameList);
//
//		if (ValidationUtils.isValid(p.getNARR())) {
//			p.setAdditionalProperty(NARRATIVE, p.getNARR());
//		}
//
//		p.setAdditionalProperty(
//				ICONLIST,
//				iconService.getIconsForText((String) p.getAdditionalProperties().get(NARRATIVE),
//						sq.getAttributeValues()));
//
//		// pagelink
//		p.setAdditionalProperty(REPORT_LINK, "reports/BSARReport");
//		p.setAdditionalProperty(REPORT_TYPE, getReportType());
//		p.setAdditionalProperty(REPORT_ID, getReportId(p));
//		p.setAdditionalProperty(REPORT_LABEL, getReportLabel(p));
//
//		Double sum = 0d;
//
//		final SuspiciousActivity t = p.getSUSPICIOUSACTIVITY();
//		if (ValidationUtils.isValid(t.getCumulativeAmount())) {
//			try {
//
//				sum += (Double) t.getCumulativeAmount();
//			} catch (final Exception e) {
//				logger.error("Could not parse cumulative amount " + t.getCumulativeAmount());
//			}
//		} else if (ValidationUtils.isValid(t.getTransactionAmnt())) {
//			try {
//
//				sum += (Double) t.getTransactionAmnt();
//			} catch (final Exception e) {
//				logger.error("Could not parse transaction amount " + t.getTransactionAmnt());
//			}
//		}
//		if (ValidationUtils.isValid(t.getDateTransaction())) {
//			datesOfEvent.add(t.getDateTransaction().toString());
//		}
//		if (ValidationUtils.isValid(t.getDateTransactionTo())) {
//			datesOfEvent.add(t.getDateTransactionTo().toString());
//		}
//
//		p.setAdditionalProperty(TOTALAMOUNTNBR, sum);
//		final String amountString = new DecimalFormat(DataFormatConstants.MONEY_FORMAT_STRING).format(sum);
//		p.setAdditionalProperty(TOTALAMOUNTSTR, amountString);
//		/**
//		 * Filing Institution
//		 */
//		Integer fileInstNumber = 1;
//		final FilingInstitution filingInst = p.getFILINST();
//
//		filingInst.setAdditionalProperty(NUMBER, fileInstNumber.toString());
//		fileInstNumber++;
//		final String coalescName = StringUtils.coalesc(" ", filingInst.getLegalName(), filingInst.getID());
//		filingInst.setAdditionalProperty(LABEL, coalescName);
//		filingInst.setAdditionalProperty(LABEL2, StringUtils.coalesc(", ", filingInst.getEnhancedStreetAddress1(),
//				filingInst.getEnhancedCity(), filingInst.getEnhancedState(), filingInst.getEnhancedZip(),
//				filingInst.getEnhancedCountryName()));
//		/**
//		 * Financial Institutions
//		 */
//		Integer finInstNumber = 1;
//		for (final FinancialInstitution financialInst : p.getFININST()) {
//			{
//
//				financialInst.setAdditionalProperty(NUMBER, finInstNumber.toString());
//				finInstNumber++;
//
//				final String coalescFinName = StringUtils.coalesc(" ", financialInst.getLegalName(),
//						financialInst.getID());
//				financialInst.setAdditionalProperty(LABEL, coalescFinName);
//
//				financialInst.setAdditionalProperty(
//						LABEL2,
//						StringUtils.coalesc(", ", financialInst.getEnhancedStreetAddress1(),
//								financialInst.getEnhancedCity(), financialInst.getEnhancedState(),
//								financialInst.getEnhancedZip(), financialInst.getEnhancedCountry()));
//			}
//		}
//
//		addSafeString(datesFiled, p.getDETAILS().getDateFiled());
//		addSafeString(datesReceived, p.getDETAILS().getDateReceived());
//
//		p.setAdditionalProperty(DATES_RECEIVED, datesReceived);
//		p.setAdditionalProperty(DATES_FILED, datesFiled);
//		p.setAdditionalProperty(DATES_OF_EVENTS, datesOfEvent);
//
//		final ArrayList<String> allDates = new ArrayList<String>(1);
//		allDates.addAll(datesFiled);
//		allDates.addAll(datesReceived);
//		allDates.addAll(datesOfEvent);
//
//		if (ValidationUtils.isValid(allDates) && (allDates.size() > 1)) {
//
//			Collections.sort(allDates);
//			p.setAdditionalProperty(FIRST_DATE, allDates.get(0));
//			p.setAdditionalProperty(LAST_DATE, allDates.get(allDates.size() - 1));
//		} else {
//			p.setAdditionalProperty(FIRST_DATE, "Unknown");
//			p.setAdditionalProperty(LAST_DATE, "Unknown");
//		}
		
		p.setAdditionalProperty(REPORT_LABEL, getReportLabel(p));
		p.setAdditionalProperty(REPORT_ID, p.getId());
		p.setAdditionalProperty(REPORT_LINK, p.getLink());
		p.setAdditionalProperty(FIRST_DATE, p.getCreatedTime());
		// FIXME: using literal strings instead of strings defined in AbstractDocumentParser
		p.setAdditionalProperty("ATS_IN_CAPTION", p.getAtsInCaption());
		p.setAdditionalProperty("ATS_IN_COMMENTS", p.getAtsInComments());
		p.setAdditionalProperty("HASHTAGS_IN_CAPTION", p.getHashTagsInCaption());
		p.setAdditionalProperty("HASHTAGS_IN_COMMENTS", p.getHashTagsInComments());
		
		return p;
	}
}
