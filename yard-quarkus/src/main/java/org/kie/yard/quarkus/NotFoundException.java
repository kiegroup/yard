package org.kie.yard.quarkus;

import org.kie.yard.api.model.Input;

import java.util.List;

public class NotFoundException extends Throwable {
    private final List<Input> notFound;

    public NotFoundException(List<Input> notFound) {
        super("Not found "+ list(notFound));
        // TODO explain what is missing
        this.notFound = notFound;
    }

    private static String list(List<Input> notFound) {
        final StringBuilder builder=new StringBuilder();
        for (Input input : notFound) {
            builder.append(input.getName());
        }

        return builder.toString();
    }

}
