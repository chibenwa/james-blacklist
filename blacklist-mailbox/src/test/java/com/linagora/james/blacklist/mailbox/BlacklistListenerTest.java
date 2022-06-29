package com.linagora.james.blacklist.mailbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.james.core.Domain;
import org.apache.james.core.MailAddress;
import org.apache.james.core.Username;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageManager;
import org.apache.james.mailbox.inmemory.InMemoryMailboxManager;
import org.apache.james.mailbox.inmemory.manager.InMemoryIntegrationResources;
import org.apache.james.mailbox.model.MailboxPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.linagora.james.blacklist.api.PerDomainAddressBlackList;
import com.linagora.james.blacklist.memory.MemoryPerDomainAddressBlackList;

class BlacklistListenerTest {
    private static final Username USERNAME = Username.of("bob@domain.tld");
    private static final MailboxPath MAILBOX_PATH = MailboxPath.forUser(USERNAME, "Spam");

    private InMemoryMailboxManager mailboxManager;
    private PerDomainAddressBlackList blacklist;

    @BeforeEach
    void setUp() {
        mailboxManager = InMemoryIntegrationResources.defaultResources().getMailboxManager();
        blacklist = new MemoryPerDomainAddressBlackList();

        BlacklistListener listener = new BlacklistListener(blacklist, mailboxManager);
        mailboxManager.getEventBus().register(listener);
    }

    @Test
    void addingAMessageInSpamMailboxShouldReportItAsBlackList() throws Exception {
        MailboxSession session = mailboxManager.createSystemSession(USERNAME);
        mailboxManager.createMailbox(MAILBOX_PATH, session);
        mailboxManager.getMailbox(MAILBOX_PATH, session)
            .appendMessage(MessageManager.AppendCommand.builder()
                .build("Sender: attacker@bad.com\r\n\r\nBODY".getBytes()), session);

        assertThat(blacklist.list(Domain.of("domain.tld")))
            .containsOnly(new MailAddress("attacker@bad.com"));
    }
}