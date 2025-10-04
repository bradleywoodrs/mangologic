package com.github.bradleywoodrs.mangologic.Discord;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.util.DiscordUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.stream.Collectors;

public class DiscordSync {
    private Plugin plugin;
    public String guildid;
    public String boosterroleid;
    public DiscordSync(Plugin plugin) {
        this.plugin = plugin;
    }

    public final DiscordSRVListener discordsrvListener = new DiscordSRVListener(plugin, this);

    public void SyncPlayer(Player player){
        LuckPerms lp = LuckPermsProvider.get();
        User user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null){
            return;
        }
        Set<String> groups = user.getNodes().stream().filter(node -> node instanceof InheritanceNode).map(node -> ((InheritanceNode) node).getGroupName()).collect(Collectors.toSet());
        String discordId = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
        if (discordId == null){
            return;
        }
        Guild guild = DiscordUtil.getJda().getGuildById(this.guildid);
        Member member = guild.getMemberById(discordId);
        if (member == null){
            return;
        }

        if (member.getRoles().stream().anyMatch(r -> r.getId().equals(this.boosterroleid))){
            if (!groups.contains("booster")){
                user.data().add(InheritanceNode.builder("booster").build());
                lp.getUserManager().saveUser(user);
            }
        }else {
            if (groups.contains("booster")){
                Set<Node> toRemove = user.data().toCollection().stream()
                        .filter(node -> node instanceof InheritanceNode)
                        .filter(node -> ((InheritanceNode) node).getGroupName().equals("booster"))
                        .collect(Collectors.toSet());
                toRemove.forEach(user.data()::remove);
                lp.getUserManager().saveUser(user);
            }
        }
    }

    public String getGuildid() {
        return guildid;
    }

    public void setGuildid(String guildid) {
        this.guildid = guildid;
    }

    public String getBoosterroleid() {
        return boosterroleid;
    }

    public void setBoosterroleid(String boosterroleid) {
        this.boosterroleid = boosterroleid;
    }
}
