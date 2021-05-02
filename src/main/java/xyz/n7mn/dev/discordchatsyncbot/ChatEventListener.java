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
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;

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
        text = text.replaceAll("zzya","っじゃ");
        text = text.replaceAll("zzyu","っじゅ");
        text = text.replaceAll("zzyo","っじょ");
        text = text.replaceAll("ssya", "っしゃ");
        text = text.replaceAll("ssyu", "っしゅ");
        text = text.replaceAll("ssyo", "っしょ");
        text = text.replaceAll("ssha", "っしゃ");
        text = text.replaceAll("sshu", "っしゅ");
        text = text.replaceAll("ssho", "っしょ");

        text = text.replaceAll("kka","っか");
        text = text.replaceAll("kki","っき");
        text = text.replaceAll("kku","っく");
        text = text.replaceAll("kke","っけ");
        text = text.replaceAll("kko","っこ");
        text = text.replaceAll("ssa","っさ");
        text = text.replaceAll("ssi","っし");
        text = text.replaceAll("ssu","っす");
        text = text.replaceAll("sse","っせ");
        text = text.replaceAll("sso","っそ");
        text = text.replaceAll("tta","った");
        text = text.replaceAll("tti","っち");
        text = text.replaceAll("ttu","っつ");
        text = text.replaceAll("tte","って");
        text = text.replaceAll("tto","っと");
        text = text.replaceAll("nna","っな");
        text = text.replaceAll("nni","っに");
        text = text.replaceAll("nnu","っぬ");
        text = text.replaceAll("nne","っね");
        text = text.replaceAll("nno","っの");
        text = text.replaceAll("hha","っは");
        text = text.replaceAll("hhi","っひ");
        text = text.replaceAll("hhu","っふ");
        text = text.replaceAll("hhe","っへ");
        text = text.replaceAll("hho","っほ");
        text = text.replaceAll("mma","っま");
        text = text.replaceAll("mmi","っみ");
        text = text.replaceAll("mmu","っむ");
        text = text.replaceAll("mme","っめ");
        text = text.replaceAll("mmo","っも");
        text = text.replaceAll("yya","っや");
        text = text.replaceAll("yyu","っゆ");
        text = text.replaceAll("yyo","っよ");
        text = text.replaceAll("rra","っら");
        text = text.replaceAll("rri","っり");
        text = text.replaceAll("rru","っる");
        text = text.replaceAll("rre","っれ");
        text = text.replaceAll("rro","っろ");
        text = text.replaceAll("wwa","っわ");
        text = text.replaceAll("wwo","っを");
        text = text.replaceAll("wwo","っん");
        text = text.replaceAll("zza","っざ");
        text = text.replaceAll("zzi","っじ");
        text = text.replaceAll("zzu","っず");
        text = text.replaceAll("zze","っぜ");
        text = text.replaceAll("zzo","っぞ");
        text = text.replaceAll("dda","っだ");
        text = text.replaceAll("ddi","っぢ");
        text = text.replaceAll("ddu","っづ");
        text = text.replaceAll("dde","っで");
        text = text.replaceAll("ddo","っど");
        text = text.replaceAll("bba","っば");
        text = text.replaceAll("bbi","っび");
        text = text.replaceAll("bbu","っぶ");
        text = text.replaceAll("bbe","っべ");
        text = text.replaceAll("bbo","っぼ");
        text = text.replaceAll("ppa","っぱ");
        text = text.replaceAll("ppi","っぴ");
        text = text.replaceAll("ppu","っぷ");
        text = text.replaceAll("ppe","っぺ");
        text = text.replaceAll("ppo","っぽ");
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
        text = text.replaceAll("lla", "っぁ");
        text = text.replaceAll("lli", "っぃ");
        text = text.replaceAll("llu", "っぅ");
        text = text.replaceAll("lle", "っぇ");
        text = text.replaceAll("llo", "っぉ");
        text = text.replaceAll("ltu", "っ");
        text = text.replaceAll("bya", "びゃ");
        text = text.replaceAll("byu", "びゅ");
        text = text.replaceAll("byo", "びょ");
        text = text.replaceAll("zya", "じゃ");
        text = text.replaceAll("zyu", "じゅ");
        text = text.replaceAll("zyo", "じょ");
        text = text.replaceAll("ja", "じゃ");
        text = text.replaceAll("ju", "じゅ");
        text = text.replaceAll("jo", "じょ");
        text = text.replaceAll("kya", "きゃ");
        text = text.replaceAll("kyu", "きゅ");
        text = text.replaceAll("kyo", "きょ");
        text = text.replaceAll("tya", "ちゃ");
        text = text.replaceAll("tyu", "ちゅ");
        text = text.replaceAll("tyo", "ちょ");
        text = text.replaceAll("cha", "ちゃ");
        text = text.replaceAll("chu", "ちゅ");
        text = text.replaceAll("cho", "ちょ");


        text = text.replaceAll("nn", "ん");
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
        text = text.replaceAll("la", "ぁ");
        text = text.replaceAll("li", "ぃ");
        text = text.replaceAll("lu", "ぅ");
        text = text.replaceAll("le", "ぇ");
        text = text.replaceAll("lo", "ぉ");
        text = text.replaceAll("ga", "が");
        text = text.replaceAll("gi", "ぎ");
        text = text.replaceAll("gu", "ぐ");
        text = text.replaceAll("ge", "げ");
        text = text.replaceAll("go", "ご");
        text = text.replaceAll("za", "ざ");
        text = text.replaceAll("zi", "じ");
        text = text.replaceAll("zu", "ず");
        text = text.replaceAll("ze", "ぜ");
        text = text.replaceAll("zo", "ぞ");
        text = text.replaceAll("da", "だ");
        text = text.replaceAll("di", "ぢ");
        text = text.replaceAll("du", "づ");
        text = text.replaceAll("de", "で");
        text = text.replaceAll("do", "ど");
        text = text.replaceAll("ba", "ば");
        text = text.replaceAll("bi", "び");
        text = text.replaceAll("bu", "ぶ");
        text = text.replaceAll("be", "べ");
        text = text.replaceAll("bo", "ぼ");
        text = text.replaceAll("pa", "ぱ");
        text = text.replaceAll("pi", "ぴ");
        text = text.replaceAll("pu", "ぷ");
        text = text.replaceAll("pe", "ぺ");
        text = text.replaceAll("po", "ぽ");

        text = text.replaceAll("a", "あ");
        text = text.replaceAll("i", "い");
        text = text.replaceAll("u", "う");
        text = text.replaceAll("e", "え");
        text = text.replaceAll("o", "お");
        text = text.replaceAll("n", "ん");
        text = text.replaceAll("-", "ー");


        return text;
    }
}
