package com.cambiaso.ioc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Prefijo para los destinos de los mensajes que vienen del cliente al servidor.
        // Ejemplo: El cliente envía a /app/hello
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefijo para los destinos de los mensajes que van del servidor al cliente (broadcast).
        // Habilita un message broker en memoria simple.
        // Ejemplo: El servidor envía a /topic/greetings
        config.enableSimpleBroker("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // El endpoint que los clientes usarán para conectarse al servidor WebSocket.
        // withSockJS() es para navegadores que no soportan WebSockets nativos.
        registry.addEndpoint("/ws-connect").withSockJS();
    }
}