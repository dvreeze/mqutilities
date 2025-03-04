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

package eu.cdevreeze.mqutilities.connection;

import com.google.common.base.Preconditions;
import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import com.ibm.msg.client.jakarta.wmq.common.CommonConstants;
import eu.cdevreeze.mqutilities.qualifier.connection.ConnectionType;
import eu.cdevreeze.mqutilities.qualifier.connection.HasConnectionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import org.eclipse.microprofile.config.Config;

/**
 * CDI-injectable SSL-enabled JMS {@link ConnectionFactory}, using 2-way SSL.
 *
 * @author Chris de Vreeze
 */
@ApplicationScoped
public class SSLConnectionFactories {

    @Produces
    @HasConnectionType(ConnectionType.TWO_WAY_SSL)
    @ApplicationScoped
    public ConnectionFactory getConnectionFactory(Config config) {
        try {
            String jakartaWmqProviderName = WMQConstants.JAKARTA_WMQ_PROVIDER;
            JmsFactoryFactory jmsFactoryFactory = JmsFactoryFactory.getInstance(jakartaWmqProviderName);

            JmsConnectionFactory cf = jmsFactoryFactory.createConnectionFactory();

            cf.setStringProperty(CommonConstants.WMQ_HOST_NAME, config.getValue("two_way_ssl.wmq_host_name", String.class));
            cf.setIntProperty(CommonConstants.WMQ_PORT, config.getValue("two_way_ssl.wmq_port", Integer.class));
            cf.setStringProperty(CommonConstants.WMQ_CHANNEL, config.getValue("two_way_ssl.wmq_channel", String.class));
            cf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, config.getValue("two_way_ssl.wmq_connection_mode", Integer.class));
            cf.setStringProperty(CommonConstants.WMQ_QUEUE_MANAGER, config.getValue("two_way_ssl.wmq_queue_manager", String.class));
            cf.setStringProperty(CommonConstants.WMQ_SSL_CIPHER_SUITE, config.getValue("two_way_ssl.wmq_ssl_cipher_suite", String.class));

            Preconditions.checkArgument(
                    config.getOptionalValue("javax.net.ssl.keyStore", String.class).isPresent(),
                    "Missing system property 'javax.net.ssl.keyStore'"
            );
            Preconditions.checkArgument(
                    config.getOptionalValue("javax.net.ssl.keyStorePassword", String.class).isPresent(),
                    "Missing system property 'javax.net.ssl.keyStorePassword'"
            );
            Preconditions.checkArgument(
                    config.getOptionalValue("javax.net.ssl.trustStore", String.class).isPresent(),
                    "Missing system property 'javax.net.ssl.trustStore'"
            );
            Preconditions.checkArgument(
                    config.getOptionalValue("javax.net.ssl.trustStorePassword", String.class).isPresent(),
                    "Missing system property 'javax.net.ssl.trustStorePassword'"
            );

            return cf;
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
