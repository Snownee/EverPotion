package snownee.everpotion.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import snownee.everpotion.network.CDrinkPacket;

public class UseScreen extends Screen {

    private static final ITextComponent TITLE = new TranslationTextComponent("gui.everpotion.use.title");

    public UseScreen() {
        super(TITLE);
    }

    @Override
    protected void init() {
        addButton(new Button(40, 40, 40, 20, "1", $ -> drink(0)));
        addButton(new Button(140, 40, 40, 20, "2", $ -> drink(1)));
        addButton(new Button(240, 40, 40, 20, "3", $ -> drink(2)));
        addButton(new Button(340, 40, 40, 20, "4", $ -> drink(3)));
    }

    private static void drink(int index) {
        new CDrinkPacket(index).send();
        Minecraft.getInstance().displayGuiScreen(null);
    }

}
