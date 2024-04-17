package net.joseph.vaultfilters.attributes.abstracts;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.attribute.ability.AbilityLevelAttribute;
import iskallia.vault.gear.attribute.custom.EffectAvoidanceGearAttribute;
import iskallia.vault.gear.attribute.custom.EffectCloudAttribute;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.gear.item.VaultGearItem;
import iskallia.vault.gear.reader.VaultGearModifierReader;
import net.joseph.vaultfilters.DataFixerParsers;
import net.joseph.vaultfilters.mixin.EffectCloudAccessor;
import net.joseph.vaultfilters.mixin.EffectCloudAttributeAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.function.TriFunction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class NumberAffixAttribute extends AffixAttribute {
    private static final Map<Class<?>, TriFunction<String, String, Number, ItemAttribute>> factories = new HashMap<>();
    protected final String name;
    protected final Number level;

    protected NumberAffixAttribute(String value, String name, Number level) {
        super(value);
        this.name = name;
        this.level = level;
    }

    public void register(TriFunction<String, String, Number, ItemAttribute> factory) {
        factories.put(getClass(), factory);
        super.register();
    }

    public ItemAttribute withValue(String displayName, String name, Number level) {
        return factories.getOrDefault(getClass(), (o1, o2, o3) -> null).apply(displayName, name, level);
    }

    @Override
    public boolean hasModifier(VaultGearModifier.AffixType type, ItemStack itemStack) {
        if (itemStack.getItem() instanceof VaultGearItem) {
            for (VaultGearModifier<?> modifier : VaultGearData.read(itemStack).getModifiers(type)) {
                Number level = getLevel(modifier);
                return this.level.getClass().isInstance(level) && level.floatValue() >= this.level.floatValue() && this.name.equals(getName(modifier));
            }
        }
        return false;
    }

    public static <T> Number getLevel(VaultGearModifier<T> modifier) {
        T value = modifier.getValue();
        if (value instanceof EffectCloudAttribute cloudAttribute) {
            EffectCloudAttribute.EffectCloud cloud = ((EffectCloudAttributeAccessor) cloudAttribute).getEffectCloud();
            String tooltip = ((EffectCloudAccessor) cloud).getTooltip();
            int index = tooltip.lastIndexOf(' ');
            if (index == -1) {
                return 1;
            }

            return switch (tooltip.substring(index + 1)) {
                case "II" -> 2;
                case "III" -> 3;
                case "IV" -> 4;
                case "V" -> 5;
                default -> 1;
            };
        } else if (value instanceof AbilityLevelAttribute levelAttribute) {
            return levelAttribute.getLevelChange();
        } else if (value instanceof EffectAvoidanceGearAttribute avoidanceAttribute) {
            return avoidanceAttribute.getChance();
        } else if (value instanceof Number number) {
            return number;
        }

        return null;
    }

    public static <T> String getDisplayName(VaultGearModifier<T> modifier, VaultGearModifier.AffixType type) {
        VaultGearModifierReader<T> reader = modifier.getAttribute().getReader();
        MutableComponent displayName = reader.getDisplay(modifier, type);
        return displayName == null ? getName(modifier) : displayName.getString();
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        List<ItemAttribute> attributes = new ArrayList<>();
        for (VaultGearModifier.AffixType type : VaultGearModifier.AffixType.values()) {
            for (VaultGearModifier<?> modifier : getModifiers(itemStack, type)) {
                if (!shouldList(type, modifier)) {
                    continue;
                }

                Number level = getLevel(modifier);
                if (level == null) {
                    continue;
                }

                String name = getName(modifier);
                String displayName = getDisplayName(modifier, type);
                attributes.add(withValue(displayName, name, level));
            }
        }
        return attributes;
    }

    @Override
    public void writeNBT(CompoundTag compoundTag) {
        super.writeNBT(compoundTag);
        String simpleKey = getTranslationKey() + "_simple";
        String levelKey = getTranslationKey() + "_level";
        if (this.level instanceof Float f) {
            compoundTag.putFloat(levelKey, f);
        } else if (this.level instanceof Double d) {
            compoundTag.putDouble(levelKey, d);
        } else if (this.level instanceof Integer i) {
            compoundTag.putInt(levelKey, i);
        }
        compoundTag.putString(simpleKey, this.name);
    }

    @Override
    public ItemAttribute readNBT(CompoundTag compoundTag) {
        String key = getTranslationKey();
        String simpleKey = key + "_simple";
        String levelKey = key + "_level";
        // If it's legacy data
        if (compoundTag.contains(getLegacyKey(), CompoundTag.TAG_STRING) && !compoundTag.contains(key)) {
            String modifierName = compoundTag.getString(getLegacyKey());
            String name = DataFixerParsers.getNameFromString(modifierName);
            byte dataType = DataFixerParsers.getTypeFromName(name);
            if (dataType == CompoundTag.TAG_BYTE) {
                dataType = modifierName.contains("%") ? CompoundTag.TAG_FLOAT : CompoundTag.TAG_INT;
            }
            double doubleValue = DataFixerParsers.getDoubleValue(modifierName);
            compoundTag.remove(getLegacyKey());
            compoundTag.putString(key,modifierName);
            compoundTag.putString(simpleKey,name);
            if (dataType == CompoundTag.TAG_INT) {
                int val = (int) Math.floor(doubleValue);
                compoundTag.putInt(levelKey, val );
                return withValue(modifierName,name,val );
            }
            if (dataType == CompoundTag.TAG_FLOAT) {
                float val = (float)doubleValue;
                compoundTag.putFloat(levelKey, val);
                return withValue(modifierName,name,val );
            }
            if (dataType == CompoundTag.TAG_DOUBLE) {
                compoundTag.putDouble(levelKey, doubleValue);
                return withValue(modifierName,name, doubleValue );
            }
        }


        Number level = null;
        byte levelType = compoundTag.getTagType(levelKey);
        if (levelType == CompoundTag.TAG_FLOAT) {
            level = compoundTag.getFloat(levelKey);
        } else if (levelType == CompoundTag.TAG_DOUBLE) {
            level = compoundTag.getDouble(levelKey);
        } else if (levelType == CompoundTag.TAG_INT) {
            level = compoundTag.getInt(levelKey);
        }

        return withValue(compoundTag.getString(key), compoundTag.getString(simpleKey), level);
    }
}
