package no.fintlabs;

import no.fintlabs.kafka.common.topic.TopicCleanupPolicyParameters;
import no.fintlabs.kafka.requestreply.ReplyProducerRecord;
import no.fintlabs.kafka.requestreply.RequestConsumerFactoryService;
import no.fintlabs.kafka.requestreply.topic.RequestTopicNameParameters;
import no.fintlabs.kafka.requestreply.topic.RequestTopicService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

@Configuration
public class ClientAuthorizationRequestConfiguration {

    private static ReplyProducerRecord<ClientAuthorization> apply(ConsumerRecord<String, String> consumerRecord) {

        return switch (consumerRecord.value()) {
            case "5679f546-b72e-41d4-bbfe-68b029a8c158" -> ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                                    .builder()
                                    .authorized(true)
                                    .clientId(consumerRecord.value())
                                    .sourceApplicationId("1")
                                    .build())
                    .build();
            case "9a319191-9bbf-4de3-af8e-616a244e4e06" -> ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                            .builder()
                            .authorized(true)
                            .clientId(consumerRecord.value())
                            .sourceApplicationId("2")
                            .build())
                    .build();
            default -> ReplyProducerRecord.<ClientAuthorization>builder()
                    .value(ClientAuthorization
                            .builder()
                            .authorized(false)
                            .clientId(consumerRecord.value())
                            .build())
                    .build();
        };

    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> clientAuthorizationRequestConsumer(
            RequestTopicService requestTopicService,
            RequestConsumerFactoryService requestConsumerFactoryService
    ) {
        RequestTopicNameParameters topicNameParameters = RequestTopicNameParameters.builder()
                .resource("authorization")
                .parameterName("client-id")
                .build();

        requestTopicService.ensureTopic(topicNameParameters, 0, TopicCleanupPolicyParameters.builder().build());

        return requestConsumerFactoryService.createFactory(
                String.class,
                ClientAuthorization.class,
                (ClientAuthorizationRequestConfiguration::apply),
                new CommonLoggingErrorHandler()

        ).createContainer(topicNameParameters);
    }

}
