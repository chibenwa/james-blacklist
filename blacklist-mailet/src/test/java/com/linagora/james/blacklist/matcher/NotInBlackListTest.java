/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package com.linagora.james.blacklist.matcher;

import static org.assertj.core.api.Assertions.assertThat;

import javax.mail.MessagingException;

import org.apache.james.core.Domain;
import org.apache.james.core.MailAddress;
import org.apache.mailet.base.test.FakeMail;
import org.apache.mailet.base.test.FakeMatcherConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import com.linagora.james.blacklist.memory.MemoryPerDomainAddressBlackList;

class NotInBlackListTest {
    private static final String DOMAIN_TLD = "domain.tld";
    private static final String JAMES_APACHE_ORG = "james.apache.org";

    private NotInBlackList testee;
    private MemoryPerDomainAddressBlackList blackList;
    private MailAddress mailAddress1;
    private MailAddress mailAddress2;
    private MailAddress mailAddress3;
    private MailAddress sender;

    @BeforeEach
    void setUp() throws Exception {
        blackList = new MemoryPerDomainAddressBlackList();
        testee = new NotInBlackList(blackList);
        testee.init(FakeMatcherConfig.builder()
            .matcherName("NotInBlackList")
            .build());

        mailAddress1 = new MailAddress("test@" + JAMES_APACHE_ORG);
        mailAddress2 = new MailAddress("test2@" + JAMES_APACHE_ORG);
        mailAddress3 = new MailAddress("test3@" + DOMAIN_TLD);
        sender = new MailAddress("sender@any.com");
    }

    @Test
    void matchShouldReturnAllRecipientsWhenBlackListEmpty() throws Exception {
        assertThat(testee.match(
            FakeMail.builder()
                .name("name")
                .sender(sender)
                .recipients(mailAddress1, mailAddress2, mailAddress3)
                .build()))
            .containsExactly(mailAddress1, mailAddress2, mailAddress3);
    }

    @Test
    void matchShouldReturnAllRecipientsWHenNoSender() throws Exception {
        assertThat(testee.match(
            FakeMail.builder()
                .name("name")
                .recipients(mailAddress1, mailAddress2, mailAddress3)
                .build()))
            .containsExactly(mailAddress1, mailAddress2, mailAddress3);
    }

    @Test
    void matchShouldNotReturnRecipientThatBlacklistedTheSender() throws Exception {
        blackList.add(Domain.of(DOMAIN_TLD), sender);

        assertThat(testee.match(
            FakeMail.builder()
                .name("name")
                .sender(sender)
                .recipients(mailAddress1, mailAddress2, mailAddress3)
                .build()))
            .containsExactly(mailAddress1, mailAddress2);
    }

    @Test
    void matchShouldNotReturnRecipientsThatBlacklistedTheSender() throws Exception {
        blackList.add(Domain.of(JAMES_APACHE_ORG), sender);

        assertThat(testee.match(
            FakeMail.builder()
                .name("name")
                .sender(sender)
                .recipients(mailAddress1, mailAddress2, mailAddress3)
                .build()))
            .containsExactly(mailAddress3);
    }
}