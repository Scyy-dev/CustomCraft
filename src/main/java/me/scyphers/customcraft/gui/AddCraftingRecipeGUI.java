package me.scyphers.customcraft.gui;

import me.scyphers.customcraft.CustomCraft;
import me.scyphers.customcraft.ui.GUI;
import me.scyphers.customcraft.ui.InventoryGUI;
import me.scyphers.customcraft.ui.ItemBuilder;
import me.scyphers.customcraft.ui.StaticGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class AddCraftingRecipeGUI extends InventoryGUI {

    private static final ItemStack AIR = ItemBuilder.empty();

    private final NamespacedKey key;

    private boolean confirm = false;
    private boolean shapeless = false;

    private final ItemStack[] ingredients = new ItemStack[9];
    private ItemStack result = new ItemStack(Material.AIR);

    /*
     * Crafting Interface
     * #########
     * #000#####
     * #000#C#0#
     * #000#####
     * #########
     * B###S###E
     * 0 - player enabled slot
     * C - button to add recipe
     * B - back button
     * S - toggle shapeless/shaped
     * E - Exit
     */

    public AddCraftingRecipeGUI(@NotNull CustomCraft plugin, @NotNull Player player, UUID viewer, NamespacedKey key) {
        super(plugin, player, viewer, "&5Add Crafting Recipe", 54);
        this.key = key;
    }

    @Override
    public void draw() {

        // Default appearance
        fill(BACKGROUND);
        setItem(45, new ItemBuilder(Material.COMPASS).name("<gold>Back</gold>").build());
        setItem(53, new ItemBuilder(Material.IRON_DOOR).name("<red>Exit</red>").build());

        // Generate the ingredient view
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                int index = getIngredientIndex(column, row);
                int inventoryIndex = getInventoryIndex(column, row);
                setItem(inventoryIndex, getItem(ingredients[index]));
            }
        }

        // Add the Crafting Table
        String addItemText = confirm ? "&7Click again to confirm adding this item!" : "&7Click to add this item!";
        setItem(23, new ItemBuilder(Material.CRAFTING_TABLE).name("&6Add Recipe!").lore(addItemText).build());

        // Add the result slot
        setItem(25, getItem(result));

        // Add the shapeless/shaped toggle
        String shapeName = shapeless ? "&6Shapeless" : "&6Shaped";
        String lore = "&8Click to toggle to change the recipe to a " + shapeName + " &8recipe";
        Material type = shapeless ? Material.WATER_BUCKET : Material.ICE;
        setItem(49, new ItemBuilder(type).name(shapeName).lore("").lore(lore).build());

    }

    @Override
    public @NotNull GUI<?> handleInteraction(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        boolean isPlayerEdit = isPlayerEditableSlot(slot);
        event.setCancelled(true);

        // Allow the player to edit any slot as they please - bulk operations are blocked however
        if (isPlayerEdit) {
            if (isPermittedPlayerEdit(event.getClick())) {
                event.setCancelled(false);
                return this;
            }

            if (slot == 25) {
                result = event.getCurrentItem();
            } else {
                int column = slot % 9;
                int row = slot / 9;
                ingredients[getIngredientIndex(column, row)] = event.getCurrentItem();
            }

            return this;

        }

        return switch (slot) {

            // Add recipe
            case 23 -> {

                if (!confirm) {
                    confirm = true;
                    this.draw();
                    yield this;
                }

                Recipe recipe = shapeless ? createShapelessRecipe(ingredients, result) : createShapedRecipe(ingredients, result);
                getPlugin().getServer().addRecipe(recipe);
                getPlugin().getMessenger().chat(getPlayer(), "crafting.addedCraftingRecipe", "%key%", key.getKey());

                this.setShouldClose(true);
                yield new StaticGUI(this);

            }

            // Toggle Shaped/Shapeless
            case 49 -> {
                this.shapeless = !this.shapeless;
                yield this;
            }

            // Navigation
            case 45 -> new CraftHomeGUI(getPlugin(), getPlayer(), getViewer(), key);
            case 53 -> {
                this.setShouldClose(true);
                yield new StaticGUI(this);
            }

            //
            default -> this;
        };

    }

    @Override
    public boolean allowPlayerInventoryEdits() {
        return true;
    }

    private int getIngredientIndex(int column, int row) {
        return column + row * 3;
    }

    private int getInventoryIndex(int column, int row) {
        return 10 + column + row * 9;
    }

    private ItemStack getItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR ? item : AIR;
    }

    private boolean isPlayerEditableSlot(int slot) {
        return switch (slot) {
            case 10, 11, 12, 19, 20, 21, 28, 29, 30, 25 -> true;
            default -> false;
        };
    }

    private boolean isPermittedPlayerEdit(ClickType type) {
        return !type.isLeftClick() || type.isRightClick();
    }

    private Recipe createShapedRecipe(ItemStack[] ingredients, ItemStack result) {
        ShapedRecipe recipe = new ShapedRecipe(key, result);
        ParsedShape shape = ParsedShape.parse(ingredients);
        recipe.shape(shape.shape);
        for (char c : shape.ingredients.keySet()) {
            if (c == ' ') continue; // Ignore empty items for the recipe
            ItemStack item = shape.ingredients().get(c);
            recipe.setIngredient(c, item);
        }

        return recipe;
    }

    private Recipe createShapelessRecipe(ItemStack[] ingredients, ItemStack result) {
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        for (ItemStack item : ingredients) {
            if (item != null && item.getType() != Material.AIR) {
                recipe.addIngredient(item);
            }
        }
        return recipe;
    }

    private static record ParsedShape(String[] shape, Map<Character, ItemStack> ingredients) {

        public static ParsedShape parse(ItemStack[] items) {

            if (items.length != 9) throw new IllegalArgumentException("Invalid item array: " + Arrays.toString(items));

            // Starting point for the character shape array
            char c = 'a';

            // We use an item to char map for finding current chars
            // We use a char to item map for ease of use beyond parsing
            Map<Character, ItemStack> charToItem = new HashMap<>();

            // Ensure we only deal with characters when forming the shape
            // using a String[] array adds additional complexity for trying to handle modification
            char[][] shape = new char[][] {"   ".toCharArray(), "   ".toCharArray(), "   ".toCharArray()};

            // Iterate over each char in the shape - it is assumed the items array matches the size
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {

                    // 2D indexing of a 1D array
                    ItemStack item = items[j + i * 3];

                    // Ignore air/null items - they indicate blank spaces in the shape
                    if (item == null || item.getType() == Material.AIR) continue;

                    // If the item already exists, use its character, otherwise generate the next character
                    char chosenChar = charToItem.entrySet().stream()
                            .filter(entry -> entry.getValue().equals(item))
                            .map(Map.Entry::getKey)
                            .findAny()
                            .orElse(c++);
                    charToItem.put(chosenChar, item);
                    shape[i][j] = chosenChar;
                }
            }

            String[] finalShape = new String[] { new String(shape[0]), new String(shape[1]), new String(shape[2]) };
            return new ParsedShape(finalShape, charToItem);
        }

    }

}
