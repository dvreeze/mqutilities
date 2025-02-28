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

package eu.cdevreeze.mqutilities.callback;

import eu.cdevreeze.mqutilities.QueueCallback;
import jakarta.jms.*;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * {@link QueueCallback} that returns the number of messages on the given queue, without
 * consuming any data.
 *
 * @author Chris de Vreeze
 */
public class GetMessageCount implements QueueCallback<Integer> {

    @Override
    public Integer apply(JMSContext jmsContext, String queueName) {
        try (QueueBrowser queueBrowser = jmsContext.createBrowser(jmsContext.createQueue(queueName))) {
            @SuppressWarnings("unchecked")
            List<Message> messages = Collections.list((Enumeration<Message>) queueBrowser.getEnumeration());
            return messages.size();
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
