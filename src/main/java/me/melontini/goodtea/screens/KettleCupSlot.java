package me.melontini.goodtea.screens;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import static me.melontini.goodtea.util.GoodTeaStuff.TEA_CUP;

public class KettleCupSlot extends Slot {
    public KettleCupSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.getItem() == TEA_CUP;
    }
}
