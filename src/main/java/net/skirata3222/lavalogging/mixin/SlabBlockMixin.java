package net.skirata3222.lavalogging.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogConfigLoader;
import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(SlabBlock.class)
public abstract class SlabBlockMixin implements LiquidBlockContainer, BucketPickup {

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void addLavaloggedProperty(StateDefinition.Builder<Block,BlockState> builder, CallbackInfo ci) {
		builder.add(LavalogPropUtil.LAVALOGGED);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectDefaultState(BlockBehaviour.Properties properties, CallbackInfo ci) {
		SlabBlock self = (SlabBlock)(Object)this;
		((BlockInvoker)self).invokeRegisterDefaultState(
			self.defaultBlockState()
				.setValue(SlabBlock.TYPE, SlabType.BOTTOM)
				.setValue(SlabBlock.WATERLOGGED, false)
				.setValue(LavalogPropUtil.LAVALOGGED, false)
		);
	}

	@Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
	private void injectLavaPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
		
		BlockState newState = cir.getReturnValue();
		Level level = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState existing = level.getBlockState(pos);
		if (!newState.hasProperty(LavalogPropUtil.LAVALOGGED)) {
			return;
		}
		FluidState fluid = level.getFluidState(pos);
		if (fluid.getType() == Fluids.LAVA
				&& newState.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
			cir.setReturnValue(newState.setValue(LavalogPropUtil.LAVALOGGED, true));
			return;
		}
		if (newState.getValue(SlabBlock.TYPE) == SlabType.DOUBLE
				&& existing.hasProperty(LavalogPropUtil.LAVALOGGED)
				&& existing.getValue(LavalogPropUtil.LAVALOGGED)) {
			cir.setReturnValue(newState.setValue(LavalogPropUtil.LAVALOGGED, false));
			return;
		}
	}

	@Inject(method = "updateShape", at = @At("RETURN"), cancellable = true)
	private void lavalogNeighbor(BlockState state, LevelReader reader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
		BlockState result = cir.getReturnValue();
		if (result.hasProperty(LavalogPropUtil.LAVALOGGED) && (Boolean)result.getValue(LavalogPropUtil.LAVALOGGED)) {
			tickAccess.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(reader));
		}
		if (result.hasProperty(LavalogPropUtil.LAVALOGGED) && result.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
			cir.setReturnValue(result.setValue(LavalogPropUtil.LAVALOGGED, false));
		}
	}

	@Inject(method = "canPlaceLiquid", at = @At("HEAD"), cancellable = true)
	private void canFillFixed(@Nullable LivingEntity player, BlockGetter world, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> cir) {
		if (fluid == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock()) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED) && state.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
			cir.setReturnValue(true);
			return;
		}
		if (fluid == Fluids.WATER && state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "placeLiquid", at = @At("HEAD"), cancellable = true)
	private void tryFillFixed(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
		if (fluidState.getType() == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock()) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
			if (state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
				if (!level.isClientSide()) {
					level.setBlock(pos, state.setValue(LavalogPropUtil.LAVALOGGED, true), 3);
					level.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
				}
				cir.setReturnValue(true);
				return;
			}
		}
		if (fluidState.getType() == Fluids.WATER && state.hasProperty(BlockStateProperties.WATERLOGGED) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
			if (state.getValue(BlockStateProperties.SLAB_TYPE) != SlabType.DOUBLE) {
				if (!level.isClientSide()) {
					level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), 3);
					level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
				}
				cir.setReturnValue(true);
				return;
			}
		}
	}

	@Inject(method = "getFluidState", at = @At("RETURN"), cancellable = true)
	private void fixFluidGetting(BlockState state, CallbackInfoReturnable<FluidState> cir) {
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
			cir.setReturnValue(Fluids.LAVA.getSource(true));
			return;
		}
		if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
			cir.setReturnValue(Fluids.WATER.getSource(true));
			return;
		}
	}

}
