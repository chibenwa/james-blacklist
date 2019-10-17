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

package com.linagora.james.blacklist.memory;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.james.core.Domain;
import org.apache.james.core.MailAddress;

import com.linagora.james.blacklist.api.PerDomainAddressBlackList;

// todo ACEU step 1
public class MemoryPerDomainAddressBlackList implements PerDomainAddressBlackList {
    @Override
    public void add(Domain domain, MailAddress address) {
        throw new NotImplementedException("ACEU 19 step 1");
    }

    @Override
    public void remove(Domain domain, MailAddress address) {
        throw new NotImplementedException("ACEU 19 step 1");
    }

    @Override
    public void clear(Domain domain) {
        throw new NotImplementedException("ACEU 19 step 1");
    }

    @Override
    public List<MailAddress> list(Domain domain) {
        throw new NotImplementedException("ACEU 19 step 1");
    }
}
