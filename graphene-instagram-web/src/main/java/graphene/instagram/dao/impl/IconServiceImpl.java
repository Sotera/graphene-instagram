package graphene.instagram.dao.impl;

import graphene.dao.IconService;
import graphene.util.Tuple;
import graphene.util.validator.ValidationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

public class IconServiceImpl implements IconService {
	// private HashMap<String, String> iconMap = new HashMap<String, String>();
	private final HashMap<Pattern, String> iconPatternMap = new HashMap<Pattern, String>();
	private final HashMap<Pattern, Tuple<String, String>> iconMap = new HashMap<Pattern, Tuple<String, String>>();
	@Inject
	private Logger logger;

	public IconServiceImpl() {
		if (iconPatternMap.isEmpty()) {
			addPattern("\\s(bomb|explos|weapon|gun|ammo|ammunition)", false,
					"fa fa-sun-o",
					"Document mentions bombs, explosives, weapons, guns or ammunition");
			addPattern(
					"\\s(terror|afghan.*|pakistan|iraq|iran|hezb|isis|isil|aqap|extremist)",
					false,
					"fa fa-crosshairs",
					"Document mentions terror, Afghanistan, Pakistan, Iraq, Iran, Hezbollah or ISIS");

			addPattern("\\s(fraud|launder|money|cash|check|wire)", false,
					"fa fa-money",
					"Document mentions fraud, laundering, money, cash, check or wire");
			addPattern(
					"\\s(violence|danger|knife|gun|weapon|murder|arson|kill|suicide)",
					false,
					"fa fa-warning",
					"Document mentions violence, danger, knife, guns, weapons, arson, killing, suicide or murder");
			addPattern("\\s(car|truck|van|motorcycle|vin)\\s", false,
					"fa fa-truck",
					"Document mentions cars, trucks or motorcycles");
			addPattern("\\s(picture|camera|video)", false, "fa fa-camera",
					"Document mentions pictures or videos");
			addPattern("\\s(guilty|crime|judge)", false, "fa fa-gavel",
					"Document mentions guilty, crime or judge");
			addPattern("\\s(fbi|dea|dod|law enforcement)", false,
					"fa fa-bell-o",
					"Document mentions FBI, DEA, DOD or Law Enforcement");
			addPattern("\\s(phone|cell)", false, "fa fa-phone-square",
					"Document mentions phones or cell phones");

			addPattern("\\s(network|ring|leader|cartel|organized)", false,
					"fa fa-sitemap",
					"Document mentions networks, rings, leaders, cartel or organized crime");
			addPattern("\\s(youtube|video)", false, "fa fa-youtube-play",
					"Document mentions videos or youtube");
			addPattern("\\s(news|report|article)", false, "fa fa-rss-square",
					"Document mentions news, reports or articles");

		}
	}

	@Override
	public void addPattern(final String pattern, final boolean caseSensitive,
			final String iconClass, final String reason) {
		Pattern p;
		if (!caseSensitive) {
			p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);

		} else {
			p = Pattern.compile(pattern);

		}
		iconPatternMap.put(p, iconClass);
		iconMap.put(p, new Tuple<String, String>(iconClass, reason));
	}

	@Override
	public Collection<Tuple<String, String>> getIconsForText(final String text,
			final String... otherKeys) {
		final Collection<Tuple<String, String>> icons = new ArrayList<Tuple<String, String>>();
		try {
			if (ValidationUtils.isValid(text, otherKeys)) {
				Matcher m;
				for (final Pattern i : iconMap.keySet()) {
					m = i.matcher(text);
					if (m.find()) {
						icons.add(iconMap.get(i));
					}
					m.reset();
				}
				Matcher m2;
				for (final String o : otherKeys) {
					m2 = Pattern.compile(o, Pattern.CASE_INSENSITIVE).matcher(
							text);
					if (m2.find()) {
						icons.add(new Tuple<String, String>("fa fa-asterisk",
								"Search term appeared in text"));
					}
					m2.reset();
				}
			}
		} catch (final Exception e) {
			logger.error("No icons will be returned: " + e.getMessage());
		}
		return icons;
	}

	@Override
	public Collection<Tuple<String, String>> getIconsForTextWithCount(
			final String text, final String... otherKeys) {
		final Collection<Tuple<String, String>> icons = new ArrayList<Tuple<String, String>>();
		if (ValidationUtils.isValid(text, otherKeys)) {
			Matcher m;

			for (final Pattern i : iconPatternMap.keySet()) {
				m = i.matcher(text);
				int count = 0;
				while (m.find()) {
					count++;
				}
				if (count > 0) {
					final Tuple<String, String> t = new Tuple<String, String>(
							iconPatternMap.get(i), "" + count);
					icons.add(t);
				}
				m.reset();
			}
			// Look through the extra keys
			Matcher m2;
			for (final String o : otherKeys) {
				m2 = Pattern.compile(o, Pattern.CASE_INSENSITIVE).matcher(text);
				int count = 0;
				while (m2.find()) {
					count++;
				}
				if (count > 0) {
					final Tuple<String, String> t = new Tuple<String, String>(
							"fa fa-asterisk", "" + count);
					icons.add(t);
				}
				m2.reset();
			}
		}
		return icons;
	}

	/**
	 * Taken from stackoverflow 5719833
	 */
	// @Override
	// public Collection<String> getIconsForKeys(String... keys) {
	// Collection<String> fromCollection = Collections2.filter(
	// Arrays.asList(keys), Predicates.in(iconMap.keySet()));
	// Function<? super Object, String> function = (Function<? super Object,
	// String>) Functions
	// .forMap(iconMap);
	// Collection<String> values = Collections2.transform(fromCollection,
	// function);
	// return values;
	// }

	@Override
	public void removePattern(final String pattern, final boolean caseSensitive) {
		if (ValidationUtils.isValid(pattern)) {
			try {
				Pattern p;
				if (caseSensitive) {
					p = Pattern.compile(pattern);
				} else {
					p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
				}
				iconPatternMap.remove(p);
			} catch (final Exception e) {
				e.printStackTrace();
				logger.error(e.getMessage());
			}
		}
	}
}
