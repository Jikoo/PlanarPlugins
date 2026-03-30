package com.github.jikoo.planarplugins.meta.arguments;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.MessageComponentSerializer;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@NullMarked
public class DataComponentTypeArgument implements CustomArgumentType.Converted<DataComponentType, String> {

  private static final DynamicCommandExceptionType NO_MATCHING_TYPE = new DynamicCommandExceptionType(
      provided -> MessageComponentSerializer.message()
          .serialize(Component.text(provided + " is not a valid DataComponentType!"))
  );

  @Override
  public DataComponentType convert(String nativeType) throws CommandSyntaxException {
    Optional<Field> match = getDataComponentFields()
        .filter(field -> field.getName().equalsIgnoreCase(nativeType))
        .findFirst();

    if (match.isEmpty()) {
      throw NO_MATCHING_TYPE.create(nativeType);
    }

    Field field = match.get();
    try {
      return (DataComponentType) field.get(null);
    } catch (ReflectiveOperationException | ClassCastException e) {
      // TODO this is a naughty hack - I can't find the generic exception handler's exception,
      //  so I'm simply going to allow it to do its thing.
      //  This shouldn't happen, and I need the exception details if it does.
      throw new IllegalStateException(e);
    }
  }

  private Stream<Field> getDataComponentFields() {
    return Arrays.stream(DataComponentTypes.class.getDeclaredFields()).filter(field -> {
      if (!DataComponentType.class.isAssignableFrom(field.getType())) {
        return false;
      }
      int modifiers = field.getModifiers();
      return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && field.canAccess(null);
    });
  }

  @Override
  public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
    getDataComponentFields().forEach(field -> {
      String name = field.getName().toLowerCase(Locale.ROOT);
      if (name.startsWith(builder.getRemainingLowerCase())) {
        builder.suggest(field.getName().toLowerCase(Locale.ROOT));
      }
    });

    return builder.buildFuture();
  }

  @Override
  public ArgumentType<String> getNativeType() {
    return StringArgumentType.word();
  }

}
