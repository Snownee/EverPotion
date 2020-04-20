package snownee.everpotion;

import java.util.Collection;
import java.util.Collections;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import snownee.everpotion.cap.EverCapabilities;
import snownee.everpotion.handler.EverHandler;

public class EverCommand {

    public static LiteralArgumentBuilder<CommandSource> init(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal(EverPotion.MODID);

        /* off */
        builder
        .then(Commands
                .literal("refill")
                .requires(ctx -> ctx.hasPermissionLevel(2))
                .executes(ctx-> refill(ctx, Collections.singleton(ctx.getSource().asPlayer())))
                .then(Commands
                        .argument("target", EntityArgument.players())
                        .executes(ctx-> refill(ctx, EntityArgument.getPlayers(ctx, "target")))
                )
        );
        /* on */

        return builder;
    }

    private static int refill(CommandContext<CommandSource> source, Collection<ServerPlayerEntity> players) {
        int i = 0;
        for (ServerPlayerEntity player : players) {
            EverHandler handler = player.getCapability(EverCapabilities.HANDLER).orElse(null);
            if (handler != null) {
                handler.refill();
                ++i;
            }
        }
        return i;
    }
}
