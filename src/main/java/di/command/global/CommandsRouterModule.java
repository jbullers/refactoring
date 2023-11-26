package di.command.global;

import dagger.Module;
import dagger.Provides;
import di.command.Command;
import di.command.CommandRouter;
import di.output.Outputter;
import java.util.Map;

@Module(includes = {
      GlobalCommandsModule.class,
})
public abstract class CommandsRouterModule {

    @Provides
    static CommandRouter router(Map<String, Command> commands, Outputter outputter) {
        return new CommandRouter(commands, outputter);
    }
}
