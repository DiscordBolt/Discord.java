package com.discordbolt.boltbot.discord.system.presence;

import com.discordbolt.boltbot.discord.api.BotModule;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import reactor.core.publisher.Mono;

public class PresenceMessage implements BotModule {

    private DiscordClient client;

    @Override
    public void initialize(DiscordClient client) {
        this.client = client;
        client.getEventDispatcher().on(ReadyEvent.class)
                .map(r -> r.getGuilds().size())
                .flatMap(i -> client.getEventDispatcher().on(GuildCreateEvent.class).take(i).last())
                .doOnNext(this::updateStatusMessage)
                .subscribe(t -> registerEvents());
    }

    private void registerEvents() {
        client.getEventDispatcher().on(GuildCreateEvent.class).subscribe(this::updateStatusMessage);
        client.getEventDispatcher().on(GuildDeleteEvent.class).subscribe(this::updateStatusMessage);
        client.getEventDispatcher().on(MemberJoinEvent.class).subscribe(this::updateStatusMessage);
        client.getEventDispatcher().on(MemberLeaveEvent.class).subscribe(this::updateStatusMessage);
    }

    private void updateStatusMessage(Event event) {
        Mono<Long> guildCount = event.getClient().getGuilds().count();
        Mono<Integer> userCount = event.getClient().getGuilds().map(guild -> guild.getMemberCount().orElse(0)).reduce(0, (a, b) -> a + b);

        guildCount.zipWith(userCount).flatMap(t -> setStatusMessage(t.getT1(), t.getT2())).subscribe();
    }

    private Mono<Void> setStatusMessage(long guildCount, int memberCount) {
        return client.updatePresence(Presence.online(Activity.playing(String.format("%d guild%s w/ %d users", guildCount, guildCount > 1 ? "s" : "", memberCount))));
    }
}
