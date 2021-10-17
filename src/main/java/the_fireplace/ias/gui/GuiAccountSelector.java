package the_fireplace.ias.gui;

import java.io.File;
import java.io.FileInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.github.mrebhan.ingameaccountswitcher.tools.Tools;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.Session;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import ru.vidtu.ias.Config;
import ru.vidtu.ias.account.Account;
import ru.vidtu.ias.account.AuthException;
import ru.vidtu.ias.mixins.MinecraftClientAccessor;
import ru.vidtu.ias.utils.SkinRenderer;
import the_fireplace.ias.IAS;
/**
 * The GUI where you can log in to, add, and remove accounts
 * @author The_Fireplace
 */
public class GuiAccountSelector extends Screen {
	public final Screen prev;
	private boolean logging;
	private String error;
	private AccountList accountsgui;
	// Buttons that can be disabled need to be here
	private ButtonWidget login;
	private ButtonWidget loginoffline;
	private ButtonWidget delete;
	private ButtonWidget edit;
	private ButtonWidget reloadskins;
	// Search
	private String prevQuery = "";
	private TextFieldWidget search;
	
	public GuiAccountSelector(Screen prev) {
		super(new TranslatableText("ias.selectaccount"));
		this.prev = prev;
	}

	@Override
	protected void init() {
		if (accountsgui == null) accountsgui = new AccountList(minecraft, width, height);
		children.add(accountsgui);
		addButton(reloadskins = new ButtonWidget(2, 2, 120, 20, I18n.translate("ias.reloadskins"), btn -> reloadSkins()));
		addButton(new ButtonWidget(this.width / 2 + 4 + 40, this.height - 52, 120, 20, I18n.translate("ias.addaccount"), btn -> add()));
		addButton(login = new ButtonWidget(this.width / 2 - 154 - 10, this.height - 52, 120, 20, I18n.translate("ias.login"), btn -> accountsgui.login()));
		addButton(edit = new ButtonWidget(this.width / 2 - 40, this.height - 52, 80, 20, I18n.translate("ias.edit"), btn -> accountsgui.edit()));
		addButton(loginoffline = new ButtonWidget(this.width / 2 - 154 - 10, this.height - 28, 110, 20, new TranslatableText("ias.login").append(" ").append(new TranslatableText("ias.offline")).asFormattedString(), btn -> accountsgui.loginOffline()));
		addButton(new ButtonWidget(this.width / 2 + 4 + 50, this.height - 28, 110, 20, I18n.translate("gui.cancel"), btn -> minecraft.openScreen(prev)));
		addButton(delete = new ButtonWidget(this.width / 2 - 50, this.height - 28, 100, 20, I18n.translate("ias.delete"), btn -> accountsgui.delete()));
		addButton(search = new TextFieldWidget(this.font, this.width / 2 - 80, 14, 160, 16, I18n.translate("ias.search")));
	    updateButtons();
	    search.setSuggestion(I18n.translate("ias.search"));
	    accountsgui.resize(width, height);
	    accountsgui.updateAccounts();
	}

	@Override
	public void tick() {
		search.tick();
		updateButtons();
		if (!prevQuery.equals(search.getText())) {
			accountsgui.updateAccounts();
			prevQuery = search.getText();
			search.setSuggestion(search.getText().isEmpty()?I18n.translate("ias.search"):"");
		}
	}

	@Override
	public void removed() {
		Config.save(minecraft);
	}
	
	@Override
	public void render(int mx, int my, float delta) {
		renderBackground();
		accountsgui.render(mx, my, delta);
		drawCenteredString(font, this.title.asFormattedString(), this.width / 2, 4, -1);
		if (error != null) {
			drawCenteredString(font, error, this.width / 2, this.height - 62, 16737380);
		}
		super.render( mx, my, delta);
		if (accountsgui.getSelected() != null) {
			Account acc = accountsgui.getSelected().account;
			minecraft.getTextureManager().bindTexture(accountsgui.getSelected().model(false));
			RenderSystem.color3f(1F, 1F, 1F);
			Screen.blit(8, height / 2 - 64 - 16, 0, 0, 64, 128, 64, 128);
			Tools.drawBorderedRect(width - 8 - 64, height / 2 - 64 - 16, width - 8, height / 2 + 64 - 16, 2, -5855578, -13421773);
			if (acc.online()) drawString(font, I18n.translate("ias.premium"), width - 8 - 61, height / 2 - 64 - 13, 6618980);
			else drawString(font, I18n.translate("ias.notpremium"), width - 8 - 61, height / 2 - 64 - 13, 16737380);
			drawString(font, I18n.translate("ias.timesused"), width - 8 - 61, height / 2 - 64 - 15 + 12, -1);
			drawString(font, String.valueOf(acc.uses()), width - 8 - 61, height / 2 - 64 - 15 + 21, -1);
			if (acc.uses() > 0) {
				drawString(font, I18n.translate("ias.lastused"), width - 8 - 61, height / 2 - 64 - 15 + 30, -1);
				drawString(font, DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
						.format(Instant.ofEpochMilli(acc.lastUse()).atZone(ZoneId.systemDefault())) , width - 8 - 61, height / 2 - 64 - 15 + 39, -1);
			}
		}
	}

