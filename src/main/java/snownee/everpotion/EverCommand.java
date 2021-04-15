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
                .literal("fill")
                .requires(ctx -> ctx.hasPermissionLevel(2))
                .executes(ctx-> setAll(ctx, Collections.singleton(ctx.getSource().asPlayer()), EverCommonConfig.refillTime))
                .then(Commands
                        .argument("target", EntityArgument.players())
                        .executes(ctx-> setAll(ctx, EntityArgument.getPlayers(ctx, "target"), EverCommonConfig.refillTime))
                )
        )

        .then(Commands
                .literal("empty")
                .requires(ctx -> ctx.hasPermissionLevel(2))
                .executes(ctx-> setAll(ctx, Collections.singleton(ctx.getSource().asPlayer()), 0))
                .then(Commands
                        .argument("target", EntityArgument.players())
                        .executes(ctx-> setAll(ctx, EntityArgument.getPlayers(ctx, "target"), 0))
                )
        );
        /* on */

		return builder;
	}

	private static int setAll(CommandContext<CommandSource> source, Collection<ServerPlayerEntity> players, int time) {
		int i = 0;
		for (ServerPlayerEntity player : players) {
			EverHandler handler = player.getCapability(EverCapabilities.HANDLER).orElse(null);
			if (handler != null) {
				handler.setAll(time);
				++i;
			}
		}
		return i;
	}
}
