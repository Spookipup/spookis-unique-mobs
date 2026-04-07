package spookipup.uniquemobs.compat.betterfortresses;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

// only applies better fortresses compat mixins when the mod is actually installed
public class BetterFortressesMixinPlugin implements IMixinConfigPlugin {

	private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("betterfortresses");

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return LOADED;
	}

	@Override public void onLoad(String mixinPackage) {}
	@Override public String getRefMapperConfig() { return null; }
	@Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override public List<String> getMixins() { return null; }
	@Override public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
