package com.github.mrebhan.ingameaccountswitcher;

import java.lang.reflect.Field;

import com.github.mrebhan.ingameaccountswitcher.tools.Config;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import ru.vidtu.iasfork.msauth.MicrosoftAccount;

/**
 * @author MRebhan
 */
public class MR {
	public static void init(){
		Config.load();
		MicrosoftAccount.load(Minecraft.getInstance());
	}
	public static void setSession(Session s) {
		try {
			for (Field f : Minecraft.class.getDeclaredFields()) {
				if (f.getType().equals(Session.class)) {
					f.setAccessible(true);
					f.set(Minecraft.getInstance(), s);
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
