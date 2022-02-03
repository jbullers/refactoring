package refactor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
abstract class SchemaElement {

    private final char argCharacter;

    SchemaElement(char argCharacter) {
        this.argCharacter = argCharacter;
    }

    char argCharacter() {
        return argCharacter;
    }

    boolean requiresParameter() {
        return true;
    }

    abstract Object parse(String parameter) throws ArgsException;

    static SchemaElement create(char argCharacter, String elementTail) throws ArgsException {
        return switch (elementTail) {
            case BooleanSchemaElement.TOKEN -> new BooleanSchemaElement(argCharacter);
            case StringSchemaElement.TOKEN -> new StringSchemaElement(argCharacter);
            case IntegerSchemaElement.TOKEN -> new IntegerSchemaElement(argCharacter);
            default -> throw new ArgsException("Argument: %c has invalid format: %s.".formatted(argCharacter, elementTail));
        };
    }
}
