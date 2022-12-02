package xyz.n7mn.dev.discordchatsyncbot;


import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
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

    @Override
    public void onGenericEvent(GenericEvent event) {
        if (event instanceof ReadyEvent){
            //TextChannel channel = event.getJDA().getGuildById(plugin.getConfig().getString("DiscordGuildID")).getTextChannelById(plugin.getConfig().getString("SendChannelID"));
            //channel.getManager().setTopic(plugin.getServer().getOnlinePlayers().size() + " / " + plugin.getServer().getMaxPlayers()).queue();
        }
    }
}

