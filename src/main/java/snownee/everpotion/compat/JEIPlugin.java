package snownee.everpotion.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import snownee.everpotion.CoreModule;
import snownee.everpotion.EverPotion;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(EverPotion.ID, "main");

	@Override
	public ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(CoreModule.CORE.get());
		registration.useNbtForSubtypes(CoreModule.UNLOCK_SLOT.get());
	}

}
