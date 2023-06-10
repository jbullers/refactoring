package refactor;

import static java.lang.Integer.parseInt;

final class IntegerSchemaElement extends SchemaElement {

    static final String TOKEN = "#";

    IntegerSchemaElement(char argCharacter) {
        super(argCharacter);
    }

    @Override
    Integer parse(String parameter) throws ArgsException {
        try {
            return parseInt(parameter);
        } catch (NumberFormatException e) {
            throw new ArgsException(
                    "Argument -%c expects an integer but was '%s'.".formatted(argCharacter(), parameter),
                    e);
        }
    }
}
