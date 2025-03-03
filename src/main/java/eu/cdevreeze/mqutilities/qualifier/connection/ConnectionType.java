/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.mqutilities.qualifier.connection;

import jakarta.inject.Qualifier;

import java.util.Arrays;

/**
 * Type of JMS connection, as used in {@link Qualifier} annotation {@link HasConnectionType}.
 *
 * @author Chris de Vreeze
 */
public enum ConnectionType {
    SIMPLE, TWO_WAY_SSL;

    public static ConnectionType parse(String s) {
        return Arrays.stream(values())
                .filter(v -> v.toString().equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow();
    }
}
