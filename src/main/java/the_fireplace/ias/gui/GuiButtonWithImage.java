package the_fireplace.ias.gui;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
/**
 * The button with the image on it.
 * @author The_Fireplace
 */
public class GuiButtonWithImage extends Button {
	private static final ResourceLocation customButtonTextures = new ResourceLocation("ias", "textures/gui/custombutton.png");

	public GuiButtonWithImage(int x, int y, IPressable p) {
		super(x, y, 20, 20, "ButterDog", p);
	}
	
	@Override
	public void renderButton(int mouseX, int mouseY, float delta) {
		if (this.visible) {
			Minecraft mc = Minecraft.getInstance();
			mc.textureManager.bindTexture(customButtonTextures);
			GlStateManager.color4f(1F, 1F, 1F, 1F);
			this.isHovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int k = getYImage(isHovered);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.blendFunc(770, 771);
			blit(this.x, this.y, 0, k * 20, 20, 20);
		}
	}
}
