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
package org.apache.syncope.client.console.wicket.markup.html.form;

import java.io.Serializable;
import java.util.List;
import org.apache.syncope.client.console.commons.Constants;
import org.apache.syncope.client.console.wicket.ajax.form.IndicatorAjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public abstract class MultiFieldPanel<E extends Serializable> extends AbstractMultiPanel<E> {

    private static final long serialVersionUID = -6322397761456513324L;

    private final FieldPanel<? extends Serializable> panelTemplate;

    private MultiFieldPanel(
            final String id,
            final String name,
            final IModel<List<E>> model,
            final FieldPanel<? extends Serializable> panelTemplate,
            final boolean eventTemplate) {

        super(id, name, model, eventTemplate);
        this.panelTemplate = panelTemplate;
    }

    public static class Builder<E extends Serializable> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final IModel<List<E>> model;

        private boolean eventTemplate = false;

        public Builder(final IModel<List<E>> model) {
            this.model = model;
        }

        /**
         * Set on_change event in order to send MultiValueSelectorEvent to page.
         *
         * @see MultiValueSelectorEvent
         * @param eventTemplate whether this is an event template
         * @return this instance, for fluent building
         */
        public Builder<E> setEventTemplate(final boolean eventTemplate) {
            this.eventTemplate = eventTemplate;
            return this;
        }

        /**
         * Default model object instance.
         *
         * @return default model object instance.
         */
        protected E newModelObject() {
            return null;
        }

        public MultiFieldPanel<E> build(final String id, final String name, final FieldPanel<E> panelTemplate) {
            return new MultiFieldPanel<E>(id, name, model, panelTemplate, eventTemplate) {

                private static final long serialVersionUID = 1L;

                @Override
                protected E newModelObject() {
                    return Builder.this.newModelObject();
                }

                @Override
                protected FieldPanel<? extends Serializable> getItemPanel(final ListItem<E> item) {
                    final FieldPanel<? extends Serializable> fieldPanel = panelTemplate.clone();
                    fieldPanel.setIndex(item.getIndex());
                    fieldPanel.setNewModel(item);
                    fieldPanel.settingsDependingComponents();
                    fieldPanel.hideLabel();

                    if (eventTemplate) {
                        fieldPanel.getField().add(new IndicatorAjaxFormComponentUpdatingBehavior(Constants.ON_CHANGE) {

                            private static final long serialVersionUID = -1107858522700306810L;

                            @Override
                            protected void onUpdate(final AjaxRequestTarget target) {
                                send(getPage(), Broadcast.BREADTH, new MultiValueSelectorEvent(target));
                            }
                        });
                    }

                    return fieldPanel;
                }

                @Override
                protected void clearInput(final Panel panel) {
                    FieldPanel.class.cast(panel).getField().clearInput();
                }
            };
        }
    }
}