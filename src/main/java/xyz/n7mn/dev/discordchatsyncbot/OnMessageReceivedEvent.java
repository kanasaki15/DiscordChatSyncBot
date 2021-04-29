package xyz.n7mn.dev.discordchatsyncbot;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class OnMessageReceivedEvent extends Event {

    private static HandlerList handlerList = new HandlerList();
    private final MessageReceivedEvent event;

    public OnMessageReceivedEvent(MessageReceivedEvent e){
        this.event = e;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList(){
        return handlerList;
    }

    public MessageReceivedEvent getEventMessageReceivedEvent(){
        return event;
    }
}
