package xyz.n7mn.dev.discordchatsyncbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

                String message = e.getMessage();
                if (message.length() == message.getBytes(StandardCharsets.UTF_8).length){

                    String s = lati2hira(message);

                    OkHttpClient client = new OkHttpClient();
                    try {
                        Request request = new Request.Builder()
                                .url("http://www.google.com/transliterate?langpair=ja-Hira|ja&text="+ URLEncoder.encode(s, "UTF-8"))
                                .build();

                        Response response = client.newCall(request).execute();
                        String MojiCode = "UTF-8";
                        if (System.getProperty("os.name").toLowerCase().startsWith("windows")){
                            MojiCode = "windows-31j";
                        }
                        String RequestText = new String(response.body().string().getBytes(),MojiCode);
                        StringBuffer sb = new StringBuffer();

                        for ( JsonElement jsonElements : new Gson().fromJson(RequestText, JsonArray.class)){
                            sb.append(jsonElements.getAsJsonArray().get(1).getAsJsonArray().get(0).getAsString());
                        }

                        sb.append(" (");
                        sb.append(message);
                        sb.append(")");

                        message = sb.toString();
                    } catch (Exception ex){
                        ex.printStackTrace();
                    }
                }

                EmbedBuilder builder = new EmbedBuilder();

                // TODO: サムネイルをスキンの頭にする
                builder.setColor(Color.ORANGE);
                builder.setAuthor(e.getPlayer().getName(),"https://mine.ly/"+e.getPlayer().getName());
                // builder.setThumbnail("");
                builder.setDescription("```"+message.replaceAll("`","｀")+"```");

                jda.getGuildById("810725404545515561").getTextChannelById(plugin.getConfig().getString("SendChannelID")).sendMessage(builder.build()).queue();
            }).start();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnMessageReceivedEvent (OnMessageReceivedEvent e){
        MessageReceivedEvent event = e.getEventMessageReceivedEvent();
        Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();

        new Thread(()->{

            User author = event.getAuthor();
            Member member = event.getMember();
            Message message = event.getMessage();

            if (author.isBot()){
                return;
            }

            if (!message.getTextChannel().getId().equals(plugin.getConfig().getString("ReceptionChannelID"))){
                return;
            }

            
            for (Player player : players){

                int length = message.getContentRaw().length();
                if (length >= 100){
                    length = 100;
                }

                if (member.getNickname() != null){
                    player.sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + message.getContentRaw().substring(0, length) + " (by " + member.getNickname()+")");
                } else {
                    player.sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + message.getContentRaw().substring(0, length) + " (by " + author.getName()+")");
                }

            }

        }).start();

    }


    private String lati2hira(String text){
        text = text.replaceAll("ttsu","っつ");
        text = text.replaceAll("tta","った");
        text = text.replaceAll("tti","っち");
        text = text.replaceAll("ttu","っつ");
        text = text.replaceAll("tte","って");
        text = text.replaceAll("tto","っと");
        text = text.replaceAll("ssa","っさ");
        text = text.replaceAll("ssi","っし");
        text = text.replaceAll("ssu","っす");
        text = text.replaceAll("sse","っせ");
        text = text.replaceAll("sso","っそ");
        text = text.replaceAll("mma","っま");
        text = text.replaceAll("mmi","っみ");
        text = text.replaceAll("mmu","っむ");
        text = text.replaceAll("mme","っめ");
        text = text.replaceAll("mmo","っも");
        text = text.replaceAll("ka", "か");
        text = text.replaceAll("ki", "き");
        text = text.replaceAll("ku", "く");
        text = text.replaceAll("ke", "け");
        text = text.replaceAll("ko", "こ");
        text = text.replaceAll("sa", "さ");
        text = text.replaceAll("si", "し");
        text = text.replaceAll("su", "す");
        text = text.replaceAll("se", "せ");
        text = text.replaceAll("so", "そ");
        text = text.replaceAll("ta", "た");
        text = text.replaceAll("ti", "ち");
        text = text.replaceAll("tu", "つ");
        text = text.replaceAll("te", "て");
        text = text.replaceAll("to", "と");
        text = text.replaceAll("na", "な");
        text = text.replaceAll("ni", "に");
        text = text.replaceAll("nu", "ぬ");
        text = text.replaceAll("ne", "ね");
        text = text.replaceAll("no", "の");
        text = text.replaceAll("ha", "は");
        text = text.replaceAll("hi", "ひ");
        text = text.replaceAll("hu", "ふ");
        text = text.replaceAll("he", "へ");
        text = text.replaceAll("ho", "ほ");
        text = text.replaceAll("ma", "ま");
        text = text.replaceAll("mi", "み");
        text = text.replaceAll("mu", "む");
        text = text.replaceAll("me", "め");
        text = text.replaceAll("mo", "も");
        text = text.replaceAll("ya", "や");
        text = text.replaceAll("yu", "ゆ");
        text = text.replaceAll("ye", "いぇ");
        text = text.replaceAll("yo", "よ");
        text = text.replaceAll("ra", "ら");
        text = text.replaceAll("ri", "り");
        text = text.replaceAll("ru", "る");
        text = text.replaceAll("re", "れ");
        text = text.replaceAll("ro", "ろ");
        text = text.replaceAll("wa", "わ");
        text = text.replaceAll("wi", "うぃ");
        text = text.replaceAll("wu", "う");
        text = text.replaceAll("we", "うぇ");
        text = text.replaceAll("wo", "を");
        text = text.replaceAll("sha", "しゃ");
        text = text.replaceAll("shi", "し");
        text = text.replaceAll("shu", "しゅ");
        text = text.replaceAll("she", "しぇ");
        text = text.replaceAll("sho", "しょ");
        text = text.replaceAll("sya", "しゃ");
        text = text.replaceAll("syi", "しぃ");
        text = text.replaceAll("syu", "しゅ");
        text = text.replaceAll("sye", "しぇ");
        text = text.replaceAll("syo", "しょ");
        text = text.replaceAll("la", "ぁ");
        text = text.replaceAll("li", "ぃ");
        text = text.replaceAll("lu", "ぅ");
        text = text.replaceAll("le", "ぇ");
        text = text.replaceAll("lo", "ぉ");
        text = text.replaceAll("sha", "しゃ");
        text = text.replaceAll("shi", "し");
        text = text.replaceAll("shu", "しゅ");
        text = text.replaceAll("she", "しぇ");
        text = text.replaceAll("sho", "しょ");
        text = text.replaceAll("nn", "ん");
        text = text.replaceAll("a", "あ");
        text = text.replaceAll("i", "い");
        text = text.replaceAll("u", "う");
        text = text.replaceAll("e", "え");
        text = text.replaceAll("o", "お");
        text = text.replaceAll("n", "ん");
        text = text.replaceAll("ltu", "っ");
        text = text.replaceAll("-", "ー");


        return text;
    }
}
