package net.skirata3222.lavalogging.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(FallingBlockEntity.class)
public abstract class FallingBlockMixin {
	
	@Invoker("<init>")
	private static FallingBlockEntity constructorInvoker(Level level, double x, double y, double z, BlockState state) {
		throw new AssertionError();
	}

	@Inject(method = "fall", at = @At("HEAD"), cancellable = true)
	private static void preserveLiquid(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<FallingBlockEntity> cir) {

		if (!(state.getBlock() instanceof AnvilBlock)) {
			return;
		}

		BlockState fallingState = state;
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			fallingState = fallingState.setValue(BlockStateProperties.WATERLOGGED, false);
		}
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED)) {
			fallingState = fallingState.setValue(LavalogPropUtil.LAVALOGGED, false);
		}

		FallingBlockEntity newFallingBlockEntity = constructorInvoker(level, pos.getX()+0.5, pos.getY(), pos.getZ()+0.5, fallingState);

		FluidState fluid = level.getFluidState(pos);
		BlockState newState;
		if (fluid.is(Fluids.WATER)) {
			newState = Blocks.WATER.defaultBlockState();
		} else if (fluid.is(Fluids.LAVA)) {
			newState = Blocks.LAVA.defaultBlockState();
		} else {
			newState = Blocks.AIR.defaultBlockState();
		}


		level.setBlock(pos, newState, 3);
		level.addFreshEntity(newFallingBlockEntity);
		cir.setReturnValue(newFallingBlockEntity);
		
	}

	@Shadow
	private BlockState blockState;

	@ModifyArg(
		method = "tick",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"
		),
		index = 1
	)
	private BlockState lavalog$applyLavaLoggingOnLanding(BlockState original) {
		FallingBlockEntity self = (FallingBlockEntity)(Object)this;
		Level level = self.level();
		BlockPos pos = self.blockPosition();

		if (!(original.getBlock() instanceof AnvilBlock)) {
			return original;
		}

		FluidState fluid = level.getFluidState(pos);

		if (fluid.getType() == Fluids.LAVA &&
			original.hasProperty(LavalogPropUtil.LAVALOGGED)) {

			return original.setValue(LavalogPropUtil.LAVALOGGED, true);
		}

		return original;
	}


}
