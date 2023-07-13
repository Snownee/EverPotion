package snownee.skillslots;

import java.util.Collection;
import java.util.List;
import java.util.function.IntUnaryOperator;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class SkillSlotsCommand {

	public static LiteralArgumentBuilder<CommandSourceStack> init(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(SkillSlots.ID);

		/* off */
		builder
		.then(Commands
				.literal("fill")
				.requires(ctx -> ctx.hasPermission(2))
				.executes(ctx-> setAll(ctx, List.of(ctx.getSource().getPlayerOrException()), true))
				.then(Commands
						.argument("target", EntityArgument.players())
						.executes(ctx -> setAll(ctx, EntityArgument.getPlayers(ctx, "target"), true))
				)
		)

		.then(Commands
				.literal("empty")
				.requires(ctx -> ctx.hasPermission(2))
				.executes(ctx-> setAll(ctx, List.of(ctx.getSource().getPlayerOrException()), false))
				.then(Commands
						.argument("target", EntityArgument.players())
						.executes(ctx -> setAll(ctx, EntityArgument.getPlayers(ctx, "target"), false))
				)
		)

		.then(Commands
				.literal("level")
				.requires(ctx -> ctx.hasPermission(2))
				.then(Commands
						.literal("add")
						.then(Commands
								.argument("target", EntityArgument.players())
								.then(Commands
										.argument("level", IntegerArgumentType.integer())
										.executes(ctx -> {
											int i = IntegerArgumentType.getInteger(ctx, "level");
											return level(ctx, EntityArgument.getPlayers(ctx, "target"), j -> i + j);
										})
								)
						)
				)
				.then(Commands
						.literal("set")
						.then(Commands
								.argument("target", EntityArgument.players())
								.then(Commands
										.argument("level", IntegerArgumentType.integer())
										.executes(ctx -> {
											int i = IntegerArgumentType.getInteger(ctx, "level");
											return level(ctx, EntityArgument.getPlayers(ctx, "target"), j -> i);
										})
								)
						)
				)
		);
        /* on */

		return builder;
	}

	private static int setAll(CommandContext<CommandSourceStack> ctx, Collection<ServerPlayer> players, boolean fill) {
		for (ServerPlayer player : players) {
			SkillSlotsHandler handler = SkillSlotsHandler.of(player);
			handler.setAll(fill);
		}
		return players.size();
	}

	private static int level(CommandContext<CommandSourceStack> source, Collection<ServerPlayer> players, IntUnaryOperator newLevel) {
		for (ServerPlayer player : players) {
			SkillSlotsHandler handler = SkillSlotsHandler.of(player);
			handler.setSlots(newLevel.applyAsInt(handler.getContainerSize()));
		}
		return players.size();
	}
}
