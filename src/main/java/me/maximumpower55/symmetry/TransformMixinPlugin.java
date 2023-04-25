package me.maximumpower55.symmetry;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.quiltmc.loader.api.QuiltLoader;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;

public class TransformMixinPlugin implements IMixinConfigPlugin {

	private class Extension implements IExtension {

		private static final Multimap<String, String> MIXINS_TO_DISABLE = HashMultimap.create();

		private static Class<?> TARGET_CLASS_CONTEXT_CLASS = null;
		private static Class<?> MIXIN_INFO_CLASS = null;
		private static Field MIXINS_FIELD = null;
		private static Field CLASS_NAME_FIELD = null;
		static {
			try {
				TARGET_CLASS_CONTEXT_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.TargetClassContext");
				MIXINS_FIELD = TARGET_CLASS_CONTEXT_CLASS.getDeclaredField("mixins");
				MIXINS_FIELD.setAccessible(true);

				MIXIN_INFO_CLASS = Class.forName("org.spongepowered.asm.mixin.transformer.MixinInfo");
				CLASS_NAME_FIELD = MIXIN_INFO_CLASS.getDeclaredField("className");
				CLASS_NAME_FIELD.setAccessible(true);
			} catch (Exception e) {
				System.out.println("Error while acquiring reflection for transforming mixin:");
				e.printStackTrace();
			}
		}

		@Override
		public boolean checkActive(MixinEnvironment environment) {
			return true;
		}

		@Override
		@SuppressWarnings("rawtypes")
		public void preApply(ITargetClassContext context) {
			for (String targetClassName : MIXINS_TO_DISABLE.keySet()) {
				if (context.getClassInfo().getName().equals(targetClassName)) {
					final var mixinsToDisable = MIXINS_TO_DISABLE.get(targetClassName);

					try {
						final var mixinsToApply = (Set<?>) MIXINS_FIELD.get(context);

						final var newMixinsToApply = Sets.newTreeSet();
						for (Object mixinInfo : mixinsToApply) {
							final var className = (String) CLASS_NAME_FIELD.get(mixinInfo);
							if (!mixinsToDisable.contains(className)) newMixinsToApply.add((Comparable) mixinInfo);
							System.out.println(String.format("Disabling mixin: {} for {}", className, targetClassName));
						}
						MIXINS_FIELD.set(context, newMixinsToApply);
					} catch (Exception e) {
						System.out.println(String.format("Failed to disable a mixin for {}", targetClassName));
						e.printStackTrace();
						return;
					}
				}
			}
		}

		@Override
		public void postApply(ITargetClassContext context) {
		}

		@Override
		public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {

		}

	}

	@Override
	public void onLoad(String mixinPackage) {
		((Extensions) ((IMixinTransformer) MixinEnvironment.getCurrentEnvironment().getActiveTransformer()).getExtensions()).add(new Extension());

		disableMixin(QuiltLoader.getMappingResolver().mapClassName("intermediary", "net.minecraft.class_2302"), "com.terraformersmc.terraform.dirt.mixin.MixinCropBlock");

		MixinExtrasBootstrap.init();
	}

	public static void disableMixin(String targetClassName, String mixinClassName) {
		Extension.MIXINS_TO_DISABLE.put(targetClassName.replace(".", "/"), mixinClassName);
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

	}

}
