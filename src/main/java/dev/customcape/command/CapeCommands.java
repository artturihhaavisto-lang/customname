package dev.customcape.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.customcape.CapeCatalog;
import dev.customcape.CapeManager;
import dev.customcape.gui.CapeSelectScreen;
import java.util.Optional;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class CapeCommands {
   private static final SuggestionProvider<FabricClientCommandSource> CAPE_SUGGESTIONS = (ctx, builder) -> {
      for (CapeCatalog.Entry entry : CapeManager.get().allEntries()) {
         builder.suggest(entry.id());
      }

      return builder.buildFuture();
   };

   private CapeCommands() {
   }

   public static void register() {
      ClientCommandRegistrationCallback.EVENT
         .register(
            (ClientCommandRegistrationCallback)(dispatcher, buildContext) -> dispatcher.register(
               (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)ClientCommands.literal(
                                 "customcape"
                              )
                              .then(ClientCommands.literal("reload").executes(ctx -> {
                                 CapeManager.get().reloadCustom();
                                 int count = CapeManager.get().textures().customEntries().size();
                                 ((FabricClientCommandSource)ctx.getSource())
                                    .sendFeedback(Component.translatable("command.customcape.reloaded", new Object[]{count}));
                                 return count;
                              })))
                           .then(ClientCommands.literal("clear").executes(ctx -> {
                              CapeManager.get().select("vanilla");
                              ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.translatable("command.customcape.cleared"));
                              return 1;
                           })))
                        .then(ClientCommands.literal("list").executes(ctx -> {
                           for (CapeCatalog.Entry entry : CapeManager.get().allEntries()) {
                              ((FabricClientCommandSource)ctx.getSource()).sendFeedback(Component.literal(entry.id() + " \u2014 " + entry.displayName()));
                           }

                           return 1;
                        })))
                     .then(
                        ClientCommands.literal("set")
                           .then(
                              ClientCommands.argument("id", StringArgumentType.greedyString())
                                 .suggests(CAPE_SUGGESTIONS)
                                 .executes(
                                    ctx -> {
                                       String id = StringArgumentType.getString(ctx, "id");
                                       Optional<CapeCatalog.Entry> entry = CapeManager.get().findEntry(id);
                                       if (entry.isEmpty()) {
                                          ((FabricClientCommandSource)ctx.getSource())
                                             .sendError(Component.translatable("command.customcape.unknown", new Object[]{id}));
                                          return 0;
                                       } else {
                                          CapeManager.get().select(id);
                                          ((FabricClientCommandSource)ctx.getSource())
                                             .sendFeedback(Component.translatable("command.customcape.set", new Object[]{entry.get().displayName()}));
                                          return 1;
                                       }
                                    }
                                 )
                           )
                     ))
                  .executes(ctx -> {
                     Minecraft client = Minecraft.getInstance();
                     client.schedule(() -> client.gui.setScreen(new CapeSelectScreen(client.gui.screen())));
                     return 1;
                  })
            )
         );
   }
}
