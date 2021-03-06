package dev.pitlor.gamekit_spring_boot_starter

import dev.pitlor.gamekit_spring_boot_starter.implementations.User
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import java.util.*

/**
 * The key for the setting specifying if a user is connected or not.
 *
 * This can be used on the front end to make online indicators. Technically it can also be used by the backend,
 * although uses are limited.
 */
const val SETTING_CONNECTED = "connected"

@Configuration
@ConfigurationProperties(prefix = "dev.pitlor")
open class Config {
    /**
     * Persistence implementation for game data
     */
    lateinit var persistenceStrategy: String
}

@Configuration
@EnableWebSocketMessageBroker
open class SocketConfig : WebSocketMessageBrokerConfigurer {
    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic")
        registry.setApplicationDestinationPrefixes("/app", "/topic")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/websocket-server").setAllowedOriginPatterns("*").withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
                val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)!!
                if (accessor.command == StompCommand.CONNECT) {
                    val uuid = accessor.getNativeHeader("uuid")?.get(0)
                    accessor.user = User(UUID.fromString(uuid))
                }

                return message
            }
        })
    }
}
