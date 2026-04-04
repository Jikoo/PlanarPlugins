package com.github.jikoo.planarplugins.signs.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@NullMarked
public class EnumArgument<T extends Enum<T>> implements CustomArgumentType.Converted<T, String> {

  private Class<T> enumClass;
  private final DynamicCommandExceptionType noMatchingType = new DynamicCommandExceptionType(
      provided -> MessageComponentSerializer.message()
          .serialize(Component.text(provided + " is not a valid " + enumClass)));

  public EnumArgument(Class<T> enumClass) {
    this.enumClass = enumClass;
  }

  @Override
  public T convert(String nativeType) throws CommandSyntaxException {
    nativeType = nativeType.toUpperCase(Locale.ROOT);
    for (T constant : enumClass.getEnumConstants()) {
      if (constant.name().equals(nativeType)) {
        return constant;
      }
    }
    throw noMatchingType.create(nativeType);
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    for (T constant : enumClass.getEnumConstants()) {
      String name = constant.name().toLowerCase(Locale.ROOT);
      if (name.startsWith(builder.getRemainingLowerCase())) {
        builder.suggest(name);
      }
    }

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }

}
