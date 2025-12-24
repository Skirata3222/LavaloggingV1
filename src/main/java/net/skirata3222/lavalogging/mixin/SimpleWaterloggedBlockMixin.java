package net.skirata3222.lavalogging.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogConfigLoader;
import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(SimpleWaterloggedBlock.class)
public interface SimpleWaterloggedBlockMixin {

	@Inject(method = "canPlaceLiquid", at = @At("HEAD"), cancellable = true)
	private void fixCanPlaceLiquid(@Nullable LivingEntity player, BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
		Block block = state.getBlock();

		if (block instanceof WallBlock || block instanceof CrossCollisionBlock) {
			if (fluid == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock()) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				cir.setReturnValue(true);
			}
			if (fluid == Fluids.WATER && state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
				cir.setReturnValue(false);
			}
		}
	}

	@Inject(method = "placeLiquid", at = @At("HEAD"), cancellable = true)
	private void fixPlaceLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
		Block block = state.getBlock();

		if ((block instanceof WallBlock || block instanceof CrossCollisionBlock) && state.hasProperty(LavalogPropUtil.LAVALOGGED)) {
			if (fluidState.getType() == Fluids.LAVA && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock()) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				if (!world.isClientSide()) {
					world.setBlock(pos, state.setValue(LavalogPropUtil.LAVALOGGED, true), 3);
					world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
				}
				cir.setReturnValue(true);
			}
			if (fluidState.getType() == Fluids.WATER && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
				if (!world.isClientSide()) {
					world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), 3);
					world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
				}
				cir.setReturnValue(true);
			}
			cir.setReturnValue(false);
		}
	}
	
}
