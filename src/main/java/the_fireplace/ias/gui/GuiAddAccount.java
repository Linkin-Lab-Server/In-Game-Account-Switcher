package the_fireplace.ias.gui;

import com.github.mrebhan.ingameaccountswitcher.tools.alt.AltDatabase;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;
import ru.vidtu.iasfork.msauth.MSAuthScreen;
import the_fireplace.ias.account.ExtendedAccountData;

/**
 * The GUI where the alt is added
 * @author The_Fireplace
 * @author evilmidget38
 */
public class GuiAddAccount extends AbstractAccountGui {

	public GuiAddAccount(Screen prev) {
		super(prev, new TranslatableText("ias.addaccount"));
	}
	
	@Override
	public void init() {
		super.init();
		addButton(new ButtonWidget(width / 2 - 60, height / 3 * 2, 120, 20, I18n.translate("ias.msauth.btn"), btn -> minecraft.openScreen(new MSAuthScreen(this))));
	}

	@Override
	public void complete()
	{
		AltDatabase.getInstance().getAlts().add(new ExtendedAccountData(getUsername(), getPassword(), getUsername()));
	}
}
