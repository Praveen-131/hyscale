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
package io.hyscale.generator.services.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Multimap;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.generator.services.processor.CustomSnippetsProcessor;
import io.hyscale.plugin.framework.models.ManifestMeta;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom K8s Snippets
 * <p>
 *  This class is responsible for patching custom K8s snippets with respect to kind on top of generated manifests
 *   or add K8s Snippet as manifest file if particular kind doesn't exist.
 * </p>
 * @author Nishanth Panthangi
 */
@Component
public class CustomSnippetsProvider {

    private static final Logger logger = LoggerFactory.getLogger(CustomSnippetsProvider.class);

    private Multimap<String,String> kindVsCustomSnippets = null;

    @Autowired
    CustomSnippetsProcessor customSnippetsProcessor;

    public void init(ServiceSpec serviceSpec) throws HyscaleException {
        if(serviceSpec == null){
            return;
        }
        TypeReference<List<String>> listTypeReference = new TypeReference<List<String>>() {};
        List<String> k8sSnippetFilePaths= serviceSpec.get(HyscaleSpecFields.k8sPatches,listTypeReference);
        this.kindVsCustomSnippets = customSnippetsProcessor.processCustomSnippetFiles(k8sSnippetFilePaths);
    }

    public String mergeCustomSnippetsIfAvailable(String kind, String yamlString) throws HyscaleException {
        if(kindVsCustomSnippets == null){
            return yamlString;
        }
        Collection<String> customSnippets = kindVsCustomSnippets.get(kind);
        if(customSnippets == null || customSnippets.isEmpty()){
            return yamlString;
        }
        for(String customSnippet : customSnippets){
            if(StringUtils.isNotBlank(customSnippet)){
                yamlString = customSnippetsProcessor.mergeYamls(yamlString,customSnippet);
            }
        }
        kindVsCustomSnippets.removeAll(kind);
        return yamlString;
    }

    public Map<ManifestMeta,String> fetchUnmergedCustomSnippets(){
        if(kindVsCustomSnippets == null || kindVsCustomSnippets.isEmpty()){
            return null;
        }
        Map<ManifestMeta,String> manifestMetaVsSnippet = new HashMap<>();
        kindVsCustomSnippets.forEach((kind,snippet)->{
            ManifestMeta manifestMeta = new ManifestMeta(kind);
            manifestMetaVsSnippet.put(manifestMeta,snippet);
        });
        return manifestMetaVsSnippet;
    }

}
