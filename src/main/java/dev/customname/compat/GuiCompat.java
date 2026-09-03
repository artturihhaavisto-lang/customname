package dev.customname.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.screens.Screen;

/**
 * Screen and toast accessors shared by both build targets. Minecraft 26.2 moved
 * them from {@link Minecraft} onto {@link Gui} (and turned the {@code screen}
 * field into accessors), so the shared source cannot call either directly.
 *
 * <p>All targets are public, so reflective lookup is safe; the resolved members
 * are cached and the call sites are rare (GUI opening, cape file drop toasts).
 */
public final class GuiCompat {
	private static final Method GUI_SCREEN; // 26.2+: Gui#screen()
	private static final Method GUI_SET_SCREEN; // 26.2+: Gui#setScreen(Screen)
	private static final Method GUI_TOASTS; // 26.2+: Gui#toastManager()
	private static final Field MC_SCREEN; // 26.1.2: Minecraft#screen
	private static final Method MC_SET_SCREEN; // 26.1.2: Minecraft#setScreen(Screen)
	private static final Method MC_TOASTS; // 26.1.2: Minecraft#getToastManager()

	static {
		GUI_SCREEN = method("screen", Gui.class);
		GUI_SET_SCREEN = method("setScreen", Gui.class, Screen.class);
		GUI_TOASTS = method("toastManager", Gui.class);
		MC_SCREEN = field("screen", Minecraft.class);
		MC_SET_SCREEN = method("setScreen", Minecraft.class, Screen.class);
		MC_TOASTS = method("getToastManager", Minecraft.class);
	}

	private GuiCompat() {
	}

	public static Screen screen(Minecraft mc) {
		if (mc == null) {
			return null;
		}

		try {
			if (GUI_SCREEN != null) {
				return (Screen)GUI_SCREEN.invoke(mc.gui);
			}

			return MC_SCREEN != null ? (Screen)MC_SCREEN.get(mc) : null;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	public static void setScreen(Minecraft mc, Screen target) {
		if (mc == null) {
			return;
		}

		try {
			if (GUI_SET_SCREEN != null) {
				GUI_SET_SCREEN.invoke(mc.gui, target);
			} else if (MC_SET_SCREEN != null) {
				MC_SET_SCREEN.invoke(mc, target);
			}
		} catch (ReflectiveOperationException ignored) {
		}
	}

	public static ToastManager toasts(Minecraft mc) {
		if (mc == null) {
			return null;
		}

		try {
			if (GUI_TOASTS != null) {
				return (ToastManager)GUI_TOASTS.invoke(mc.gui);
			}

			return MC_TOASTS != null ? (ToastManager)MC_TOASTS.invoke(mc) : null;
		} catch (ReflectiveOperationException e) {
			return null;
		}
	}

	private static Method method(String name, Class<?> owner, Class<?>... params) {
		try {
			return owner.getMethod(name, params);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}

	private static Field field(String name, Class<?> owner) {
		try {
			return owner.getField(name);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}
}
