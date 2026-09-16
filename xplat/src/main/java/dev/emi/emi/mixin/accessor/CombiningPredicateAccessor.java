package dev.emi.emi.mixin.accessor;

import java.util.List;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.CombiningPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CombiningPredicate.class)
public interface CombiningPredicateAccessor {

	@Accessor("predicates")
	List<BlockPredicate> emi$getPredicates();
}
