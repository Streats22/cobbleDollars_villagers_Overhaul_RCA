package nl.streats1.cobbledollarsvillagersoverhaul.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.streats1.cobbledollarsvillagersoverhaul.Config;
import nl.streats1.cobbledollarsvillagersoverhaul.network.CobbleDollarsShopPayloads;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public final class CobbleDollarsConfigHelper {

    private static final String COBBLEDOLLARS_CONFIG_SUBDIR = "cobbledollars";
    private static final String BANK_FILE = "bank.json";
    private static final String DEFAULT_SHOP_FILE = "default_shop.json";
    private static final String EMERALD_ITEM = "minecraft:emerald";

    private static final String DEFAULT_SHOP_KEY = "defaultShop";
    private static final String MERCHANT_SHOP_KEY = "merchantShop";

    private static int cachedBankEmeraldPrice = -1;

    public static OptionalInt getBankEmeraldPrice() {
        if (cachedBankEmeraldPrice >= 0) return OptionalInt.of(cachedBankEmeraldPrice);
        if (cachedBankEmeraldPrice == -2) return OptionalInt.empty();
        Path configDir = getConfigDirectory();
        if (configDir == null) {
            return OptionalInt.empty();
        }
        Path bankFile = configDir.resolve(COBBLEDOLLARS_CONFIG_SUBDIR).resolve(BANK_FILE);
        if (!Files.isRegularFile(bankFile)) {
            cachedBankEmeraldPrice = -2;
            return OptionalInt.empty();
        }
        try {
            String content = Files.readString(bankFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            if (!root.has("bank")) {
                cachedBankEmeraldPrice = -2;
                return OptionalInt.empty();
            }
            JsonElement bankEl = root.get("bank");
            if (bankEl.isJsonArray()) {
                JsonArray bank = bankEl.getAsJsonArray();
                for (JsonElement entry : bank) {
                    if (!entry.isJsonObject()) continue;
                    JsonObject obj = entry.getAsJsonObject();
                    String item = obj.has("item") ? obj.get("item").getAsString() : null;
                    if (EMERALD_ITEM.equals(item) && obj.has("price")) {
                        int price = obj.get("price").getAsInt();
                        if (price > 0) {
                            cachedBankEmeraldPrice = price;
                            return OptionalInt.of(price);
                        }
                    }
                }
            }
            cachedBankEmeraldPrice = -2;
            return OptionalInt.empty();
        } catch (Exception e) {
            cachedBankEmeraldPrice = -2;
            return OptionalInt.empty();
        }
    }

    public static List<CobbleDollarsShopPayloads.ShopOfferEntry> getDefaultShopBuyOffers() {
        Path configDir = getConfigDirectory();
        if (configDir == null) return List.of();
        Path shopFile = configDir.resolve(COBBLEDOLLARS_CONFIG_SUBDIR).resolve(DEFAULT_SHOP_FILE);
        if (!Files.isRegularFile(shopFile)) return List.of();
        try {
            String content = Files.readString(shopFile);
            JsonObject root = JsonParser.parseString(content).getAsJsonObject();
            List<CobbleDollarsShopPayloads.ShopOfferEntry> out = new ArrayList<>();
            ResourceLocation airId = BuiltInRegistries.ITEM.getKey(Items.AIR);

            if (root.has(DEFAULT_SHOP_KEY)) {
                JsonElement arrEl = root.get(DEFAULT_SHOP_KEY);
                if (arrEl.isJsonArray()) {
                    JsonArray categories = arrEl.getAsJsonArray();
                    for (JsonElement catEl : categories) {
                        if (!catEl.isJsonObject()) continue;
                        JsonObject catObj = catEl.getAsJsonObject();
                        for (String catName : catObj.keySet()) {
                            JsonElement offersEl = catObj.get(catName);
                            if (!offersEl.isJsonArray()) continue;
                            for (JsonElement offerEl : offersEl.getAsJsonArray()) {
                                if (!offerEl.isJsonObject()) continue;
                                JsonObject o = offerEl.getAsJsonObject();
                                String itemId = o.has("item") ? o.get("item").getAsString() : null;
                                if (itemId == null || itemId.isEmpty()) continue;
                                int price = parsePrice(o.get("price"));
                                if (price <= 0) continue;
                                ResourceLocation id = ResourceLocation.tryParse(itemId);
                                if (id == null) continue;
                                out.add(new CobbleDollarsShopPayloads.ShopOfferEntry(id, 1, price, airId, 0, true));
                            }
                        }
                    }
                }
            }
            if (out.isEmpty() && root.has(MERCHANT_SHOP_KEY)) {
                JsonObject merchantShop = root.getAsJsonObject(MERCHANT_SHOP_KEY);
                for (String category : merchantShop.keySet()) {
                    JsonElement catEl = merchantShop.get(category);
                    if (!catEl.isJsonObject()) continue;
                    for (String itemId : catEl.getAsJsonObject().keySet()) {
                        int price = parsePrice(catEl.getAsJsonObject().get(itemId));
                        if (price <= 0) continue;
                        ResourceLocation id = ResourceLocation.tryParse(itemId);
                        if (id == null) continue;
                        out.add(new CobbleDollarsShopPayloads.ShopOfferEntry(id, 1, price, airId, 0, true));
                    }
                }
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    private static int parsePrice(JsonElement el) {
        if (el == null || el.isJsonNull()) return 0;
        if (el.isJsonPrimitive()) {
            var p = el.getAsJsonPrimitive();
            if (p.isNumber()) return p.getAsInt();
            if (p.isString()) {
                String s = p.getAsString().trim().toLowerCase();
                int mult = 1;
                if (s.endsWith("k")) { mult = 1000; s = s.substring(0, s.length() - 1); }
                else if (s.endsWith("m")) { mult = 1_000_000; s = s.substring(0, s.length() - 1); }
                try {
                    return (int) (Double.parseDouble(s) * mult);
                } catch (NumberFormatException ignored) {}
            }
        }
        return 0;
    }

    public static int getEffectiveEmeraldRate() {
        if (Config.SYNC_COBBLEDOLLARS_BANK_RATE.get()) {
            OptionalInt bank = getBankEmeraldPrice();
            if (bank.isPresent()) return bank.getAsInt();
        }
        return Config.COBBLEDOLLARS_EMERALD_RATE.get();
    }

    private static Path getConfigDirectory() {
        try {
            Class<?> pathsClass = Class.forName("net.neoforged.fml.loading.FMLPaths");
            Object configDir = pathsClass.getField("CONFIGDIR").get(null);
            if (configDir instanceof Path p) return p;
        } catch (Throwable ignored) {
        }
        try {
            Class<?> pathsClass = Class.forName("net.minecraftforge.fml.loading.FMLPaths");
            Object configDir = pathsClass.getField("CONFIGDIR").get(null);
            if (configDir instanceof Path p) return p;
        } catch (Throwable ignored) {
        }
        return Path.of("config").toAbsolutePath();
    }
}
