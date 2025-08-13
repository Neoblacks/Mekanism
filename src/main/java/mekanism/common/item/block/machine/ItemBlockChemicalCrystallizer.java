package mekanism.common.item.block.machine;

import java.util.List;
import mekanism.api.Upgrade;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import mekanism.common.attachments.component.AttachedEjector;
import mekanism.common.attachments.component.AttachedSideConfig;
import mekanism.common.attachments.component.UpgradeAware;
import mekanism.common.block.prefab.BlockTileModel;
import mekanism.common.config.MekanismConfig;
import mekanism.common.item.block.ItemBlockTooltip;
import mekanism.common.registries.MekanismDataComponents;
import mekanism.common.tile.machine.TileEntityChemicalCrystallizer;
import mekanism.common.util.text.TextUtils;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

public class ItemBlockChemicalCrystallizer extends ItemBlockTooltip<BlockTileModel<TileEntityChemicalCrystallizer, ?>> {

    public ItemBlockChemicalCrystallizer(BlockTileModel<TileEntityChemicalCrystallizer, ?> block, Properties properties) {
        super(block, true, properties
              .component(MekanismDataComponents.EJECTOR, AttachedEjector.DEFAULT)
              .component(MekanismDataComponents.SIDE_CONFIG, AttachedSideConfig.CRYSTALLIZER)
        );
    }

    @Override
    protected void addTypeDetails(@NotNull ItemStack stack, @NotNull Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.addTypeDetails(stack, context, tooltip, flag);
        
        // Calculate production rate based on base speed and upgrades
        // Base ticks required for Chemical Crystallizer
        int baseTicks = TileEntityChemicalCrystallizer.BASE_TICKS_REQUIRED;
        
        // Try to get upgrades from the item stack to calculate actual rate
        UpgradeAware upgradeAware = stack.get(MekanismDataComponents.UPGRADES);
        int speedUpgrades = upgradeAware != null ? upgradeAware.getUpgradeCount(Upgrade.SPEED) : 0;
        
        // Calculate the actual ticks with speed upgrades using similar logic to MekanismUtils
        double actualTicks = baseTicks;
        if (speedUpgrades > 0) {
            // Use the same formula as MekanismUtils.getTicksD but without requiring a tile
            double speedMultiplier = Math.pow(MekanismConfig.general.maxUpgradeMultiplier.get(), 
                                              -((double) speedUpgrades / Upgrade.SPEED.getMax()));
            actualTicks = baseTicks * speedMultiplier;
        }
        
        // Calculate items per second
        double itemsPerSecond;
        if (actualTicks >= 1) {
            itemsPerSecond = SharedConstants.TICKS_PER_SECOND / actualTicks;
        } else {
            // If very fast (less than 1 tick), calculate operations per tick
            itemsPerSecond = SharedConstants.TICKS_PER_SECOND / Math.max(1, actualTicks);
        }
        
        // Format as items per second
        String rateText = TextUtils.format(itemsPerSecond) + "/s";
        tooltip.add(MekanismLang.PRODUCTION_RATE.translateColored(EnumColor.INDIGO, EnumColor.GRAY, rateText));
        
        // Only show upgrade note if no upgrades are installed
        if (speedUpgrades == 0) {
            tooltip.add(MekanismLang.PRODUCTION_RATE_UPGRADES.translateColored(EnumColor.DARK_GRAY, EnumColor.GRAY));
        }
    }
}