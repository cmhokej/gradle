/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.file.archive.compression;

import org.gradle.api.internal.resources.URIBuilder;
import org.gradle.api.resources.ReadableResource;
import org.gradle.api.resources.internal.ReadableResourceInternal;

import java.io.InputStream;
import java.net.URI;

abstract class AbstractArchiver implements CompressedReadableResource {
    protected final ReadableResource resource;
    protected final URI uri;

    public AbstractArchiver(ReadableResource resource) {
        assert resource != null;
        this.uri = new URIBuilder(resource.getURI()).schemePrefix(getSchemePrefix()).build();
        this.resource = resource;
    }

    abstract protected String getSchemePrefix();

    abstract public InputStream read();

    public String getDisplayName() {
        return resource.getDisplayName();
    }

    public URI getURI() {
        return uri;
    }

    public String getBaseName() {
        return resource.getBaseName();
    }

    @Override
    public ReadableResource getCompressedResource() {
        return resource;
    }

    @Override
    public ReadableResource getBackingResource() {
        ReadableResource resource = getCompressedResource();
        if (resource instanceof ReadableResourceInternal) {
            return ((ReadableResourceInternal) resource).getBackingResource();
        } else {
            return resource;
        }
    }
}
