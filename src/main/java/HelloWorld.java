String greetingFor(String name) {
    return "Hello " + name;
}

void main() {
    for (var name : List.of("Jason", "Fred", "World")) {
        println(greetingFor(name));
    }
}