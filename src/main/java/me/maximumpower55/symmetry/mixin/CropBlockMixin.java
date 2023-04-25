package me.maximumpower55.symmetry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.terraformersmc.terraform.dirt.TerraformDirtBlockTags;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

// Original mixin from Terraform disabled through TransformMixinPlugin.java, reimplemented to not crash with balm
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

	@WrapOperation(
		method = "getGrowthSpeed",
		at = @At(value = "INVOKE", target = "net/minecraft/block/BlockState.is(Lnet/minecraft/block/Block;)Z")
	)
	private static boolean sym$isOfFarmlandTag(BlockState state, Block block, Operation<Boolean> original) {
		return original.call(state, block) || (block == Blocks.FARMLAND && state.is(TerraformDirtBlockTags.FARMLAND));
	}

}
