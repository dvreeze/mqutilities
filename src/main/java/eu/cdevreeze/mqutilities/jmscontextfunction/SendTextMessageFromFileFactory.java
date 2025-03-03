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

package eu.cdevreeze.mqutilities.jmscontextfunction;

import eu.cdevreeze.mqutilities.JmsContextFunctionFactory;
import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunctionFactory;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Named;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Factory of {@link SendTextMessageFromFile} objects.
 *
 * @author Chris de Vreeze
 */
@ApplicationScoped
@Named("SendTextMessageFromFile")
@Typed({JmsContextToJsonObjectFunctionFactory.class, JmsContextFunctionFactory.class})
public class SendTextMessageFromFileFactory implements JmsContextToJsonObjectFunctionFactory {

    @Override
    public SendTextMessageFromFile apply(List<String> args) {
        Objects.checkIndex(1, args.size());
        String queueName = Objects.requireNonNull(args.get(0));
        Path file = Path.of(Objects.requireNonNull(args.get(1)));
        return new SendTextMessageFromFile(queueName, file);
    }
}
