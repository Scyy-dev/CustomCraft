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
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
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
        super(plugin, player, viewer, "<dark_purple>Add Crafting Recipe</dark_purple>", 54);
        this.key = key;
    }

    @Override
    public void draw() {

        System.out.println("ingredients: " + Arrays.toString(ingredients));

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
        String addItemText = confirm ? "<gray>Click again to confirm adding this item!</gray>" : "<gray>Click to add this item!</gray>";
        setItem(23, new ItemBuilder(Material.CRAFTING_TABLE).name("<gold>Add Recipe!</gold>").lore(addItemText).build());

        // Add the result slot
        setItem(25, getItem(result));

        // Add the shapeless/shaped toggle
        String shapeName = shapeless ? "<gold>Shapeless</gold>" : "<gold>Shaped</gold>";
        String lore = "<dark_gray>Click to toggle to change the recipe to a " + shapeName + " recipe</dark_gray>";
        Material type = shapeless ? Material.WATER_BUCKET : Material.ICE;
        setItem(49, new ItemBuilder(type).name(shapeName).lore("").lore(lore).build());

    }

    @Override
    public @NotNull GUI<?> handleInteraction(InventoryClickEvent event) {

        int slot = event.getRawSlot();
        if (!isValidAction(event.getAction())) {
            event.setCancelled(true);
        }

        // Player inventory click
        if (slot >= this.getSize()) {
            return this;
        }

        System.out.println("cursor: " + event.getCursor());
        System.out.println("current: " + event.getCurrentItem());
        System.out.println("action: " + event.getAction());
        System.out.println("click type: " + event.getClick());

        ItemStack cursor = getItem(event.getCursor()).clone();

        // Result slot click
        if (slot == 25) {
            if (cursor.getType() != Material.AIR) {
                result = cursor;
                System.out.println("updating result item");
            } else {
                result = AIR;
                System.out.println("removing result item");
            }
            return this;
        }

        int column = slot % 9 - 1;
        int row = slot / 9 - 1;

        // Crafting inventory click
        if (column >= 0 && column <= 2 && row >= 0 && row <= 2) {
            int index = column + row * 3;
            if (cursor.getType() != Material.AIR) {
                ingredients[index] = cursor;
                System.out.println("Updating ingredient item");
            } else {
                ingredients[index] = AIR;
                System.out.println("Removing ingredient item");
            }
            return this;
        }

        event.setCancelled(true);

        return switch (slot) {

            // Add recipe
            case 23 -> {

                if (!confirm) {
                    confirm = true;
                    this.draw();
                    yield this;
                }

                if (!validRecipe()) {
                    getPlugin().getMessenger().chat(getPlayer(), "crafting.invalidCraftingRecipe");
                    confirm = false;
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

    @NotNull
    private ItemStack getItem(ItemStack item) {
        return item != null && item.getType() != Material.AIR ? item : AIR;
    }

    private boolean isPlayerEditableSlot(int slot) {
        if (slot > this.getSize()) return true;
        return switch (slot) {
            case 10, 11, 12, 19, 20, 21, 28, 29, 30, 25 -> true;
            default -> false;
        };
    }

    private boolean isPermittedPlayerEdit(ClickType type) {
        return type == ClickType.RIGHT || type == ClickType.LEFT;
    }

    private boolean isValidAction(InventoryAction action) {
        return switch (action) {
            case PICKUP_ALL, PICKUP_HALF, PICKUP_SOME, PICKUP_ONE,
                    PLACE_ALL, PLACE_SOME, PLACE_ONE, SWAP_WITH_CURSOR -> true;
            default -> false;
        };
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

    private boolean validRecipe() {
        if (getItem(result).getType() == Material.AIR) return false;
        return !Arrays.stream(ingredients).allMatch(item -> getItem(item).getType() == Material.AIR);
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
