package xyz.n7mn.dev.discordchatsyncbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

class BotListener extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        new Thread(()->{
            Bukkit.getPluginManager().callEvent(new OnMessageReceivedEvent(event));
        }).start();
    }
}

