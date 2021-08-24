package the_fireplace.ias.gui;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;
import com.github.mrebhan.ingameaccountswitcher.tools.Tools;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AccountData;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;
import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import ru.vidtu.iasfork.msauth.Account;
import ru.vidtu.iasfork.msauth.MicrosoftAccount;
import the_fireplace.ias.account.AlreadyLoggedInException;
import the_fireplace.ias.account.ExtendedAccountData;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.tools.HttpTools;
import the_fireplace.ias.tools.JavaTools;
import the_fireplace.ias.tools.SkinTools;
import the_fireplace.iasencrypt.EncryptionTools;
/**
 * The GUI where you can log in to, add, and remove accounts
 * @author The_Fireplace
 */
public class GuiAccountSelector extends GuiScreen {
	public final GuiScreen prev;
  private int selectedAccountIndex = 0;
  private int prevIndex = 0;
  private Throwable loginfailed;
  private ArrayList<Account> queriedaccounts = convertData();
  private GuiAccountSelector.List accountsgui;
  //Buttons that can be disabled need to be here
  private GuiButton login;
  private GuiButton loginoffline;
  private GuiButton delete;
  private GuiButton edit;
  private GuiButton reloadskins;
  //Search
  private String query;
  private GuiTextField search;
  
  public GuiAccountSelector(GuiScreen prev) {
	this.prev = prev;
  }

	@SuppressWarnings("unchecked")
	@Override
  public void initGui() {
		queriedaccounts = convertData();
    Keyboard.enableRepeatEvents(true);
    accountsgui = new GuiAccountSelector.List(this.mc);
    accountsgui.registerScrollButtons(5, 6);
    query = I18n.format("ias.search");
    this.buttonList.clear();
    //Above Top Row
    this.buttonList.add(reloadskins = new GuiButton(8, 2, 2, 120, 20, I18n.format("ias.reloadskins")));
    //Top Row
    this.buttonList.add(new GuiButton(0, this.width / 2 + 4 + 40, this.height - 52, 120, 20, I18n.format("ias.addaccount")));
    this.buttonList.add(login = new GuiButton(1, this.width / 2 - 154 - 10, this.height - 52, 120, 20, I18n.format("ias.login")));
    this.buttonList.add(edit = new GuiButton(7, this.width / 2 - 40, this.height - 52, 80, 20, I18n.format("ias.edit")));
    //Bottom Row
    this.buttonList.add(loginoffline = new GuiButton(2, this.width / 2 - 154 - 10, this.height - 28, 110, 20, I18n.format("ias.login") + " " + I18n.format("ias.offline")));
    this.buttonList.add(new GuiButton(3, this.width / 2 + 4 + 50, this.height - 28, 110, 20, I18n.format("gui.cancel")));
    this.buttonList.add(delete = new GuiButton(4, this.width / 2 - 50, this.height - 28, 100, 20, I18n.format("ias.delete")));
    search = new GuiTextField(this.fontRendererObj, this.width / 2 - 80, 14, 160, 16);
    search.setText(query);
    updateButtons();
    if (!queriedaccounts.isEmpty()) SkinTools.buildSkin(queriedaccounts.get(selectedAccountIndex).alias());
  }

  @Override
  public void updateScreen() {
    this.search.updateCursorCounter();
    updateText();
    updateButtons();
    if (!(prevIndex == selectedAccountIndex)) {
      updateShownSkin();
      prevIndex = selectedAccountIndex;
    }
  }

  private void updateShownSkin() {
    if (!queriedaccounts.isEmpty()) SkinTools.buildSkin(queriedaccounts.get(selectedAccountIndex).alias());
  }

