package refactor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
final class StringSchemaElement extends SchemaElement {

    static final String TOKEN = "*";

    StringSchemaElement(char argCharacter) {
        super(argCharacter);
    }

    @Override
    String parse(String parameter) {
        return parameter;
    }
}
