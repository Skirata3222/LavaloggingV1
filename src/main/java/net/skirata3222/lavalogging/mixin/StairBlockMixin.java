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
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogConfigLoader;
import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(StairBlock.class)
public abstract class StairBlockMixin implements LiquidBlockContainer, BucketPickup {
	
	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void addLavaloggedProperty(StateDefinition.Builder<Block,BlockState> builder, CallbackInfo ci) {
		builder.add(LavalogPropUtil.LAVALOGGED);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectDefaultState(BlockState baseState, BlockBehaviour.Properties properties, CallbackInfo ci) {
		StairBlock self = (StairBlock)(Object)this;
		((BlockInvoker)self).invokeRegisterDefaultState(
			self.defaultBlockState()
			.setValue(StairBlock.FACING, Direction.NORTH)
			.setValue(StairBlock.HALF, Half.BOTTOM)
			.setValue(StairBlock.SHAPE, StairsShape.STRAIGHT)
			.setValue(StairBlock.WATERLOGGED, false)
			.setValue(LavalogPropUtil.LAVALOGGED, false)

		);
	}

	@Inject(method = "getStateForPlacement", at = @At("TAIL"), cancellable = true)
	private void injectLavaPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = cir.getReturnValue();
		FluidState fluidState = ctx.getLevel().getFluidState(ctx.getClickedPos());
		if (fluidState.getType() == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock())) {
			cir.setReturnValue(state.trySetValue(LavalogPropUtil.LAVALOGGED, true));
			return;
		} else {
			cir.setReturnValue(state.trySetValue(LavalogPropUtil.LAVALOGGED,false));
			return;
		}
	}

	@Inject(method = "updateShape", at = @At("RETURN"))
	private void lavalogNeighbor(BlockState state, LevelReader reader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random, CallbackInfoReturnable<BlockState> cir) {
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED) && (Boolean)state.getValue(LavalogPropUtil.LAVALOGGED)) {
			tickAccess.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(reader));
		}
	}

	@Override
	public boolean canPlaceLiquid(@Nullable LivingEntity player, BlockGetter getter, BlockPos pos, BlockState state, Fluid fluid) {
		if (fluid == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock()) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
			return true;
		}
		if (fluid == Fluids.WATER && state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
			return false;
		}
		return fluid == Fluids.WATER;
	}

	@Override
	public boolean placeLiquid(LevelAccessor world, BlockPos pos, BlockState state, FluidState fluidState) {
		if (fluidState.getType() == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock()) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
			if (!world.isClientSide()) {
				world.setBlock(pos, state.setValue(LavalogPropUtil.LAVALOGGED, true), 3);
				world.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(world));
			}
			return true;
		}
		if (fluidState.getType() == Fluids.WATER && state.hasProperty(LavalogPropUtil.LAVALOGGED) && !state.getValue(LavalogPropUtil.LAVALOGGED) && !state.getValue(BlockStateProperties.WATERLOGGED)) {
			if (!world.isClientSide()) {
				world.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, true), 3);
				world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
			}
			return true;
		}
		return false;
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