	/**
	 * Reload Skins
	 */
	private void reloadSkins() {
		Config.save(minecraft);
		SkinRenderer.loadAllAsync(minecraft, true, () -> accountsgui.children().forEach(ae -> {
			ae.model(true);
			ae.face(true);
		}));
	}

	/**
	 * Add an account
	 */
	private void add() {
		minecraft.openScreen(new AbstractAccountGui(this, new TranslatableText("ias.addaccount"), acc -> {
			Config.accounts.add(acc);
			Config.save(minecraft);
			accountsgui.updateAccounts();
		}));
	}
	
	@Override
	public boolean keyPressed(int key, int oldkey, int mods) {
		if (key == GLFW.GLFW_KEY_ESCAPE) {
			minecraft.openScreen(prev);
			return true;
		}
		if (search.isFocused()) {
			if (key == GLFW.GLFW_KEY_ENTER && search.isFocused()) {
				search.setSelected(false);
				return true;
			}
		} else {
			if (key == GLFW.GLFW_KEY_DELETE && delete.active) {
				accountsgui.delete();
				return true;
			}
			if (key == GLFW.GLFW_KEY_ENTER && !search.isFocused() && (login.active || loginoffline.active)) {
				if (Screen.hasShiftDown() && loginoffline.active) {
					accountsgui.loginOffline();
				} else if (login.active) {
					accountsgui.login();
				} else {
					accountsgui.loginOffline();
				}
				return true;
			}
			if (key == GLFW.GLFW_KEY_F5) {
				reloadSkins();
				return true;
			}
		}
		return super.keyPressed(key, oldkey, mods);
	}
	
	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
	
	@Override
	public boolean charTyped(char charT, int mods) {
		if (!search.isFocused()) {
			if (charT == '+') {
				add();
				return true;
			}
			if (charT == '/' && edit.active) {
				accountsgui.edit();
				return true;
			}
			if (charT == 'r' || charT == 'R') {
				reloadSkins();
				return true;
			}
		}
		return super.charTyped(charT, mods);
	}

	private void updateButtons() {
		login.active = !accountsgui.empty() && accountsgui.getSelected().account.online() && !logging;
		loginoffline.active = !accountsgui.empty();
		delete.active = !accountsgui.empty();
		edit.active = !accountsgui.empty() && accountsgui.getSelected().account.editable();
		reloadskins.active = !accountsgui.empty();
	}

	public class AccountList extends AlwaysSelectedEntryListWidget<AccountEntry> {
		public AccountList(MinecraftClient mc, int width, int height) {
			super(mc, width, height, 32, height - 64, 14);
		}
		
		public void resize(int width, int height) {
			this.width = width;
			this.height = height;
			this.top = 32;
			this.bottom = height - 64;
		}

		public void updateAccounts() {
			clearEntries();
			Config.accounts.stream()
					.filter(acc -> search.getText().isEmpty()
							|| (Config.caseSensitiveSearch ? acc.alias().startsWith(search.getText())
									: acc.alias().toLowerCase().startsWith(search.getText().toLowerCase())))
					.forEach(acc -> addEntry(new AccountEntry(acc)));
			this.setSelected(empty()?null:getEntry(0));
		}
		
		public void login() {
			if (empty()) return;
			Account acc = getSelected().account;
			if (!acc.online()) return;
			logging = true;
			updateButtons();
			acc.use();
			acc.login(minecraft, t -> {
				logging = false;
				if (t == null) {
					minecraft.openScreen(prev);
				} else if (t instanceof AuthException) {
					IAS.LOG.warn("Unable to login", t);
					error = ((AuthException) t).getText().asFormattedString();
				} else {
					IAS.LOG.warn("Unable to login", t);
					error = I18n.translate("ias.auth.unknown", t.toString());
				}
			});
		}
		
		public void loginOffline() {
			if (empty()) return;
			Account acc = getSelected().account;
			acc.use();
			((MinecraftClientAccessor)minecraft).setSession(new Session(acc.alias(), UUIDTypeAdapter.fromUUID(new UUID(0, 0)), "0", "legacy"));
		}
		
		public void edit() {
			if (empty() || !getSelected().account.editable()) return;
			minecraft.openScreen(new AbstractAccountGui(GuiAccountSelector.this, new TranslatableText("ias.editaccount"), acc -> {
				Config.accounts.set(Config.accounts.indexOf(getSelected().account), acc);
			}));
		}
		
		public void delete() {
			if (empty()) return;
			Account acc = getSelected().account;
			minecraft.openScreen(new ConfirmScreen(b -> {
				if (b) {
					Config.accounts.remove(acc);
					updateButtons();
					updateAccounts();
				}
				minecraft.openScreen(GuiAccountSelector.this);
			}, new TranslatableText("ias.delete.title"), new TranslatableText("ias.delete.text", acc.alias())));
		}
		
