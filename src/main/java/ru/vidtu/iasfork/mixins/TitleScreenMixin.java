package ru.vidtu.iasfork.mixins;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import ru.vidtu.iasfork.IASMMPos;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.ias.gui.GuiButtonWithImage;
import the_fireplace.ias.tools.SkinTools;

@Mixin(TitleScreen.class)
public class TitleScreenMixin extends Screen {
	private static boolean skinsLoaded, modMenu = false;
	private static int textX, textY;
	protected TitleScreenMixin(Text title) {
		super(title);
	}
	
	@Inject(method = "init", at = @At("TAIL"))
	public void onInit(CallbackInfo ci) {
		if (!skinsLoaded) {
			SkinTools.cacheSkins(false);
			modMenu = FabricLoader.getInstance().isModLoaded("modmenu");
			skinsLoaded = true;
		}
		try {
			ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
			textX = ((Number) engine.eval(ConfigValues.TEXT_X.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height)))).intValue();
			textY = ((Number) engine.eval(ConfigValues.TEXT_Y.replace("%width%", Integer.toString(width)).replace("%height%", Integer.toString(height)))).intValue();
		} catch (Throwable t) {
			textX = width / 2;
			textY = height / 4 + 48 + 72 + 12 + (modMenu?32:22);
		}
		addButton(new GuiButtonWithImage(width / 2 + 104, height / 4 + 48 + 72 + (modMenu?IASMMPos.buttonOffset():-12), btn -> {
			if (Config.getInstance() == null) {
				Config.load();
			}
			client.openScreen(new GuiAccountSelector(this));
		}));
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void onRender(MatrixStack ms, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		drawCenteredString(ms, textRenderer, I18n.translate("ias.loggedinas") + " " + client.getSession().getUsername() + ".", textX, textY, 0xFFCC8888);
	}
}
