package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(FallingBlock.class)
public class FallingBlockMixin {
	
	@Inject(method = "fall", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/level/LevelAccessor;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"), cancellable = true)
	private void preserveLiquid(BlockPos pos, BlockState state, CallbackInfo ci) {
		FallingBlockEntity self = (FallingBlockEntity)(Object)this;
		LevelAccessor level = self.level();
		BlockState fallingState = self.getBlockState();

		// Only modify anvils
		if (!(fallingState.getBlock() instanceof AnvilBlock)) {
			return;
		}

		FluidState fluid = level.getFluidState(pos);

		// Water → waterlogged anvil
		if (fluid.getType() == Fluids.WATER) {
			fallingState = fallingState
				.setValue(BlockStateProperties.WATERLOGGED, true)
				.setValue(LavalogPropUtil.LAVALOGGED, false);
		}

		// Lava → lavalogged anvil
		if (fluid.getType() == Fluids.LAVA) {
			fallingState = fallingState
				.setValue(BlockStateProperties.WATERLOGGED, false)
				.setValue(LavalogPropUtil.LAVALOGGED, true);
		}

		// Place the modified state instead of vanilla’s
		level.setBlock(pos, fallingState, 3);

		// Prevent vanilla from placing the unmodified state
		ci.cancel();

		}

}
