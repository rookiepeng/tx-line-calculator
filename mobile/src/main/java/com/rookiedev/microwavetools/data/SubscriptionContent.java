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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

public class SubscriptionContent implements Parcelable {
    @Nullable
    public final String title;
    @Nullable
    public final String subtitle;
    @Nullable
    public final String description;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(description);
    }

    public static final Creator<SubscriptionContent> CREATOR
            = new Creator<SubscriptionContent>() {
        public SubscriptionContent createFromParcel(Parcel in) {
            return new SubscriptionContent(in);
        }

        public SubscriptionContent[] newArray(int size) {
            return new SubscriptionContent[size];
        }
    };

    private SubscriptionContent(Parcel in) {
        title = in.readString();
        subtitle = in.readString();
        description = in.readString();
    }

    private SubscriptionContent(@Nullable String title,
                                @Nullable String subtitle,
                                @Nullable String desc) {
        this.title = title;
        this.subtitle = subtitle;
        this.description = desc;
    }

    class Builder {
        private String title = null;
        private String subTitle = null;
        private String description = null;

        Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        Builder setSubtitle(String subtitle) {
            this.subTitle = subtitle;
            return this;
        }

        Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public SubscriptionContent build() {
            return new SubscriptionContent(title, subTitle, description);
        }
    }
}
