package me.scyphers.customcraft.newui;

import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.List;

public abstract class PagedListGUI<T> extends MenuGUI {

    private List<T> items;

    private final int height, width, totalPerPage;

    private int page;

    private final int previousPageSlot, nextPageSlot;

    private final int columnFromWidth;
    private final int startIndex;

    public PagedListGUI(@NotNull Session session, @NotNull String name, int size, @Range(from = 1, to = 4) int height,
                        @Range(from = 1, to = 7) int width, int previousPageSlot, int nextPageSlot) {
        super(session, name, size);
        this.height = height;
        this.width = width;
        this.totalPerPage = height * width;
        this.previousPageSlot = previousPageSlot;
        this.nextPageSlot = nextPageSlot;

        this.columnFromWidth = getColumnFromWidth(width);
        // this.startIndex = 9 * getRowFromHeight(height) + columnFromWidth;
        int verticalWidth = getVerticalWidth(size, height);
        this.startIndex = 9 * verticalWidth + columnFromWidth;
    }

    @Override
    public void draw() {

        super.draw();

        ItemStack[] inventoryItems = this.getInventory().getContents();

        // Determine the start position of the items from the list
        // cases where the integer is bigger than the list are handling in the loop
        int listIndexStart = totalPerPage * page;

        // The index of the items in the inventory, determined based on a simple map from width and height
        int invIndex = startIndex;

        items = createList();

        // Iterate over the current page of items
        for (int i = listIndexStart; i < listIndexStart + totalPerPage; i++) {

            // Check if it is safe to retrieve the item and display it
            if (i < items.size()) {
                T item = items.get(i);
                inventoryItems[invIndex] = this.display(item);

                // Otherwise, display nothing
            } else {
                inventoryItems[invIndex] = this.displayBlank();
            }

            invIndex++;

            // Wrap the inventory index back around if this display row has finished
            if ((invIndex - (9 - columnFromWidth)) % 9 == 0) invIndex += 2 * columnFromWidth;

        }

        // Display the pagination buttons
        if (page != 0) inventoryItems[previousPageSlot] = this.previousPageButton(page);
        if (!isLastPage() || allowsInfinitePages()) {
            inventoryItems[nextPageSlot] = this.nextPageButton(page + 2);
        }

        this.getInventory().setContents(inventoryItems);
    }

    @Override
    public InventoryGUI onClick(int slot, ClickType type, InventoryAction action) {
        // Check if the item clicked was an item on the list
        int wrappedClick = getIndexOfItemWithBorder(slot, getRowFromHeight(height) * 2, getColumnFromWidth(width) * 2);
        int indexedClick = totalPerPage * page + wrappedClick;
        if (wrappedClick != -1 && indexedClick < items.size()) {
            T item = items.get(indexedClick);
            return this.handleItemInteraction(slot, type, action, item);
        }

        // Check if the item clicked was a pagination button
        if (slot == previousPageSlot && page != 0) {
            this.page -= 1;
            this.draw();
            return this;
        }

        if (slot == nextPageSlot) {
            this.page += 1;
            this.draw();
            return this;
        }

        return this.handleGenericInteraction(slot, type, action);
    }

    public abstract @NotNull List<T> createList();

    public abstract @NotNull ItemStack display(T item);

    public abstract @Nullable ItemStack displayBlank();

    public abstract boolean allowsInfinitePages();

    public @NotNull ItemStack previousPageButton(int page) {
        return new me.scyphers.customcraft.ui.ItemBuilder(Material.ARROW).name("<gold>Page " + (page - 1) + "</gold>").build();
    }

    public @NotNull ItemStack nextPageButton(int page) {
        return new ItemBuilder(Material.ARROW).name("<gold>Page " + (page + 1) + "</gold>").build();
    }

    public abstract @NotNull InventoryGUI handleGenericInteraction(int slot, ClickType type, InventoryAction action);

    public abstract @NotNull InventoryGUI handleItemInteraction(int slot, ClickType type, InventoryAction action, T item);

    public List<T> getItems() {
        return items;
    }

    public void setListItems(List<T> items) {
        this.items = items;
    }

    // This check will also return true for pages that extend beyond the item list limit
    // this is to ensure that players cannot continue to scroll infinitely if they somehow go past the page limit
    private boolean isLastPage() {
        return items.size() <= totalPerPage * (page + 1);
    }

    public static int getColumnFromWidth(int width) {
        return switch (width) {
            case 1 -> 4;
            case 2, 3 -> 3;
            case 4, 5 -> 2;
            case 6, 7 -> 1;
            default -> -1;
        };
    }

    public static int getRowFromHeight(int height) {
        return switch (height) {
            case 1, 2 -> 2;
            case 3, 4 -> 1;
            default -> -1;
        };
    }

    public static int getVerticalWidth(int size, int height) {
        return ((size / 9) - height) / 2;
    }

    public static int getIndexOfItemWithBorder(int inventoryIndex, int borderRows, int borderColumns) {

        int invRows = 6 - borderRows;
        int invColumns = 9 - borderColumns;

        int invRow = inventoryIndex / 9;
        int invColumn = inventoryIndex % 9;

        if (invRow < borderRows / 2 || invRow > invRows + ((borderRows / 2) - 1)) return -1;
        if (invColumn < borderColumns / 2 || invColumn > invColumns + ((borderColumns / 2) - 1)) return -1;

        return ((invRow - (borderRows / 2)) * invColumns) + (invColumn - (borderColumns / 2));

    }

}
