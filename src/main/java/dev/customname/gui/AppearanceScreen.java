package dev.customname.gui;

import dev.customcape.CapeFilePicker;
import dev.customcape.CapeManager;
import dev.customcape.CapeTextureManager;
import dev.customname.config.NameConfig;
import dev.customname.config.RankPresets;
import dev.customname.util.ChatRewriter;
import dev.customname.util.ColorCodes;
import dev.customname.util.DisplayNameBuilder;
import dev.customname.util.SkyblockLevels;
import dev.customname.util.TabDisplayRewriter;
import dev.customname.compat.GuiCompat;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AppearanceScreen extends Screen {
   private static final int PANEL_W = 520;
   private static final int PANEL_H = 448;
   private static final int TEXT = -723208;
   private static final int WELL = 335544320;
   private static final SystemToastId CAPE_TOAST = new SystemToastId(4000L);
   private final Screen parent;
   private AppearanceScreen.Tab tab;
   private final NameConfig draft;
   private int panelLeft;
   private int panelTop;
   private int mouseX;
   private int mouseY;
   private int previewTop;
   private int splitX;
   private int bodyTop;
   private EditBox nameBox;
   private EditBox prefixBox;
   private EditBox levelBox;
   private StringWidget previewWidget;
   private StringWidget skyblockWidget;
   private final List<int[]> fieldWells = new ArrayList<>();

   public AppearanceScreen(Screen parent, AppearanceScreen.Tab tab) {
      super(Component.literal("custom"));
      this.parent = parent;
      this.tab = tab == null ? AppearanceScreen.Tab.NAME : tab;
      this.draft = NameConfig.get().copy();
   }

   public static void open() {
      open(AppearanceScreen.Tab.NAME);
   }

   public static void open(AppearanceScreen.Tab tab) {
      Minecraft mc = Minecraft.getInstance();
      mc.schedule(() -> mc.setScreenAndShow(new AppearanceScreen(GuiCompat.screen(mc), tab)));
   }

   protected void init() {
      this.fieldWells.clear();
      this.panelLeft = (this.width - PANEL_W) / 2;
      this.panelTop = (this.height - PANEL_H) / 2;
      int x = this.panelLeft + 16;
      int y = this.panelTop + 10;
      int inner = 488;
      this.addRenderableWidget(new StringWidget(x, y + 2, 70, 10, whisper("custom"), this.font));
      this.addRenderableWidget(
         new GlassButton(
            x + 78, y, 48, 16, Component.literal("name"), () -> this.tab == AppearanceScreen.Tab.NAME, () -> this.switchTab(AppearanceScreen.Tab.NAME)
         )
      );
      this.addRenderableWidget(
         new GlassButton(
            x + 130, y, 48, 16, Component.literal("cape"), () -> this.tab == AppearanceScreen.Tab.CAPE, () -> this.switchTab(AppearanceScreen.Tab.CAPE)
         )
      );
      this.addRenderableWidget(new BookButton(x + inner - 52 - 20, y, 16, 16, () -> false, this::openGuide));
      this.addRenderableWidget(new GlassButton(x + inner - 52, y, 52, 16, Component.literal("done"), () -> false, this::saveAndClose));
      y = this.panelTop + 36;
      if (this.tab == AppearanceScreen.Tab.NAME) {
         this.initName(x, y, inner);
      } else {
         this.initCape(x, y, inner);
      }

      if (this.tab == AppearanceScreen.Tab.NAME) {
         this.refreshPreview();
         if (this.nameBox != null) {
            this.setInitialFocus(this.nameBox);
         }
      }
   }

   private void switchTab(AppearanceScreen.Tab next) {
      if (this.tab != next) {
         this.tab = next;
         this.rebuildWidgets();
      }
   }

   private void openGuide() {
      this.minecraft.setScreenAndShow(new FormatGuideScreen(this));
   }

   private void initName(int x, int y, int inner) {
      this.previewTop = y;
      this.addRenderableWidget(new StringWidget(x, y + 2, 36, 10, fieldLabel("chat"), this.font));
      this.previewWidget = (StringWidget)this.addRenderableWidget(new StringWidget(x + 40, y, inner - 40, 12, Component.literal("\u2026"), this.font));
      this.addRenderableWidget(new StringWidget(x, y + 16, 36, 10, fieldLabel("tab"), this.font));
      this.skyblockWidget = (StringWidget)this.addRenderableWidget(new StringWidget(x + 40, y + 14, inner - 40, 12, Component.literal("\u2026"), this.font));
      y += 40;
      this.bodyTop = y;

      int leftW = 286;
      int label = 52;
      int rowH = 16;
      int sectionGap = 4;
      int headerH = 14;
      this.splitX = x + leftW + 8;
      int listW = inner - leftW - 12;
      int listTop = y;
      // Dedicated footer band for clear — nothing else may enter it.
      int footerTop = this.panelTop + PANEL_H - 26;
      int contentLimit = footerTop - 6;
      int listH = Math.max(40, contentLimit - listTop);

      this.addRenderableWidget(new SectionHeader(x, y, leftW, "Identity"));
      int ly = y + headerH;
      this.addRenderableWidget(new StringWidget(x, ly + 3, label, 10, fieldLabel("Name"), this.font));
      this.nameBox = this.field(x + label, ly, leftW - label, this.draft.name, "username", value -> this.draft.name = value);
      ly += rowH;
      this.addRenderableWidget(new StringWidget(x, ly + 3, label, 10, fieldLabel("Prefix"), this.font));
      this.prefixBox = this.field(x + label, ly, leftW - label - 52, this.draft.prefix, "&c[&fTAG&c]", value -> {
         this.draft.prefix = value;
         this.draft.presetId = "";
      });
      this.addRenderableWidget(
         new GlassButton(x + leftW - 48, ly, 48, 16, Component.literal("save"), () -> false, this::saveCurrentPrefix)
            .tooltip("Pin this prefix in the saved list. Click \u00d7 there to remove it.")
      );

      ly += rowH + sectionGap;
      this.addRenderableWidget(new SectionHeader(x, ly, leftW, "Name"));
      ly += headerH;
      this.addRenderableWidget(new StringWidget(x, ly + 3, label, 10, fieldLabel("Style"), this.font));
      this.styleChipRow(x + label, ly, true);
      ly += rowH;
      this.addRenderableWidget(new StringWidget(x, ly + 3, label, 10, fieldLabel("Color"), this.font));
      this.colorField(
         x + label,
         ly,
         displayHex(this.draft.nameColor),
         "#hex",
         value -> {
            String normalized = ColorCodes.normalizeColorCode(value);
            if (value.isBlank() || !normalized.isEmpty()) {
               this.draft.nameColor = normalized;
            }
         },
         () -> rgbOf(this.draft.nameColor),
         () -> displayHex(this.draft.nameColor),
         hex -> this.draft.nameColor = ColorCodes.normalizeColorCode(hex)
      );
      this.addRenderableWidget(
         new ToggleRow(x + label + 96, ly, leftW - label - 96, "Name chroma", this.draft.nameChroma, v -> {
            this.draft.nameChroma = v;
            this.refreshPreview();
         }).tooltip("Animate the name between the two colors below.")
      );
      ly += rowH;
      this.chromaPair(x, ly, label, true);

      ly += rowH + sectionGap;
      this.addRenderableWidget(new SectionHeader(x, ly, leftW, "Prefix"));
      ly += headerH;
      this.addRenderableWidget(new StringWidget(x, ly + 3, label, 10, fieldLabel("Style"), this.font));
      this.styleChipRow(x + label, ly, false);
      ly += rowH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Prefix chroma", this.draft.prefixChroma, v -> {
            this.draft.prefixChroma = v;
            this.refreshPreview();
         }).tooltip("Animate the prefix between the two colors below.")
      );
      ly += rowH;
      this.chromaPair(x, ly, label, false);

      ly += rowH + sectionGap;
      this.addRenderableWidget(new SectionHeader(x, ly, leftW, "Display"));
      ly += headerH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Enable custom name", this.draft.enabled, v -> {
            this.draft.enabled = v;
            this.refreshPreview();
         })
      );
      ly += rowH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Name color matches rank", this.draft.nameMatchesRankColor, v -> {
            this.draft.nameMatchesRankColor = v;
            this.refreshPreview();
         }).tooltip("Color the custom name after the rank prefix: the rank letter color, or the same gradient when the prefix uses chroma.")
      );
      ly += rowH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Show rank in tab list", this.draft.showRankInTab, v -> {
            this.draft.showRankInTab = v;
            this.refreshPreview();
         }).tooltip("Off: no rank in the tab list while in SkyBlock (level + name only). Lobbies and other games show the rank. Chat and name tags always keep the rank.")
      );
      ly += rowH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Spoof Skyblock level", this.draft.spoofSkyblockLevel, v -> {
            this.draft.spoofSkyblockLevel = v;
            this.refreshPreview();
         }).tooltip("Replace your Skyblock level tag in chat and tab. Rank and emblem stay.")
      );
      ly += rowH;
      this.addRenderableWidget(new StringWidget(x, ly + 3, label, 10, fieldLabel("Level"), this.font));
      this.levelBox = this.field(x + label, ly, 56, this.draft.spoofSkyblockLevelValue, "420", value -> this.draft.spoofSkyblockLevelValue = value);

      ly += rowH + sectionGap;
      this.addRenderableWidget(new SectionHeader(x, ly, leftW, "Nametags"));
      ly += headerH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Show my nametag as tab name", this.draft.ownTabListNameTag, v -> this.draft.ownTabListNameTag = v)
            .tooltip("Show your tab-list name above your head in third person.")
      );
      ly += rowH;
      this.addRenderableWidget(new ToggleRow(x, ly, leftW, "Hide my nametag", this.draft.hideOwnNameTag, v -> this.draft.hideOwnNameTag = v));
      ly += rowH;
      this.addRenderableWidget(
         new ToggleRow(x, ly, leftW, "Hide other players nametags", this.draft.hideOtherNameTags, v -> this.draft.hideOtherNameTags = v)
      );

      // Clear sits alone in the footer band, never under nametag rows.
      this.addRenderableWidget(new GlassButton(x, footerTop, 56, 16, Component.literal("clear"), () -> false, this::clearName));

      PrefixPickList list = new PrefixPickList(this.minecraft, listW, listH, listTop, this.draft, new PrefixPickList.Listener() {
         {
            Objects.requireNonNull(AppearanceScreen.this);
         }

         @Override
         public void onSelectNone() {
            AppearanceScreen.this.applyRank(null);
         }

         @Override
         public void onSelectRank(RankPresets.RankPreset preset) {
            AppearanceScreen.this.applyRank(preset);
         }

         @Override
         public void onSelectSaved(NameConfig.SavedPrefix preset) {
            AppearanceScreen.this.applySaved(preset);
         }

         @Override
         public void onDeleteSaved(NameConfig.SavedPrefix preset) {
            AppearanceScreen.this.deleteSaved(preset);
         }
      });
      list.updateSizeAndPosition(listW, listH, this.splitX + 4, listTop);
      this.addRenderableWidget(list);
   }

   private void styleChipRow(int x, int y, boolean name) {
      if (name) {
         this.styleChip(x, y, 18, "B", "Bold", () -> this.draft.nameBold, v -> this.draft.nameBold = v, Style.EMPTY.withBold(true));
         this.styleChip(x + 20, y, 18, "I", "Italic", () -> this.draft.nameItalic, v -> this.draft.nameItalic = v, Style.EMPTY.withItalic(true));
         this.styleChip(x + 40, y, 18, "U", "Underline", () -> this.draft.nameUnderline, v -> this.draft.nameUnderline = v, Style.EMPTY.withUnderlined(true));
         this.styleChip(x + 60, y, 18, "S", "Strikethrough", () -> this.draft.nameStrikethrough, v -> this.draft.nameStrikethrough = v, Style.EMPTY.withStrikethrough(true));
         this.styleChip(x + 80, y, 18, "k", "Obfuscated", () -> this.draft.nameObfuscated, v -> this.draft.nameObfuscated = v, Style.EMPTY);
      } else {
         this.styleChip(x, y, 18, "B", "Bold", () -> this.draft.prefixBold, v -> this.draft.prefixBold = v, Style.EMPTY.withBold(true));
         this.styleChip(x + 20, y, 18, "I", "Italic", () -> this.draft.prefixItalic, v -> this.draft.prefixItalic = v, Style.EMPTY.withItalic(true));
         this.styleChip(x + 40, y, 18, "U", "Underline", () -> this.draft.prefixUnderline, v -> this.draft.prefixUnderline = v, Style.EMPTY.withUnderlined(true));
         this.styleChip(x + 60, y, 18, "S", "Strikethrough", () -> this.draft.prefixStrikethrough, v -> this.draft.prefixStrikethrough = v, Style.EMPTY.withStrikethrough(true));
         this.styleChip(x + 80, y, 18, "k", "Obfuscated", () -> this.draft.prefixObfuscated, v -> this.draft.prefixObfuscated = v, Style.EMPTY);
      }
   }

   private void chromaPair(int x, int y, int label, boolean name) {
      this.addRenderableWidget(new StringWidget(x, y + 3, label, 10, fieldLabel("From"), this.font));
      if (name) {
         this.colorField(
            x + label,
            y,
            displayHex(this.draft.nameChromaStart),
            "#from",
            v -> this.setChroma(true, true, v),
            () -> rgbOf(this.draft.nameChromaStart),
            () -> displayHex(this.draft.nameChromaStart),
            hex -> this.setChroma(true, true, hex)
         );
         this.addRenderableWidget(new StringWidget(x + label + 94, y + 3, 18, 10, fieldLabel("To"), this.font));
         this.colorField(
            x + label + 114,
            y,
            displayHex(this.draft.nameChromaEnd),
            "#to",
            v -> this.setChroma(true, false, v),
            () -> rgbOf(this.draft.nameChromaEnd),
            () -> displayHex(this.draft.nameChromaEnd),
            hex -> this.setChroma(true, false, hex)
         );
      } else {
         this.colorField(
            x + label,
            y,
            displayHex(this.draft.prefixChromaStart),
            "#from",
            v -> this.setChroma(false, true, v),
            () -> rgbOf(this.draft.prefixChromaStart),
            () -> displayHex(this.draft.prefixChromaStart),
            hex -> this.setChroma(false, true, hex)
         );
         this.addRenderableWidget(new StringWidget(x + label + 94, y + 3, 18, 10, fieldLabel("To"), this.font));
         this.colorField(
            x + label + 114,
            y,
            displayHex(this.draft.prefixChromaEnd),
            "#to",
            v -> this.setChroma(false, false, v),
            () -> rgbOf(this.draft.prefixChromaEnd),
            () -> displayHex(this.draft.prefixChromaEnd),
            hex -> this.setChroma(false, false, hex)
         );
      }
   }

   private void colorField(
      int x,
      int y,
      String value,
      String hint,
      Consumer<String> typed,
      java.util.function.IntSupplier color,
      java.util.function.Supplier<String> current,
      Consumer<String> picked
   ) {
      this.hexField(x, y, 70, value, hint, typed);
      this.addRenderableWidget(new PipetteButton(x + 74, y, color, () -> this.openColorPicker(current.get(), picked)));
   }

   private void openColorPicker(String current, Consumer<String> onPick) {
      this.minecraft.setScreenAndShow(new ColorPickerScreen(this, current, onPick));
   }

   public void afterColorPicked() {
      this.rebuildWidgets();
      this.refreshPreview();
   }

   private static int rgbOf(String color) {
      String normalized = ColorCodes.normalizeColorCode(color);
      if (normalized.startsWith("&#") && normalized.length() >= 8) {
         try {
            return Integer.parseInt(normalized.substring(2, 8), 16);
         } catch (NumberFormatException ignored) {
         }
      }
      return 0xFFFFFF;
   }

   private void initCape(int x, int y, int inner) {
      CapeManager manager = CapeManager.get();
      if (manager != null) {
         manager.reloadCustom();
      }

      this.addRenderableWidget(new SectionHeader(x, y, 120, "Preview"));
      int footerTop = this.panelTop + PANEL_H - 26;
      this.addRenderableWidget(
         new ToggleRow(x, footerTop - 40, 120, "Show on you", manager != null && manager.config().applyToSelf(), v -> {
            manager.config().setApplyToSelf(v);
            manager.config().save();
         })
      );
      this.addRenderableWidget(
         new ToggleRow(x, footerTop - 22, 120, "Show on others", manager != null && manager.config().applyToOthers(), v -> {
            manager.config().setApplyToOthers(v);
            manager.config().save();
         })
      );
      int listX = x + 128;
      int listW = inner - 128;
      int listH = Math.max(40, footerTop - 6 - y);
      CapePickList list = new CapePickList(this.minecraft, listW, listH, y, 20);
      list.updateSizeAndPosition(listW, listH, listX, y);
      this.addRenderableWidget(list);
      this.addRenderableWidget(
         new GlassButton(x + 128, footerTop, 56, 16, Component.literal("upload"), CapeFilePicker::isOpen, this::uploadCape)
            .tooltip("Pick a 64\u00d732 PNG. You can also drop the file on this menu.")
      );
      this.addRenderableWidget(new GlassButton(x + 188, footerTop, 56, 16, Component.literal("reload"), () -> false, () -> {
         if (CapeManager.get() != null) {
            CapeManager.get().reloadCustom();
         }

         this.rebuildWidgets();
      }).tooltip("Reload PNGs from config/customcape/custom/"));
      this.addRenderableWidget(new StringWidget(x + 248, footerTop + 3, Math.max(40, inner - 248), 10, fieldLabel("64\u00d732 png"), this.font));
   }

   private void uploadCape() {
      if (CapeManager.get() != null && !CapeFilePicker.isOpen()) {
         CapeFilePicker.pickPng(path -> {
            if (path != null) {
               this.applyImportedCape(CapeManager.get().importCape(path));
            }
         });
      }
   }

   private void applyImportedCape(CapeTextureManager.ImportResult result) {
      if (result != null) {
         this.toastCape(result);
         if (this.minecraft != null && GuiCompat.screen(this.minecraft) == this) {
            this.rebuildWidgets();
         }
      }
   }

   private void toastCape(CapeTextureManager.ImportResult result) {
      if (this.minecraft != null) {
         SystemToast.addOrUpdate(
            GuiCompat.toasts(this.minecraft),
            result.success() ? CAPE_TOAST : SystemToastId.FILE_DROP_FAILURE,
            Component.literal(result.success() ? "Cape uploaded" : "Cape upload failed"),
            Component.literal(result.message())
         );
      }
   }

   public void onFilesDrop(List<Path> files) {
      if (this.tab == AppearanceScreen.Tab.CAPE && CapeManager.get() != null && files != null && !files.isEmpty()) {
         CapeTextureManager.ImportResult last = null;

         for (Path file : files) {
            last = CapeManager.get().importCape(file);
            if (!last.success()) {
               this.toastCape(last);
            }
         }

         if (last != null && last.success()) {
            this.toastCape(last);
         }

         this.rebuildWidgets();
      } else {
         super.onFilesDrop(files);
      }
   }

   private EditBox field(int x, int y, int w, String value, String hint, Consumer<String> responder) {
      this.fieldWells.add(new int[]{x - 2, y, x + w + 2, y + 16});
      EditBox box = new EditBox(this.font, x, y, w, 16, Component.literal(hint));
      box.setMaxLength(128);
      box.setBordered(false);
      box.setTextShadow(false);
      box.setTextColor(-723208);
      box.setValue(value != null ? value : "");
      box.setHint(whisper(hint));
      box.setResponder(text -> {
         responder.accept(text);
         this.refreshPreview();
      });
      this.addRenderableWidget(box);
      return box;
   }

   private EditBox hexField(int x, int y, int w, String value, String hint, Consumer<String> responder) {
      EditBox box = this.field(x, y, w, value, hint, responder);
      box.setMaxLength(7);
      return box;
   }

   private void styleChip(int x, int y, int w, String letter, String tip, BooleanSupplier on, Consumer<Boolean> set, Style style) {
      this.addRenderableWidget(new GlassButton(x, y, w, 16, Component.literal(letter).withStyle(style), on, () -> {
         set.accept(!on.getAsBoolean());
         this.refreshPreview();
      }, true).tooltip(tip));
   }

   private static Component fieldLabel(String text) {
      return Component.literal(text).withStyle(Style.EMPTY.withColor(TextColor.fromRgb(9345192)));
   }

   private static Component whisper(String text) {
      return fieldLabel(text);
   }

   private void setChroma(boolean name, boolean start, String value) {
      String color = ColorCodes.normalizeColorCode(value);
      if (!color.isEmpty()) {
         String hex = "#" + color.substring(2);
         if (name) {
            this.draft.nameChroma = true;
            if (start) {
               this.draft.nameChromaStart = hex;
            } else {
               this.draft.nameChromaEnd = hex;
            }
         } else {
            this.draft.prefixChroma = true;
            if (start) {
               this.draft.prefixChromaStart = hex;
            } else {
               this.draft.prefixChromaEnd = hex;
            }
         }
      }
   }

   private static String displayHex(String color) {
      String normalized = ColorCodes.normalizeColorCode(color);
      return normalized.startsWith("&#") ? "#" + normalized.substring(2) : "";
   }

   private void applyRank(RankPresets.RankPreset preset) {
      this.draft.prefix = preset == null ? "" : preset.format();
      this.draft.presetId = preset == null ? "" : preset.id();
      if (this.prefixBox != null) {
         this.prefixBox.setValue(this.draft.prefix);
      }

      this.refreshPreview();
   }

   private void applySaved(NameConfig.SavedPrefix saved) {
      this.draft.prefix = saved.prefix;
      this.draft.presetId = NameConfig.savedPresetId(saved.id);
      this.draft.prefixChroma = saved.chroma;
      this.draft.prefixChromaStart = saved.chromaStart;
      this.draft.prefixChromaEnd = saved.chromaEnd;
      this.rebuildWidgets();
   }

   private void saveCurrentPrefix() {
      if (this.draft.prefix != null && !this.draft.prefix.isBlank()) {
         NameConfig.SavedPrefix saved = this.draft
            .upsertSavedPrefix(this.draft.prefix, this.draft.prefixChroma, this.draft.prefixChromaStart, this.draft.prefixChromaEnd);
         this.draft.presetId = NameConfig.savedPresetId(saved.id);
         this.flushSavedPrefixes();
         this.rebuildWidgets();
      }
   }

   private void deleteSaved(NameConfig.SavedPrefix saved) {
      this.draft.removeSavedPrefix(saved.id);
      if (NameConfig.savedPresetId(saved.id).equals(this.draft.presetId)) {
         this.draft.presetId = "";
      }

      this.flushSavedPrefixes();
      this.rebuildWidgets();
   }

   private void flushSavedPrefixes() {
      NameConfig live = NameConfig.get();
      if (live.savedPrefixes == null) {
         live.savedPrefixes = new ArrayList<>();
      }

      live.savedPrefixes.clear();
      if (this.draft.savedPrefixes != null) {
         for (NameConfig.SavedPrefix preset : this.draft.savedPrefixes) {
            live.savedPrefixes.add(preset.copy());
         }
      }

      NameConfig.save();
   }

   private void clearName() {
      this.draft.name = "";
      this.draft.nameColor = "";
      this.draft.prefix = "";
      this.draft.presetId = "";
      this.draft.nameChroma = false;
      this.draft.prefixChroma = false;
      this.draft.nameBold = false;
      this.draft.nameItalic = false;
      this.draft.nameUnderline = false;
      this.draft.nameStrikethrough = false;
      this.draft.nameObfuscated = false;
      this.draft.prefixBold = false;
      this.draft.prefixItalic = false;
      this.draft.prefixUnderline = false;
      this.draft.prefixStrikethrough = false;
      this.draft.prefixObfuscated = false;
      this.draft.replaceLevelWithPrefix = false;
      this.draft.spoofSkyblockLevel = false;
      this.draft.spoofSkyblockLevelValue = "420";
      this.draft.ownTabListNameTag = false;
      this.draft.hideOwnNameTag = false;
      this.draft.hideOtherNameTags = false;
      this.draft.enabled = true;
      this.rebuildWidgets();
   }

   private void saveAndClose() {
      NameConfig.get().applyFrom(this.draft);
      NameConfig.save();
      this.onClose();
   }

   private void refreshPreview() {
      if (this.previewWidget != null && this.skyblockWidget != null) {
         String real = this.minecraft != null && this.minecraft.player != null ? this.minecraft.player.getGameProfile().name() : "Player";
         MutableComponent vanillaSender = Component.empty();
         vanillaSender.append(SkyblockLevels.buildLevelTag(224));
         vanillaSender.append(Component.literal(" "));
         // No emblem here: the diamond is an optional SkyBlock cosmetic, not part
         // of the default chat sender line.
         vanillaSender.append(Component.literal("[MVP+]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(43690))));
         vanillaSender.append(Component.literal(" "));
         vanillaSender.append(Component.literal(real));

         this.previewWidget.setMessage(ChatRewriter.previewChatLine(vanillaSender, this.draft, real));

         MutableComponent skyblock = Component.empty();
         String levelText = this.draft.spoofSkyblockLevel ? TabDisplayRewriter.formatLevel(this.draft.spoofSkyblockLevelValue) : null;
         int level = levelText != null ? Integer.parseInt(levelText) : 224;

         skyblock.append(SkyblockLevels.buildLevelTag(level));
         skyblock.append(Component.literal(" "));
         if (this.draft.prefix != null && !this.draft.prefix.isBlank()) {
            skyblock.append(DisplayNameBuilder.buildPrefix(this.draft));
         } else {
            skyblock.append(Component.literal("[MVP+]").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(43690))));
         }

         skyblock.append(Component.literal(" "));
         skyblock.append(DisplayNameBuilder.buildNameOnly(real, this.draft));
         this.skyblockWidget.setMessage(skyblock);
      }
   }

   public void tick() {
      super.tick();
      if (this.tab == AppearanceScreen.Tab.NAME) {
         this.refreshPreview();
      }
   }

   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      if (this.minecraft.level != null) {
         this.extractBlurredBackground(graphics);
      } else {
         this.extractPanorama(graphics, partialTick);
         this.extractBlurredBackground(graphics);
      }

      graphics.fill(0, 0, this.width, this.height, 1711671824);
      graphics.fill(this.panelLeft - 1, this.panelTop - 1, this.panelLeft + PANEL_W + 1, this.panelTop + PANEL_H + 1, 872415231);
      graphics.fill(this.panelLeft, this.panelTop, this.panelLeft + PANEL_W, this.panelTop + PANEL_H, -1005579236);
      graphics.fillGradient(this.panelLeft, this.panelTop, this.panelLeft + PANEL_W, this.panelTop + 36, 587202559, 16777215);
      graphics.fill(this.panelLeft + 16, this.panelTop + 30, this.panelLeft + PANEL_W - 16, this.panelTop + 31, 587202559);
      if (this.tab == AppearanceScreen.Tab.NAME) {
         int innerLeft = this.panelLeft + 16;
         int innerRight = this.panelLeft + PANEL_W - 16;
         if (this.previewTop > 0) {
            graphics.fill(innerLeft - 2, this.previewTop - 3, innerRight + 2, this.previewTop + 34, 335544320);
         }

         if (this.splitX > 0 && this.bodyTop > 0) {
            graphics.fill(this.splitX, this.bodyTop, this.splitX + 1, this.panelTop + PANEL_H - 30, 587202559);
         }

         for (int[] well : this.fieldWells) {
            graphics.fill(well[0], well[1], well[2], well[3], 570425344);
         }
      }

      if (this.tab == AppearanceScreen.Tab.CAPE && this.minecraft.player != null) {
         int px = this.panelLeft + 16;
         int py = this.panelTop + 48;
         extractCapePreview(graphics, px, py, px + 110, py + 150, 48, 0.0625F, this.mouseX, this.mouseY, this.minecraft.player);
      }
   }

   private static void extractCapePreview(
      GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int scale, float yOffset, float mouseX, float mouseY, LivingEntity entity
   ) {
      float centerX = (x1 + x2) / 2.0F;
      float centerY = (y1 + y2) / 2.0F;
      float lookX = (float)Math.atan((centerX - mouseX) / 40.0);
      float lookY = (float)Math.atan((centerY - mouseY) / 40.0);
      Quaternionf pose = new Quaternionf().rotateZ((float) Math.PI);
      Quaternionf xRot = new Quaternionf().rotateX(lookY * 20.0F * (float) (Math.PI / 180.0));
      pose.mul(xRot);
      EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
      EntityRenderState state = dispatcher.getRenderer(entity).createRenderState(entity, 1.0F);
      state.shadowPieces.clear();
      state.outlineColor = 0;
      state.nameTag = null;
      if (state instanceof LivingEntityRenderState living) {
         float look = lookX * 20.0F;
         living.bodyRot = look;
         living.yRot = look;
         living.xRot = living.pose == Pose.FALL_FLYING ? 0.0F : -lookY * 20.0F;
         living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
         living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
         living.scale = 1.0F;
      }

      if (state instanceof AvatarRenderState avatar) {
         CapeManager manager = CapeManager.get();
         if (manager != null) {
            PlayerSkin applied = manager.applyCape(avatar.skin);
            if (applied != null) {
               avatar.skin = applied;
            }
         }

         avatar.showCape = avatar.skin != null && avatar.skin.cape() != null;
         avatar.capeFlap = 12.0F;
         avatar.capeLean = 0.0F;
         avatar.capeLean2 = 0.0F;
      }

      graphics.entity(state, scale, new Vector3f(0.0F, state.boundingBoxHeight / 2.0F + yOffset, 0.0F), pose, xRot, x1, y1, x2, y2);
   }

   public void onClose() {
      this.minecraft.setScreenAndShow(this.parent);
   }

   public boolean isPauseScreen() {
      return false;
   }

   public static enum Tab {
      NAME,
      CAPE;

      private Tab() {
      }
   }
}
