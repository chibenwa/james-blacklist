package com.linagora.james.blacklist.mailbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.james.core.Domain;
import org.apache.james.core.MailAddress;
import org.apache.james.core.Username;
import org.apache.james.events.Event;
import org.apache.james.events.EventListener;
import org.apache.james.events.Group;
import org.apache.james.mailbox.MailboxManager;
import org.apache.james.mailbox.MailboxSession;
import org.apache.james.mailbox.MessageUid;
import org.apache.james.mailbox.Role;
import org.apache.james.mailbox.events.MailboxEvents;
import org.apache.james.mailbox.exception.MailboxException;
import org.apache.james.mailbox.model.FetchGroup;
import org.apache.james.mailbox.model.MailboxId;
import org.apache.james.mailbox.model.MessageRange;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.message.DefaultMessageBuilder;

import com.github.fge.lambdas.Throwing;
import com.linagora.james.blacklist.api.PerDomainAddressBlackList;

public class BlacklistListener implements EventListener.GroupEventListener {
    public static class BlacklistListenerGroup extends Group {

    }

    private static final Group GROUP = new BlacklistListenerGroup();

    private final PerDomainAddressBlackList blackList;
    private final MailboxManager mailboxManager;

    @Inject
    public BlacklistListener(PerDomainAddressBlackList blackList, MailboxManager mailboxManager) {
        this.blackList = blackList;
        this.mailboxManager = mailboxManager;
    }

    @Override
    public Group getDefaultGroup() {
        return GROUP;
    }

    @Override
    public void event(Event event) throws Exception {
        if (event instanceof MailboxEvents.Added) {
            MailboxEvents.Added added = (MailboxEvents.Added) event;
            handleAddedEvent(added);
        }
    }

    private void handleAddedEvent(MailboxEvents.Added added) throws IOException, MailboxException {
        if (added.getMailboxPath().getName().equalsIgnoreCase(Role.SPAM.getDefaultMailbox())) {
            Username username = added.getUsername();
            if (username.hasDomainPart()) {
                Domain domain = username.getDomainPart().get();

                InputStream inputStream = retrieveMessageContent(added.getMailboxId(),
                    added.getUids().iterator().next(),
                    mailboxManager.createSystemSession(username));

                parseSender(inputStream).ifPresent(address -> blackList.add(domain, address));
            }
        }
    }

    // Demonstrate the use of the mailbox API
    private InputStream retrieveMessageContent(MailboxId mailboxId, MessageUid uid, MailboxSession mailboxSession) throws IOException, MailboxException {
        return mailboxManager.getMailbox(mailboxId, mailboxSession)
            .getMessages(MessageRange.one(uid), FetchGroup.HEADERS, mailboxSession)
            .next()
            .getFullContent()
            .getInputStream();
    }

    // Demonstrate mime parsing with MIME4J library
    private Optional<MailAddress> parseSender(InputStream inputStream) throws IOException {
        Message message = new DefaultMessageBuilder()
            .parseMessage(inputStream);

        try {
            return Optional.ofNullable(message.getSender())
                .map(Mailbox::getAddress)
                .map(Throwing.function(MailAddress::new));
        } finally {
            message.dispose();
        }
    }
}
