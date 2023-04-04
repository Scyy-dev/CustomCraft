package me.scyphers.customcraft.newgui;

import me.scyphers.customcraft.newui.InventoryGUI;
import me.scyphers.customcraft.newui.ItemBuilder;
import me.scyphers.customcraft.newui.Session;
import me.scyphers.customcraft.newui.StaticGUI;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AddCraftingRecipeGUI extends InventoryGUI {

    private static final ItemStack AIR = ItemBuilder.empty();

    private final NamespacedKey key;

    private boolean confirm = false;
    private boolean shapeless = false;

    public AddCraftingRecipeGUI(Session session, NamespacedKey key) {
        super(session, "<dark_purple>Add Crafting Recipe</dark_purple>", 54);
        this.key = key;
    }

    @Override
    public void draw() {
        fill(BACKGROUND);
        setItem(45, new ItemBuilder(Material.COMPASS).name("<gold>Back</gold>").build());
        setItem(53, new ItemBuilder(Material.IRON_DOOR).name("<red>Exit</red>").build());

        // Generate the ingredient view
        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                int inventoryIndex = getInventoryIndex(column, row);
                setItem(inventoryIndex, AIR);
            }
        }

        setItem(23, confirmButton());
        setItem(25, AIR);
        setItem(49, toggleShapeButton());

    }

    @Override
    public InventoryGUI onClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();

        // Block illegal actions that can pull from the inventory
        if (!isValidAction(event.getAction())) {
            event.setCancelled(true);
            return this;
        }

        // Players can always use their inventory
        if (slot >= this.getSize()) {
            return this;
        }

        int column = slot % 9 - 1;
        int row = slot / 9 - 1;

        // Crafting inventory / result click
        if (column >= 0 && column <= 2 && row >= 0 && row <= 2 || slot == 25) {
            event.setCancelled(false);
            return this;
        }

        // Other interaction - block it
        event.setCancelled(true);
        return switch (slot) {

            // Add recipe
            case 23 -> {

                if (!confirm) {
                    confirm = true;
                    this.setItem(23, confirmButton());
                    this.setUpdate(true);
                    yield this;
                }

                ItemStack[] ingredients = getIngredients();
                ItemStack result = item(getItem(25));

                if (!isValidRecipe(ingredients, result)) {
                    getSession().chat("crafting.invalidCraftingRecipe");
                    confirm = false;
                    yield this;
                }

                Recipe recipe = shapeless ? createShapelessRecipe(ingredients, result) : createShapedRecipe(ingredients, result);
                getSession().getPlugin().getServer().addRecipe(recipe);
                getSession().chat("crafting.addedCraftingRecipe", "%key%", key.getKey());
                this.setClose(true);
                yield new StaticGUI(this);
            }

            // Toggle Shaped/Shapeless
            case 49 -> {
                this.shapeless = !this.shapeless;
                this.setItem(49, toggleShapeButton());
                yield this;
            }

            // Navigation
            case 45 -> new CraftHomeGUI(getSession(), key);
            case 53 -> {
                this.setClose(true);
                yield new StaticGUI(this);
            }

            //
            default -> this;
        };

    }

    @NotNull
    private ItemStack item(ItemStack item) {
        return item != null && item.getType() != Material.AIR ? item : AIR;
    }

    private int getIngredientIndex(int column, int row) {
        return column + row * 3;
    }

    private int getInventoryIndex(int column, int row) {
        return 10 + column + row * 9;
    }

    private ItemStack[] getIngredients() {

        ItemStack[] ingredients = new ItemStack[9];
        ItemStack[] inventory = getInventory().getContents();

        for (int column = 0; column < 3; column++) {
            for (int row = 0; row < 3; row++) {
                int ingredientIndex = column + row * 3;
                int inventoryIndex = getInventoryIndex(column, row);
                ingredients[ingredientIndex] = inventory[inventoryIndex];
            }
        }

        return ingredients;
    }

    private boolean isValidAction(InventoryAction action) {
        return switch (action) {
            case PICKUP_ALL, PICKUP_HALF, PICKUP_SOME, PICKUP_ONE,
                    PLACE_ALL, PLACE_SOME, PLACE_ONE, SWAP_WITH_CURSOR -> true;
            default -> false;
        };
    }

    private boolean isValidRecipe(ItemStack[] ingredients, ItemStack result) {
        if (item(result).getType() == Material.AIR) return false;
        return !Arrays.stream(ingredients).allMatch(item -> item(item).getType() == Material.AIR);
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

    private ItemStack confirmButton() {
        String addItemText = confirm ? "<gray>Click again to confirm adding this item!</gray>" : "<gray>Click to add this item!</gray>";
        return new ItemBuilder(Material.CRAFTING_TABLE).name("<gold>Add Recipe!</gold>").lore(addItemText).build();
    }

    private ItemStack toggleShapeButton() {
        String shapeName = shapeless ? "<gold>Shapeless</gold>" : "<gold>Shaped</gold>";
        String lore = "<dark_gray>Click to toggle to change the recipe to a " + shapeName + " recipe</dark_gray>";
        Material type = shapeless ? Material.WATER_BUCKET : Material.ICE;
        return new ItemBuilder(type).name(shapeName).lore("").lore(lore).build();
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
