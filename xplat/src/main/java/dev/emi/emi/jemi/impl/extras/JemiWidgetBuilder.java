package dev.emi.emi.jemi.impl.extras;

import java.util.Collection;

import dev.emi.emi.api.widget.WidgetHolder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IDrawableWidget;
import mezz.jei.api.gui.widgets.IRecipeWidgetTooltipCallback;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class JemiWidgetBuilder implements IDrawableWidget {
	public int x = 0, y = 0;
	public int width, height;
	public WidgetConstructor constructor;

	public JemiWidgetBuilder(int width, int height, WidgetConstructor constructor) {
		this.width = width;
		this.height = height;
		this.constructor = constructor;
	}

	public void addWidgets(WidgetHolder holder) {
		constructor.accept(this, holder);
	}

	@Override
	public IDrawableWidget setPosition(int xPos, int yPos) {
		this.x = xPos;
		this.y = yPos;
		return this;
	}

	@Override
	public IDrawableWidget setPosition(int areaX, int areaY, int areaWidth, int areaHeight,
			HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		return setPosition(areaX + horizontalAlignment.getXPos(areaWidth, width),
			areaY + verticalAlignment.getYPos(areaHeight, height));
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public IDrawableWidget setTooltip(FormattedText tooltip) {
		// Tooltips are not implemented for jemi widgets
		return this;
	}

	@Override
	public IDrawableWidget setTooltip(Collection<? extends FormattedText> tooltip) {
		// Tooltips are not implemented for jemi widgets
		return this;
	}

	@Override
	public IDrawableWidget setTooltip(TooltipComponent tooltip) {
		// Tooltips are not implemented for jemi widgets
		return this;
	}

	@Override
	public IDrawableWidget setTooltip(IRecipeWidgetTooltipCallback tooltipCallback) {
		// Tooltips are not implemented for jemi widgets
		return this;
	}

	public static interface WidgetConstructor {

		void accept(JemiWidgetBuilder builder, WidgetHolder holder);
	}
}
