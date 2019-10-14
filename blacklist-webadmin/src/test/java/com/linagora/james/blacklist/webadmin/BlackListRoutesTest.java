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

package com.linagora.james.blacklist.webadmin;

import static io.restassured.RestAssured.when;
import static io.restassured.RestAssured.with;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;

import org.apache.james.webadmin.WebAdminServer;
import org.apache.james.webadmin.WebAdminUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.linagora.james.blacklist.memory.MemoryPerDomainAddressBlackList;

import io.restassured.RestAssured;
import io.restassured.filter.log.LogDetail;

class BlackListRoutesTest {
    private WebAdminServer webAdminServer;

    @BeforeEach
    void setUp() {
        webAdminServer = WebAdminUtils.createWebAdminServer(new BlackListRoutes(new MemoryPerDomainAddressBlackList()))
            .start();

        RestAssured.requestSpecification = WebAdminUtils.buildRequestSpecification(webAdminServer)
            .setBasePath("backList")
            .log(LogDetail.METHOD)
            .build();
    }

    @AfterEach
    void stop() {
        webAdminServer.destroy();
    }

    @Test
    void listDomainsShouldReturnEmptyByDefault() {
        when()
            .get("domain.tld")
        .then()
            .body(".", hasSize(0));
    }

    @Test
    void listShouldReturn200() {
        when()
            .get("domain.tld")
        .then()
            .statusCode(200);
    }

    @Test
    void listShouldReturnAddedElements() {
        with()
            .put("domain.tld/benwa@apache.org");

        when()
            .get("domain.tld")
        .then()
            .body(".", contains("benwa@apache.org"));
    }

    @Test
    void listShouldNotReturnDeletedElements() {
        with()
            .put("domain.tld/benwa@apache.org");

        with()
            .delete("domain.tld/benwa@apache.org");

        when()
            .get("domain.tld")
        .then()
            .body(".", hasSize(0));
    }

    @Test
    void listShouldNotReturnElementsOfDeletedDomains() {
        with()
            .put("domain.tld/benwa@apache.org");

        with()
            .delete("domain.tld");

        when()
            .get("domain.tld")
        .then()
            .body(".", hasSize(0));
    }

    @Test
    void addShouldReturn201() {
        when()
            .put("domain.tld/benwa@apache.org")
        .then()
            .statusCode(204);
    }

    @Test
    void addShouldBeIdempotent() {
        with()
            .put("domain.tld/benwa@apache.org");

        when()
            .put("domain.tld/benwa@apache.org")
        .then()
            .statusCode(204);
    }

    @Test
    void deleteShouldReturn201() {
        when()
            .delete("domain.tld/benwa@apache.org")
        .then()
            .statusCode(204);
    }

    @Test
    void deleteDomainShouldReturn201() {
        when()
            .delete("domain.tld")
        .then()
            .statusCode(204);
    }
}