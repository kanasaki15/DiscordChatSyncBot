package xyz.n7mn.dev.discordchatsyncbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.util.Collection;

class ChatEventListener implements Listener {

    private final JDA jda;
    private final Plugin plugin;

    public ChatEventListener(JDA jda, Plugin plugin){
        this.jda = jda;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void AsyncPlayerChatEvent(AsyncPlayerChatEvent e){
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED){
            new Thread(()->{
                EmbedBuilder builder = new EmbedBuilder();

                // TODO: サムネイルをスキンの頭にする
                builder.setColor(Color.ORANGE);
                builder.setAuthor(e.getPlayer().getName(),"https://mine.ly/"+e.getPlayer().getName());
                // builder.setThumbnail("");
                builder.setDescription("```"+e.getMessage()+"```");

                jda.getGuildById("810725404545515561").getTextChannelById(plugin.getConfig().getString("SendChannelID")).sendMessage(builder.build()).queue();
            }).start();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnMessageReceivedEvent (OnMessageReceivedEvent e){
        MessageReceivedEvent event = e.getEventMessageReceivedEvent();

        User author = event.getAuthor();
        Member member = event.getMember();
        Message message = event.getMessage();

        if (author.isBot()){
            return;
        }

        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        new Thread(()->{
            
            for (Player player : players){
                if (member.getNickname() != null){
                    player.sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + message.getContentRaw() + " (by " + member.getNickname()+")");
                } else {
                    player.sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + message.getContentRaw() + " (by " + author.getName()+")");
                }

            }

        }).start();

    }

}
