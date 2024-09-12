package net.joseph.vaultfilters.attributes.catalysts;

import net.joseph.vaultfilters.attributes.abstracts.CatalystModifierAttribute;

public class CatalystHasModifierAttribute extends CatalystModifierAttribute {
    public CatalystHasModifierAttribute(String value) {
        super(value);
    }

    @Override
    public String getTranslationKey() {
        return "catalyst_modifier";
    }
}
