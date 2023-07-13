package snownee.skillslots.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import snownee.kiwi.loader.Platform;
import snownee.skillslots.SkillSlotsModule;

public class CommonProxy {
	public CommonProxy() {
		MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
		MinecraftForge.EVENT_BUS.addListener(this::playerLoggedIn);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, this::causeDamage);
		MinecraftForge.EVENT_BUS.addListener(this::clonePlayer);
		if (Platform.isPhysicalClient()) {
			ClientProxy.init();
		}
	}

	public static boolean isFakePlayer(Player player) {
		return player instanceof FakePlayer;
	}

	public static double getReachDistance(Player player) {
		return player.getReachDistance();
	}

	private void registerCommands(RegisterCommandsEvent event) {
		SkillSlotsModule.registerCommands(event.getDispatcher());
	}

	private void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			SkillSlotsModule.playerLoggedIn(player);
		}
	}

	private void causeDamage(LivingDamageEvent event) {
		SkillSlotsModule.causeDamage(event.getSource(), event.getEntity(), event.getAmount());
	}

	private void clonePlayer(PlayerEvent.Clone event) {
		SkillSlotsModule.clonePlayer(event.getOriginal(), event.getEntity());
	}
}
