package net.joseph.vaultfilters.attributes.abstracts;

import com.simibubi.create.content.logistics.filter.ItemAttribute;
import net.minecraft.nbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class BooleanAttribute extends VaultAttribute<Boolean> {
    private static final Map<Class<?>, Function<Boolean, ItemAttribute>> factories = new HashMap<>();

    protected BooleanAttribute(Boolean value) {
        super(value);
    }

    public void register(Function<Boolean, ItemAttribute> factory) {
        factories.put(getClass(), factory);
        super.register();

    }

    @Override
    public ItemAttribute withValue(Boolean value) {
        return factories.getOrDefault(getClass(), ignored -> null).apply(value);
    }

    @Override
    public void writeNBT(CompoundTag compoundTag) {
        compoundTag.putString(getSubNBTKey(), String.valueOf(this.value));
    }

    @Override
    public ItemAttribute readNBT(CompoundTag compoundTag) {
        return withValue(Boolean.parseBoolean(compoundTag.getString(getSubNBTKey())));
    }
}
