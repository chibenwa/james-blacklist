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

package com.linagora.james.blacklist;

import static io.restassured.RestAssured.with;
import static org.awaitility.Duration.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Duration.ONE_MINUTE;

import java.util.concurrent.TimeUnit;

import org.apache.james.GuiceJamesServer;
import org.apache.james.JamesServerBuilder;
import org.apache.james.JamesServerExtension;
import org.apache.james.MemoryJamesServerMain;
import org.apache.james.mailrepository.api.MailRepositoryUrl;
import org.apache.james.modules.TestJMAPServerModule;
import org.apache.james.modules.protocols.SmtpGuiceProbe;
import org.apache.james.utils.DataProbeImpl;
import org.apache.james.utils.MailRepositoryProbeImpl;
import org.apache.james.utils.SMTPMessageSender;
import org.apache.james.utils.WebAdminGuiceProbe;
import org.apache.james.webadmin.RandomPortSupplier;
import org.apache.james.webadmin.WebAdminConfiguration;
import org.apache.james.webadmin.WebAdminUtils;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.awaitility.core.ConditionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.linagora.james.blacklist.webadmin.BlackListRoutes;

import io.restassured.RestAssured;

class IntegrationTest {
    private static final MailRepositoryUrl ERROR_REPOSITORY = MailRepositoryUrl.from("memory://var/mail/error/");
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final int LIMIT_TO_10_MESSAGES = 10;
    private static final String DOMAIN_TLD = "domain.tld";
    private static final String SENDER = "sender@any.com";
    private static final String USER = "bob@" + DOMAIN_TLD;
    private static final String PASS = "pass";
    private static Duration slowPacedPollInterval = ONE_HUNDRED_MILLISECONDS;
    private static Duration ONE_MILLISECOND = new Duration(1, TimeUnit.MILLISECONDS);
    private static ConditionFactory calmlyAwait = Awaitility.with()
        .pollInterval(slowPacedPollInterval)
        .and()
        .with()
        .pollDelay(ONE_MILLISECOND)
        .await();
    private static ConditionFactory awaitAtMostOneMinute = calmlyAwait.atMost(ONE_MINUTE);

    SMTPMessageSender messageSender = new SMTPMessageSender(DOMAIN_TLD);

    @RegisterExtension
    static JamesServerExtension jamesServerExtension = new JamesServerBuilder()
        .server(configuration -> GuiceJamesServer.forConfiguration(configuration)
            .combineWith(MemoryJamesServerMain.IN_MEMORY_SERVER_AGGREGATE_MODULE)
            .overrideWith(new TestJMAPServerModule(LIMIT_TO_10_MESSAGES))
            .overrideWith(MemoryJamesServerMain.WEBADMIN_TESTING)
            .overrideWith(binder -> binder.bind(WebAdminConfiguration.class)
                .toInstance(WebAdminConfiguration.builder()
                    .enabled()
                    .corsDisabled()
                    .host("127.0.0.1")
                    .port(new RandomPortSupplier())
                    .additionalRoute(BlackListRoutes.class.getCanonicalName())
                    .build())))
        .build();

    @BeforeEach
    void setUp(GuiceJamesServer server) throws Exception {
        server.getProbe(DataProbeImpl.class)
            .fluent()
            .addDomain(DOMAIN_TLD)
            .addUser(USER, PASS);

        RestAssured.requestSpecification = WebAdminUtils.spec(server.getProbe(WebAdminGuiceProbe.class).getWebAdminPort());
    }

    @AfterEach
    void tearDown() throws Exception {
        messageSender.close();
    }

    @Disabled("Not stable")
    @Test
    void blacklistShouldWork(GuiceJamesServer server) throws Exception {
        with()
            .put("/backList/" + DOMAIN_TLD + "/" + SENDER)
            .prettyPeek();

        messageSender.connect(LOCALHOST_IP, server.getProbe(SmtpGuiceProbe.class).getSmtpPort())
            .sendMessage(SENDER, USER);

        awaitAtMostOneMinute.until(() -> server.getProbe(MailRepositoryProbeImpl.class).getRepositoryMailCount(ERROR_REPOSITORY) == 1);
    }
}
