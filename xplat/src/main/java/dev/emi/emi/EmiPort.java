package dev.emi.emi;

import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.mixin.accessor.SmithingTransformRecipeAccessor;
import dev.emi.emi.platform.EmiAgnos;
import dev.emi.emi.registry.EmiRecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConditionalValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.NumberDispatcher;
import net.minecraft.world.level.storage.loot.providers.number.ints.Quotient;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.level.storage.loot.providers.number.ints.WeightedListValue;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.TallFlowerBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.Collection;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class EmiPort {
	private static final net.minecraft.util.RandomSource RANDOM = net.minecraft.util.RandomSource.create();

	public static MutableComponent literal(String s) {
		return Component.literal(s);
	}

	public static MutableComponent literal(String s, ChatFormatting formatting) {
		return Component.literal(s).withStyle(formatting);
	}

	public static MutableComponent literal(String s, ChatFormatting... formatting) {
		return Component.literal(s).withStyle(formatting);
	}

	public static MutableComponent literal(String s, Style style) {
		return Component.literal(s).setStyle(style);
	}
	
	public static MutableComponent translatable(String s) {
		return Component.translatable(s);
	}
	
	public static MutableComponent translatable(String s, ChatFormatting formatting) {
		return Component.translatable(s).withStyle(formatting);
	}
	
	public static MutableComponent translatable(String s, Object... objects) {
		return Component.translatable(s, objects);
	}

	public static MutableComponent append(MutableComponent text, Component appended) {
		return text.append(appended);
	}

	public static FormattedCharSequence ordered(Component text) {
		return text.getVisualOrderText();
	}

	public static Collection<Identifier> findResources(ResourceManager manager, String prefix, Predicate<String> pred) {
		return manager.listResources(prefix, i -> pred.test(i.toString())).keySet();
	}

	public static InputStream getInputStream(Resource resource) {
		try {
			return resource.open();
		} catch (Exception e) {
			return null;
		}
	}

	public static BannerPatternLayers addRandomBanner(BannerPatternLayers patterns, Random random) {
		return EmiPortClient.addRandomBanner(patterns, random);
	}

	public static boolean canTallFlowerDuplicate(TallFlowerBlock tallFlowerBlock) {
		try {
			return tallFlowerBlock.isValidBonemealTarget(null, null, null, BonemealSource.INTERACTION)
				&& tallFlowerBlock.isBonemealSuccess(null, null, null, null, BonemealSource.INTERACTION);
		} catch(Exception e) {
			return false;
		}
	}

	public static int getGuiScale(Minecraft client) {
		return (int) client.getWindow().getGuiScale();
	}

	public static Registry<Item> getItemRegistry() {
		return BuiltInRegistries.ITEM;
	}

	public static Registry<Block> getBlockRegistry() {
		return BuiltInRegistries.BLOCK;
	}

	public static Registry<Fluid> getFluidRegistry() {
		return BuiltInRegistries.FLUID;
	}

	public static Registry<Potion> getPotionRegistry() {
		return BuiltInRegistries.POTION;
	}

	/**
	 * Data driven values such as composting chances and fuel burn times are defined by number
	 * providers that may branch on the state of the block they are used in. This resolves the
	 * expected value of such a provider, following the branch that applies in normal usage.
	 */
	public static float getExpectedValue(ResolvableInt value) {
		if (value instanceof ResolvableInt.Constant constant) {
			return constant.value();
		} else if (value instanceof ResolvableInt.Reference reference && Minecraft.getInstance().level != null) {
			return Minecraft.getInstance().level.registryAccess().lookup(Registries.CONTEXT_INT_PROVIDER)
				.flatMap(registry -> registry.get(reference.key()))
				.map(holder -> getExpectedValue(holder.value())).orElse(0f);
		}
		return 0;
	}

	public static float getExpectedValue(ContextIntProvider provider) {
		if (provider instanceof ConstantValue constant) {
			return constant.value();
		} else if (provider instanceof WeightedListValue weighted) {
			float total = 0, sum = 0;
			for (Weighted<Holder<ContextIntProvider>> entry : weighted.distribution().unwrap()) {
				total += entry.weight();
				sum += entry.weight() * getExpectedValue(entry.value().value());
			}
			return total == 0 ? 0 : sum / total;
		} else if (provider instanceof NumberDispatcher dispatcher) {
			return getExpectedValue(dispatcher.defaultValue().value());
		} else if (provider instanceof Quotient quotient) {
			float divisor = getExpectedValue(quotient.right().value());
			return divisor == 0 ? 0 : getExpectedValue(quotient.left().value()) / divisor;
		} else if (provider instanceof ConditionalValue conditional) {
			return getExpectedValue(conditional.onFalse().value());
		}
		return 0;
	}

	public static Registry<Enchantment> getEnchantmentRegistry() {
		return EmiPortClient.getEnchantmentRegistry();
	}

	public static Button newButton(int x, int y, int w, int h, Component name, OnPress action) {
		return Button.builder(name, action).pos(x, y).size(w, h).build();
	}

	public static ItemStack getOutput(Recipe<?> recipe) {
		return EmiPortClient.getOutput(recipe);
	}

	public static void focus(EditBox widget, boolean focused) {
		EmiPortClient.focus(widget, focused);
	}

	public static Stream<Item> getDisabledItems() {
		return EmiPortClient.getDisabledItems();
	}

	public static Identifier getId(Recipe<?> recipe) {
		return EmiRecipes.recipeIds.get(recipe);
	}

	public static @Nullable RecipeHolder<?> getRecipe(Identifier id) {
		if (id == null) {
			return null;
		}
		Collection<RecipeHolder<?>> recipes = getRecipes();
		if (recipes != null) {
			for (RecipeHolder<?> holder : recipes) {
				if (holder.id().identifier().equals(id)) {
					return holder;
				}
			}
		}
		return null;
	}

	public static @Nullable Collection<RecipeHolder<?>> getRecipes() {
		return EmiAgnos.getRecipes();
	}

	public static Comparison compareStrict() {
		return Comparison.compareComponents();
	}

	public static ItemStack setPotion(ItemStack stack, Potion potion) {
		stack.update(DataComponents.POTION_CONTENTS, PotionContents.EMPTY, getPotionRegistry().wrapAsHolder(potion), PotionContents::withPotion);
		return stack;
	}

	public static DataComponentPatch emptyExtraData() {
		return DataComponentPatch.EMPTY;
	}

	public static Identifier id(String id) {
		return Identifier.parse(id);
	}

	public static Identifier id(String namespace, String path) {
		return Identifier.fromNamespaceAndPath(namespace, path);
	}

}
