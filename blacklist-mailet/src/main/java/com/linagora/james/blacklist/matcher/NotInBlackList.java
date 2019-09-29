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

import java.util.Collection;

import javax.inject.Inject;

import org.apache.james.core.Domain;
import org.apache.james.core.MailAddress;
import org.apache.mailet.Mail;
import org.apache.mailet.base.GenericMatcher;

import com.github.steveash.guavate.Guavate;
import com.linagora.james.blacklist.api.PerDomainAddressBlackList;

public class NotInBlackList extends GenericMatcher {
    private final PerDomainAddressBlackList blackList;

    @Inject
    public NotInBlackList(PerDomainAddressBlackList blackList) {
        this.blackList = blackList;
    }

    @Override
    public Collection<MailAddress> match(Mail mail) {
        return mail.getRecipients()
            .stream()
            .filter(recipient -> !isSenderBlackListed(mail, recipient))
            .collect(Guavate.toImmutableList());
    }

    private Boolean isSenderBlackListed(Mail mail, MailAddress recipient) {
        Domain domain = recipient.getDomain();
        return mail.getMaybeSender().asOptional()
            .map(sender -> isBlackListed(domain, sender))
            .orElse(true);
    }

    private boolean isBlackListed(Domain domain, MailAddress sender) {
        return blackList.list(domain)
            .contains(sender);
    }
}