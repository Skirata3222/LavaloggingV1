package net.skirata3222.lavalogging.mixin;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import net.skirata3222.lavalogging.util.LavalogConfigLoader;
import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(AnvilBlock.class)
public abstract class AnvilBlockMixin extends FallingBlock implements LiquidBlockContainer, BucketPickup {

	public AnvilBlockMixin(Properties properties) {
		super(properties);
		//TODO Auto-generated constructor stub
	}

	@Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
	private void addLavaloggedProperty(StateDefinition.Builder<Block,BlockState> builder, CallbackInfo ci) {
		builder.add(BlockStateProperties.WATERLOGGED, LavalogPropUtil.LAVALOGGED);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void injectDefaultState(BlockBehaviour.Properties properties, CallbackInfo ci) {
		Block self = (Block)(Object)this;
		((BlockInvoker)self).invokeRegisterDefaultState(
			self.defaultBlockState()
			.setValue(LavalogPropUtil.LAVALOGGED, false)
			.setValue(BlockStateProperties.WATERLOGGED, false)
		);
	}

	@Inject(method = "getStateForPlacement", at = @At("TAIL"), cancellable = true)
	private void injectLavaPlacement(BlockPlaceContext ctx, CallbackInfoReturnable<BlockState> cir) {
		BlockState state = cir.getReturnValue();
		FluidState fluidState =  ctx.getLevel().getFluidState(ctx.getClickedPos());
		if (fluidState.getType() == Fluids.LAVA && state.hasProperty(LavalogPropUtil.LAVALOGGED) && LavalogConfigLoader.BLOCKLIST.contains(state.getBlock())) {
			cir.setReturnValue(state.trySetValue(LavalogPropUtil.LAVALOGGED,true));
		}
		if (fluidState.getType() == Fluids.WATER) {
			cir.setReturnValue(state.trySetValue(BlockStateProperties.WATERLOGGED, true));
		}
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader reader, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
			tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(reader));
		}
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
			tickAccess.scheduleTick(pos, Fluids.LAVA, Fluids.LAVA.getTickDelay(reader));
		}

		// Call the inherited implementation
		return super.updateShape(state, reader, tickAccess, pos, direction, neighborPos, neighborState, random);
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

	public FluidState getFluidState(BlockState state) {
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED) && state.getValue(LavalogPropUtil.LAVALOGGED)) {
			return Fluids.LAVA.getSource(true);
		}
		if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
			return Fluids.WATER.getSource(true);
		}
		return Fluids.EMPTY.defaultFluidState();
	}

	public ItemStack pickupBlock(@Nullable LivingEntity player, LevelAccessor level, BlockPos pos, BlockState state) {
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)
				&& state.getValue(BlockStateProperties.WATERLOGGED)) {

			level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), 3);
			return new ItemStack(Items.WATER_BUCKET);
		}
		if (state.hasProperty(LavalogPropUtil.LAVALOGGED)
				&& state.getValue(LavalogPropUtil.LAVALOGGED)) {

			level.setBlock(pos, state.setValue(LavalogPropUtil.LAVALOGGED, false), 3);
			return new ItemStack(Items.LAVA_BUCKET);
		}
		return ItemStack.EMPTY;
	}

	public Optional<SoundEvent> getPickupSound() {
		return Optional.of(SoundEvents.BUCKET_FILL);
	}


}
