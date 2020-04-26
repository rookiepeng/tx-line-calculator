/*
 * Copyright 2020 Google LLC. All rights reserved.
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

package com.rookiedev.microwavetools.data;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

public class ContentResource {
    private static final String URL_KEY = "url";

    public final String url;

    public ContentResource(String url) {
        this.url = url;
    }

    @Nullable
    public static ContentResource listFromMap(Map<String, Object> map) {
        Object url = map.get(URL_KEY);
        if (url instanceof String) {
            return new ContentResource((String) url);
        } else {
            return null;
        }
    }

    @Nullable
    public static ContentResource fromJsonString(String dataString) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(dataString, ContentResource.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
