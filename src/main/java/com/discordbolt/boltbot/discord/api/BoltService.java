package com.discordbolt.boltbot.discord.api;

import com.discordbolt.api.commands.BotCommand;
import com.discordbolt.api.commands.CommandContext;
import com.discordbolt.boltbot.discord.util.BeanUtil;
import discord4j.core.DiscordClient;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("BoltService")
@Profile("prod")
@DependsOn("BeanUtil") //This ensures BeanUtil is setup before it is used by Bot Modules
public class BoltService {

    public static final String PACKAGE_PREFIX = "com.discordbolt.boltbot";
    private static final Logger LOGGER = LoggerFactory.getLogger(BoltService.class);

    private DiscordClient client;
    private String version;
    private List<BotModule> botModules;

    @Autowired
    public BoltService(DiscordConfiguration config, @Value("${boltbot.version:DEV-SNAPSHOT}") String version) {
        LOGGER.info("Starting BoltBot version {}", version);
        this.client = config.getClient();
        this.version = version;
        initModules();
        config.login();
    }

    private void initModules() {
        LOGGER.info("Registering Bolt Modules");

        botModules = new Reflections(PACKAGE_PREFIX).getSubTypesOf(BotModule.class).stream().map(c -> {
            try {
                BotModule m = c.getDeclaredConstructor().newInstance();
                m.initialize(client);
                return Optional.of(m);
            } catch (Exception e) {
                LOGGER.error("Unable to initialize module '" + c.getName() + "'", e);
                return Optional.<BotModule>empty();
            }
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    public List<BotModule> getBotModules() {
        return Collections.unmodifiableList(botModules);
    }

    public String getVersion() {
        return version;
    }

    @BotCommand(command = "ping", description = "ping", usage = "ping", module = "misc")
    public static void ping(CommandContext context) {
        context.replyWith("Pong!").subscribe();
    }

    @BotCommand(command = "version", description = "Version of Bolt", usage = "version", module = "misc", aliases = "v")
    public static void version(CommandContext context) {
        context.replyWith("BoltBot is currently running version " + BeanUtil.getBean(BoltService.class).getVersion()).subscribe();
    }
}
