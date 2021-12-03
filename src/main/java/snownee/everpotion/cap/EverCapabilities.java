package snownee.everpotion.cap;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import snownee.everpotion.handler.EverHandler;

public final class EverCapabilities {

	public static Capability<EverHandler> HANDLER = CapabilityManager.get(new CapabilityToken<>() {
	});

}
