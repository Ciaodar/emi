package dev.emi.emi.platform;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.recipe.EmiBrewingRecipe;
import dev.emi.emi.registry.EmiPluginContainer;
import dev.emi.emi.runtime.EmiLog;

public abstract class EmiAgnos {
	public static EmiAgnos delegate;

	static {
		try {
			Class.forName("dev.emi.emi.platform.fabric.EmiAgnosFabric");
		} catch (Throwable t) {
		}
		try {
			Class.forName("dev.emi.emi.platform.forge.EmiAgnosForge");
		} catch (Throwable t) {
		}
		try {
			Class.forName("dev.emi.emi.platform.neoforge.EmiAgnosNeoForge");
		} catch (Throwable t) {
		}
	}

	public static boolean isForge() {
		return delegate.isForgeAgnos();
	}

	protected abstract boolean isForgeAgnos();

	public static String getModName(String namespace) {
		return delegate.getModNameAgnos(namespace);
	}

	protected abstract String getModNameAgnos(String namespace);

	public static Path getConfigDirectory() {
		return delegate.getConfigDirectoryAgnos();
	}

	protected abstract Path getConfigDirectoryAgnos();

	public static boolean isDevelopmentEnvironment() {
		return delegate.isDevelopmentEnvironmentAgnos();
	}

	protected abstract boolean isDevelopmentEnvironmentAgnos();

	public static boolean isModLoaded(String id) {
		return delegate.isModLoadedAgnos(id);
	}

	protected abstract boolean isModLoadedAgnos(String id);

	public static List<String> getAllModNames() {
		return delegate.getAllModNamesAgnos();
	}

	protected abstract List<String> getAllModNamesAgnos();

	public static List<String> getAllModAuthors() {
		return delegate.getAllModAuthorsAgnos();
	}

	protected abstract List<String> getAllModAuthorsAgnos();

	public static List<String> getModsWithPlugins() {
		return delegate.getModsWithPluginsAgnos();
	}

	protected abstract List<String> getModsWithPluginsAgnos();

	public static List<EmiPluginContainer> getPlugins() {
		return delegate.getPluginsAgnos();
	}

	protected abstract List<EmiPluginContainer> getPluginsAgnos();

	public static void addBrewingRecipes(EmiRegistry registry) {
		delegate.addBrewingRecipesAgnos(registry);
	}

