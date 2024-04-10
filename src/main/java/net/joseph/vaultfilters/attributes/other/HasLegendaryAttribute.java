package net.joseph.vaultfilters.attributes.other;

import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import net.joseph.vaultfilters.attributes.abstracts.BooleanAttribute;
import net.minecraft.world.item.ItemStack;

public class HasLegendaryAttribute extends BooleanAttribute {
    public HasLegendaryAttribute(Boolean value) {
        super(true);
    }

    @Override
    public Boolean getValue(ItemStack itemStack) {
        if (!(itemStack.getItem() instanceof VaultGearItem)) {
            return null;
        }

        VaultGearData data = VaultGearData.read(itemStack);
        for (VaultGearModifier<?> modifier : data.getAllModifierAffixes()) {
            if (modifier.getCategory() == VaultGearModifier.AffixCategory.LEGENDARY) {
                return true;
            }
        }

        return null;
    }

    @Override
    public String getTranslationKey() {
        return "has_legendary";
    }

    @Override
    public String getSubNBTKey() {
        return "legendary";
    }
}