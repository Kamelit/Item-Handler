package org.minecrafttest.main.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minecrafttest.main.Hologram.ScoresHologram.HologramScoresParkour;
import org.minecrafttest.main.ItemHandler;
import org.minecrafttest.main.Version.Component.ColorText;
import org.minecrafttest.main.Version.MessageBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HologramCommand implements CommandExecutor, TabCompleter {

    private final ItemHandler plugin = ItemHandler.getPlugin();

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)){
            MessageBuilder onlyPlayersMessageBuilder = MessageBuilder.createMessageBuilder();
            onlyPlayersMessageBuilder.append("[" + plugin.getName() + "] ", ColorText.DARK_GRAY)
                    .append("Only players can execute this command ", ColorText.DARK_GRAY)
                    .build();
            onlyPlayersMessageBuilder.senderMessage(commandSender);
            return false;
        }

        Player player = (Player) commandSender;

        if (strings[0].equalsIgnoreCase("show") && strings[1].equalsIgnoreCase("score")){
            HologramScoresParkour hologramScoresParkour = new HologramScoresParkour();
            hologramScoresParkour.ShowScores(player.getLocation());
            return true;
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (strings.length == 1){
            return Collections.singletonList("show");
        }

        if (strings[0].contains("show") && strings.length == 2){
            return Collections.singletonList("score");
        }

        return new ArrayList<>();
    }
}
