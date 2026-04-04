package com.github.jikoo.planarplugins.signs.lined;

import net.kyori.adventure.text.Component;

public interface Lined {

  int size();

  void line(int index, Component component);

}
