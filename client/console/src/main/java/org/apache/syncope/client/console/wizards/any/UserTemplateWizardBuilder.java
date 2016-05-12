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
package org.apache.syncope.client.console.wizards.any;

import java.io.Serializable;
import java.util.List;
import org.apache.syncope.client.console.layout.UserFormLayoutInfo;
import org.apache.syncope.client.console.rest.TaskRestClient;
import org.apache.syncope.client.console.wizards.AjaxWizard;
import org.apache.syncope.common.lib.to.PullTaskTO;
import org.apache.syncope.common.lib.to.UserTO;
import org.apache.syncope.common.lib.types.AnyTypeKind;
import org.apache.wicket.PageReference;
import org.apache.wicket.event.IEventSink;

public class UserTemplateWizardBuilder extends UserWizardBuilder implements TemplateWizardBuilder<UserTO> {

    private static final long serialVersionUID = 6716803168859873877L;

    private final PullTaskTO task;

    private final String anyType;

    public UserTemplateWizardBuilder(
            final PullTaskTO task,
            final List<String> anyTypeClasses,
            final UserFormLayoutInfo formLayoutInfo,
            final PageReference pageRef) {
        super(null, anyTypeClasses, formLayoutInfo, pageRef);
        this.task = task;
        this.anyType = AnyTypeKind.USER.name();

        if (task.getTemplates().containsKey(this.anyType)) {
            setItem(new UserWrapper(UserTO.class.cast(task.getTemplates().get(this.anyType))));
        } else {
            setItem(new UserWrapper(new UserTO()));
        }
    }

    @Override
    public AjaxWizard<AnyWrapper<UserTO>> build(final String id) {
        return super.build(id, AjaxWizard.Mode.TEMPLATE);
    }

    @Override
    protected Serializable onApplyInternal(final AnyWrapper<UserTO> modelObject) {
        task.getTemplates().put(anyType, modelObject.getInnerObject());
        new TaskRestClient().update(task);
        return task;
    }

    @Override
    public TemplateWizardBuilder<UserTO> setEventSink(final IEventSink eventSink) {
        this.eventSink = eventSink;
        return this;
    }
}