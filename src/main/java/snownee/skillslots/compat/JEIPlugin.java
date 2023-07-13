package snownee.skillslots.compat;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IColorHelper;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.skillslots.SkillSlots;
import snownee.skillslots.SkillSlotsModule;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

	public static final ResourceLocation UID = new ResourceLocation(SkillSlots.ID, "main");
	private static IColorHelper colorHelper;

	public static int pickItemColor(ItemStack stack, int fallback) {
		if (colorHelper == null) {
			return fallback;
		}
		List<Integer> colors = colorHelper.getColors(stack, 1);
		return colors.isEmpty() ? fallback : colors.get(colors.size() - 1);
	}

	@Override
	public @NotNull ResourceLocation getPluginUid() {
		return UID;
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		registration.useNbtForSubtypes(SkillSlotsModule.UNLOCK_SLOT.get());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		colorHelper = jeiRuntime.getJeiHelpers().getColorHelper();
	}

	@Override
	public void onRuntimeUnavailable() {
		colorHelper = null;
	}
}
