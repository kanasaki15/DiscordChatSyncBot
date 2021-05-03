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
import java.util.Hashtable;
import java.util.Map;

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
        Map<String,String> list = new Hashtable<>();

        list.put("ttsu","っつ");
        list.put("zzya","っじゃ");
        list.put("zzyu","っじゅ");
        list.put("zzye","っじぇ");
        list.put("zzyo","っじょ");
        list.put("ssya","っしゃ");
        list.put("ssha","っしゃ");
        list.put("sshi","っし");
        list.put("ssyu","っしゅ");
        list.put("sshu","っしゅ");
        list.put("ssye","っしぇ");
        list.put("sshe","っしぇ");
        list.put("ssyo","っしょ");
        list.put("ssho","っしょ");
        list.put("ttsa", "っつぁ");
        list.put("ttsi", "っつぃ");
        list.put("ttse", "っつぇ");
        list.put("ttso", "っつぉ");
        list.put("ccha", "っちゃ");
        list.put("cchi", "っち");
        list.put("cchu", "っちゅ");
        list.put("cche", "っちぇ");
        list.put("ccho", "っちょ");
        list.put("ttya", "っちゃ");
        list.put("ttyi", "っちぃ");
        list.put("ttyu", "っちゅ");
        list.put("ttye", "っちぇ");
        list.put("ttyo", "っちょ");
        list.put("wyi", "ゐ");
        list.put("wye", "ゑ");
        list.put("tsa", "つぁ");
        list.put("tsi", "つぃ");
        list.put("tsu", "つ");
        list.put("tse", "つぇ");
        list.put("tso", "つぉ");
        list.put("kka","っか");
        list.put("kki","っき");
        list.put("kku","っく");
        list.put("kke","っけ");
        list.put("kko","っこ");
        list.put("ssa","っさ");
        list.put("ssi","っし");
        list.put("ssu","っす");
        list.put("sse","っせ");
        list.put("sso","っそ");
        list.put("tta","った");
        list.put("tti","っち");
        list.put("ttu","っつ");
        list.put("tte","って");
        list.put("tto","っと");
        list.put("hha","っは");
        list.put("hhi","っひ");
        list.put("hhu","っふ");
        list.put("hhe","っへ");
        list.put("hho","っほ");
        list.put("mma","っま");
        list.put("mmi","っみ");
        list.put("mmu","っむ");
        list.put("mme","っめ");
        list.put("mmo","っも");
        list.put("yya","っや");
        list.put("yyu","っゆ");
        list.put("yyo","っよ");
        list.put("rra","っら");
        list.put("rri","っり");
        list.put("rru","っる");
        list.put("rre","っれ");
        list.put("rro","っろ");
        list.put("wwa","っわ");
        list.put("wwo","っを");
        list.put("zza","っざ");
        list.put("zzi","っじ");
        list.put("zzu","っず");
        list.put("zze","っぜ");
        list.put("zzo","っぞ");
        list.put("dda","っだ");
        list.put("ddi","っぢ");
        list.put("ddu","っづ");
        list.put("dde","っで");
        list.put("ddo","っど");
        list.put("bba","っば");
        list.put("bbi","っび");
        list.put("bbu","っぶ");
        list.put("bbe","っべ");
        list.put("bbo","っぼ");
        list.put("ppa","っぱ");
        list.put("ppi","っぴ");
        list.put("ppu","っぷ");
        list.put("ppe","っぺ");
        list.put("ppo","っぽ");
        list.put("sha", "しゃ");
        list.put("shi", "し");
        list.put("shu", "しゅ");
        list.put("she", "しぇ");
        list.put("sho", "しょ");
        list.put("sya", "しゃ");
        list.put("syi", "しぃ");
        list.put("syu", "しゅ");
        list.put("sye", "しぇ");
        list.put("syo", "しょ");
        list.put("lla", "っぁ");
        list.put("lli", "っぃ");
        list.put("llu", "っぅ");
        list.put("lle", "っぇ");
        list.put("llo", "っぉ");
        list.put("ltu", "っ");
        list.put("bya", "びゃ");
        list.put("byu", "びゅ");
        list.put("byo", "びょ");
        list.put("zya", "じゃ");
        list.put("zyu", "じゅ");
        list.put("zyo", "じょ");
        list.put("ja", "じゃ");
        list.put("kya", "きゃ");
        list.put("kyi", "きぃ");
        list.put("kyu", "きゅ");
        list.put("kye", "きぇ");
        list.put("kyo", "きょ");
        list.put("tya", "ちゃ");
        list.put("tyu", "ちゅ");
        list.put("tyo", "ちょ");
        list.put("cha", "ちゃ");
        list.put("chu", "ちゅ");
        list.put("cho", "ちょ");
        list.put("hya", "ひゃ");
        list.put("hyi", "ひぃ");
        list.put("hyu", "ひゅ");
        list.put("hye", "ひぇ");
        list.put("hyo", "ひょ");
        list.put("pya", "ぴゃ");
        list.put("pyi", "ぴぃ");
        list.put("pyu", "ぴゅ");
        list.put("pye", "ぴぇ");
        list.put("pyo", "ぴょ");
        list.put("lya", "ゃ");
        list.put("lyi", "ぃ");
        list.put("lyu", "ゅ");
        list.put("lye", "ぇ");
        list.put("lyo", "ょ");
        list.put("nn", "ん");
        list.put("ka", "か");
        list.put("ki", "き");
        list.put("ku", "く");
        list.put("ke", "け");
        list.put("ko", "こ");
        list.put("sa", "さ");
        list.put("si", "し");
        list.put("su", "す");
        list.put("se", "せ");
        list.put("so", "そ");
        list.put("ta", "た");
        list.put("ti", "ち");
        list.put("tu", "つ");
        list.put("te", "て");
        list.put("to", "と");
        list.put("na", "な");
        list.put("ni", "に");
        list.put("nu", "ぬ");
        list.put("ne", "ね");
        list.put("no", "の");
        list.put("ha", "は");
        list.put("hi", "ひ");
        list.put("hu", "ふ");
        list.put("he", "へ");
        list.put("ho", "ほ");
        list.put("ma", "ま");
        list.put("mi", "み");
        list.put("mu", "む");
        list.put("me", "め");
        list.put("mo", "も");
        list.put("ya", "や");
        list.put("yu", "ゆ");
        list.put("ye", "いぇ");
        list.put("yo", "よ");
        list.put("ra", "ら");
        list.put("ri", "り");
        list.put("ru", "る");
        list.put("re", "れ");
        list.put("ro", "ろ");
        list.put("wa", "わ");
        list.put("wi", "うぃ");
        list.put("wu", "う");
        list.put("we", "うぇ");
        list.put("wo", "を");
        list.put("la", "ぁ");
        list.put("li", "ぃ");
        list.put("lu", "ぅ");
        list.put("le", "ぇ");
        list.put("lo", "ぉ");
        list.put("ga", "が");
        list.put("gi", "ぎ");
        list.put("gu", "ぐ");
        list.put("ge", "げ");
        list.put("go", "ご");
        list.put("za", "ざ");
        list.put("zi", "じ");
        list.put("zu", "ず");
        list.put("ze", "ぜ");
        list.put("zo", "ぞ");
        list.put("da", "だ");
        list.put("di", "ぢ");
        list.put("du", "づ");
        list.put("de", "で");
        list.put("do", "ど");
        list.put("ba", "ば");
        list.put("bi", "び");
        list.put("bu", "ぶ");
        list.put("be", "べ");
        list.put("bo", "ぼ");
        list.put("pa", "ぱ");
        list.put("pi", "ぴ");
        list.put("pu", "ぷ");
        list.put("pe", "ぺ");
        list.put("po", "ぽ");
        list.put("ji","じ");
        list.put("ju", "じゅ");
        list.put("je", "じぇ");
        list.put("jo", "じょ");
        list.put("a", "あ");
        list.put("i", "い");
        list.put("u", "う");
        list.put("e", "え");
        list.put("o", "お");
        list.put("n", "ん");
        list.put("-", "ー");

        for (Map.Entry<String, String> entry : list.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            text = text.replaceAll(k, v);
        }


        return text;
    }
}
