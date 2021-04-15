package snownee.everpotion.cap;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import snownee.everpotion.handler.EverHandler;

public final class EverCapabilities {

	@CapabilityInject(EverHandler.class)
	public static Capability<EverHandler> HANDLER = null;

}
