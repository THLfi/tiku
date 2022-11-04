package fi.thl.pivot.model;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

public class ContactInformation {

	private enum ContactInformationType {
		LINK, MAIL, NAME, PHONE;
	}

	private static final Pattern CONTACT_PATTERN = Pattern.compile("^meta:contact(link|mail|name|phone)$");

	private Map<ContactInformationType, Label> contactData = Maps.newEnumMap(ContactInformationType.class);

	public Label getContactInformationByTypeName(String type) {
		return contactData.get(ContactInformationType.valueOf(type.toUpperCase()));
	}

	public void addContactData(Tuple t) {
		Matcher m = CONTACT_PATTERN.matcher(t.predicate);
		if (m.matches()) {
			String contactType = m.group(1).toUpperCase();
			setContactInformation(contactType, t);
		}
		else {
			throw new IllegalArgumentException("Unrecognized contact information type '" + t.predicate + "'");
		}
	}

	private void setContactInformation(String type, Tuple tuple) {
		ContactInformationType typeAsEnum = ContactInformationType.valueOf(type);
		if (contactData.containsKey(typeAsEnum)) {
			contactData.get(typeAsEnum).setValue(tuple.lang, tuple.object);
		}
		else {
			Label label = Label.create(tuple.lang, tuple.object);
			contactData.put(typeAsEnum, label);
		}
	}

}