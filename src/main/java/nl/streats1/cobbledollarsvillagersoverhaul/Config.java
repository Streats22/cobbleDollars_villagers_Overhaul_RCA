package nl.streats1.cobbledollarsvillagersoverhaul;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue COBBLEDOLLARS_EMERALD_RATE = BUILDER
            .comment("CobbleDollars per 1 emerald when paying villagers. Default 500 matches CobbleDollars bank. Ignored if syncCobbleDollarsBankRate is true and their bank.json is found.")
            .defineInRange("cobbledollarsEmeraldRate", 500, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue SYNC_COBBLEDOLLARS_BANK_RATE = BUILDER
            .comment("If true, use the emerald price from CobbleDollars' config/cobbledollars/bank.json so villager rate matches their bank (1 emerald = X). Falls back to cobbledollarsEmeraldRate if file not found.")
            .define("syncCobbleDollarsBankRate", true);

    public static final ModConfigSpec.BooleanValue VILLAGERS_ACCEPT_COBBLEDOLLARS = BUILDER
            .comment("If true, villager trades that cost emeralds can be paid with CobbleDollars balance instead.")
            .define("villagersAcceptCobbleDollars", true);

    public static final ModConfigSpec.BooleanValue USE_COBBLEDOLLARS_SHOP_UI = BUILDER
            .comment("Use CobbleDollars-style shop UI when trading with villagers.")
            .define("useCobbleDollarsShopUi", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
