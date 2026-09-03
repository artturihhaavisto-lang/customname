package dev.customcape;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;

public final class CapeFilePicker {
   private static final AtomicBoolean OPEN = new AtomicBoolean(false);

   private CapeFilePicker() {
   }

   public static boolean isOpen() {
      return OPEN.get();
   }

   public static void pickPng(Consumer<Path> onPicked) {
      if (OPEN.compareAndSet(false, true)) {
         Thread thread = new Thread(() -> {
            Path chosen = null;

            try {
               CapeFilePicker.NativeAttempt attempt = pickNative();
               if (attempt.tried()) {
                  chosen = attempt.path();
               } else {
                  chosen = pickAwt();
               }
            } catch (Exception var6) {
               CustomCape.LOGGER.warn("Cape file picker failed", var6);
            } finally {
               OPEN.set(false);
            }

            Path result = chosen != null && Files.isRegularFile(chosen) ? chosen : null;
            Minecraft.getInstance().schedule(() -> onPicked.accept(result));
         }, "customcape-file-picker");
         thread.setDaemon(true);
         thread.start();
      }
   }

   private static CapeFilePicker.NativeAttempt pickNative() {
      return switch (Util.getPlatform()) {
         case LINUX, SOLARIS -> linux();
         case OSX -> onPath("osascript") ? CapeFilePicker.NativeAttempt.tried(osascript()) : CapeFilePicker.NativeAttempt.unavailable();
         case WINDOWS -> !onPath("powershell") && !onPath("powershell.exe") && !onPath("pwsh")
            ? CapeFilePicker.NativeAttempt.unavailable()
            : CapeFilePicker.NativeAttempt.tried(powershell());
         default -> CapeFilePicker.NativeAttempt.unavailable();
      };
   }

   private static CapeFilePicker.NativeAttempt linux() {
      if (onPath("zenity")) {
         return CapeFilePicker.NativeAttempt.tried(zenity());
      } else if (onPath("kdialog")) {
         return CapeFilePicker.NativeAttempt.tried(kdialog());
      } else {
         return onPath("qarma")
            ? CapeFilePicker.NativeAttempt.tried(run("qarma", "--file-selection", "--title=Choose a 64x32 cape PNG", "--file-filter=PNG | *.png"))
            : CapeFilePicker.NativeAttempt.unavailable();
      }
   }

   private static Path zenity() {
      return run("zenity", "--file-selection", "--title=Choose a 64x32 cape PNG", "--file-filter=PNG images | *.png", "--file-filter=All files | *");
   }

   private static Path kdialog() {
      String start = System.getProperty("user.home", ".");
      return run("kdialog", "--getopenfilename", start, "*.png");
   }

   private static Path osascript() {
      return run("osascript", "-e", "POSIX path of (choose file of type {\"public.png\",\"png\"} with prompt \"Choose a 64x32 cape PNG\")");
   }

   private static Path powershell() {
      String script = String.join(
         "; ",
         "Add-Type -AssemblyName System.Windows.Forms",
         "$d = New-Object System.Windows.Forms.OpenFileDialog",
         "$d.Filter = 'PNG cape (*.png)|*.png'",
         "$d.Title = 'Choose a 64x32 cape PNG'",
         "$d.Multiselect = $false",
         "if ($d.ShowDialog() -eq [System.Windows.Forms.DialogResult]::OK) { $d.FileName }"
      );
      String shell = onPath("pwsh") && !onPath("powershell") && !onPath("powershell.exe") ? "pwsh" : "powershell";
      return run(shell, "-NoProfile", "-STA", "-Command", script);
   }

   private static Path pickAwt() {
      try {
         System.setProperty("java.awt.headless", "false");
         FileDialog dialog = new FileDialog((Frame)null, "Choose a 64x32 cape PNG", 0);
         dialog.setFilenameFilter((dirx, name) -> name.toLowerCase(Locale.ROOT).endsWith(".png"));
         dialog.setFile("*.png");
         dialog.setVisible(true);
         String file = dialog.getFile();
         String dir = dialog.getDirectory();
         dialog.dispose();
         return file != null && dir != null ? Path.of(dir, file) : null;
      } catch (Throwable var3) {
         CustomCape.LOGGER.warn("AWT file dialog unavailable", var3);
         return null;
      }
   }

   private static boolean onPath(String binary) {
      String path = System.getenv("PATH");
      if (path != null && !path.isBlank()) {
         for (String dir : path.split(File.pathSeparator)) {
            if (!dir.isBlank()) {
               Path file = Path.of(dir, binary);
               Path exe = Path.of(dir, binary.endsWith(".exe") ? binary : binary + ".exe");
               if (Files.isExecutable(file) || Files.isExecutable(exe)) {
                  return true;
               }
            }
         }

         return false;
      } else {
         return false;
      }
   }

   private static Path run(String... command) {
      try {
         ProcessBuilder builder = new ProcessBuilder(command);
         builder.redirectError(Redirect.DISCARD);
         Process process = builder.start();
         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));

         String line;
         try {
            line = reader.readLine();
         } catch (Throwable var8) {
            try {
               reader.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         reader.close();
         int code = process.waitFor();
         if (code == 0 && line != null && !line.isBlank()) {
            Path path = Path.of(line.trim());
            return Files.isRegularFile(path) ? path : null;
         } else {
            return null;
         }
      } catch (Exception var9) {
         return null;
      }
   }

   private record NativeAttempt(boolean tried, Path path) {
      static CapeFilePicker.NativeAttempt tried(Path path) {
         return new CapeFilePicker.NativeAttempt(true, path);
      }

      static CapeFilePicker.NativeAttempt unavailable() {
         return new CapeFilePicker.NativeAttempt(false, null);
      }
   }
}