  @Override
  protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
    boolean flag = search.isFocused();
    this.search.mouseClicked(mouseX, mouseY, mouseButton);
    if (!flag && search.isFocused()) {
      query = "";
      updateText();
      updateQueried();
    }
  }

  private void updateText() {
    search.setText(query);
  }

  @Override
  public void onGuiClosed() {
    Keyboard.enableRepeatEvents(false);
    Config.save();
    MicrosoftAccount.save(mc);
  }

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		accountsgui.drawScreen(par1, par2, par3);
		this.drawCenteredString(fontRendererObj, I18n.format("ias.selectaccount"), this.width / 2, 4, -1);
		if (loginfailed != null) {
			this.drawCenteredString(fontRendererObj, loginfailed.getLocalizedMessage(), this.width / 2, this.height - 62, 16737380);
		}
		search.drawTextBox();
		if (!queriedaccounts.isEmpty()) {
			SkinTools.javDrawSkin(8, height / 2 - 64 - 16, 64, 128);
			Tools.drawBorderedRect(width - 8 - 64, height / 2 - 64 - 16, width - 8, height / 2 + 64 - 16, 2, -5855578, -13421773);
			if (queriedaccounts.get(selectedAccountIndex) instanceof ExtendedAccountData) {
				ExtendedAccountData ead = (ExtendedAccountData) queriedaccounts.get(selectedAccountIndex);
				if (ead.premium != null) {
					if (ead.premium) this.drawString(fontRendererObj, I18n.format("ias.premium"), width - 8 - 61, height / 2 - 64 - 13, 6618980);
					else this.drawString(fontRendererObj, I18n.format("ias.notpremium"), width - 8 - 61, height / 2 - 64 - 13, 16737380);
				}
				this.drawString(fontRendererObj, I18n.format("ias.timesused"), width - 8 - 61, height / 2 - 64 - 15 + 12, -1);
				this.drawString(fontRendererObj, String.valueOf(ead.useCount), width - 8 - 61, height / 2 - 64 - 15 + 21, -1);
				if (ead.useCount > 0) {
					this.drawString(fontRendererObj, I18n.format("ias.lastused"), width - 8 - 61, height / 2 - 64 - 15 + 30, -1);
					this.drawString(fontRendererObj, JavaTools.getFormattedDate(), width - 8 - 61, height / 2 - 64 - 15 + 39, -1);
				}
			} else {
				this.drawString(fontRendererObj, I18n.format("ias.premium"), width - 8 - 61, height / 2 - 64 - 13, 6618980);
			}
		}
		super.drawScreen(par1, par2, par3);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.enabled) {
			if (button.id == 3) {
				escape();
			} else if (button.id == 0) {
				add();
			} else if (button.id == 4) {
				delete();
			} else if (button.id == 1) {
				login(selectedAccountIndex);
			} else if (button.id == 2) {
				logino(selectedAccountIndex);
			} else if (button.id == 7) {
				edit();
			} else if (button.id == 8) {
				reloadSkins();
			} else {
				accountsgui.actionPerformed(button);
			}
		}
	}

  /**
   * Reload Skins
   */
  private void reloadSkins() {
    Config.save();
    SkinTools.cacheSkins(true);
    updateShownSkin();
  }

  /**
   * Leave the gui
   */
  private void escape() {
    mc.displayGuiScreen(prev);
  }

  /**
   * Delete the selected account
   */
	private void delete() {
		mc.displayGuiScreen(new GuiYesNo((b, i) -> {
			if (b) {
				AltDatabase.getInstance().getAlts().remove(getCurrentAsEditable());
				if (this.queriedaccounts.get(selectedAccountIndex) instanceof MicrosoftAccount)
					MicrosoftAccount.msaccounts.remove(this.queriedaccounts.get(selectedAccountIndex));
				if (selectedAccountIndex > 0)
					selectedAccountIndex--;
				updateQueried();
				updateButtons();
			}
			mc.displayGuiScreen(this);
		}, I18n.format("ias.delete.title"), I18n.format("ias.delete.text", queriedaccounts.get(selectedAccountIndex).alias()), 0));
	}

  /**
   * Add an account
   */
  private void add() {
    mc.displayGuiScreen(new GuiAddAccount(this));
  }

  /**
   * Login to the account in offline mode, then return to main menu
   *
   * @param selected The index of the account to log in to
   */
  private void logino(int selected) {
    Account data = queriedaccounts.get(selected);
    AltManager.getInstance().setUserOffline(data.alias());
    loginfailed = null;
    ExtendedAccountData current = getCurrentAsEditable();
    if (current != null) {
    	current.useCount++;
        current.lastused = JavaTools.getDate();
    }
  }

  /**
   * Attempt login to the account, then return to main menu if successful
   *
   * @param selected The index of the account to log in to
   */
  private void login(int selected) {
    Account data = queriedaccounts.get(selected);
    loginfailed = data.login();
    if (loginfailed == null) {
      ExtendedAccountData current = getCurrentAsEditable();
      if (current != null) {
    	  current.premium = true;
          current.useCount++;
          current.lastused = JavaTools.getDate();
      }
    } else if (loginfailed instanceof AlreadyLoggedInException) {
      getCurrentAsEditable().lastused = JavaTools.getDate();
    } else if (HttpTools.ping("http://minecraft.net")) {
      getCurrentAsEditable().premium = false;
    }
  }

  /**
   * Edits the current account's information
   */
  private void edit() {
    mc.displayGuiScreen(new GuiEditAccount(this, selectedAccountIndex));
  }

  private void updateQueried() {
    queriedaccounts = convertData();
    if (!query.equals(I18n.format("ias.search")) && !query.equals("")) {
      for (int i = 0; i < queriedaccounts.size(); i++) {
        if (!queriedaccounts.get(i).alias().contains(query) && ConfigValues.CASESENSITIVE) {
          queriedaccounts.remove(i);
          i--;
        } else if (!queriedaccounts.get(i).alias().toLowerCase().contains(query.toLowerCase()) && !ConfigValues.CASESENSITIVE) {
          queriedaccounts.remove(i);
          i--;
        }
      }
    }
    if (!queriedaccounts.isEmpty()) {
      while (selectedAccountIndex >= queriedaccounts.size()) {
        selectedAccountIndex--;
      }
    }
  }

  @Override
  protected void keyTyped(char character, int keyIndex) {
    if (keyIndex == Keyboard.KEY_UP && !queriedaccounts.isEmpty()) {
      if (selectedAccountIndex > 0) {
        selectedAccountIndex--;
      }
    } else if (keyIndex == Keyboard.KEY_DOWN && !queriedaccounts.isEmpty()) {
      if (selectedAccountIndex < queriedaccounts.size() - 1) {
        selectedAccountIndex++;
      }
    } else if (keyIndex == Keyboard.KEY_ESCAPE) {
      escape();
    } else if (keyIndex == Keyboard.KEY_DELETE && delete.enabled) {
      delete();
    } else if (character == '+') {
      add();
    } else if (character == '/' && edit.enabled) {
      edit();
    } else if (!search.isFocused() && keyIndex == Keyboard.KEY_R) {
      reloadSkins();
    } else if (keyIndex == Keyboard.KEY_RETURN && !search.isFocused() && (login.enabled || loginoffline.enabled)) {
      if ((Keyboard.isKeyDown(Keyboard.KEY_RSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) && loginoffline.enabled) {
        logino(selectedAccountIndex);
      } else {
        if (login.enabled)
          login(selectedAccountIndex);
      }
    } else if (keyIndex == Keyboard.KEY_BACK) {
      if (search.isFocused() && query.length() > 0) {
        query = query.substring(0, query.length() - 1);
        updateText();
        updateQueried();
      }
    } else if (keyIndex == Keyboard.KEY_F5) {
      reloadSkins();
    } else if (character != 0) {
      if (search.isFocused()) {
        if (keyIndex == Keyboard.KEY_RETURN) {
          search.setFocused(false);
          updateText();
          updateQueried();
          return;
        }
        query += character;
        updateText();
        updateQueried();
      }
    }
  }

  private ArrayList<Account> convertData() {
    @SuppressWarnings("unchecked")
	ArrayList<AccountData> tmp = (ArrayList<AccountData>) AltDatabase.getInstance().getAlts().clone();
    ArrayList<Account> converted = new ArrayList<>();
    int index = 0;
    for (AccountData data : tmp) {
      if (data instanceof ExtendedAccountData) {
        converted.add((ExtendedAccountData) data);
      } else {
        converted.add(new ExtendedAccountData(EncryptionTools.decode(data.user), EncryptionTools.decode(data.pass), data.alias));
        AltDatabase.getInstance().getAlts().set(index, new ExtendedAccountData(EncryptionTools.decode(data.user), EncryptionTools.decode(data.pass), data.alias));
      }
      index++;
    }
    converted.addAll(MicrosoftAccount.msaccounts);
    return converted;
  }

  private ArrayList<AccountData> getAccountList() {
    return AltDatabase.getInstance().getAlts();
  }

  private ExtendedAccountData getCurrentAsEditable() {
    for (AccountData dat : getAccountList()) {
      if (dat instanceof ExtendedAccountData) {
        if (((ExtendedAccountData)dat).equals(queriedaccounts.get(selectedAccountIndex))) {
          return (ExtendedAccountData) dat;
        }
      }
    }
    return null;
  }

  private void updateButtons() {
    login.enabled = !queriedaccounts.isEmpty() && (queriedaccounts.get(selectedAccountIndex) instanceof MicrosoftAccount || !EncryptionTools.decode(((ExtendedAccountData)queriedaccounts.get(selectedAccountIndex)).pass).equals(""));
    loginoffline.enabled = !queriedaccounts.isEmpty();
    delete.enabled = !queriedaccounts.isEmpty();
    edit.enabled = !queriedaccounts.isEmpty() && queriedaccounts.get(selectedAccountIndex) instanceof ExtendedAccountData;
    reloadskins.enabled = !queriedaccounts.isEmpty();
  }

  class List extends GuiSlot {
    public List(Minecraft mcIn) {
      super(mcIn, GuiAccountSelector.this.width, GuiAccountSelector.this.height, 32, GuiAccountSelector.this.height - 64, 14);
    }

    @Override
    protected int getSize() {
      return GuiAccountSelector.this.queriedaccounts.size();
    }

    @Override
    protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
      GuiAccountSelector.this.selectedAccountIndex = slotIndex;
      GuiAccountSelector.this.updateButtons();

      if (isDoubleClick && GuiAccountSelector.this.login.enabled) {
        GuiAccountSelector.this.login(slotIndex);
      }
    }

    @Override
    protected boolean isSelected(int slotIndex) {
      return slotIndex == GuiAccountSelector.this.selectedAccountIndex;
    }

    @Override
    protected int getContentHeight() {
      return GuiAccountSelector.this.queriedaccounts.size() * 14;
    }

    @Override
    protected void drawBackground() {
      GuiAccountSelector.this.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int p_192637_1_, int p_192637_2_, int p_192637_3_, int p_192637_4_, Tessellator p_148126_5_, int p_192637_5_, int p_192637_6_) {
      {
        Account data = queriedaccounts.get(p_192637_1_);
        String s = data.alias();
        if (StringUtils.isEmpty(s)) {
          s = I18n.format("ias.alt") + " " + (p_192637_1_ + 1);
        }
        int color = 16777215;
        if (Minecraft.getMinecraft().getSession().getUsername().equals(data.alias())) {
          color = 0x00FF00;
        }
        GuiAccountSelector.this.drawString(GuiAccountSelector.this.fontRendererObj, s, p_192637_2_ + 2, p_192637_3_ + 1, color);
      }
    }
  }
}
