package dev.customname.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.customname.config.NameConfig;
import dev.customname.config.RankPresets;
import dev.customname.gui.AppearanceScreen;
import dev.customname.util.ColorCodes;
import dev.customname.util.DisplayNameBuilder;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CustomNameCommands {
   private CustomNameCommands() {
   }

   public static void register() {
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, registryAccess) -> {
               LiteralArgumentBuilder<FabricClientCommandSource> root = (LiteralArgumentBuilder<FabricClientCommandSource>)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal(
                                                   "customname"
                                                )
                                                .then(ClientCommands.literal("gui").executes(CustomNameCommands::openGui)))
                                             .then(
                                                ClientCommands.literal("set")
                                                   .then(
                                                      ClientCommands.argument("name", StringArgumentType.greedyString()).executes(CustomNameCommands::setName)
                                                   )
                                             ))
                                          .then(
                                             ClientCommands.literal("color")
                                                .then(
                                                   ClientCommands.argument("color", StringArgumentType.greedyString()).executes(CustomNameCommands::setColor)
                                                )
                                          ))
                                       .then(
                                          ClientCommands.literal("rank")
                                             .then(
                                                ClientCommands.argument("preset", StringArgumentType.word())
                                                   .suggests((ctx, builder) -> suggestRanks(builder))
                                                   .executes(CustomNameCommands::setRank)
                                             )
                                       ))
                                    .then(
                                       ClientCommands.literal("prefix")
                                          .then(ClientCommands.argument("prefix", StringArgumentType.greedyString()).executes(CustomNameCommands::setPrefix))
                                    ))
                                 .then(ClientCommands.literal("clear").executes(CustomNameCommands::clear)))
                              .then(ClientCommands.literal("presets").executes(CustomNameCommands::listPresets)))
                           .then(ClientCommands.literal("show").executes(CustomNameCommands::show)))
                        .then(ClientCommands.literal("reload").executes(CustomNameCommands::reload)))
                     .then(ClientCommands.literal("help").executes(CustomNameCommands::help)))
                  .executes(CustomNameCommands::openGui);
               dispatcher.register(root);
               dispatcher.register((LiteralArgumentBuilder)ClientCommands.literal("cname").redirect(dispatcher.getRoot().getChild("customname")));
            }
         );
   }

   private static CompletableFuture<Suggestions> suggestRanks(SuggestionsBuilder builder) {
      RankPresets.all().keySet().forEach(builder::suggest);
      RankPresets.all().values().forEach(p -> builder.suggest(p.label()));
      return builder.buildFuture();
   }

   private static int openGui(CommandContext<FabricClientCommandSource> ctx) {
      AppearanceScreen.open(AppearanceScreen.Tab.NAME);
      return 1;
   }

   private static int setName(CommandContext<FabricClientCommandSource> ctx) {
      String name = StringArgumentType.getString(ctx, "name");
      NameConfig config = NameConfig.get();
      config.name = name;
      config.enabled = true;
      NameConfig.save();
      ((FabricClientCommandSource)ctx.getSource())
         .sendFeedback(Component.literal("Custom name set to: ").withStyle(ChatFormatting.GRAY).append(DisplayNameBuilder.buildForLocalPlayer()));
      return 1;
   }

   private static int setColor(CommandContext<FabricClientCommandSource> ctx) {
      String raw = StringArgumentType.getString(ctx, "color");
      if (!raw.equalsIgnoreCase("clear") && !raw.equalsIgnoreCase("none") && !raw.equals("-")) {
         NameConfig config = NameConfig.get();
         config.nameColor = ColorCodes.normalizeColorCode(raw);
         config.enabled = true;
         NameConfig.save();
         ((FabricClientCommandSource)ctx.getSource())
            .sendFeedback(Component.literal("Name color set. Preview: ").withStyle(ChatFormatting.GRAY).append(DisplayNameBuilder.buildForLocalPlayer()));
         return 1;
      } else {
         NameConfig.get().nameColor = "";
         NameConfig.save();
         ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("Name color cleared.").withStyle(ChatFormatting.GREEN));
         return 1;
      }
   }

   private static int setRank(CommandContext<FabricClientCommandSource> ctx) {
      String id = StringArgumentType.getString(ctx, "preset");
      Optional<RankPresets.RankPreset> preset = RankPresets.get(id);
      if (preset.isEmpty()) {
         ((FabricClientCommandSource)ctx.getSource()).sendError(Component.literal("Unknown preset '" + id + "'. Use /customname presets"));
         return 0;
      } else {
         NameConfig config = NameConfig.get();
         config.prefix = preset.get().format();
         config.presetId = preset.get().id();
         config.enabled = true;
         NameConfig.save();
         MutableComponent msg = Component.literal("Rank set to ")
            .withStyle(ChatFormatting.GRAY)
            .append(ColorCodes.parse(preset.get().format()))
            .append(Component.literal(" \u2014 preview: ").withStyle(ChatFormatting.DARK_GRAY))
            .append(DisplayNameBuilder.buildForLocalPlayer());
         ((FabricClientCommandSource)ctx.getSource()).sendFeedback(msg);
         return 1;
      }
   }

   private static int setPrefix(CommandContext<FabricClientCommandSource> ctx) {
      NameConfig config = NameConfig.get();
      config.prefix = StringArgumentType.getString(ctx, "prefix");
      config.presetId = "";
      config.enabled = true;
      NameConfig.save();
      ((FabricClientCommandSource)ctx.getSource())
         .sendFeedback(Component.literal("Custom prefix set. Preview: ").withStyle(ChatFormatting.GRAY).append(DisplayNameBuilder.buildForLocalPlayer()));
      return 1;
   }

   private static int clear(CommandContext<FabricClientCommandSource> ctx) {
      NameConfig.get().clear();
      ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("Custom name and rank cleared.").withStyle(ChatFormatting.GREEN));
      return 1;
   }

   private static int listPresets(CommandContext<FabricClientCommandSource> ctx) {
      ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("Rank presets:").withStyle(ChatFormatting.GOLD));

      for (RankPresets.RankPreset preset : RankPresets.all().values()) {
         ((FabricClientCommandSource)ctx.getSource())
            .sendFeedback(
               Component.literal("  /customname rank " + preset.id() + "  ")
                  .withStyle(ChatFormatting.GRAY)
                  .append(ColorCodes.parse(preset.format()))
                  .append(Component.literal(" \u2014 " + preset.description()).withStyle(ChatFormatting.DARK_GRAY))
            );
      }

      return 1;
   }

   private static int show(CommandContext<FabricClientCommandSource> ctx) {
      NameConfig config = NameConfig.get();
      if (!config.hasCustomDisplay()) {
         ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("No custom name configured.").withStyle(ChatFormatting.YELLOW));
         return 1;
      } else {
         ((FabricClientCommandSource)ctx.getSource())
            .sendFeedback(Component.literal("Current display: ").withStyle(ChatFormatting.GRAY).append(DisplayNameBuilder.buildForLocalPlayer()));
         return 1;
      }
   }

   private static int reload(CommandContext<FabricClientCommandSource> ctx) {
      NameConfig.load();
      ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("Reloaded customname.json").withStyle(ChatFormatting.GREEN));
      return 1;
   }

   private static int help(CommandContext<FabricClientCommandSource> ctx) {
      ((FabricClientCommandSource)ctx.getSource())
         .sendFeedback(Component.literal("  /customname gui  \u2014 open the name + cape menu").withStyle(ChatFormatting.GRAY));
      ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal("Keybind: N  (cape tab: K)").withStyle(ChatFormatting.DARK_GRAY));
      return 1;
   }
}
