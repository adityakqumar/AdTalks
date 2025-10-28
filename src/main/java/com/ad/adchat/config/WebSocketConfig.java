package com.ad.adchat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configures WebSocket and STOMP messaging.
 * @EnableWebSocketMessageBroker enables WebSocket message handling, backed by a message broker.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Registers the STOMP endpoints, mapping each to a specific URL and enabling SockJS fallback options.
     * SockJS is used to enable fallback options for browsers that don’t support WebSocket.
     * @param registry The STOMP endpoint registry.
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The "/ws" endpoint is what the client will use to connect to the WebSocket server.
        registry.addEndpoint("/ws").withSockJS();
    }

    /**
     * Configures the message broker.
     * @param registry The message broker registry.
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Defines that messages whose destination starts with "/app" should be routed to message-handling methods.
        registry.setApplicationDestinationPrefixes("/app");

        // Defines that messages whose destination starts with "/topic" should be routed to the message broker.
        // The message broker broadcasts messages to all connected clients who are subscribed to a particular topic.
        registry.enableSimpleBroker("/topic");
    }
}
