package org.kie.yard.impl2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.SourceVersion;

public class QuotedExprParsed {
    private static final int ESCAPE_CHAR = "`".codePointAt(0);

    private final Set<String> usedSymbols;
    private final String rewrittenExpression;

    private QuotedExprParsed(List<String> usedSymbols, String rewrittenExpression) {
        this.usedSymbols = usedSymbols.stream().collect(Collectors.toUnmodifiableSet());
        this.rewrittenExpression = rewrittenExpression;
    }

    public String getRewrittenExpression() {
        return rewrittenExpression;
    }

    public Collection<String> getUsedSymbols() {
        return usedSymbols;
    }

    public static QuotedExprParsed from(String expr) { 
        StringBuilder rewittenExpr = new StringBuilder();
        StringBuilder quotedBuffer = new StringBuilder();
        // TODO consider including scope so to check the symbols are present
        List<String> usedSymbols = new ArrayList<>();
        OfInt it = expr.codePoints().iterator();
        State state = State.UNQUOTED;
        while (it.hasNext()) {
            int c = it.nextInt();
            if (c == ESCAPE_CHAR) {
                switch (state) {
                    case UNQUOTED:
                        state = State.QUOTED;
                        break;
                    case QUOTED:
                        state = State.UNQUOTED;
                        var originalSymbol = quotedBuffer.toString();
                        usedSymbols.add(originalSymbol);
                        var escaped = escapeIdentifier(originalSymbol);
                        rewittenExpr.append(escaped);
                        quotedBuffer = new StringBuilder();
                        break;
                    default:
                        throw new IllegalStateException();
                }
            } else {
                switch (state) {
                    case UNQUOTED:
                        rewittenExpr.appendCodePoint(c);
                        break;
                    case QUOTED:
                        quotedBuffer.appendCodePoint(c);
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }
        }
        return new QuotedExprParsed(usedSymbols, rewittenExpr.toString());
    }

    private static enum State {
        UNQUOTED, QUOTED
    }

    public static String escapeIdentifier(String partOfIdentifier) {
        String id = partOfIdentifier;
        if (!Character.isJavaIdentifierStart(id.charAt(0))) {
            id = "_" + id;
        }
        id = id.replaceAll("_", "__");
        if (SourceVersion.isKeyword(id)) {
            id = "_" + id;
        }
        StringBuilder result = new StringBuilder();
        char[] cs = id.toCharArray();
        for (char c : cs) {
            if (Character.isJavaIdentifierPart(c)) {
                result.append(c);
            } else {
                result.append("_" + Integer.valueOf(c));
            }
        }
        return result.toString();
    }
}
