/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.client.console.widgets;

import de.agilecoders.wicket.core.markup.html.bootstrap.image.Icon;
import de.agilecoders.wicket.extensions.markup.html.bootstrap.icon.FontAwesomeIconTypeBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.syncope.client.console.BookmarkablePageLinkBuilder;
import org.apache.syncope.client.console.SyncopeConsoleSession;
import org.apache.syncope.client.console.pages.Remediations;
import org.apache.syncope.client.console.rest.RemediationRestClient;
import org.apache.syncope.client.console.wicket.ajax.IndicatorAjaxTimerBehavior;
import org.apache.syncope.common.lib.to.RemediationTO;
import org.apache.syncope.common.lib.types.StandardEntitlement;
import org.apache.wicket.PageReference;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.metadata.MetaDataRoleAuthorizationStrategy;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.time.Duration;

public class RemediationsWidget extends AlertWidget<RemediationTO> {

    private static final long serialVersionUID = 1817429725840355068L;

    private final RemediationRestClient restClient = new RemediationRestClient();

    private final List<RemediationTO> lastRemediations = new ArrayList<>();

    public RemediationsWidget(final String id, final PageReference pageRef) {
        super(id);
        setOutputMarkupId(true);

        latestAlertsList.add(new IndicatorAjaxTimerBehavior(Duration.seconds(30)) {

            private static final long serialVersionUID = 7298597675929755960L;

            @Override
            protected void onTimer(final AjaxRequestTarget target) {
                if (!latestAlerts.getObject().equals(lastRemediations)) {
                    refreshLatestAlerts(target);
                }
            }
        });
    }

    public final void refreshLatestAlerts(final AjaxRequestTarget target) {
        latestAlerts.getObject().clear();
        latestAlerts.getObject().addAll(lastRemediations);

        linkAlertsNumber.setDefaultModelObject(latestAlerts.getObject().size());
        target.add(linkAlertsNumber);

        headerAlertsNumber.setDefaultModelObject(latestAlerts.getObject().size());
        target.add(headerAlertsNumber);

        latestFive.removeAll();
        target.add(latestAlertsList);

        lastRemediations.clear();
        lastRemediations.addAll(latestAlerts.getObject());
    }

    @Override
    protected IModel<List<RemediationTO>> getLatestAlerts() {
        return new ListModel<RemediationTO>() {

            private static final long serialVersionUID = 541491929575585613L;

            @Override
            public List<RemediationTO> getObject() {
                List<RemediationTO> updatedRemediations;
                if (SyncopeConsoleSession.get().owns(StandardEntitlement.REMEDIATION_LIST)
                        && SyncopeConsoleSession.get().owns(StandardEntitlement.REMEDIATION_READ)) {

                    updatedRemediations = restClient.getRemediations().stream().
                            sorted(Comparator.comparing(RemediationTO::getInstant)).
                            collect(Collectors.toList());
                } else {
                    updatedRemediations = Collections.<RemediationTO>emptyList();
                }

                return updatedRemediations;
            }
        };
    }

    @Override
    protected Panel getAlertLink(final String panelid, final RemediationTO event) {
        return new RemediationsWidget.InnerPanel(panelid, event);
    }

    @Override
    protected AbstractLink getEventsLink(final String linkid) {
        BookmarkablePageLink<Remediations> remediations = BookmarkablePageLinkBuilder.build(linkid, Remediations.class);
        MetaDataRoleAuthorizationStrategy.authorize(remediations, WebPage.ENABLE, StandardEntitlement.REMEDIATION_LIST);
        return remediations;
    }

    @Override
    protected Icon getIcon(final String iconid) {
        return new Icon(iconid,
                FontAwesomeIconTypeBuilder.on(FontAwesomeIconTypeBuilder.FontAwesomeGraphic.medkit).build());
    }

    public static final class InnerPanel extends Panel {

        private static final long serialVersionUID = 8074027899915634928L;

        public InnerPanel(final String id, final RemediationTO alert) {
            super(id);

            AjaxLink<String> approval = new AjaxLink<String>("remediation") {

                private static final long serialVersionUID = 7021195294339489084L;

                @Override
                public void onClick(final AjaxRequestTarget target) {
                    // do nothing
                }

                @Override
                protected void onComponentTag(final ComponentTag tag) {
                    super.onComponentTag(tag);
                    tag.put("title", alert.getRemoteName().trim());
                }
            };

            add(approval);

            approval.add(new Label("label", alert.getOperation().name() + " " + alert.getAnyType()));

            approval.add(new Label("resource", alert.getResource()));

            approval.add(new Label("instant",
                    SyncopeConsoleSession.get().getDateFormat().format(alert.getInstant())).
                    setRenderBodyOnly(true));
        }
    }
}
