/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.viewer.wicket.viewer.integration;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import org.apache.wicket.request.Request;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.session.SessionLoggingService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.applib.services.iactnlayer.ThrowingRunnable;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.interaction.session.InteractionService;
import org.apache.isis.core.interaction.session.InteractionTracker;
import org.apache.isis.core.internaltestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.core.security.authentication.AuthenticationRequest;
import org.apache.isis.core.security.authentication.AuthenticationRequestPassword;
import org.apache.isis.core.security.authentication.Authenticator;
import org.apache.isis.core.security.authentication.manager.AnonymousInteractionFactory;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authentication.singleuser.SingleUserAuthentication;
import org.apache.isis.core.security.authentication.standard.RandomCodeGeneratorDefault;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

public class AuthenticatedWebSessionForIsis_Authenticate {


    @Rule
    public final JUnitRuleMockery2 context =
            JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    @Mock
    protected Request mockRequest;
    private AuthenticationManager authMgr;
    @Mock protected Authenticator mockAuthenticator;
    @Mock protected IsisAppCommonContext mockCommonContext;
    @Mock protected InteractionService mockInteractionService;
    @Mock protected InteractionTracker mockInteractionTracker;
    @Mock protected ServiceRegistry mockServiceRegistry;

    protected AuthenticatedWebSessionForIsis webSession;

    @Before
    public void setUp() throws Exception {
        authMgr = new AuthenticationManager(
                Collections.singletonList(mockAuthenticator),
                AnonymousInteractionFactory.forTesting(),
                new RandomCodeGeneratorDefault());

        context.checking(new Expectations() {
            {
                allowing(mockCommonContext).getServiceRegistry();
                will(returnValue(mockServiceRegistry));

                allowing(mockServiceRegistry).select(SessionLoggingService.class);
                will(returnValue(Can.empty()));

                allowing(mockCommonContext).lookupServiceElseFail(InteractionFactory.class);
                will(returnValue(mockInteractionService));

                allowing(mockCommonContext).getInteractionTracker();
                will(returnValue(mockInteractionTracker));

                allowing(mockInteractionTracker).currentAuthentication();
                will(returnValue(Optional.of(new SingleUserAuthentication())));

                allowing(mockInteractionService)
                .runAuthenticated(with(new SingleUserAuthentication()), with(any(ThrowingRunnable.class)));

                allowing(mockInteractionService)
                .runAnonymous(with(any(ThrowingRunnable.class)));

                // ignore

                // must provide explicit expectation, since Locale is final.
                allowing(mockRequest).getLocale();
                will(returnValue(Locale.getDefault()));

                // stub everything else out
                ignoring(mockRequest);
            }
        });

    }

    protected void setupWebSession() {
        webSession = new AuthenticatedWebSessionForIsis(mockRequest) {
            private static final long serialVersionUID = 1L;

            {
                commonContext = mockCommonContext;
            }

            @Override
            protected AuthenticationManager getAuthenticationManager() {
                return authMgr;
            }
        };
    }



    @Test
    public void delegatesToAuthenticationManagerAndCachesAuthSessionIfOk() {

        context.checking(new Expectations() {
            {
                oneOf(mockAuthenticator).canAuthenticate(AuthenticationRequestPassword.class);
                will(returnValue(true));
                oneOf(mockAuthenticator).authenticate(with(any(AuthenticationRequest.class)), with(any(String.class)));
                will(returnValue(new SingleUserAuthentication()));
            }
        });

        setupWebSession();

        assertThat(webSession.authenticate("jsmith", "secret"), is(true));
        assertThat(webSession.getAuthentication(), is(not(nullValue())));
    }

    @Test
    public void delegatesToAuthenticationManagerAndHandlesIfNotAuthenticated() {
        context.checking(new Expectations() {
            {
                oneOf(mockAuthenticator).canAuthenticate(AuthenticationRequestPassword.class);
                will(returnValue(true));
                oneOf(mockAuthenticator).authenticate(with(any(AuthenticationRequest.class)), with(any(String.class)));
                will(returnValue(null));
            }
        });

        setupWebSession();

        assertThat(webSession.authenticate("jsmith", "secret"), is(false));
        assertThat(webSession.getAuthentication(), is(nullValue()));
    }

}
