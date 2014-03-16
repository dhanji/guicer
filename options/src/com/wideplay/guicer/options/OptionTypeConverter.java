package com.wideplay.guicer.options;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Coerces options into declared types.
 *
 * @author dhanji@gmail.com
 */
public class OptionTypeConverter {

  @SuppressWarnings("unchecked")
  public <T> T convert(String source, Class<?> type) {
    if (Set.class == type && String.class == source.getClass()) {
      Set<String> set = Sets.newHashSet();
      for (String s : source.split(","))
        set.add(s.trim());
      return (T) set;
    }

    if (Boolean.class.equals(type) || boolean.class.equals(type)) {
      return (T) Boolean.valueOf(source);
    } else if (Double.class.equals(type) || double.class.equals(type)) {
      return (T) Double.valueOf(source);
    } else if (Integer.class.equals(type) || int.class.equals(type)) {
      return (T) Integer.valueOf(source);
    } else if (Long.class.equals(type) || long.class.equals(type)) {
      return (T) Long.valueOf(source);
    }

    return (T) source;
  }
}
