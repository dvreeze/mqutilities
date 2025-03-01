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

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.mqutilities.JmsContextToElementFunction;
import eu.cdevreeze.yaidom4j.dom.immutabledom.Element;
import eu.cdevreeze.yaidom4j.dom.immutabledom.Node;
import eu.cdevreeze.yaidom4j.dom.immutabledom.Text;
import jakarta.jms.*;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

import static eu.cdevreeze.yaidom4j.dom.immutabledom.Nodes.elem;

/**
 * {@link JmsContextToElementFunction} that returns all (text) message payloads on the given queue, without
 * consuming any data. The text message payloads are returned as CDATA sections.
 *
 * @author Chris de Vreeze
 */
public class ShowAllMessages implements JmsContextToElementFunction {

    private final String queueName;

    public ShowAllMessages(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public Element apply(JMSContext jmsContext) {
        try (QueueBrowser queueBrowser = jmsContext.createBrowser(jmsContext.createQueue(queueName))) {
            @SuppressWarnings("unchecked")
            List<Message> messages = Collections.list((Enumeration<Message>) queueBrowser.getEnumeration());

            return elem(new QName("queueContent"))
                    .plusChild(elem(new QName("queue")).plusText(queueName))
                    .plusChild(
                            elem(new QName("textMessagePayloads"))
                                    .plusChildren(
                                            messages.stream()
                                                    .flatMap(msg ->
                                                            extractOptionalTextPayload(msg).map(t ->
                                                                            elem(new QName("textMessagePayload"))
                                                                                    .plusChild(t)
                                                                    )
                                                                    .stream()
                                                    )
                                                    .collect(ImmutableList.toImmutableList())
                                    )
                    );
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }

    private Optional<Node> extractOptionalTextPayload(Message message) {
        try {
            if (message instanceof TextMessage textMessage) {
                return Optional.of(new Text(textMessage.getText(), true));
            } else {
                return Optional.empty();
            }
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
