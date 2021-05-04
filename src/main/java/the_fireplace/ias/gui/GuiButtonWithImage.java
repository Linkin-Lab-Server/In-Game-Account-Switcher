package the_fireplace.ias.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
/**
 * The button with the image on it.
 * @author The_Fireplace
 */
public class GuiButtonWithImage extends Button {

	private static final ResourceLocation customButtonTextures = new ResourceLocation("ias:textures/gui/custombutton.png");
	public GuiButtonWithImage(int x, int y, IPressable p) {
		super(x, y, 20, 20, new StringTextComponent(""), p);
	}
	
	@Override
	public void renderButton(MatrixStack ms, int mouseX, int mouseY, float delta) {
		if (this.visible) {
			Minecraft mc = Minecraft.getInstance();
			mc.getTextureManager().bind(customButtonTextures);
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = getYImage(isHovered);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(770, 771, 1, 0);
			RenderSystem.blendFunc(770, 771);
			blit(ms, this.x, this.y, 0, k * 20, 20, 20);
		}
	}
}
