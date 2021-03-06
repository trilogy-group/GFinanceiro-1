package org.pinguin.gf.service.infra;

import org.pinguin.gf.domain.journalentry.JournalEntry;
import org.pinguin.gf.service.api.journalentry.JournalEntryTO;

import fr.xebia.extras.selma.Mapper;

@Mapper(withIgnoreFields = { "org.pinguin.gf.service.api.journalentry.JournalEntryTO.id",
		"org.pinguin.gf.service.api.journalentry.JournalEntryTO.links",
		"org.pinguin.gf.service.api.account.AccountTO.id", "org.pinguin.gf.service.api.account.AccountTO.links" })
public interface JournalEntryMapper {

	JournalEntryTO asTO(JournalEntry entity);

	JournalEntry asEntity(JournalEntryTO to);

}
