package the_fireplace.ias.gui;

import org.lwjgl.input.Keyboard;

import joptsimple.internal.Strings;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiPasswordField extends GuiTextField
{
	public GuiPasswordField(FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height)
	{
		super(fontrendererObj, x, y, par5Width, par6Height);
	}

	@Override
	public void drawTextBox()
	{
		String password = getText();
		replaceText(Strings.repeat('*', getText().length()));
		super.drawTextBox();
		replaceText(password);
	}

	@Override
	public boolean textboxKeyTyped(char typedChar, int keyCode)
	{
		if (GuiScreen.isCtrlKeyDown() && (keyCode == Keyboard.KEY_C || keyCode == Keyboard.KEY_X)) return false;
		return super.textboxKeyTyped(typedChar, keyCode);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton)
	{
		// Minecraft has variable-width fonts, so replace the text with asterisks so that the correct cursor position is calculated
		String password = getText();
		replaceText(Strings.repeat('*', getText().length()));
		super.mouseClicked(mouseX, mouseY, mouseButton);
		replaceText(password);
	}

	/**
	 * Sets the text of the field while maintaining the cursor positions
	 * @param newText
	 */
	private void replaceText(String newText)
	{
		int cursorPosition = getCursorPosition();
		int selectionEnd = getSelectionEnd();
		setText(newText);
		setCursorPosition(cursorPosition);
		setSelectionPos(selectionEnd);
	}
}
