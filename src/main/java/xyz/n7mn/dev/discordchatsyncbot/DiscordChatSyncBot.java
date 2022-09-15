package xyz.n7mn.dev.discordchatsyncbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class DiscordChatSyncBot extends JavaPlugin {

    private JDA jda = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();

        try {
            jda = JDABuilder.createLight(getConfig().getString("DiscordToken"), GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS)
                    .addEventListeners(new BotListener(this))
                    .enableCache(CacheFlag.VOICE_STATE)
                    .enableCache(CacheFlag.EMOJI)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .setActivity(Activity.playing(getConfig().getString("EventText") + " IP : " + getConfig().getString("MCAddress")))
                    .build();

            Bukkit.getPluginManager().registerEvents(new ChatEventListener(jda, this), this);
        } catch (Exception e){
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic

        if (jda == null){
            return;
        }

        try {
            //TextChannel channel = jda.getGuildById(getConfig().getString("DiscordGuildID")).getTextChannelById(getConfig().getString("SendChannelID"));
            //channel.getManager().setTopic("").queue();

            jda.shutdown();
        } catch (Exception e){
            // e.printStackTrace();
        }
    }
}
