package the_fireplace.ias;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

import com.github.mrebhan.ingameaccountswitcher.MR;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import the_fireplace.ias.config.ConfigValues;
import the_fireplace.ias.events.ClientEvents;
import the_fireplace.ias.tools.SkinTools;
import the_fireplace.iasencrypt.Standards;
/**
 * @author The_Fireplace
 */
@Mod("ias")
public class IAS {
	public static Properties config = new Properties();
	public static void syncConfig(boolean save) {
		config.setProperty(ConfigValues.CASESENSITIVE_NAME, String.valueOf(ConfigValues.CASESENSITIVE));
		config.setProperty(ConfigValues.ENABLERELOG_NAME, String.valueOf(ConfigValues.ENABLERELOG));
		config.setProperty(ConfigValues.TEXT_POS_NAME + ".x", ConfigValues.TEXT_X);
		config.setProperty(ConfigValues.TEXT_POS_NAME + ".y", ConfigValues.TEXT_Y);
		if (save) {
			try {
				File f = new File(Minecraft.getInstance().gameDir, "config/ias.properties");
				f.getParentFile().mkdirs();
				FileWriter fw = new FileWriter(f);
				config.store(fw, "IAS config");
				fw.close();
			} catch (Throwable t) {
				System.err.println("Unable to save IAS config");
				t.printStackTrace();
			}
		}
	}
	
	public IAS() {
		try {
			File f = new File(Minecraft.getInstance().gameDir, "config/ias.properties");
			if (f.exists()) {
				FileReader fr = new FileReader(f);
				config.load(fr);
				fr.close();
			}
			ConfigValues.CASESENSITIVE = Boolean.parseBoolean(config.getProperty(ConfigValues.CASESENSITIVE_NAME, String.valueOf(ConfigValues.CASESENSITIVE_DEFAULT)));
			ConfigValues.ENABLERELOG = Boolean.parseBoolean(config.getProperty(ConfigValues.ENABLERELOG_NAME, String.valueOf(ConfigValues.ENABLERELOG_DEFAULT)));
			ConfigValues.TEXT_X = config.getProperty(ConfigValues.TEXT_POS_NAME + ".x", "");
			ConfigValues.TEXT_Y = config.getProperty(ConfigValues.TEXT_POS_NAME + ".y", "");
		} catch (Throwable t) {
			System.err.println("Unable to load IAS config");
			t.printStackTrace();
		}
		syncConfig(false);
		try {
			Class.forName("net.minecraft.util.math.MathHelper");
		} catch (Throwable t) {
			Standards.updateFolder();
		}
		MR.init();
		MinecraftForge.EVENT_BUS.register(new ClientEvents());
		Standards.importAccounts();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientComplete);
	}
	
	public void onClientComplete(FMLLoadCompleteEvent event) {
		SkinTools.cacheSkins(false);
	}
}
