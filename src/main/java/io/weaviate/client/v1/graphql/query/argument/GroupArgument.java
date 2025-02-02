package io.weaviate.client.v1.graphql.query.argument;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Builder
@ToString
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class GroupArgument implements Argument {
  GroupType type;
  Float force;

  @Override
  public String build() {
    Set<String> arg = new LinkedHashSet<>();

    if (type != null) {
      arg.add(String.format("type:%s", type));
    }
    if (force != null) {
      arg.add(String.format("force:%s", force));
    }

    return String.format("group:{%s}", String.join(" ", arg));
  }
}
