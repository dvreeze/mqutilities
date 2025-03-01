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

package eu.cdevreeze.mqutilities;

import jakarta.json.JsonObject;

import java.util.List;

/**
 * {@link JmsContextFunctionFactory} creating {@link JmsContextToJsonObjectFunction} instances.
 *
 * @author Chris de Vreeze
 */
@FunctionalInterface
public interface JmsContextToJsonObjectFunctionFactory extends JmsContextFunctionFactory<JsonObject> {

    @Override
    JmsContextToJsonObjectFunction apply(List<String> args);
}
