package Techtony96.Discord.modules.audiostreamer.commands;

import Techtony96.Discord.api.commands.BotCommand;
import Techtony96.Discord.api.commands.CommandContext;
import Techtony96.Discord.api.commands.exceptions.CommandException;
import Techtony96.Discord.modules.audiostreamer.AudioStreamer;

/**
 * Created by Techt on 4/25/2017.
 */
public class PauseCommand {

    @BotCommand(command = "pause", description = "Pause the currently playing song", usage = "Pause", module = "Audio Streamer Module", allowedChannels = "music")
    public static void pauseCommand(CommandContext cc){
        try {
            AudioStreamer.getVoiceManager().pause(cc.getGuild(), cc.getUser());
        } catch (CommandException e) {
            cc.replyWith(e.getMessage());
            return;
        }
    }

    @BotCommand(command = "unpause", aliases = "resume", description = "Unpause the currently playing song", usage = "Unpause", module = "Audio Streamer Module", allowedChannels = "music")
    public static void unpauseCommand(CommandContext cc){
        try {
            AudioStreamer.getVoiceManager().unpause(cc.getGuild(), cc.getUser());
        } catch (CommandException e) {
            cc.replyWith(e.getMessage());
            return;
        }
    }
}
