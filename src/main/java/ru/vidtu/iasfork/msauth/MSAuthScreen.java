package ru.vidtu.iasfork.msauth;

import java.util.ArrayList;
import java.util.List;

import com.github.mrebhan.ingameaccountswitcher.MR;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import ru.vidtu.iasfork.msauth.AuthSys.MicrosoftAuthException;
import the_fireplace.ias.gui.AbstractAccountGui;
import the_fireplace.ias.gui.GuiAccountSelector;
import the_fireplace.iasencrypt.EncryptionTools;

public class MSAuthScreen extends Screen implements MSAuthHandler {
	public static final String[] symbols = new String[]{"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ _ _ ▃ ▄ ▅ ▆ ▇ █",
			"_ _ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇", "_ _ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆", "_ _ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅", "_ ▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄",
			"▃ ▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _", "▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "█ ▇ ▆ ▅ ▄ ▃ _ _ _ _ _", "▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _ _", "▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _ _",
			"▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _ _", "▄ ▅ ▆ ▇ █ ▇ ▆ ▅ ▄ ▃ _"};
	private static final ResourceLocation DEMO_BG = new ResourceLocation("textures/gui/demo_background.png");
	
	public Screen prev;
	public List<IReorderingProcessor> text = new ArrayList<>();
	public boolean endTask = false;
	public int tick;
	public final boolean add;
	public boolean cancelButton = true;
	
	public MSAuthScreen(Screen prev) {
		super(new TranslationTextComponent("ias.msauth.title"));
		this.prev = prev;
		this.add = true;
		AuthSys.start(this);
	}
	
	public MSAuthScreen(Screen prev, String token, String refresh) {
		super(new TranslationTextComponent("ias.msauth.title"));
		this.prev = prev;
		this.add = false;
		AuthSys.start(token, refresh, this);
	}
	
	@Override
	public void init() {
		addButton(new Button(width / 2 - 50, (this.height + 114) / 2, 100, 20, new TranslationTextComponent("gui.cancel"), btn -> minecraft.setScreen(prev))).active = cancelButton;
	}
	
	@Override
	public void init(Minecraft client, int width, int height) {
		prev.init(client, width, height);
		super.init(client, width, height);
	}
	
	@Override
	public void tick() {
		tick++;
		buttons.get(0).active = cancelButton;
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
		renderBackground(ms);
		
		if (prev != null) prev.render(ms, 0, 0, delta);
		fill(ms, 0, 0, width, height, Integer.MIN_VALUE);
		
		this.minecraft.getTextureManager().bind(DEMO_BG);
		this.blit(ms, (this.width - 248) / 2, (this.height - 166) / 2, 0, 0, 248, 166);
		
		font.draw(ms, this.title.getString(), width / 2 - font.width(this.title.getString()) / 2, (this.height - 156) / 2, -16777216);
		for (int i = 0; i < text.size(); i++) {
			font.draw(ms, text.get(i), width / 2 - font.width(text.get(i)) / 2, height / 2 + i * 10 - text.size() * 5, 0xFF353535);
		}
		if (!endTask) font.draw(ms, symbols[tick % symbols.length], width / 2 - font.width(symbols[tick % symbols.length]) / 2, height - 10, 0xFFFF9900);
		super.render(ms, mouseX, mouseY, delta);
	}

	@Override
	public void removed() {
		AuthSys.stop();
		prev.removed();
		super.removed();
	}
	
	@Override
	public void setState(String s) {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> this.text = mc.font.split(new TranslationTextComponent(s), 240));
	}

	@Override
	public void error(Throwable t) {
		cancelButton = true;
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> {
			endTask = true;
			if (t instanceof MicrosoftAuthException) {
				this.text = mc.font.split(new TranslationTextComponent("ias.msauth.error", t.getMessage()).withStyle(TextFormatting.DARK_RED), 240);
			} else {
				this.text = mc.font.split(new TranslationTextComponent("ias.msauth.error", t.toString()).withStyle(TextFormatting.DARK_RED), 240);
			}
		});
	}

	@Override
	public void success(String name, String uuid, String token, String refresh) {
		Minecraft mc = Minecraft.getInstance();
		mc.execute(() -> {
			if (add) {
				MicrosoftAccount.msaccounts.add(new MicrosoftAccount(name, EncryptionTools.encode(token), EncryptionTools.encode(refresh)));
				mc.setScreen(new GuiAccountSelector(prev instanceof AbstractAccountGui?(((AbstractAccountGui)prev).prev instanceof GuiAccountSelector?((GuiAccountSelector)((AbstractAccountGui)prev).prev).prev:((AbstractAccountGui)prev).prev):prev));
			} else {
				MR.setSession(new Session(name, uuid, token, "mojang"));
				mc.setScreen(null);
			}
		});
	}

	@Override
	public void cancellble(boolean b) {
		this.cancelButton = b;
	}
}
