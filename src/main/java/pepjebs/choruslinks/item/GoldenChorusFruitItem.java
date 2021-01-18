package pepjebs.choruslinks.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import pepjebs.choruslinks.ChorusLinksMod;
import pepjebs.choruslinks.utils.ChorusLinksUtils;

import java.util.List;

public class GoldenChorusFruitItem extends Item {

    private final int radiusMultiplier;

    public GoldenChorusFruitItem(Settings settings, int radiusMultiplier1) {
        super(settings);
        this.radiusMultiplier = radiusMultiplier1;
    }

    @Override
    public boolean isFood() {
        return true;
    }

    @Nullable
    @Override
    public FoodComponent getFoodComponent() {
        return (new FoodComponent.Builder()).hunger(4).saturationModifier(1.2F).alwaysEdible().build();
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        ItemStack is = super.finishUsing(stack, world, user);
        if (world.isClient) return is;
        ChorusLinksMod.LOGGER.info("Consumed Golden Chorus Fruit...");
        BlockPos targetChorusLink = ChorusLinksUtils.doChorusLinkSearch(stack, world, user);
        if (targetChorusLink != null) {
            user.teleport(targetChorusLink.getX() + 0.5,
                    targetChorusLink.getY() + 1,
                    targetChorusLink.getZ() + 0.5);
        }
        return is;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.set(0, new LiteralText(tooltip.get(0).getString()).formatted(Formatting.AQUA));
        if (hasGlint(stack)) {
            tooltip.set(0, new LiteralText(tooltip.get(0).getString()).formatted(Formatting.LIGHT_PURPLE));
        }
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.getItem().getTranslationKey().contains("enchanted");
    }

    public int getRadiusMultiplier() { return radiusMultiplier; }
}