package the_fireplace.ias.events;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;

/**
 * @author The_Fireplace
 */
public class ClientEvents {
	private static int textX, textY;
	@SubscribeEvent
	public void guiEvent(InitGuiEvent.Post event){
		GuiScreen gui = event.getGui();
		if(gui instanceof GuiMainMenu) {
			try {
				ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
				textX = ((Number) engine.eval(ConfigValues.TEXT_X.replace("%width%", Integer.toString(event.getGui().width))
						.replace("%height%", Integer.toString(event.getGui().height)))).intValue();
				textY = ((Number) engine.eval(ConfigValues.TEXT_Y.replace("%width%", Integer.toString(event.getGui().width))
						.replace("%height%", Integer.toString(event.getGui().height)))).intValue();
			} catch (Throwable t) {
				textX = event.getGui().width / 2;
				textY = event.getGui().height / 4 + 48 + 72 + 12 + 22;
			}
			event.addButton(new GuiButtonWithImage(gui.width / 2 + 104, gui.height / 4 + 48 + 72 + 12, 20, 20, "", () -> {
				if(Config.getInstance() == null){
					Config.load();
				}
				Minecraft.getInstance().displayGuiScreen(new GuiAccountSelector());
			}));
		}
	}
	
	@SubscribeEvent
	public void onTick(GuiScreenEvent.DrawScreenEvent.Post t) {
		Minecraft mc = Minecraft.getInstance();
		GuiScreen screen = mc.currentScreen;
		if (screen instanceof GuiMainMenu) {
			screen.drawCenteredString(mc.fontRenderer, I18n.format("ias.loggedinas") + " " + Minecraft.getInstance().getSession().getUsername() +".", textX, textY, 0xFFCC8888);
		} else if(screen instanceof GuiMultiplayer){
			if (Minecraft.getInstance().getSession().getToken().equals("0")) {
				List<String> list = mc.fontRenderer.listFormattedStringToWidth(I18n.format("ias.offlinemode"), t.getGui().width);
				for (int i = 0; i < list.size(); i++) {
					screen.drawCenteredString(mc.fontRenderer, list.get(i), screen.width / 2, i * 9 + 1, 16737380);
				}
			}
		}
	}
}
