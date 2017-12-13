package net.ajpappas.discord.api.commands;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IUser;

public class CommandListener {

    public CommandListener() {
        CommandModule.getClient().getDispatcher().registerListener(this);
    }

    @EventSubscriber
    public void onMesageEvent(MessageReceivedEvent e) {
        String message = e.getMessage().getContent();
        IUser user = e.getAuthor();

        // Ignore bots
        if (user.isBot()) {
            return;
        }

        // Message is just a single prefix.
        if (message.length() <= 1) {
            return;
        }

        // Check if message started with our command prefix
        if (!message.startsWith(CommandManager.getCommandPrefix(e.getGuild()))) {
            return;
        }

        int userArgCount = message.split(" ").length;
        CustomCommand customCommand = CommandManager.getCommands().stream().filter(command -> command.getCommands().length <= userArgCount).filter(command -> command.matches(message)).reduce((first, second) -> second).orElse(null);

        if (customCommand != null) {
            customCommand.preexec(e.getMessage(), user);
        }
    }
}