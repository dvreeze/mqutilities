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

import eu.cdevreeze.mqutilities.JmsContextToJsonObjectFunction;
import jakarta.jms.*;
import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * {@link JmsContextToJsonObjectFunction} that returns the number of messages on the given queue, without
 * consuming any data.
 *
 * @author Chris de Vreeze
 */
public class GetMessageCount implements JmsContextToJsonObjectFunction {

    private final String queueName;

    public GetMessageCount(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public JsonObject apply(JMSContext jmsContext) {
        try (QueueBrowser queueBrowser = jmsContext.createBrowser(jmsContext.createQueue(queueName))) {
            @SuppressWarnings("unchecked")
            List<Message> messages = Collections.list((Enumeration<Message>) queueBrowser.getEnumeration());
            return Json.createObjectBuilder(
                    Map.of(
                            "queue", queueName,
                            "messageCount", String.valueOf(messages.size())
                    )
            ).build();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
