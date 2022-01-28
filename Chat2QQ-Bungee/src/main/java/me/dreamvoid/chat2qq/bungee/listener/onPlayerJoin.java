package me.dreamvoid.chat2qq.bungee.listener;

import me.dreamvoid.chat2qq.bungee.BungeePlugin;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.internal.httpapi.MiraiHttpAPI;
import me.dreamvoid.miraimc.internal.httpapi.exception.AbnormalStatusException;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.IOException;
import java.util.NoSuchElementException;

public class onPlayerJoin implements Listener {
    private final BungeePlugin plugin;
    public onPlayerJoin(BungeePlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoinEvent(PostLoginEvent e){
        if(plugin.getConfig().getBoolean("bot.send-player-join-quit-message",false)&&!e.getPlayer().hasPermission("chat2qq.join.silent")){
            plugin.getProxy().getScheduler().runAsync(plugin, () -> {
                String message = plugin.getConfig().getString("bot.player-join-message").replace("%player%", e.getPlayer().getName());
                plugin.getConfig().getLongList("bot.bot-accounts").forEach(bot -> plugin.getConfig().getLongList("bot.group-ids").forEach(group -> {
                    try {
                        MiraiBot.getBot(bot).getGroup(group).sendMessageMirai(message);
                    } catch (NoSuchElementException e1) {
                        if (MiraiHttpAPI.Bots.containsKey(bot)) {
                            try {
                                MiraiHttpAPI.INSTANCE.sendGroupMessage(MiraiHttpAPI.Bots.get(bot), group, message);
                            } catch (IOException | AbnormalStatusException ex) {
                                plugin.getLogger().warning("使用" + bot + "发送消息时出现异常，原因: " + ex);
                            }
                        } else plugin.getLogger().warning("指定的机器人" + bot + "不存在，是否已经登录了机器人？");
                    }
                }));
            });
        }
    }
}