	/**
	 * Brewing is a regular recipe type as of 26.3, so this is platform independent.
	 */
	protected void addBrewingRecipesAgnos(EmiRegistry registry) {
		for (RecipeHolder<BrewingRecipe> holder : getBrewingRecipes()) {
			BrewingRecipe recipe = holder.value();
			try {
				EmiIngredient reagent = EmiIngredient.of(recipe.getReagent().ingredient());
				if (reagent.isEmpty()) {
					continue;
				}
				EmiStack output = EmiStack.of(recipe.getOutput().create());
				List<EmiStack> inputs = getBrewingInputs(recipe.getInput());
				for (int i = 0; i < inputs.size(); i++) {
					Identifier id = holder.id().identifier();
					if (i > 0) {
						id = EmiPort.id(id.getNamespace(), id.getPath() + "_" + i);
					}
					registry.addRecipe(new EmiBrewingRecipe(inputs.get(i), reagent, output, id));
				}
			} catch (Exception e) {
				EmiLog.error("Error registering brewing recipe " + holder.id().identifier(), e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static List<RecipeHolder<BrewingRecipe>> getBrewingRecipes() {
		Collection<RecipeHolder<?>> recipes = getRecipes();
		if (recipes == null) {
			return List.of();
		}
		List<RecipeHolder<BrewingRecipe>> list = Lists.newArrayList();
		for (RecipeHolder<?> holder : recipes) {
			if (holder.value() instanceof BrewingRecipe) {
				list.add((RecipeHolder<BrewingRecipe>) holder);
			}
		}
		return list;
	}

	private static List<EmiStack> getBrewingInputs(PotionIngredient ingredient) {
		List<ItemStack> bases = ingredient.ingredient().items()
			.map(holder -> new ItemStack(holder.value())).toList();
		Optional<HolderSet<Potion>> potions = ingredient.potions().flatMap(PotionsPredicate::potions);
		if (potions.isEmpty()) {
			return bases.stream().map(EmiStack::of).toList();
		}
		List<EmiStack> stacks = Lists.newArrayList();
		for (Holder<Potion> potion : potions.get()) {
			for (ItemStack base : bases) {
				stacks.add(EmiStack.of(EmiPort.setPotion(base.copy(), potion.value())));
			}
		}
		return stacks;
	}

	public static List<ClientTooltipComponent> getItemTooltip(ItemStack stack) {
		return delegate.getItemTooltipAgnos(stack);
	}

	protected abstract List<ClientTooltipComponent> getItemTooltipAgnos(ItemStack stack);

	public static Component getFluidName(Fluid fluid, DataComponentPatch componentChanges) {
		return delegate.getFluidNameAgnos(fluid, componentChanges);
	}

	protected abstract Component getFluidNameAgnos(Fluid fluid, DataComponentPatch componentChanges);

	public static List<Component> getFluidTooltip(Fluid fluid, DataComponentPatch componentChanges) {
		return delegate.getFluidTooltipAgnos(fluid, componentChanges);
	}

	protected abstract List<Component> getFluidTooltipAgnos(Fluid fluid, DataComponentPatch componentChanges);

	public static boolean isFloatyFluid(FluidEmiStack stack) {
		return delegate.isFloatyFluidAgnos(stack);
	}

	protected abstract boolean isFloatyFluidAgnos(FluidEmiStack stack);

	public static void renderFluid(FluidEmiStack stack, GuiGraphicsExtractor draw, int x, int y, float delta) {
		renderFluid(stack, draw, x, y, delta, 0, 0, 16, 16);
	}

	public static void renderFluid(FluidEmiStack stack, GuiGraphicsExtractor draw, int x, int y, float delta, int xOff, int yOff, int width, int height) {
		delegate.renderFluidAgnos(stack, draw, x, y, delta, xOff, yOff, width, height);
	}

	protected abstract void renderFluidAgnos(FluidEmiStack stack, GuiGraphicsExtractor draw, int x, int y, float delta, int xOff, int yOff, int width, int height);

	public static EmiStack createFluidStack(Object object) {
		return delegate.createFluidStackAgnos(object);
	}

	protected abstract EmiStack createFluidStackAgnos(Object object);

	public static boolean canBatch(ItemStack stack) {
		return delegate.canBatchAgnos(stack);
	}
	
	protected abstract boolean canBatchAgnos(ItemStack stack);

	public static Map<Item, Integer> getFuelMap() {
		return delegate.getFuelMapAgnos();
	}

	/**
	 * Fuels are defined by the cooking fuel data component as of 26.3, so this is platform independent.
	 */
	protected Map<Item, Integer> getFuelMapAgnos() {
		Map<Item, Integer> fuelMap = Maps.newLinkedHashMap();
		for (Item item : EmiPort.getItemRegistry()) {
			CookingFuel fuel = item.getDefaultInstance().get(DataComponents.COOKING_FUEL);
			if (fuel != null) {
				int time = (int) EmiPort.getExpectedValue(fuel.burnTime());
				if (time > 0) {
					fuelMap.put(item, time);
				}
			}
		}
		return fuelMap;
	}

	public static boolean isEnchantable(ItemStack stack, Enchantment enchantment) {
		return delegate.isEnchantableAgnos(stack, enchantment);
	}
	
	protected abstract boolean isEnchantableAgnos(ItemStack stack, Enchantment enchantment);

	public static @Nullable Collection<RecipeHolder<?>> getRecipes() {
		return delegate.getRecipesAgnos();
	}

	protected @Nullable Collection<RecipeHolder<?>> getRecipesAgnos() { return null; }
}
