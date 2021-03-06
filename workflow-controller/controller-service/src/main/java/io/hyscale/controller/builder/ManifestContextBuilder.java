/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.controller.builder;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.manager.RegistryManager;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class ManifestContextBuilder {

    @Autowired
    private RegistryManager registryManager;

    @SuppressWarnings("java:S2583")
    public ManifestContext build(WorkflowContext workflowContext) throws HyscaleException {

        if (workflowContext == null) {
            return null;
        }
        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        if (serviceSpec == null) {
            return null;
        }
        String registryUrl = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);

        ManifestContext manifestContext = new ManifestContext();
        manifestContext.setAppName(workflowContext.getAppName());
        manifestContext.setEnvName(workflowContext.getEnvName());
        manifestContext.setNamespace(workflowContext.getNamespace());
        manifestContext.setImageRegistry(registryManager.getImageRegistry(registryUrl));
        manifestContext.addGenerationAttribute(ManifestGenConstants.IMAGE_SHA_SUM,
                workflowContext.getAttribute(WorkflowConstants.IMAGE_SHA_SUM));
        Map<String, String> customLabels = getCustomLabels(workflowContext);
        if (customLabels != null && !customLabels.isEmpty()) {
            manifestContext.putCustomLabel(customLabels);
        }
        return manifestContext;
    }

    @SuppressWarnings({"java:S1172","java:S1130"})
    private Map<String, String> getCustomLabels(WorkflowContext context) throws HyscaleException {
        // TODO Generate add on labels, ensure only K8s allowed values
        return null;
    }
}
