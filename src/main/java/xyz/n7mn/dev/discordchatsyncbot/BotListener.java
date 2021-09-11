package xyz.n7mn.dev.discordchatsyncbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

class BotListener extends ListenerAdapter {

    private final Plugin plugin;

    public BotListener(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        Bukkit.getScheduler().runTask(plugin, ()->{
            Bukkit.getPluginManager().callEvent(new OnMessageReceivedEvent(event));
        });
    }
}

