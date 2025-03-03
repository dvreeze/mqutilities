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
import eu.cdevreeze.mqutilities.qualifier.connection.ConnectionType;
import eu.cdevreeze.mqutilities.qualifier.connection.HasConnectionType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.JMSRuntimeException;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * CDI-injectable "simple" JMS {@link ConnectionFactory}, using password authentication.
 * The configuration properties default to the values for the Dockerized MQ queue manager
 * that can be used for training purposes.
 *
 * @author Chris de Vreeze
 */
@ApplicationScoped
public class SimpleConnectionFactories {

    @Produces
    @HasConnectionType(ConnectionType.SIMPLE)
    @ApplicationScoped
    public ConnectionFactory getConnectionFactory() {
        // Ugly!
        Config config = ConfigProvider.getConfig();

        try {
            String jakartaWmqProviderName = WMQConstants.JAKARTA_WMQ_PROVIDER;
            JmsFactoryFactory jmsFactoryFactory = JmsFactoryFactory.getInstance(jakartaWmqProviderName);

            JmsConnectionFactory cf = jmsFactoryFactory.createConnectionFactory();

            cf.setStringProperty(CommonConstants.WMQ_HOST_NAME, config.getValue("simple.wmq_host_name", String.class));
            cf.setIntProperty(CommonConstants.WMQ_PORT, config.getValue("simple.wmq_port", Integer.class));
            cf.setStringProperty(CommonConstants.WMQ_CHANNEL, config.getValue("simple.wmq_channel", String.class));
            cf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, config.getValue("simple.wmq_connection_mode", Integer.class));
            cf.setStringProperty(CommonConstants.WMQ_QUEUE_MANAGER, config.getValue("simple.wmq_queue_manager", String.class));
            cf.setBooleanProperty(CommonConstants.USER_AUTHENTICATION_MQCSP, config.getValue("simple.user_authentication_mqcsp", Boolean.class));
            cf.setStringProperty(CommonConstants.USERID, config.getValue("simple.userid", String.class));
            cf.setStringProperty(CommonConstants.PASSWORD, config.getValue("simple.password", String.class));

            return cf;
        } catch (JMSException e) {
            throw new JMSRuntimeException(e.getMessage(), e.getErrorCode(), e);
        }
    }
}
