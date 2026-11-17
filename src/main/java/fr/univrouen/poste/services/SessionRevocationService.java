/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.services;

import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class SessionRevocationService {
    private final org.springframework.beans.factory.ObjectProvider<SessionRegistry> registries;

    public SessionRevocationService(org.springframework.beans.factory.ObjectProvider<SessionRegistry> registries) {
        this.registries = registries;
    }

    /** Do not revoke sessions for a database change that is rolled back. */
    public void revoke(String username) {
        SessionRegistry registry = registries.getIfAvailable();
        if (username == null || registry == null) return;
        Runnable expiration = () -> registry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof UserDetails details
                        && username.equalsIgnoreCase(details.getUsername()))
                .forEach(principal -> registry.getAllSessions(principal, false)
                        .forEach(session -> session.expireNow()));
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() { expiration.run(); }
            });
        } else {
            expiration.run();
        }
    }
}
