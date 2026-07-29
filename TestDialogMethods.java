public class TestDialogMethods {
    public static void main(String[] args) throws Exception {
        io.papermc.paper.registry.data.dialog.body.ItemDialogBody.Builder b1 = io.papermc.paper.dialog.DialogBody.item(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND));
        io.papermc.paper.registry.data.dialog.body.ItemDialogBody.Builder b2 = b1.width(16);
        System.out.println("Same object? " + (b1 == b2));
        System.out.println("b1 class: " + b1.getClass().getName());
    }
}