		public void swap(int first, int second) {
			Account entry = Config.accounts.get(first);
			Config.accounts.set(first, Config.accounts.get(second));
			Config.accounts.set(second, entry);
			Config.save(minecraft);
			updateAccounts();
			setSelected(children().get(second));
		}
		
		public boolean empty() {
			return this.children().isEmpty();
		}
		
		@Override
		public int getRowLeft() {
			return super.getRowLeft();
		}
	}
	
	public class AccountEntry extends AlwaysSelectedEntryListWidget.Entry<AccountEntry> {
		public Account account;
		public Identifier modelTexture, faceTexture;
		public AccountEntry(Account account) {
			this.account = account;
		}
		
		@Override
		public void render(int i, int y, int x, int w, int h, int mx, int my, boolean hover, float delta) {
			int color = -1;
			if (minecraft.getSession().getUsername().equals(account.alias())) color = 0x00FF00;
			drawString(font, account.alias(), x + 10, y + 1, color);
			minecraft.getTextureManager().bindTexture(face(false));
			Screen.blit(x, y + 1, 0, 0, 8, 8, 8, 8);
			if (accountsgui.getSelected() == this) {
				minecraft.getTextureManager().bindTexture(new Identifier("textures/gui/server_selection.png"));
				boolean movableDown = i + 1 < accountsgui.children().size();
				boolean movableUp = i > 0;
				if (movableDown) {
					boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6 && hover;
					Screen.blit(x + w - 35, y - 18, 48, hoveredDown?32:0, 32, 32, 256, 256);
				}
				if (movableUp) {
					boolean hoveredUp = mx > x + w - (movableDown?28:16) && mx < x + w - (movableDown?16:6) && hover;
					Screen.blit(x + w - (movableDown?30:19), y - 3, 96, hoveredUp?32:0, 32, 32, 256, 256);
				}
			}
		}
		
		@Override
		public boolean mouseClicked(double mx, double my, int button) {
			if (button == 0 && accountsgui.getSelected() == this) {
				int x = accountsgui.getRowLeft();
				int w = accountsgui.getRowWidth();
				int i = accountsgui.children().indexOf(this);
				boolean movableDown = i + 1 < accountsgui.children().size();
				boolean movableUp = i > 0;
				if (movableDown) {
					boolean hoveredDown = mx > x + w - 16 && mx < x + w - 6;
					if (hoveredDown) {
						minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1F));
						accountsgui.swap(i, i + 1);
					}
				}
				if (movableUp) {
					boolean hoveredUp = mx > x + w - (movableDown?28:16) && mx < x + w - (movableDown?16:6);
					if (hoveredUp) {
						minecraft.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1F));
						accountsgui.swap(i, i - 1);
					}
				}
				return true;
			}
			accountsgui.setSelected(this);
			return true;
		}
		
		public Identifier model(boolean forceReload) {
			if (forceReload) {
				minecraft.getTextureManager().destroyTexture(modelTexture);
				modelTexture = null;
			}
			if (modelTexture == null) {
				File model = new File(new File(minecraft.runDirectory, "cachedImages/models"), account.alias() + ".png");
				File face = new File(new File(minecraft.runDirectory, "cachedImages/faces"), account.alias() + ".png");
				SkinRenderer.loadSkin(minecraft, account.alias(), account.uuid(), model, face, false);
				try (FileInputStream fis = new FileInputStream(model); NativeImage ni = NativeImage.read(fis)) {
					NativeImageBackedTexture nibt = new NativeImageBackedTexture(ni);
					modelTexture = minecraft.getTextureManager().registerDynamicTexture("iasmodel_" + account.alias().hashCode(), nibt);
				} catch (Throwable t) {
					IAS.LOG.warn("Unable to bake skin model: " + account.alias(), t);
					modelTexture = new Identifier("iaserror", "skin");
				}
			}
			return modelTexture;
		}
		
		public Identifier face(boolean forceReload) {
			if (forceReload) {
				minecraft.getTextureManager().destroyTexture(faceTexture);
				faceTexture = null;
			}
			if (faceTexture == null) {
				File model = new File(new File(minecraft.runDirectory, "cachedImages/models"), account.alias() + ".png");
				File face = new File(new File(minecraft.runDirectory, "cachedImages/faces"), account.alias() + ".png");
				SkinRenderer.loadSkin(minecraft, account.alias(), account.uuid(), model, face, false);
				try (FileInputStream fis = new FileInputStream(face); NativeImage ni = NativeImage.read(fis)) {
					NativeImageBackedTexture nibt = new NativeImageBackedTexture(ni);
					faceTexture = minecraft.getTextureManager().registerDynamicTexture("iasface_" + account.alias().hashCode(), nibt);
				} catch (Throwable t) {
					IAS.LOG.warn("Unable to bake skin face: " + account.alias(), t);
					faceTexture = new Identifier("iaserror", "skin");
				}
			}
			return faceTexture;
		}
		
	}
}
