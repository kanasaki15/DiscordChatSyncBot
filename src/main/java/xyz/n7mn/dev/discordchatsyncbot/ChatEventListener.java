package xyz.n7mn.dev.discordchatsyncbot;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import jdk.nashorn.internal.scripts.JD;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.awt.*;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

class ChatEventListener implements Listener {

    private final JDA jda;
    private final Plugin plugin;

    public ChatEventListener(JDA jda, Plugin plugin){
        this.jda = jda;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerJoinEvent (PlayerJoinEvent e){
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED){
            new Thread(()->{
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.GREEN);
                builder.setFooter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                builder.setDescription(e.getPlayer().getName()+"さんが入室しました。 (" + plugin.getServer().getOnlinePlayers().size() + "人います。)");

                TextChannel channel = jda.getGuildById(plugin.getConfig().getString("DiscordGuildID")).getTextChannelById(plugin.getConfig().getString("SendChannelID"));
                channel.sendMessageEmbeds(builder.build()).queue();
                //channel.getManager().setTopic(plugin.getServer().getOnlinePlayers().size() + " / " + plugin.getServer().getMaxPlayers()).queue();

            }).start();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void PlayerJoinEvent (PlayerQuitEvent e){
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED){
            new Thread(()->{
                EmbedBuilder builder = new EmbedBuilder();
                builder.setColor(Color.RED);
                builder.setFooter(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                builder.setDescription(e.getPlayer().getName()+"さんが退出しました。 (" + (plugin.getServer().getOnlinePlayers().size() - 1) + "人います。)");

                TextChannel channel = jda.getGuildById(plugin.getConfig().getString("DiscordGuildID")).getTextChannelById(plugin.getConfig().getString("SendChannelID"));
                channel.sendMessageEmbeds(builder.build()).queue();
                //channel.getManager().setTopic((plugin.getServer().getOnlinePlayers().size() - 1) + " / " + plugin.getServer().getMaxPlayers()).queue();

            }).start();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void AsyncPlayerChatEvent(AsyncPlayerChatEvent e){
        if (jda != null && jda.getStatus() == JDA.Status.CONNECTED){

            if (e.isCancelled()){
                if (plugin.getServer().getPluginManager().getPlugin("Trouble in Crafter Town") == null){
                    // イベントキャンセルしてたら無視する (TCT以外)
                    return;
                }
            }

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
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                builder.setFooter(format.format(new Date()));

                // スキン取得されてるしな
                AtomicReference<String> skinURL = new AtomicReference<>("");
                String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                try {
                    Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
                    Method getProfileMethod = craftPlayerClass.getMethod("getProfile");

                    GameProfile profile = (GameProfile) getProfileMethod.invoke(e.getPlayer());
                    Collection<Property> textures = profile.getProperties().get("textures");
                    if (textures != null){
                        textures.iterator().forEachRemaining(ac ->{
                            if (ac.getName().equals("textures")){
                                String img_base64 = ac.getValue();
                                byte[] decode = Base64.getDecoder().decode(img_base64);
                                SkinClass json = new Gson().fromJson(new String(decode, StandardCharsets.UTF_8), SkinClass.class);

                                skinURL.set(json.getTextures().getSKIN().getUrl());

                            }
                        });


                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                builder.setColor(Color.ORANGE);
                if (!e.getPlayer().getName().startsWith(".")){
                    builder.setAuthor(e.getPlayer().getName(),"https://mine.ly/"+e.getPlayer().getName());
                } else {
                    builder.setAuthor(e.getPlayer().getName().replaceAll("\\.","") + " (BE)");
                }

                builder.setDescription("```"+message.replaceAll("`","｀")+"```");
                if (skinURL.get().length() > 0){
                    builder.setThumbnail("https://skin.7mi.site/?url="+skinURL.get());
                } else if (!e.getPlayer().getName().startsWith(".")) {
                    builder.setThumbnail("https://cravatar.eu/avatar/"+e.getPlayer().getName()+"/64.png");
                }
                jda.getGuildById(plugin.getConfig().getString("DiscordGuildID")).getTextChannelById(plugin.getConfig().getString("SendChannelID")).sendMessageEmbeds(builder.build()).queue();

            }).start();
        }
    }

    public void PlayerCommandPreprocessEvent (PlayerCommandPreprocessEvent e){
        if (!plugin.getConfig().getBoolean("SendCommand")){
            return;
        }
        String message = e.getMessage();

        EmbedBuilder builder = new EmbedBuilder();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        builder.setFooter(format.format(new Date()));

        // スキン取得されてるしな
        AtomicReference<String> skinURL = new AtomicReference<>("");
        String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        try {
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nmsVersion + ".entity.CraftPlayer");
            Method getProfileMethod = craftPlayerClass.getMethod("getProfile");

            GameProfile profile = (GameProfile) getProfileMethod.invoke(e.getPlayer());
            Collection<Property> textures = profile.getProperties().get("textures");
            if (textures != null){
                textures.iterator().forEachRemaining(ac ->{
                    if (ac.getName().equals("textures")){
                        String img_base64 = ac.getValue();
                        byte[] decode = Base64.getDecoder().decode(img_base64);
                        SkinClass json = new Gson().fromJson(new String(decode, StandardCharsets.UTF_8), SkinClass.class);

                        skinURL.set(json.getTextures().getSKIN().getUrl());

                    }
                });


            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        builder.setColor(Color.ORANGE);
        if (!e.getPlayer().getName().startsWith(".")){
            builder.setAuthor(e.getPlayer().getName(),"https://mine.ly/"+e.getPlayer().getName());
        } else {
            builder.setAuthor(e.getPlayer().getName().replaceAll("\\.","") + " (BE)");
        }

        builder.setDescription("```"+message.replaceAll("`","｀")+"```");
        if (skinURL.get().length() > 0){
            builder.setThumbnail("https://skin.7mi.site/?url="+skinURL.get());
        } else if (!e.getPlayer().getName().startsWith(".")) {
            builder.setThumbnail("https://cravatar.eu/avatar/"+e.getPlayer().getName()+"/64.png");
        }
        jda.getGuildById(plugin.getConfig().getString("DiscordGuildID")).getTextChannelById(plugin.getConfig().getString("SendChannelID")).sendMessageEmbeds(builder.build()).queue();

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

            if (!message.getChannel().getId().equals(plugin.getConfig().getString("ReceptionChannelID"))){
                return;
            }

            StringBuffer sb = new StringBuffer();

            sb.append(message.getContentRaw().substring(0, Math.min(message.getContentRaw().length(), 100)));

            if (message.getAttachments().size() > 0){
                for (Message.Attachment file : message.getAttachments()){
                    sb.append("\n");
                    sb.append(file.getUrl());
                }
            }

            if (member.getNickname() != null){
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + sb.toString() + ChatColor.RESET + " (by " + member.getNickname()+")");
            } else {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + sb.toString() + ChatColor.RESET + " (by " + author.getName()+")");
            }

            for (Player player : players){

                Bukkit.getScheduler().runTask(plugin, ()->{
                    if (member.getNickname() != null){
                        player.sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + message.getContentDisplay().substring(0, 100) + sb.toString() + ChatColor.RESET + " (by " + member.getNickname()+")");
                    } else {
                        player.sendMessage(ChatColor.AQUA + "[Discord] " + ChatColor.RESET + message.getContentDisplay().substring(0, 100) + sb.toString() + ChatColor.RESET + " (by " + author.getName()+")");
                    }
                });
            }

        }).start();

    }


    private String lati2hira(String text){
        List<Moji> list = new ArrayList<>();

        list.add(new Moji("ttsu","っつ"));
        list.add(new Moji("zzya","っじゃ"));
        list.add(new Moji("zzyu","っじゅ"));
        list.add(new Moji("zzye","っじぇ"));
        list.add(new Moji("zzyo","っじょ"));
        list.add(new Moji("ssya","っしゃ"));
        list.add(new Moji("ssha","っしゃ"));
        list.add(new Moji("sshi","っし"));
        list.add(new Moji("ssyu","っしゅ"));
        list.add(new Moji("sshu","っしゅ"));
        list.add(new Moji("ssye","っしぇ"));
        list.add(new Moji("sshe","っしぇ"));
        list.add(new Moji("ssyo","っしょ"));
        list.add(new Moji("ssho","っしょ"));
        list.add(new Moji("ttsa", "っつぁ"));
        list.add(new Moji("ttsi", "っつぃ"));
        list.add(new Moji("ttse", "っつぇ"));
        list.add(new Moji("ttso", "っつぉ"));
        list.add(new Moji("ccha", "っちゃ"));
        list.add(new Moji("cchi", "っち"));
        list.add(new Moji("cchu", "っちゅ"));
        list.add(new Moji("cche", "っちぇ"));
        list.add(new Moji("ccho", "っちょ"));
        list.add(new Moji("ttya", "っちゃ"));
        list.add(new Moji("ttyi", "っちぃ"));
        list.add(new Moji("ttyu", "っちゅ"));
        list.add(new Moji("ttye", "っちぇ"));
        list.add(new Moji("ttyo", "っちょ"));
        list.add(new Moji("wyi", "ゐ"));
        list.add(new Moji("wye", "ゑ"));
        list.add(new Moji("tsa", "つぁ"));
        list.add(new Moji("tsi", "つぃ"));
        list.add(new Moji("tsu", "つ"));
        list.add(new Moji("tse", "つぇ"));
        list.add(new Moji("tso", "つぉ"));
        list.add(new Moji("kka","っか"));
        list.add(new Moji("kki","っき"));
        list.add(new Moji("kku","っく"));
        list.add(new Moji("kke","っけ"));
        list.add(new Moji("kko","っこ"));
        list.add(new Moji("ssa","っさ"));
        list.add(new Moji("ssi","っし"));
        list.add(new Moji("ssu","っす"));
        list.add(new Moji("sse","っせ"));
        list.add(new Moji("sso","っそ"));
        list.add(new Moji("tta","った"));
        list.add(new Moji("tti","っち"));
        list.add(new Moji("ttu","っつ"));
        list.add(new Moji("tte","って"));
        list.add(new Moji("tto","っと"));
        list.add(new Moji("hha","っは"));
        list.add(new Moji("hhi","っひ"));
        list.add(new Moji("hhu","っふ"));
        list.add(new Moji("hhe","っへ"));
        list.add(new Moji("hho","っほ"));
        list.add(new Moji("mma","っま"));
        list.add(new Moji("mmi","っみ"));
        list.add(new Moji("mmu","っむ"));
        list.add(new Moji("mme","っめ"));
        list.add(new Moji("mmo","っも"));
        list.add(new Moji("yya","っや"));
        list.add(new Moji("yyu","っゆ"));
        list.add(new Moji("yyo","っよ"));
        list.add(new Moji("rra","っら"));
        list.add(new Moji("rri","っり"));
        list.add(new Moji("rru","っる"));
        list.add(new Moji("rre","っれ"));
        list.add(new Moji("rro","っろ"));
        list.add(new Moji("wwa","っわ"));
        list.add(new Moji("wwo","っを"));
        list.add(new Moji("zza","っざ"));
        list.add(new Moji("zzi","っじ"));
        list.add(new Moji("zzu","っず"));
        list.add(new Moji("zze","っぜ"));
        list.add(new Moji("zzo","っぞ"));
        list.add(new Moji("dda","っだ"));
        list.add(new Moji("ddi","っぢ"));
        list.add(new Moji("ddu","っづ"));
        list.add(new Moji("dde","っで"));
        list.add(new Moji("ddo","っど"));
        list.add(new Moji("bba","っば"));
        list.add(new Moji("bbi","っび"));
        list.add(new Moji("bbu","っぶ"));
        list.add(new Moji("bbe","っべ"));
        list.add(new Moji("bbo","っぼ"));
        list.add(new Moji("ppa","っぱ"));
        list.add(new Moji("ppi","っぴ"));
        list.add(new Moji("ppu","っぷ"));
        list.add(new Moji("ppe","っぺ"));
        list.add(new Moji("ppo","っぽ"));
        list.add(new Moji("sha", "しゃ"));
        list.add(new Moji("shi", "し"));
        list.add(new Moji("shu", "しゅ"));
        list.add(new Moji("she", "しぇ"));
        list.add(new Moji("sho", "しょ"));
        list.add(new Moji("sya", "しゃ"));
        list.add(new Moji("syi", "しぃ"));
        list.add(new Moji("syu", "しゅ"));
        list.add(new Moji("sye", "しぇ"));
        list.add(new Moji("syo", "しょ"));
        list.add(new Moji("lla", "っぁ"));
        list.add(new Moji("lli", "っぃ"));
        list.add(new Moji("llu", "っぅ"));
        list.add(new Moji("lle", "っぇ"));
        list.add(new Moji("llo", "っぉ"));
        list.add(new Moji("ltu", "っ"));
        list.add(new Moji("bya", "びゃ"));
        list.add(new Moji("byu", "びゅ"));
        list.add(new Moji("byo", "びょ"));
        list.add(new Moji("zya", "じゃ"));
        list.add(new Moji("zyu", "じゅ"));
        list.add(new Moji("zyo", "じょ"));
        list.add(new Moji("ja", "じゃ"));
        list.add(new Moji("kya", "きゃ"));
        list.add(new Moji("kyi", "きぃ"));
        list.add(new Moji("kyu", "きゅ"));
        list.add(new Moji("kye", "きぇ"));
        list.add(new Moji("kyo", "きょ"));
        list.add(new Moji("tya", "ちゃ"));
        list.add(new Moji("tyu", "ちゅ"));
        list.add(new Moji("tyo", "ちょ"));
        list.add(new Moji("cha", "ちゃ"));
        list.add(new Moji("chu", "ちゅ"));
        list.add(new Moji("cho", "ちょ"));
        list.add(new Moji("hya", "ひゃ"));
        list.add(new Moji("hyi", "ひぃ"));
        list.add(new Moji("hyu", "ひゅ"));
        list.add(new Moji("hye", "ひぇ"));
        list.add(new Moji("hyo", "ひょ"));
        list.add(new Moji("pya", "ぴゃ"));
        list.add(new Moji("pyi", "ぴぃ"));
        list.add(new Moji("pyu", "ぴゅ"));
        list.add(new Moji("pye", "ぴぇ"));
        list.add(new Moji("pyo", "ぴょ"));
        list.add(new Moji("lya", "ゃ"));
        list.add(new Moji("lyi", "ぃ"));
        list.add(new Moji("lyu", "ゅ"));
        list.add(new Moji("lye", "ぇ"));
        list.add(new Moji("lyo", "ょ"));
        list.add(new Moji("xtu", "っ"));
        list.add(new Moji("nn", "ん"));
        list.add(new Moji("ka", "か"));
        list.add(new Moji("ki", "き"));
        list.add(new Moji("ku", "く"));
        list.add(new Moji("ke", "け"));
        list.add(new Moji("ko", "こ"));
        list.add(new Moji("sa", "さ"));
        list.add(new Moji("si", "し"));
        list.add(new Moji("su", "す"));
        list.add(new Moji("se", "せ"));
        list.add(new Moji("so", "そ"));
        list.add(new Moji("ta", "た"));
        list.add(new Moji("ti", "ち"));
        list.add(new Moji("tu", "つ"));
        list.add(new Moji("te", "て"));
        list.add(new Moji("to", "と"));
        list.add(new Moji("na", "な"));
        list.add(new Moji("ni", "に"));
        list.add(new Moji("nu", "ぬ"));
        list.add(new Moji("ne", "ね"));
        list.add(new Moji("no", "の"));
        list.add(new Moji("ha", "は"));
        list.add(new Moji("hi", "ひ"));
        list.add(new Moji("hu", "ふ"));
        list.add(new Moji("he", "へ"));
        list.add(new Moji("ho", "ほ"));
        list.add(new Moji("ma", "ま"));
        list.add(new Moji("mi", "み"));
        list.add(new Moji("mu", "む"));
        list.add(new Moji("me", "め"));
        list.add(new Moji("mo", "も"));
        list.add(new Moji("ya", "や"));
        list.add(new Moji("yu", "ゆ"));
        list.add(new Moji("ye", "いぇ"));
        list.add(new Moji("yo", "よ"));
        list.add(new Moji("ra", "ら"));
        list.add(new Moji("ri", "り"));
        list.add(new Moji("ru", "る"));
        list.add(new Moji("re", "れ"));
        list.add(new Moji("ro", "ろ"));
        list.add(new Moji("wa", "わ"));
        list.add(new Moji("wi", "うぃ"));
        list.add(new Moji("wu", "う"));
        list.add(new Moji("we", "うぇ"));
        list.add(new Moji("wo", "を"));
        list.add(new Moji("la", "ぁ"));
        list.add(new Moji("li", "ぃ"));
        list.add(new Moji("lu", "ぅ"));
        list.add(new Moji("le", "ぇ"));
        list.add(new Moji("lo", "ぉ"));
        list.add(new Moji("ga", "が"));
        list.add(new Moji("gi", "ぎ"));
        list.add(new Moji("gu", "ぐ"));
        list.add(new Moji("ge", "げ"));
        list.add(new Moji("go", "ご"));
        list.add(new Moji("za", "ざ"));
        list.add(new Moji("zi", "じ"));
        list.add(new Moji("zu", "ず"));
        list.add(new Moji("ze", "ぜ"));
        list.add(new Moji("zo", "ぞ"));
        list.add(new Moji("da", "だ"));
        list.add(new Moji("di", "ぢ"));
        list.add(new Moji("du", "づ"));
        list.add(new Moji("de", "で"));
        list.add(new Moji("do", "ど"));
        list.add(new Moji("ba", "ば"));
        list.add(new Moji("bi", "び"));
        list.add(new Moji("bu", "ぶ"));
        list.add(new Moji("be", "べ"));
        list.add(new Moji("bo", "ぼ"));
        list.add(new Moji("pa", "ぱ"));
        list.add(new Moji("pi", "ぴ"));
        list.add(new Moji("pu", "ぷ"));
        list.add(new Moji("pe", "ぺ"));
        list.add(new Moji("po", "ぽ"));
        list.add(new Moji("ji","じ"));
        list.add(new Moji("ju", "じゅ"));
        list.add(new Moji("je", "じぇ"));
        list.add(new Moji("jo", "じょ"));
        list.add(new Moji("a", "あ"));
        list.add(new Moji("i", "い"));
        list.add(new Moji("u", "う"));
        list.add(new Moji("e", "え"));
        list.add(new Moji("o", "お"));
        list.add(new Moji("n", "ん"));
        list.add(new Moji("-", "ー"));

        for (Moji moji : list) {
            text = text.replaceAll(moji.getKey(), moji.getValue());
        }


        return text;
    }

    private class SkinClass {

        private long timestamp;
        private String profileId;
        private String profileName;
        private Skin textures;

        public long getTimestamp() {
            return timestamp;
        }

        public String getProfileId() {
            return profileId;
        }

        public String getProfileName() {
            return profileName;
        }

        public Skin getTextures() {
            return textures;
        }
    }

    private class Skin {
        private SkinData SKIN;

        public SkinData getSKIN() {
            return SKIN;
        }
    }

    private class SkinData {
        private String url;
        private SkinMeta metadata;

        public String getUrl() {
            return url;
        }

        public SkinMeta getMetadata() {
            return metadata;
        }
    }

    private class SkinMeta {
        private String model;

        public String getModel() {
            return model;
        }
    }

    private class Moji{
        private String key;
        private String value;

        public Moji(String key, String value){
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
}


