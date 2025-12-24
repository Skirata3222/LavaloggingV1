package net.skirata3222.lavalogging.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.LevelAccessor;

@Mixin(BucketItem.class)
public interface BucketItemInvoker {
	
	@Invoker("playEmptySound")
	void invokePlayEmptySound(@Nullable LivingEntity user, LevelAccessor level, BlockPos pos);

}
