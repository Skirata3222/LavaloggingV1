package net.skirata3222.lavalogging.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidFillable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import net.skirata3222.lavalogging.util.Lavaloggable;

@Mixin(BucketItem.class)
public abstract class BucketItemMixin {

	@Shadow
	protected abstract void playEmptyingSound(@Nullable PlayerEntity player, WorldAccess world, BlockPos pos);
	

	@Inject(method = "use", at = @At("HEAD"), cancellable = true)
	private void injectLavaUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		ItemStack itemStack = user.getStackInHand(hand);

		if (itemStack.getItem() == Items.LAVA_BUCKET) {
			BlockHitResult hit = ItemInvoker.callRaycast(world, user, RaycastContext.FluidHandling.NONE);
			if (hit.getType() == HitResult.Type.BLOCK) {
				BlockPos pos = hit.getBlockPos();
				BlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				BlockPos adjacent = pos.offset(hit.getSide());
				BlockState adjState = world.getBlockState(adjacent);
				Block adjBlock = adjState.getBlock();
				if ((block instanceof FluidFillable block2) && block2.canFillWithFluid(user, world, pos, state, Fluids.LAVA)) {
					if (block2.tryFillWithFluid(world, pos, state, Fluids.LAVA.getDefaultState())) {
						this.playEmptyingSound(user,world,pos);
						((BucketItem)(Object)this).onEmptied(user, world, itemStack, pos);
						if (user instanceof ServerPlayerEntity) {
							Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, pos, itemStack);
						}
						user.incrementStat(Stats.USED.getOrCreateStat((BucketItem)(Object)this));
						ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, BucketItem.getEmptiedStack(itemStack,user));
						cir.setReturnValue(ActionResult.SUCCESS.withNewHandStack(itemStack2));
						return;
					}
				} else if ((adjBlock instanceof FluidFillable block3) && block3.canFillWithFluid(user, world, adjacent, adjState, Fluids.LAVA)){
					// if the block clicked isn't fluidfillable, check if the adjacent block is
					if (block3.tryFillWithFluid(world, adjacent, adjState, Fluids.LAVA.getDefaultState())) {
						this.playEmptyingSound(user,world,adjacent);
						((BucketItem)(Object)this).onEmptied(user, world, itemStack, adjacent);
						if (user instanceof ServerPlayerEntity) {
							Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)user, adjacent, itemStack);
						}
						user.incrementStat(Stats.USED.getOrCreateStat((BucketItem)(Object)this));
						ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, user, BucketItem.getEmptiedStack(itemStack,user));
						cir.setReturnValue(ActionResult.SUCCESS.withNewHandStack(itemStack2));
						return;
					}
				}
			}
		}
		if (itemStack.getItem() == Items.WATER_BUCKET) {
			BlockHitResult hit = ItemInvoker.callRaycast(world, user, RaycastContext.FluidHandling.NONE);
			if (hit.getType() == HitResult.Type.BLOCK) {
				Boolean fail1 = false;
				Boolean fail2 = false;
				BlockPos pos = hit.getBlockPos();
				BlockState state = world.getBlockState(pos);
				BlockPos adjacent = pos.offset(hit.getSide());
				BlockState adjState = world.getBlockState(adjacent);
				if (state.contains(Lavaloggable.LAVALOGGED) && state.get(Lavaloggable.LAVALOGGED)) fail1 = true;
				if (adjState.contains(Lavaloggable.LAVALOGGED)  && adjState.get(Lavaloggable.LAVALOGGED)) fail2=true;
				if (fail1 && fail2) {
					// if both the clicked block AND the block next to it where the water would otherwise go are lavalogged, just don't place the water
					cir.setReturnValue(ActionResult.FAIL);
					return;
				}
			}
		}
	}

}
