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

import com.ibm.msg.client.jakarta.jms.JmsConnectionFactory;
import com.ibm.msg.client.jakarta.jms.JmsFactoryFactory;
import com.ibm.msg.client.jakarta.wmq.WMQConstants;
import com.ibm.msg.client.jakarta.wmq.common.CommonConstants;
import eu.cdevreeze.mqutilities.ConnectionFactorySupplier;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;

import java.util.Objects;

/**
 * {@link ConnectionFactorySupplier} using 2-way SSL.
 * <p>
 * The configuration settings of the returned {@link jakarta.jms.ConnectionFactory} are taken
 * from system properties (with the same names as the corresponding constants in
 * {@link CommonConstants}).
 *
 * @author Chris de Vreeze
 */
public class SSLConnectionFactorySupplier implements ConnectionFactorySupplier {

    @Override
    public ConnectionFactory get() {
        try {
            String jakartaWmqProviderName = WMQConstants.JAKARTA_WMQ_PROVIDER;
            JmsFactoryFactory jmsFactoryFactory = JmsFactoryFactory.getInstance(jakartaWmqProviderName);

            JmsConnectionFactory cf = jmsFactoryFactory.createConnectionFactory();

            cf.setStringProperty(
                    CommonConstants.WMQ_HOST_NAME,
                    Objects.requireNonNull(System.getProperty("WMQ_HOST_NAME"))
            );
            cf.setIntProperty(
                    CommonConstants.WMQ_PORT,
                    Integer.parseInt(System.getProperty("WMQ_PORT", "1414"))
            );
            cf.setStringProperty(
                    CommonConstants.WMQ_CHANNEL,
                    Objects.requireNonNull(System.getProperty("WMQ_CHANNEL"))
            );
            cf.setIntProperty(
                    CommonConstants.WMQ_CONNECTION_MODE,
                    Integer.parseInt(System.getProperty("WMQ_CONNECTION_MODE", String.valueOf(CommonConstants.WMQ_CM_CLIENT)))
            );
            cf.setStringProperty(
                    CommonConstants.WMQ_QUEUE_MANAGER,
                    Objects.requireNonNull(System.getProperty("WMQ_QUEUE_MANAGER"))
            );

            cf.setStringProperty(
                    CommonConstants.WMQ_SSL_CIPHER_SUITE,
                    System.getProperty("WMQ_SSL_CIPHER_SUITE", "TLS_RSA_WITH_AES_128_CBC_SHA256")
            );

            Objects.requireNonNull(
                    System.getProperty("javax.net.ssl.keyStore"),
                    "Missing system property 'javax.net.ssl.keyStore'"
            );
            Objects.requireNonNull(
                    System.getProperty("javax.net.ssl.keyStorePassword"),
                    "Missing system property 'javax.net.ssl.keyStorePassword'"
            );
            Objects.requireNonNull(
                    System.getProperty("javax.net.ssl.trustStore"),
                    "Missing system property 'javax.net.ssl.trustStore'"
            );
            Objects.requireNonNull(
                    System.getProperty("javax.net.ssl.trustStorePassword"),
                    "Missing system property 'javax.net.ssl.trustStorePassword'"
            );
            return cf;
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
