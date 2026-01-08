package net.skirata3222.lavalogging.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import net.skirata3222.lavalogging.util.LavalogPropUtil;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

	@Shadow
	protected abstract void playEmptySound(@Nullable LivingEntity player, LevelAccessor level, BlockPos pos);

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void injectLavaUse(Level level, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		BucketItem self = (BucketItem)(Object)this;
		Fluid fluid = self.getContent();
		BlockHitResult hit = ItemRaycastInvoker.invokeRaycast(level, user, fluid == Fluids.EMPTY ? ClipContext.Fluid.SOURCE_ONLY : ClipContext.Fluid.NONE);
		if (hit.getType() != HitResult.Type.BLOCK) {
			return;
		}
		BlockPos pos = hit.getBlockPos();
		// BlockState state = level.getBlockState(pos);
		// Block block = state.getBlock();
		BlockPos adjPos = pos.relative(hit.getDirection());
		BlockState adjState = level.getBlockState(adjPos);
		Block adjBlock = adjState.getBlock();
		BlockPos targetPos = user.isShiftKeyDown() ? adjPos : pos;
		BlockState targetState = level.getBlockState(targetPos);
		Block targetBlock = targetState.getBlock();

		
		
		if (fluid == Fluids.LAVA && targetState.hasProperty(LavalogPropUtil.LAVALOGGED)) {
			if (targetState.getValue(LavalogPropUtil.LAVALOGGED)) {
				cir.setReturnValue(InteractionResult.FAIL);
				return;
			}

			if (((LiquidBlockContainer)targetBlock).canPlaceLiquid(user, level, targetPos, targetState, fluid)) {
				if (!level.isClientSide()) {
					level.setBlock(targetPos, targetState.setValue(LavalogPropUtil.LAVALOGGED, true), 3);
					level.scheduleTick(targetPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
				}
				if (!user.getAbilities().instabuild) {
					user.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				
				((BucketItemInvoker)(Object)this).invokePlayEmptySound(user, level, targetPos);

				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
		}

		if (fluid == Fluids.LAVA && adjState.hasProperty(LavalogPropUtil.LAVALOGGED)) {
			// if we're targetting a block and its not lavaloggable (or already lavalogged) but the one next to it is, attempt to lavalog the one next to it
			if (adjState.getValue(LavalogPropUtil.LAVALOGGED)) {
				cir.setReturnValue(InteractionResult.FAIL);
				return;
			}

			if (((LiquidBlockContainer)adjBlock).canPlaceLiquid(user, level, adjPos, adjState, fluid)) {
				if (!level.isClientSide()) {
					level.setBlock(adjPos, adjState.setValue(LavalogPropUtil.LAVALOGGED, true), 3);
					level.scheduleTick(adjPos, Fluids.LAVA, Fluids.LAVA.getTickDelay(level));
				}
				if (!user.getAbilities().instabuild) {
					user.setItemInHand(hand, new ItemStack(Items.BUCKET));
				}
				
				((BucketItemInvoker)(Object)this).invokePlayEmptySound(user, level, adjPos);

				cir.setReturnValue(InteractionResult.SUCCESS);
				return;
			}
		}

		if (fluid == Fluids.WATER
				&& targetState.hasProperty(LavalogPropUtil.LAVALOGGED)
				&& targetState.getValue(LavalogPropUtil.LAVALOGGED)) {
			cir.setReturnValue(InteractionResult.FAIL);
			return;
		}

		if (self.getContent() == Fluids.EMPTY
		&& targetState.hasProperty(LavalogPropUtil.LAVALOGGED)
		&& targetState.getValue(LavalogPropUtil.LAVALOGGED)) {

			if (!level.isClientSide()) {
				level.setBlock(targetPos, targetState.setValue(LavalogPropUtil.LAVALOGGED, false), 3);
				if (!user.getAbilities().instabuild) {
					user.setItemInHand(hand, new ItemStack(Items.LAVA_BUCKET));
				}
			}
			level.playSound(user,targetPos,SoundEvents.BUCKET_FILL_LAVA,SoundSource.BLOCKS,1.0F,1.0F);
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

	}
	
}
